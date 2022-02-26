---
layout: post
title: "Skew된 Data에 대한 GroupBy, JOIN 연산 최적화"
categories: "bigdata"
---

## 들어가며

'Data가 skew'되었다는 것은 Data의 분배가 고르지 않고 편향되었다는 것을 의미한다. 좀 더 쉽게 이야기하면 어느 특정 데이터만 많이 존재한다는 것이다.

예를 들어보자. 파레토 법칙 혹은 80:20 법칙이라는 것이 있다. 상위 20%가 전체의 80%를 생산한다는 것을 의미한다. (확실치는 않지만) Youtube의 경우 조회 수 상위 20%의 동영상이 80%의 재생을 했을 가능성이 있다.

이런 상황에서 '동영상별 재생수'를 계산하게 되면 상위 20%의 동영상 때문에 계산 시간이 많이 걸리게 된다. 이유는 data가 skew되어있는 상황에서는 분산 처리 효과가 떨어지기 떄문이다. 즉, 병렬성이 떨어지므로 수행 속도가 느려지게 된다.

아래의 방법을 이용하면 skew된 Data를 균등하게 분배할 수 있으므로 분산이 잘 되어 처리 속도를 빠르게 할 수 있다.

하지만 skew가 많지 않은 경우 오히려 처리 속도가 느려진다. 따라서 아래 내용을 적용하기 전에 꼭 테스트를 하기 바란다. 필자가 보유한 data set으로 테스트해봐도 아래 기법은 오히려 느려졌다. 아마 skew가 심하지 않기 떄문이라고 생각된다. 즉, 최적화를 위해 부가 연산을 해야하는데 이 연산 비용이 최적화하지 않은 질의 수행 시간보다 적어야 한다.

테스트에 사용된 Spark 버전: 2.4.4

참고 문서

