---
layout: post
title: "앞으로 기대되는 Apache Druid의 3가지 기능"
categories: "bigdata"
---

2022년 3월 1일에 Imply blog에 올라온 [A new shape for Apache Druid](https://imply.io/blog/a-new-shape-for-apache-druid/)라는 글을 요약해본다.

앞으로 3가지 기능을 제공 예정인데, 당장 사용할 수있는 기능이 아니지만 앞으로를 기대하게 된다.

## 들어가며

우리 팀에서 Druid를 2017년 여름경부터 사용했으니깐 거의 만 5년 정도 사용한 듯 하다. Druid 사용 경험을 모아서 2018년에는 팀 멤버들과 함께 외부에 발표도 진행했었다.

- [Web analytics at scale with Druid at naver.com](https://www.slideshare.net/JasonJungsuHEO/web-analytics-at-scale-with-druid-at-navercom), Strata London 2018
- [Druid로 쉽고 빠르게 빅데이터 분석하기](https://deview.kr/2018/schedule/244), Deview 2018

현재까지도 Druid를 아주 잘 사용 중이고 Druid도 5년 동안 많은 발전이 있어왔지만 팀에서 Druid use-case 적으로 큰 변화는 없다. 5년 전처럼 지금도 동일 모습으로 잘 작동 중이다.

그러던 중 [A new shape for Apache Druid](https://imply.io/blog/a-new-shape-for-apache-druid/)라는 글을 봤는데 흥미로운 주제가 보여서 공유할까 한다.

## 기능 1:  Multi-stage distributed queries

현재 Druid의 쿼리 수행 방식은 Broker가 질의를 받아서 Historical에 질의를 전송한 뒤에 partial 결과를 Broker에서 합치는 방식이다. Elasticsearch의 Coordinator와 Data Node의 관계를 생각하면 된다.

이 방식은 OLTP 성 질의에는 최적화되어있다. top-k 질의에서 `k`가 작은 경우에 성능이 매우 빠르지만 `k`가 커질 수록 질의처리 모델 상 Broker 병복에 의해 성능이 느려진다. 이는 Elasticsearch에도 존재하는 문제이다.

예를 들어보자. 1억명 사용자의 로그 100억개가 Historical 10개에 나뉘어 있다고 하자. 만약 `SELECT ... GROUP BY age ORDER BY COUNT(*) DESC` 같은 질의는 Druid에서 매우 빠르게 처리한다. 왜냐면 `age`의 개수가 많아봐야 100개이므로 Historical은 최대 100개의 레코드만 Broker에 전달하면 된다.

그런데 만약 `SELECT ... GROUP BY user_id ORDER BY COUNT(*)`를 질의한다고 하자. LIMIT이 없음에 주의하자. 이때는 1억개의 레코드가 Broker에 모이게 되므로 성능이 매우 느리다.

이걸 없애기 위해서는 질의 처리 모델을 Spark 처럼 변경해야하고 Druid에서는 "Multi-stage distributed queries"라고 부르기로 한 것 간다.

Druid나 Elasticsearch를 사용하면서 아쉬웠던 점인데 매우 기대되는 기능이다.

진행 단계는 매우 느리다. https://github.com/apache/druid/issues/12262 를 보면 2022년 2월에서야 겨우 proposal이 나온 정도이다.

그런데 블로그 글 원문을 보면 초기 버전을 Imply 고객에게 제공한 걸 봐서 이미 개발이 어느 정도 완료된듯 하다

## 기능 2: Ingestion and external data

Druid를 사용하면서 제일 어려웠던 점 중 하나는 "입수" 작업이다. 버전이 올라가면서 입수가 쉬워진 듯하지만 여전히 어렵다.

그래서 `INSERT INTO SELECT` 같은 구문을 지원하려는 듯 하다.

## 기능 3: An option to further separate storage and compute

아마 Druid를 처음 접하는 사용자가 헷갈려하는 개념 중 하나가 storage model 일 것 같다. HDFS 같은 Deep storage와 Historical에 저장된 segment cache가 존재한다. (크게 어려운 개념은 아니라서 금방 익숙해질 수 있다)

질의를 위해선 Historical에 segment를 download해야하므로 Historical의 Disk size가 중요하다.

OLTP 성 질의가 아닌 경우, 속도가 어느 정도 느려도 괘찮은데 이를 위해서 "deep storeage로부터 data를 바로 읽는 모드"를 지원 예정이라고 한다.

물론 기존과 같은 prefetch 방식도 계속 지원한다.

## 마무리

특히 "기능 1"과 "기능 3"이 기대된다. 이들이 구현 완료되면 Druid의 질의 처리 방식이 Spark과 점점 더 유사해질 것 같다. OLTP와 OLAP으르 두루두루 잘 지원하는 Druid가 되면 좋겠다.
