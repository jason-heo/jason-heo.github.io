---
layout: post
title: "okHttp3 MockWebServer를 이용한 Mock Elasticsearch"
categories: "programming"
---

테스트에 사용된 코드는 github repository에 올려두었다 [바로가기](https://github.com/jason-heo/okhttp3-mockwebserver-elastisearch)

개발 언어는 Scala이다.

## 개요

Elasticsearch(이하 es)가 매우매우 좋은 제품인 것은 분명하다. 그런데 es 사용 중에 어려움이 있는데 간혹 가다 429 에러와 timeout이 발생한다는 점이고 es client program들은 이를 잘 대응해야한다.

es 호출 제어권이 우리에게 있는 경우는 retry code를 넣고 이를 테스트하기가 쉽다. 그런데 본인의 경우 Spark에서 es hadoop을 이용하여 데이터를 저장하고 있는데 처리 흐름이 Spark에 있다보니 테스트가 쉽지 않다.

es가 비정상적인 상황인 상황을 쉽게 만들 수 있다면 그나마 test가 쉬울 듯 한데, 강제로 429 에러를 발생시키거나 timeout을 만들어내기가 어렵다.

그래서 생각한 것이 okHttp3 [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)를 이용하여 아주 간단하게 es를 흉내내는 것이었다.

MockWebServer의 문서를 보면 다음과 같이 나와있다.

> test that your code survives in awkward-to-reproduce situations like 500 errors or slow-loading responses

즉 test 시에 에러 상황을 쉽게 재현할 수 있다는 장점이있다.

정상적인 기능 테스트라면 es docker를 이용하는 것이 좋은 선택이지만, 비정상적인 상황에서의 기능 테스트를 위해선 MockWebServer가 좋은 해법이라 생각한다.

## MockWebServer 간략 소개

okHttp3라는 http client program에서 만든 mock web server이다. 본인의 경우 fabric8 kubernetes client의 test code에서 [k8s mock server](https://github.com/fabric8io/kubernetes-client/tree/master/kubernetes-tests/src/test/java/io/fabric8/kubernetes/client/mock) 관련된 코드를 보다가 알게되었다. ([관련 글](/programming/2021/09/27/fabric8-mock-server.html) 참고)

MockWebServer에서는 두 가지 mode를 제공한다.

- queue mode
  - 반환할 response를 미리 queue에 등록해둔다
  - 요청이 들어오면 queue에 저장된 response가 순서대로 반환된다
- dispatcher mode
  - 요청에 따른 response를 직접 구현할 수 있다

우선 간단한 queue mode부터 살펴보자. 아래 내용은 MockWebServer의 [`README.md`](https://github.com/square/okhttp/blob/master/mockwebserver/README.md)에서 발췌하였다.

```java
  // Create a MockWebServer. These are lean enough that you can create a new
  // instance for every unit test.
  MockWebServer server = new MockWebServer();

  // Schedule some responses.
  server.enqueue(new MockResponse().setBody("hello, world!"));
  server.enqueue(new MockResponse().setBody("sup, bra?"));
  server.enqueue(new MockResponse().setBody("yo dog"));

  // Start the server.
  server.start();
```

위와 같은 response를 queue에 저장한 경우, path가 뭐가되었든 첫 번째 요청에 대해서는 "hello, world!"가 반환된다. 당연하겠지만 두 번째 요청에 대해서는 "sup, bra?"가 반횐된다.

테스트에 사용하기엔 뭔가 기능이 부족해보인다.

이를 위해 dispatcher mode가 존재한다.

```java
final Dispatcher dispatcher = new Dispatcher() {

    @Override
    public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

        switch (request.getPath()) {
            case "/v1/login/auth/":
                return new MockResponse().setResponseCode(200);
            case "/v1/check/version/":
                return new MockResponse().setResponseCode(200).setBody("version=9");
            case "/v1/profile/info":
                return new MockResponse().setResponseCode(200).setBody("{\\\"info\\\":{\\\"name\":\"Lucas Albuquerque\",\"age\":\"21\",\"gender\":\"male\"}}");
        }
        return new MockResponse().setResponseCode(404);
    }
};
server.setDispatcher(dispatcher);
```

별다른 설명이 없더라도 어떤 일을 하는지 쉽게 이해할 수 있으리라 생각된다.

client가 `/v1/login/auth`를 호출한 경우 `200 OK`가 return된다. `request.getMethod()`를 이용하면 request method도 알 수 있으므로 method에 따른 분기도 가능하다.

MockWebServer의 좋은 점은 서두에 말한 것처럼 장애 상황을 쉽게 만들 수 있다는 점이다.

`setResponseCode(500)`만 설정하면 요청에 대한 500 에러 상황을 만들 수 있다.

`setBodyDelay(10, TimeUnit.SECONDS)`를 이용하면 결과를 10초 뒤에 내보낼 수도 있다.

`setSocketPolicy(SocketPolicy.NO_RESPONSE)`를 이용하면 결과를 안내보낼 수도 있다 (client는 계속 기다리게 된다)

그럼 이제 실제 작동하는 코드를 살펴보자.

## queue mode

우선 queue mode이다. 전체 소스 코드는 [여기](https://github.com/jason-heo/okhttp3-mockwebserver-elastisearch/blob/main/src/test/scala/io/github/jasonheo/QueueTest.scala)에서 볼 수 있다.

우선 다음과 같이 `beforeAll()`을 이용하여 test가 시작할 때 3개의 response를 queue에 저장하였다.

```scala
  override def beforeAll(): Unit = {
    server = new MockWebServer

    server.enqueue(new MockResponse().setBody("Hi"))
    server.enqueue(new MockResponse().setBody("Hello"))
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
    // delay를 주고 싶은 경우는 다음과 같이 설정하면 된다
    // server.enqueue(new MockResponse().setBodyDelay(10, TimeUnit.SECONDS).setBody("Goodbye"))

    server.start(7200)
  }
```

세 번째 response는 `SocketPolicy.NO_RESPNSE`로 설정하였으므로 client에서는 `SocketTimeoutException`가 발생한다.

그리고 다음과 같이 `getResponse()`라는 utility 함수를 만들었다. (http client library로는 okHttp3를 사용하였다)

```scala
  private def getResponse(path: String): String = {
    val request = new Request.Builder()
      .url(s"http://localhost:7200/${path}")
      .build

    val call = httpClient.newCall(request)

    val response = call.execute()

    val responseStr = response.body().string()

    response.close()

    responseStr
  }
```

실제 test 시나리오는 다음과 같다.

```java
  "mockServer" must "return response in a queue" in {
    getResponse("/foo") should be("Hi")
    getResponse("/bar") should be("Hello")

    intercept[SocketTimeoutException] { // intercept는 exception을 잡는 scala test 함수이다
      getResponse("/baz")
    }
  }
```

queue에 등록된 것과 같이 처음 두개의 요청에 대해선 "Hi", "Hello"가 return되었고, 세 번째 요청은 `SocketTimeoutException`가 발생하였다.

## Dispatcher mode

아래에 설명된 전체 코드는 [여기](https://github.com/jason-heo/okhttp3-mockwebserver-elastisearch/blob/main/src/test/scala/io/github/jasonheo/EsDispatcherTest.scala)에서 볼 수 있다.

dispatcher mode의 예제 코드는 좀 더 복잡하다.

es의 index 관련 API를 mock해보았다.

- index 생성: `PUT /<index-name>`
- index 삭제: `DELETE /<index-name>`
- index 목록 조회: `GET /*`

위 3개 요청을 mocking만 할 것이므로 index 목록은 `val indices = mutable.HashSet[String]()`에 저장하였다.

`PUT /<index-name>` 요청이 들어오는 경우 index-name을 `indices` 자료 구조에 저장하고 결과를 전송한다. `DELETE /<index-name>`의 경우 `indices` 자료 구조에서 해당 index를 삭제하고 결과를 전송한다.

대략적인 dispatcher의 틀은 다음과 같다.

```scala
      override def dispatch(request: RecordedRequest): MockResponse = {
        println(s"request = '${request.getMethod} ${request.getPath}'")

        if (request.getMethod == "GET") {
          processGetRequest(request)
        }
        else if (request.getMethod == "PUT") {
          createIndex(request)
        }
        else if (request.getMethod == "DELETE") {
          deleteIndex(request)
        }
        else {
          notSupported(request)
        }
      }
```

index를 생성하는 `createIndex()`는 다음과 같이 생겼다.

```scala
      private def createIndex(request: RecordedRequest): MockResponse = {
        val indexName = getIndexName(request)

        if (indices.contains(indexName)) { // index가 이미 존재하는 경우 -> error
          getMockResponse()
            .setResponseCode(400)
            .setBody(
              s"""{
                 |  "error": {
                 |    "root_cause": [
                 |      {
                 |        "type": "resource_already_exists_exception",
                 |        "reason": "index [${indexName}/Qf61cScBSY2gxFQWO4VQig] already exists",
                 |        "index_uuid": "Qf61cScBSY2gxFQWO4VQig",
                 |        "index": "${indexName}"
                 |      }
                 |    ],
                 |    "type": "resource_already_exists_exception",
                 |    "reason": "index [${indexName}/Qf61cScBSY2gxFQWO4VQig] already exists",
                 |    "index_uuid": "Qf61cScBSY2gxFQWO4VQig",
                 |    "index": "${indexName}"
                 |  },
                 |  "status": 400
                 |}""".stripMargin('|'))

        }
        else { // index가 없는 경우 -> indices에 추가
          indices.add(indexName)

          getMockResponse()
            .setResponseCode(200)
            .setBody(
              s"""{
                 |  "acknowledged": true,
                 |  "shards_acknowledged": true,
                 |  "index": "${indexName}"
                 |}""".stripMargin('|'))
        }
      }
```

이후 [ES High Level Rest Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)를 이용하여 index를 생성/조회해보았다. ES High Level Rest Client는 본 글의 범위를 넘어서므로 index를 생성하는 코드만 보고가자. (참고: 지금보니깐 High Level Rest Client가 7.15.0 버전부터 Deprecated되었다. es는 버전에 따라 변경되는 것이 너무 빨라서 따라가기가 힘들다)

```scala
    val esClient: RestHighLevelClient = new RestHighLevelClient(
      RestClient.builder(
        new HttpHost("localhost", 8200, "http")
      )
    )

    val request = new CreateIndexRequest(indexName)

    esClient.indices().create(request, RequestOptions.DEFAULT)
```

test 시나리오는 다음과 같다.

```scala
  server.setDispatcher(EsDispatcher.get("7.16.2"))

  server.start(8200)

  getIndices().size should be(0) // 서버가 최초 실행된 이후에는 index가 없다

  createIndex("idx1") // index 두 개를 생성한다
  createIndex("idx2")

  getIndices().size should be(2) // 조회한 index 개수가 2개인지 확인

  getIndices().sorted should be(Seq("idx1", "idx2")) // index 내용 확인
```

## 마무리

MockWebServer를 이용하면 es의 비정상 상황을 쉽게 재현할 수 있어 보인다.

검색을 해보니깐 node, python의 경우 이미 mock을 제공하는 것 같다.

- node: https://www.npmjs.com/package/@elastic/elasticsearch-mock
- python: https://pypi.org/project/ElasticMock/

직접 mock을 해보니 index 관련 API 3개만 해도 구현해야할 것이 생각보다 많았다. 위의 구현물은 어떤 기능을 제공하는지 궁금하다.

java의 경우에도 [Mock elasticsearch with mock-server](https://thomasdecaux.medium.com/mock-elasticsearch-with-mock-server-5811cf141035)라는 글이 있는데 이것도 완전한 구현체는 아니고 본 포스트와 비슷한 것 같다.

{% include test-for-data-engineer.md %}
