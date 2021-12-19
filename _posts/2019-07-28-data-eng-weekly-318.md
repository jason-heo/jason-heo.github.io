---
layout: post
title: "Data Engineering weekly news issue #318 (2019.07.21 발행)"
categories: "bigdata"
---

{% include data-eng-weekly.md %}

## 이슈 #318

### Operating a Large, Distributed System in a Reliable Way

Uber에서 작성한 글.

https://blog.pragmaticengineer.com/operating-a-high-scale-distributed-system/

분산 시스템 운영의 Best Practice들을 공유한다.

구글에서 작성한 [SRE Book](https://landing.google.com/sre/sre-book/toc/)이라는 무료 책도 알게 되었다.


### Presto at Pinterest

https://medium.com/@Pinterest_Engineering/presto-at-pinterest-a8bda7515e52

한 3년 전에 Presto를 써 봤을 때 성능적으로 큰 장점이 안 보였는데, 사용하는 곳이 많은 것 같다.

Pinterest에서 사용중인 Presto Cluster의 규모는 Data가 수백 PB, 하이브 테이블이 수만개, 메모리가 100TB 이상, core 개수가 1.4만개 이상이라고 한다.

### OctoSQL

https://github.com/cube2222/octosql

Octo하면 팔이 8개 달린 문어가 생각난다. OctoSQL도 문어에서 이름을 따왔다고 한다.

여러 개의 Data Source로부터 data를 읽어들이는 것 문어로 형상화했다한다.

csv 파일과 redis에 각각 Data가 존재할 때 아래처럼 설정 파일을 만든 후

```
dataSources:
  - name: cats
    type: csv
    config:
      path: "~/Documents/cats.csv"
  - name: people
    type: redis
    config:
      address: "localhost:6379"
      password: ""
      databaseIndex: 0
      databaseKeyName: "id"
```

아래와 같은 SQL을 실행하면 cvs와 redis의 Data를 JOIN할 수 있다.

```
octosql "SELECT p.city, FIRST(c.name), COUNT(DISTINCT c.name) cats, SUM(c.livesleft) catlives
FROM cats c JOIN people p ON c.ownerid = p.id
GROUP BY p.city
ORDER BY catlives DESC
LIMIT 9"
```

### LinkedIn has open sourced Brooklin

https://engineering.linkedin.com/blog/2019/brooklin-open-source

Linkedin에서 사용 중인 streaming 입수 시스템이라는데, 하루에 2조 개의 메시지를 처리 중이라고 한다.

다양한 Data Source(Kafka, Kinesis, Oracle, MySQL) Data를 읽을 수 있고, Destination도 다양하다고 하는데, Spark Streaming과의 차이점은 무엇일까?

{% include spark-reco.md %}