- [Apache Spark Core—Deep Dive—Proper Optimization](https://www.youtube.com/watch?v=daXEp4HmS-E)
- [Why Your Spark Apps Are Slow Or Failing, Part II: Data Skew and Garbage Collection](https://dzone.com/articles/why-your-spark-apps-are-slow-or-failing-part-ii-da)

## Data Set 및 질의

상품 테이블과 주문 테이블이 있다고 가정한다.

```scala
case class Product(prod_id: String, price: Int)
case class Order(order_id: String, prod_id: String)

val product = Seq(
    Product("prod01", 100),
    Product("prod02", 200),
    Product("prod03", 200)
).toDF

val order = Seq(
    Order("order01", "prod01"),
    Order("order02", "prod01"),
    Order("order03", "prod01"),
    Order("order04", "prod01"),
    Order("order05", "prod01"),
    Order("order06", "prod02"),
    Order("order07", "prod02"),
    Order("order08", "prod03")
).toDF

product.createOrReplaceTempView("product")
order.createOrReplaceTempView("order")
```

계산하고자하는 것은 다음과 같다.

- 상품별 주문 건수 조회
    - groupBy 연산
- 주문별 상품금액 조회
    - join 연산

특정 상품에 대한 주문량이 많은 것을 가정한다. 즉, 상품id가 skew되어 있는 상황이다.

## skew된 data에 대한 groupBy 연산 최적화

최적화되지 않는 SELECT문은 다음과 같다.

```sql
spark.sql("""
    SELECT prod_id, COUNT(*)
    FROM order
    GROUP BY prod_id
    ORDER BY COUNT(*) DESC
""").show

+-------+--------+
|prod_id|count(1)|
+-------+--------+
| prod01|       5|
| prod02|       2|
| prod03|       1|
+-------+--------+
```

최적화된 SELECT문은 다음과 같다.

```
spark.sql("""
    SELECT prod_id, SUM(cnt)
    FROM (
        SELECT prod_id,
            FLOOR(RAND() * 2) AS rnd, -- 0, 1 사이의 랜덤 값 생성
                                      -- 실제 data에서는 더 큰 값을 주는 게 좋다
            COUNT(*) AS cnt
        FROM order
        GROUP BY prod_id, rnd -- groupBy에 rnd가 포함되었다
                              -- 즉, 동일 prod_id라 하더라도
                              -- 다른 partition에 저장된다
    ) t
    GROUP BY prod_id -- 상품별로 다시 취합
    ORDER BY SUM(cnt) DESC
""").show

+-------+--------+
|prod_id|sum(cnt)|
+-------+--------+
| prod01|       5|
| prod02|       2|
| prod03|       1|
+-------+--------+
```

약간 복잡해보이는데 결과를 동일하다. skew 정도에 따라 수행 시간이 빨라질 수도 있지만 일반적인 경우라면 더 느려지게 된다.

위의 예에서는 랜덤 값을 두 가지 종류로 발급하였으므로 동일 상품이 두 개의 partition으로 나뉘게 된다.

즉, inner query에서는 상품별로 partial한 결과를 만들고, outer query에서 partial한 결과를 전체 결과로 만들게 된다.

작동 방법을 이해하려면 inner query 결과를 눈으로 이해해보면 된다.

```sql
spark.sql("""
    SELECT prod_id,
        FLOOR(RAND() * 2) AS rnd,
        COUNT(*) AS cnt
    FROM order
    GROUP BY prod_id, rnd
    ORDER BY prod_id

+-------+---+---+
|prod_id|rnd|cnt|
+-------+---+---+
| prod01|  0|  3| <= prod01이 rnd=0, rnd=1에 의해 두 개의 레코드로 나뉘었다
| prod01|  1|  2| <= prod01의 두 번째 레코드
| prod02|  1|  1|
| prod02|  0|  1|
| prod03|  0|  1|
+-------+---+---+
""").show
```

위 결과에서 볼 수 있듯이 `prod01`이 두 개의 레코드로 출력되었다. outer query에서는 이를 다시 한번 취합한다. outer query를 수행할 때는 skewness가 낮기 떄문에 처리 속도가 빠르다.

## skew된 data에 대한 join 연산 최적화

join은 좀 더 복잡하다. 일반적인 fact table, dimension table의 JOIN에서 사용되는 기법이고 모든 JOIN에서 작동될지는 확실치 않다.

최적화되기 전의 SELECT문은 다음과 같다.

```scala
spark.sql("""
    SELECT order.order_id, order.prod_id, product.price
    FROM order INNER JOIN product USING(prod_id)
    ORDER BY order.order_id
""").show

+--------+-------+-----+
|order_id|prod_id|price|
+--------+-------+-----+
| order01| prod01|  100|
| order02| prod01|  100|
| order03| prod01|  100|
| order04| prod01|  100|
| order05| prod01|  100|
| order06| prod02|  200|
| order07| prod02|  200|
| order08| prod03|  200|
+--------+-------+-----+
```

최적화된 SELECT문을 단계별로 만들어보자.

우선 `order` 테이블에 랜덤 값을 할당해야햔다.

```sql
spark.sql("""
    SELECT order_id,
        prod_id,
        FLOOR(RAND() * 2) AS rnd
    FROM order
""").show

+--------+-------+---+
|order_id|prod_id|rnd|
+--------+-------+---+
| order01| prod01|  1|
| order02| prod01|  0|
| order03| prod01|  1|
| order04| prod01|  0|
| order05| prod01|  0|
| order06| prod02|  0|
| order07| prod02|  1|
| order08| prod03|  1|
+--------+-------+---+
```

여기까지는 groupBy에서 했던 것과 동일하다.

이번에는 `product` 테이블을 처리해야할 때이다. 그런데 `product` 테이블에 랜덤 값을 생성한 뒤 join을 하기가 쉽지 않다.

예를 들어 `product` 테이블에 아래와 같이 랜덤 값을 생성하는 것은 JOIN하는데 아무런 의미도 효과도 없다. 만약 `rnd`를 JOIN 조건에 넣는다고 하더라도 join이 되지 않는다.

```scala
spark.sql("""
    -- 무의미한 SELECT문
    SELECT prod_id,
        price,
        FLOOR(RAND()*2) AS rnd
    FROM product
""").show

+-------+-----+---+
|prod_id|price|rnd|
+-------+-----+---+
| prod01|  100|  0|
| prod02|  200|  1|
| prod03|  200|  1|
+-------+-----+---+
```

올바른 방법은 다음과 같이 `product` 테이블의 레코드를 랜덤 값 개수 만큼 뻥튀기 시키는 것이다.

```scala
spark.sql("""
    SELECT prod_id,
        price,
        EXPLODE(ARRAY(0, 1)) AS rnd
    FROM product
""").show

+-------+-----+---+
|prod_id|price|rnd|
+-------+-----+---+
| prod01|  100|  0| <= prod01의 레코드가 두개가 되었다
| prod01|  100|  1| <= rnd의 값이 0인 것과 1인 것 두개가 생성되었다
| prod02|  200|  0|
| prod02|  200|  1|
| prod03|  200|  0|
| prod03|  200|  1|
+-------+-----+---+
```

이제 JOIN을 위한 준비가 다되었다. 위 SELECT 문 두 개의 결과를 (`prod_id`, `rnd`) 필드의 값으로 JOIN하면 된다.

완성된 SQL은 다음과 같다.

```scala
spark.sql("""
    SELECT order_id,
        prod_id,
        price
    FROM (
        SELECT order_id,
            prod_id,
            FLOOR(RAND() * 2) AS rnd
        FROM order
      ) order INNER JOIN (
        SELECT prod_id,
            price,
            EXPLODE(ARRAY(0, 1)) AS rnd
        FROM product
      ) product USING(prod_id, rnd)
    ORDER BY order.order_id
""").show

+--------+-------+-----+
|order_id|prod_id|price|
+--------+-------+-----+
| order01| prod01|  100|
| order02| prod01|  100|
| order03| prod01|  100|
| order04| prod01|  100|
| order05| prod01|  100|
| order06| prod02|  200|
| order07| prod02|  200|
| order08| prod03|  200|
+--------+-------+-----+
```

## 참고: Spark 3.0

Spark 3.0에서는 Adaptive Query Execution이라는 기능이 추가되었는데 skew join 최적화를 지원한다. 자세한 것은 [이 글](https://medium.com/@jeevan.madhur22/spark-3-0-features-demo-data-skewness-aqe-a5c237d3d5db)을 참고해보자

{% include spark-reco.md %}
