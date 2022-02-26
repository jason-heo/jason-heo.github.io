---
layout: post
title: "Data Injection: Don't produce me, I will produce myself"
categories: "bigdata"
---

Software Engineering에 있어서 가장 중요한 것 1개를 꼽으라고 한다면 나는 주저없이 Dependency Injection을 꼽을 것이다.

Data Engineering에서도 이와 비슷한 개념이 있는데 이것을 나는 오늘부터 Data Injection이라고 부를 것이다. 구글링해봐도 아직은 검색 결과가 없는 내가 최초로 정립한 기법일 수 있겠다.

## 기초 - Dependency Injection : Don't call me, I will call you

Dependency Injection에 대한 내용은 인터넷에 많이있지만, Data Injection과 비교하기 위해 간단히 보고 넘어가자.

다음과 같은 code가 있다고 하자.

```java
public class Transaction {
  private Payment payment = new CardPayment();

  public boolean process(int amount) {
    return payment.process(amount);
  }
}
```

`class Transaction` 안에서 `new`가 호출되고 있다. 이런 class들은 test하기가 어렵다. 왜냐? class 안에서 `class CardPayment` instance를 만들었으므로, test 과정에서 실제 카드 결제가 발생하기 때문이다.

이를 해결하기 위해선 외부에서 new로 생성한 instance를 `Transaction`에 전달해야한다.


```java
public class Transaction {
  private Payment payment = new CardPayment();

  public Transaction(Payment payment) {
    this.payment = payment;
  }

  public boolean process(int amount) {
      return payment.process(amount);
  }
}

// Transaction을 사용하는 곳의 코드
Payment payment = new MockedCardPayment();

Transaction transaction = new Transaction(mockedPayment);
```

이렇게 mock class를 전달하게 되면 test 과정에서 실제 카드 결제가 발생하지 않는다.

의존성이 외부에서 제공되므로 이를 Dependency Injection이라고 한다. 또한 "Don't call me, I will call you"로도 널리 알려져있다.

## Data Injection: Don't produce me, I will produce myself

Data Engineering에서도 테스트하기 좋은 코드를 작성하는 것은 중요하다. 오늘 나는 Data Injection을 제안하고자 한다. 아마 많은 개발자들이 이와 같은 기법을 사용하고 있을 것이므로 용어 제안 정도로 이해하면 좋겠다.

### Data Injection이 적용되지 않은 코드

Parquet에 있는 웹 로그를 읽어서 url별 pv를 계산하여 다시 Parquet file에 저장하는 코드가 있다고 해보자. 다음과 같은 코드도 작동하는데 아무 문제없을 것이다.

```scala
def pvPerUrl() {
  val df = spark.read.parquet("/path/to/foo")

  df.createOrReplaceTempView("logs")

  val df2 = spark.sql("""
    SELECT url, COUNT(*) AS pv
    FROM logs
    GROUP BY url
  """)

  df2.write.parquet("/path/to/bar")
}
```

기능상 아무런 문제가 없는 코드이다. 그런데 어떤 문제가 있을까? 그렇다 `def pvPerUrl()`의 test code를 작성하기 어렵다는 문제가 있다.

test에 사용되는 input을 내 마음대로 줄 수가 없고, 주어진 input의 groupBy 결과가 올바른지 확인하기 어렵다.

### Data Injection을 적용한 결과

Data Injection을 적용한 코드는 다음과 같다.

```scala
def pvPerUrl(df: DataFrame): DataFrame {
  df.createOrReplaceTempView("logs")

  spark.sql("""
    SELECT url, COUNT(*) AS pv
    FROM logs
    GROUP BY url
  """)
}

val df = Seq(("naver.com", "f"), ("daum.net", "m")).toDF("url", "gender")

val df2 = pvPerUrl(df)

df2.count should be(2)
```

즉, `dev pvPerUrl()` 내에서 DataFrame을 생성하지 않고 외부에서 DataFrame을 inject하였다.

이를 Data Injection이라고 불렀다. 또한 Dependency Injection과 비슷하게 "Don't produce me, I will produce myself"라는 라임을 넣어봤다.

{% include test-for-data-engineer.md %}
