---
layout: post
title: "가독성을 위해 map()의 결과로 case class를 사용해도 속도 저하가 없을까?"
categories: "bigdata"
---

Spark에서 입력 data를 다른 형식으로 변환할 때 `map()`을 사용한다. 변환된 값이 1개의 여러 개이면 Tuple로 return하는 것이 일반적이다. 그런데, Tuple을 사용할 때의 문제는 가독성이 떨어진다는 점이다.

### 1.

예를 보자.

```scala
ds.
  map(row => (row.timestamp, row.lat, row.numCars)). // Tuple로 return
  filter(tuple => tuple._3 == 1).                    // 3번째 필드가 1인지 검사
  count
```

`filter()` 부분을 주목했으면 하는데, `tuple._3` 처럼 사용해야하다보니 가독성이 안 좋다. 즉, "세 번째 필드가 어떤 걸 의미하더라?" 헷갈리게 된다. 위처럼 단순한 예제에서야 그마나 쉽게 답을 알 수 있지만, 필드 개수가 많거나 혹은 변환 로직이 복잡해서 별도 함수에서 map을 처리한다던가 하면 필드 번호에 어떤 의미의 값이 저장되었는지 찾기가 어렵다.

### 2.

이를 회피하기 위해선 아래와 같이 사용하는 방법이 있다.

```scala
ds.
  map(row => (row.timestamp, row.lat, row.numCars)).
  filter(tuple => tuple match {case (timestamp, lat, numCars) => tuple._3 == 1}).
  count
```

Tuple의 필드 번호마다 이름을 붙여주는 방식이라 첫 번째 방식보다는 가독성이 좋지만 실수를 할 가능성이 있다. (필드 순서를 바꿔 적는 등)

참고: 위의 코드는 `filter{case (timestamp, lat, numCars) => ...}` 처럼 사용 가능해야하는데, Spark Dataset에 버그가 있어서 이 방식으로 사용이 불가능하다. ([버그 확인](https://issues.apache.org/jira/browse/SPARK-19492))

### 3.

가독성을 위한 제일 좋은 방법은 `case class`를 사용하는 방법이다.

```scala
case class MappedLog(timestamp: java.sql.Timestamp, lat: Double, numCars: Int)

ds.
  map(row => MappedLog(row.timestamp, row.lat, row.numCars)).
  filter(mappedRow => mappedRow.numCars == 1).
  count
```

이 경우, `map()`에서는 `MappedLog`라는 class의 instance를 return하기 때문에 필드 숫자가 아닌 필드의 이름으로 접근이 가능하여 가독성이 높다.

그런데 "Tuple을 사용했을 때 대비 속도 저하가 없을까?"라는 걱정이 든다.

### 4. 실험 결과 성능 저하 없음

그래서 성능 실험을 해 봤다. 실험 전부터 성능 저하가 없을 것이라 생각했었는데 (왜냐하면 `Tuple`이든 `MappledLog`든 둘다 class니깐 instance를 만들고 GC에 의해 메모리에서 해제되는 일련의 과정이 동일할 것이라 성능 저하가 없을 것 같았음), 역시나 성능 저하가 없었다

### 5. 실험에 사용된 코드

그래서 실험을 해 봤다. 실험용 데이터는 Kaggle에서 제공하는 [Shared Cars Locations](https://www.kaggle.com/doit-intl/autotel-shared-car-locations/downloads/autotel-shared-car-locations.zip/1)를 활용했다. (메뉴얼하게 랜덤 값을 생성해도 되지만 그래도 현실과 유사한 데이터를 사용하고 싶어서)

Shared Cars Locations는 필드 개수가 많지 않으며 data type도 적절히 들어있어서 좋다.

아래는 test code이니 참고할 분은 참고하시길.

```scala

// 성능 측정용 함수: http://jason-heo.github.io/programming/2019/04/13/elapsed-time.html
def elapsedTime[R](block: => R): R = {
    val s = System.currentTimeMillis
    val result = block    // call-by-name
    val e = System.currentTimeMillis
    println("[elapsedTime]: " + ((e - s) / 1000.0f) + " sec")
    result
}

// input csv의 schema
case class InputLog(timestamp: java.sql.Timestamp,
                    lat: Double,
                    lon: String,     // 원래는 Double이긴 한데, String으로 읽음
                    numCars: Int,
                    carList: String) // 원래는 [182] 같은 array 표현이 저장되어 있으나,
                                     // csv는 array를 지원하지 않아서 String으로 읽음

// 파일 읽기
val ds = spark.
            read.
            schema(org.apache.spark.sql.Encoders.product[InputLog].schema).
            csv("/tmp/car-location/1M.csv").as[InputLog]

// 1. tuple & 필드 번호 사용
elapsedTime({
    ds.
      map(row => (row.timestamp, row.lat, row.numCars)).
      filter(tuple => tuple._3 == 1).
      count
})

// 2. tuple & match case 사용
elapsedTime({
    ds.
      map(row => (row.timestamp, row.lat, row.numCars)).
      filter(tuple => tuple match {case (timestamp, lat, numCars) => tuple._3 == 1}).
      count
})

// 3. case case 사용
// map에서는 아래 3개의 필드만 return한다
case class MappedLog(timestamp: java.sql.Timestamp,
                    lat: Double,
                    numCars: Int)

elapsedTime({
    ds.
      map(row => MappedLog(row.timestamp, row.lat, row.numCars)).
      filter(mappedRow => mappedRow.numCars == 1).
      count
})
```
