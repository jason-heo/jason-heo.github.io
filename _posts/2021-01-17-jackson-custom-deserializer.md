---
layout: post
title: "Scala 언어에서 Jackson Custom Deserializer 만들기"
categories: "programming"
---

## 목차

- [1. 개요](#1-개요)
- [2. Custom Deserializer 예1 - 기본 사용법](#2-custom-deserializer-예1---기본-사용법)
  - [2-1) model](#2-1-model)
  - [2-2) Custom Serializer](#2-2-custom-serializer)
  - [2-3) 사용 예](#2-3-사용-예)
  - [2-4) asText vs textValue](#2-4-astext-vs-textvalue)
- [3. Custom Deserializer 예2 - empty object를 null로](#3-custom-deserializer-예2---empty-object를-null로)
  - [3-1) model](#3-1-model)
  - [3-2) Custom Deserializer](#3-2-custom-deserializer)
  - [3-3) 사용 예](#3-3-사용-예)
- [4. Custom Deserializer 예3 - Array of object 변환](#4-custom-deserializer-예3---array-of-object-변환)
  - [4-1) model](#4-1-model)
  - [4-2) Custom Deserializer](#4-2-custom-deserializer)
  - [4-3) 사용 예](#4-3-사용-예)
- [5. Custom Deserializer에서 ObjectMapper 사용하기](#5-custom-deserializer에서-objectmapper-사용하기)
  - [5-1) model](#5-1-model)
  - [5-2) Custom Deserializer](#5-2-custom-deserializer)
- [6. 기타 - codehaus jackson vs faster xml jackson](#6-기타---codehaus-jackson-vs-faster-xml-jackson)

## 1. 개요

Jackson을 사용하면 json을 class instance로 만들기 쉽다. 반대로 class를 json으로 변경하기 쉽다. 이를 serialize, deserialize라고 한다. (이하 se/der)

- serialize: class instance를 json으로 변환
- deserialize: json을 class instance로 변환

ser/de는 Jackson이 자동으로 해주지만 뭐든 그렇듯이 사람 손을 타야하는 경우가 있다. 이 경우 custom serializer, deserializer를 만들면 된다.

만드는 방법은 인터넷에 검색해보면 많은 자료가 나오는데 대부분 Java로 코딩되어 있다. 그래서 이번 글에서는 Scala를 이용하여 Deserialize하는 코드를 소개하려한다

Custom ser/de가 아닌 default 방식의 Jackson 사용법은 본인이 작성한 [Scala에서 Jackson 라이브러리 사용법](/programming/2020/11/27/scala-jackson-example.html)를 참고한다.

## 2. Custom Deserializer 예1 - 기본 사용법

[전체 소스 코드](https://github.com/jason-heo/jackson-custom-deserializer/blob/master/src/main/scala/io/github/jasonheo/Ex1.scala)

### 2-1) model

- json model
  ```json
  {
    "id": 1,
    "name": "Kim"
  }
  ```
- scala class
  ```scala
  case class Person(id: Int, name: String)
  ```

### 2-2) Custom Serializer

```scala
class InquiryResBodyDeserializer extends JsonDeserializer[Person] {
  override def deserialize(jsonParser: JsonParser,
                           ctxt: DeserializationContext): Person = {
    val node: JsonNode = jsonParser.getCodec.readTree(jsonParser)

    Person(node.get("id").intValue(), node.get("name").textValue())
  }
}
```

Custom Deserializer를 등록하는 방법은 다음과 같이 두가지 방법이 있다

- 방법1: annotation을 사용하는 방법
  ```scala
  @JsonDeserialize(using = classOf[PersonDeserializer])
  case class Person(id: Int, name: String)
  ```
- 방법2: ObjectMapper에 module을 등록하는 방법
  ```scala
  val objectMapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper
  objectMapper.registerModule(DefaultScalaModule)

  val module: SimpleModule = new SimpleModule()

  module.addDeserializer(classOf[Person], new PersonDeserializer)
  objectMapper.registerModule(module)
  ```

참고 자료: [Getting Started with Custom Deserialization in Jackson](https://www.baeldung.com/jackson-deserialization)를 Scala에 맞게 수정했음

### 2-3) 사용 예

```scala
val objectMapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper
objectMapper.registerModule(DefaultScalaModule)

val json = """
{
  "id": 1,
  "name": "Kim"
}
"""

val person: Person = objectMapper.readValue(json, classOf[Person])

println(person)
// 출력 결과: Person(1,Kim)
```

### 2-4) asText vs textValue

- https://www.xspdf.com/resolution/52642662.html
- https://stackoverflow.com/a/40134224/2930152

## 3. Custom Deserializer 예2 - empty object를 null로

[전체 소스 코드](https://github.com/jason-heo/jackson-custom-deserializer/blob/master/src/main/scala/io/github/jasonheo/Ex2.scala)

### 3-1) model

- json model
  ```json
  {
    "name": "Kim",
    "company": {
      "name": "my-company",
      "address": "Seoul"
    }
  }
  ```
- scala class
  ```scala
  case class Person(name: String, company: Option[Company])

  case class Company(name: String, address: String)
  ```
- 문제 정의
    - `company: {}`이 입력되더라도 `company: null`과 같이 작동하도록 해보자
    - Jackson의 기본 동작은 `company: {name: null, address: null}`과 동일해서 약간 불편하다

### 3-2) Custom Deserializer

`Company` 부분만 Custom Deserializer를 만들면된다.

```scala
class CompanyDeserializer extends JsonDeserializer[Company] {
  override def deserialize(jsonParser: JsonParser,
                           ctxt: DeserializationContext): Company = {
    val node: JsonNode = jsonParser.getCodec.readTree(jsonParser)

    // 참고: https://stackoverflow.com/a/63030594/2930152
    // 위 문서에서는 if에 `node.asText().isEmpty()` 조건도 있었으나 제거했음
    if (node.isNull || node.size == 0) {
      null
    }
    else {
      Company(
        name = node.get("name").textValue(),
        address = node.get("address").textValue()
      )
    }
  }
}
```

참고 자료: https://stackoverflow.com/a/63030594/2930152 를 scala에 맞게 수정했음

### 3-3) 사용 예

```
val objectMapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper
objectMapper.registerModule(DefaultScalaModule)

val json = """
{
  "name": "Kim",
  "company": {}
}
"""

val person: Person = objectMapper.readValue(json, classOf[Person])

println(person)
// 출력 결과: Person(Kim,None)
```

## 4. Custom Deserializer 예3 - Array of object 변환

이번엔 Array를 iterate해보자. Scala 문법 때문에 약간 어려워진다.

위의 예에서는 1개의 Company 정보만 저장했지만 이번에는 복수 개의 Company 정보를 저장하고 있다.

[전체 소스 코드](https://github.com/jason-heo/jackson-custom-deserializer/blob/master/src/main/scala/io/github/jasonheo/Ex3.scala)

### 4-1) model

- json model
  ```json
  {
    "name": "Kim",
    "companies": [
      {
        "name": "my-company",
        "address": "Seoul"
      },
      {
        "name": "your-company",
        "address": "Busan"
      },
    ]
  }
  ```
- scala class
  ```scala
  case class Company(name: String, address:String)

  case class Person(name: String, companies: Option[List[Company]])
  ```

### 4-2) Custom Deserializer

이번엔 `Person`에 대한 Deserializer를 만들었다. `companies` key를 iterate할 것인데 `Company`에 대한 Deserializer를 만들면 Array를 iterate할 수 없기 때문이다.

```scala
class PersonDeserializer extends JsonDeserializer[Person] {
  override def deserialize(jsonParser: JsonParser,
                           ctxt: DeserializationContext): Person = {
    val node: JsonNode = jsonParser.getCodec.readTree(jsonParser)

    val companiesNode: JsonNode = node.get("companies")

    if (companiesNode.isArray) {
      import scala.collection.JavaConverters._

      // 핵심 포인트: asScala를 이용하여 scala 객체로 만든 뒤 map을 사용하는 부분
      val companies: List[Company] = companiesNode.asScala.map {
        companyNode: JsonNode => {
          Company(
            name = companyNode.get("name").textValue(),
            address = companyNode.get("address").textValue()
          )
        }
      }.toList

      Person(
        name = node.get("name").textValue(),
        companies = Some(companies)
      )
    }
    else { // "companies"에 값이 없는 경우
      Person(
        node.get("name").textValue(),
        None
      )
    }
  }
}
```

### 4-3) 사용 예

```scala
val objectMapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper
objectMapper.registerModule(DefaultScalaModule)

val json = """
{
  "name": "Kim",
  "companies": [
    {
      "name": "my-company",
      "address": "Seoul"
    },
    {
      "name": "your-company",
      "address": "Busan"
    }
  ]
}
"""

val person: Person = objectMapper.readValue(json, classOf[Person])

println(person)
// 출력 결과: Person(Kim,Some(List(Company(my-company,Seoul), Company(your-company,Busan))))
```

## 5. Custom Deserializer에서 ObjectMapper 사용하기

Custom Deserialzer를 사용 중에 deep nested한 field를 만나면 번거롭다. 이때는 단순하게 ObjectMapper를 다시 사용하는 것도 방법이다. 물론 serialize, deseriailze를 여러 번 해야해서 성능은 별로 안 좋을 것이다. 이 방법보다 더 좋고 우아한 방법이 있을 것 같기도 한데 아직은 잘 모르겠다.

아래 예는 `Person` class에 대한 Deserializer이다. `Company`를 deserialize할 때는 Jackson ObjectMapper를 그대로 사용하였다.

[전체 소스 코드](https://github.com/jason-heo/jackson-custom-deserializer/blob/master/src/main/scala/io/github/jasonheo/Ex4.scala)

### 5-1) model

- json model
  ```json
  {
    "name": "Kim",
    "company": {
      "name": "my-company",
      "address": "Seoul"
    }
  }
  ```
- scala class
  ```scala
  case class Person(name: String, company: Option[Company])

  case class Company(name: String, address: String)
  ```

### 5-2) Custom Deserializer

```scala
class PersonDeserializer extends JsonDeserializer[Person] {
  override def deserialize(jsonParser: JsonParser,
                           ctxt: DeserializationContext): Person = {
    val node: JsonNode = jsonParser.getCodec.readTree(jsonParser)

    val company: Company = {
      // objectMapper를 이용하여 Company를 생성한다
      val objectMapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper
      objectMapper.registerModule(DefaultScalaModule)

      objectMapper.readValue(node.get("company").toString, classOf[Company])
    }

    Person(
      name = node.get("name").textValue(),
      company = Some(company)
    )
  }
}
```

## 6. 기타 - codehaus jackson vs faster xml jackson

jackson을 사용하면서 헷갈리는 점이 IntelliJ 등에서 자동으로 import를 할 때 codehaus와 fasterxml 두 개가 존재한다는 점이다.

2012년에 Jackson 2.0이 release되면서 codehaus에서 fasterxml로 이동하였다.

인터넷을 검색해보면 codehaus를 import하는 코드들이 있는데 잘 돌아가지를 않는다. 꼭 fasterxml를 import해서 사용하도록 하자.

참고1: https://stackoverflow.com/a/30782762/2930152
참고2: [Presentation Jackson 2.0](https://github.com/FasterXML/jackson-docs/wiki/Presentation-Jackson-2.0)
