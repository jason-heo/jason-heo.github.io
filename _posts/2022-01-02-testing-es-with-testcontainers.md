---
layout: post
title: "Elasticsearch Test 환경 만들기 (부제: testContainers를 이용한 DB test 환경 구축)"
categories: "bigdata"
---

### 개요

"Unit Test에서 Elasticsearch를 편하게 연동하는 방법"을 찾다가 [testContainers](https://www.testcontainers.org/)라는 것에 대해 알게 되었다.

찾게된 과정은 [Elasticsearch 단위테스트를 위한 testContainer사용하기](https://honeyinfo7.tistory.com/301)라는 글과 동일하다. 잘만 사용한다면 Test Case 작성하는데 많은 도움이 될 듯 하다.

Elasticsearch 이외에도 지원되는 DB가 많이 있다. 많이 사용되는 것들 몇 개를 적어보자면 다음과 같다.

- Cassandra
- DB2
- InfluxDB
- MongDB
- MS SQL Server
- MySQL
- Oracle-XE
- PostgreSQL

그리고 문서상에서 DB로 분류되지 않았지만 다음과 같은 것들도 지원한다

- Kafka
- Mockserver
- Nginx
- Solr

testContainers를 사용하기 위해선 Test할 장비에 docker가 설치되어 있어야 한다. OS별 호환되는 docker 버전은 [General Docker requirements](https://www.testcontainers.org/supported_docker_environment/)에서 볼 수 있다.

최근 Docker Desktop 라이센스 문제로 Docker Desktop Mac에서 Docker Machine을 사용 중인데 Docker Machine v0.8.0 이후 버전이라면 testContainers를 사용할 수 있다고 한다.

본인이 업무에서 주로 사용하는 것은 Kafka와 Elasticsearch이다.

Kafka는 지난번에 작성한 [Kafka Unit Test with EmbeddedKafka](/bigdata/2021/12/19/kafka-unit-test.html)로 충분했다.

본 글에서는 testContainers를 이용하여 Elasticsearch test 환경을 만들어보자.

참고로 본인은 Scala를 사용했다.

### Elasticsearch container 예

`build.sbt`에 다음의 내용을 추가한다.

```
libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % Test,
  "org.testcontainers" % "elasticsearch" % "1.16.2" % Test,
  "com.squareup.okhttp3" % "okhttp" % "4.9.3" % Test
)
```

이제 Test Case를 작성해보자.

```
import okhttp3.{OkHttpClient, Request}
import org.scalatest.{FlatSpec, Matchers}
import org.testcontainers.elasticsearch.ElasticsearchContainer

class EsTest extends FlatSpec with Matchers {
  "ES testContainers" must "work" in {
    val container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.16.2")

    container.start()

    val httpClient = new OkHttpClient

	// "GET /"을 요청한다
    val request = new Request.Builder()
      .url("http://" + container.getHttpHostAddress)
      .build

    val call = httpClient.newCall(request)

    val response = call.execute()

	// "GET /" 결과에 test에 사용한 버전인 7.16.2가 포함되었는지 테스트
    response.body.string().contains("7.16.2") should be(true)

    container.close()
  }
}
```

(test code를 단순화하기 위해서 okHttp3를 사용했다)

ES 접속 주소는 `container.getHttpHostAddress()`로 알 수 있다. 본인의 경우 `192.168.99.107:32787`라고 출력되었다. 이 값을 브라우저에 입력하면 당연하겠지만 아래와 같은 내용이 출력된다.

```json
{
  "name" : "b1dfac8c87fc",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "GlYrMc98T0KGgm0WFXJE8g",
  "version" : {
    "number" : "7.16.2",
    "build_flavor" : "default",
    "build_type" : "docker",
    "build_hash" : "2b937c44140b6559905130a8650c64dbd0879cfb",
    "build_date" : "2021-12-18T19:42:46.604893745Z",
    "build_snapshot" : false,
    "lucene_version" : "8.10.1",
    "minimum_wire_compatibility_version" : "6.8.0",
    "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
```

최초 테스트 수행 시에는 docker image를 download해야하므로 약간 멈춘 것처럼 보일 수 있다. 그리고 실제 test시에도 EmbeddedKafka보다는 체감적으로 느려보인다. 이는 docker container가 실행되고, elasticsearch deamon이 올라오면서 data가 준비되기 때문이다.

test 중에 `docker ps`를 하면 아래와 같은 docker container가 보인다.

```
$ docker ps
CONTAINER ID        IMAGE                                                  COMMAND                  CREATED             STATUS              PORTS                                              NAMES
150af272d2e8        docker.elastic.co/elasticsearch/elasticsearch:7.12.0   "/bin/tini -- /usr/l…"   12 seconds ago      Up 14 seconds       0.0.0.0:32783->9200/tcp, 0.0.0.0:32782->9300/tcp   happy_chebyshev
1f38d3410be9        testcontainers/ryuk:0.3.3                              "/app"                   13 seconds ago      Up 14 seconds       0.0.0.0:32781->8080/tcp                            testcontainers-ryuk-230890e3-4adc-4da8-ac8d-e5d43cb07756
```

뭔가 이상한 듯 하면 container의 log를 확인해보자.

test가 종료되면 container가 자동으로 종료된다.
