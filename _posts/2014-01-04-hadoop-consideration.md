---
layout: post
title: "Hadoop 고려 사항"
date: 2014-03-06 21:34:00
categories: computer
---

## 들어가기 전에

사실 Hadoop은 Apache에서 만든 분산 platform이다. HBase, HDFS 등 여러 모듈들이 모여서 Hadoop platform을 구성한다. 본 문서에서는 Hadoop이란 용어는 Data 저장소인 HBASE와 MapReduce를 혼용하는 의미로 사용되었다. 이미 인터넷에 많은 자료가 있기 때문에 본 문서는 인터넷 상의 좋은 문서를 나열한 문서라고 생각하라.

## Database 순위

우선 전세계 DB 순위를 보자. (순위는 매월 갱신되며 installation 수치에 기반한 순위는 아니고 인기도에 기반한 순위이다.) RDBMS부터 Key-Value store, Search Engine까지 2013년 5월 현재 154개 제품에 대한 순위가 나열되어 있다.

http://db-engines.com/en/ranking

위 사이트를 방문해 보면 알겠지만, 무수히 많은.. 이름도 들어보지 못한 DB가 많이 있다. NoSQL을 도입하고자 한다면 위의 제품중 한개를 선택하게 될 것이다.

NoSQL을 도입하려는 당신! 저렇게 많은 DB 중 어떤 것을 선택할 것인가?

{% include adsense-content.md %}
 
## HBase의 아키텍처

HBase는 Hadoop platform에서 DB 역할을 담당한다. Hadoop을 도입하기 전에 HBase의 특징을 잘 알고 있어야 한다. 이는 비단 Hadoop 만의 문제는 아니다. 우선 HBase 메뉴얼에서 아키텍처 부분을 읽어보자.

http://hbase.apache.org/book/architecture.html#arch.overview

문서를 읽어보면 NoSQL에 대한 간단한 설명, Hadoop, HBase, HDFS 간의 관계에 대해서 설명이 되어 있다.

## Hadoop의 Data Modeling

대부분의 NoSQL은 Data를 Key/Value 형태로 저장한다. (대부분이 그렇다는 것일 뿐 모두 그렇진 않다.) 따라서 데이터를 저장하는 PUT와 데이터를 조회하는 GET 연산만 제공한다. 여기서 더 중요한 것은 Data에 대한 key로 접근할 때는 빠르지만, Value에 대한 Index Scan은 지원하지 않는 NoSQL이 많다는 것이다.

즉, RDBMS에서는 1개 데이터는 여러 컬럼이 모인 1개의 row에 저장이 되고 특정 컬럼에 Index를 걸면 빠르게 조회를 할 수 있다. 하지만, NoSQL에서는 2개의 컬럼 즉, key와 value만 존재하고 INDEX는 key에만 걸렸다고 생각하면 된다.

따라서 HBase에서 Data를 어떻게 저장하고 어떤 방식으로 Data를 조회할 수 있는지 잘 파악해둬야 한다. 아래 HBase의 메뉴얼을 읽어 보면 된다.

http://hbase.apache.org/book/datamodel.html

## Hadoop에서의 Join

RDBMS에 친숙한 개발자들은 RDBMS에서 제공하는 풍부한 연산들을 NoSQL에서도 사용할 수 있을 것이라고 생각할 수도 있다. 그러나 RDBMS와 NoSQL들은 Data Modeling이 완전히 다르기 때문에 제공되는 연산이 RDBMS와 다르다. 다르다는 의미가 RDBMS보다 더 풍부한 연산을 제공하는 것이면 좋은데 RDBMS에서는 앞서 설명한 것처럼 기본적으로 PUT/GET 밖에 지원하지 않는다고 생각하면 된다.

그렇다면 Join은 어떻게 해결해야 할까? 당신이 사용해야 하는 Data에 대해 Join 연산이 필요없다면 제일 좋겠지만....

이에 대한 간단한 답도 HBase 메뉴얼에 나와 있다.

http://hbase.apache.org/book/joins.html

사실 데이터를 반정규화해서 중복 저장하는 수 밖에 없으며 데이터에 대해 어떤 Join을 할지에 따라 중복해야 할 내용이 달라진다. 새로운 Join이 필요한 경우 데이터를 재구성해서 넣어야 한다.

## Data 질의 언어

RDB에서는 SQL이라는 훌륭하고 직관적인 언어를 제공한다. 그런데 NoSQL에서는 질의 언어 자체가 없거나, 있어도 아주 단순한 질의 밖에 수행하지 못한다. 데이터를 조회할 수 있는 프로그램조차도 없어서 데이터 조회를 위해서 일일히 프로그램을 작성해야 하는 NoSQL도 많다.

Hadoop도 마찬가지이다. MapReduce에서 hello world 겪인 하둡에서의 word count를 보면, (문서내 단어의 노출 횟수 합산)

```java
  public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output,
     Reporter reporter) throws IOException {
   String line = value.toString();
   StringTokenizer tokenizer = new StringTokenizer(line);
   while (tokenizer.hasMoreTokens()) {
    word.set(tokenizer.nextToken());
    output.collect(word, one);
   }
  }
 }
 
 public static class Reduce extends MapReduceBase
     implements Reducer<Text, IntWritable, Text, IntWritable> {
  public void reduce(Text key, Iterator<IntWritable> values,
     OutputCollector<Text, IntWritable> output,
     Reporter reporter) throws IOException {
   int sum = 0;
   while (values.hasNext()) {
    sum += values.next().get();
   }
   output.collect(key, new IntWritable(sum));
  }
 }
```

단순한 word count에 대해서도 위처럼 날 코딩을 해야 하는데 좀 더 복잡한 연산을 하는 경우 개발자가 신경써야 할 일은 많아진다. 특히 MapReduce의 기본 개념에는 JOIN 연산이 없다. 즉, Reduce의 결과를 다른 Reduce의 결과와 연산하거나 다른 Map의 입력으로 사용하려면 개발자가 직접 코딩해야 한다.

MapReduce 자체는 개념이 간단하고 확장성이 우수하지만, 이렇게 개발하기가 귀찮기 때문에 전세계의 우수한 개발자들이 하둡에서 MapReduce를 쉽게 할 수 있는 기능을 개발하고 있다.

다행이도 Java도 날코딩하는 수준보다는 약간 편하게, HDFS에 저장된 파일을 shell script을 이용하여 MapReduce 하는 방법도 있긴 하지만 SQL을 사용하는 것보단 불편할 수 밖에 없다.

## 하둡을 DB처럼 다루는 ‘타조’…국내 대학생 개발 눈길

http://news.naver.com/main/read.nhn?mode=LSD&mid=sec&sid1=105&oid=138&aid=0002004195

위의 뉴스를 잘 읽어보자. 

> 타조 프로젝트 어떻게 시작하게 됐나?

> 손지훈  : 저희가 원래 하둡에 관심이 많아서 2008년부터 관심있게 보고 논문도 쓰고 했다. 그런데 하둡을 이용하려니 일일이 코딩을 해야 했다. 하둡에 직접 SQL을 던지면 좋겠다는 생각을 하다가 직접 만들어보자고 결정했다.

> 최현식  : 저희가 원래 하이브를 이용했는데 아쉬운 면이 많았다. 더 좋은 방법이 있을텐데 생각하다가 여기까지 오게됐다.

본인이 근무하는 회사에서도 내부 사용용도로 이미 2008년에 위와 비슷한 것을 만들긴 했는데, 어쨌든 요즘은 Hadoop 사용하기가 옛날보단 편해졌다. Hive에서 사용하는 HiveQL도 이런 노력의 산물일 것이다.

## BigData의 art는 Data Modeling

RDBMS에서는 테이블에서 빠르게 검색하고자 할 때는 원하는 컬럼에 INDEX를 생성하면 된다.
그런데 NoSQL에서는? HBASE 같은 ColumDB는 key에 대한 접근은 빠르지만 Value에 대한 검색을 할 수 없는 경우가 많다. 따라서 Value의 일부 필드에 대해 빠른 접근을 하려는 경우, Value를 key로 하고 원본 Data의 key를 Value로 갖는 새로운 레코드를 저장해야 한다.

본 문서 통틀어서 이 부분이 제일 중요하다. 내가 도입하려는 NoSQL에서의 Data Modeling을 이해하고 어떤 Operation을 제공하는지 이해해야 한다. 내 업무에서 필요한 Data에 대한 Operation이 NoSQL에서 지원하지 않는 경우 다른 NoSQL을 알아봐야 하지만, 대부분 내가 원하는 모든 기능을 제공하는 NoSQL이 없다.

따라서 내가 원하는 업무를 해결하기 위한 Data Modeling이 중요하게 되는 것이다. 이 부분 역시 이미 좋은 문서가 있기 때문에 링크를 걸어 둔다.

http://blog.naver.com/joycestudy?Redirect=Log&logNo=100112523223

## HIVE

본인은 2008년부터 2010년까지 하둡을 많이 썼었는데, 당시 HIVE라는 것이 공개되어 있었는지 모르지만, 이 글을 준비하면서 HIVE에 대해 간단히 찾아 봤는데 좋아 보인다.

- http://www.slideshare.net/cwsteinbach/hive-quick-start-tutorial
- http://db-engines.com/en/system/Hive

Hadoop 위에서 실행되는 Relational Model이라고 보면 될 듯 한데, 실제 써보진 않았지만 Hadoop을 이용하여 쌩으로 개발하는 것 보다는 많이 편할 듯 하다. 최근에 오렐리의 HIVE 책을 KT cloudware 직원들이 번역하여 출간되었다. 

http://www.yes24.com/24/goods/8655208?scode=032&OzSrank=1

## OLTP? OLAP?

Hadoop의 장점은 거의 무한히 확장 가능한 확장성이지만, 단점은 데이터 모델링을 이해해야 하고, 직접 개발해야 할 내용이 많다는 점이다. 또하나의 단점으로 응답 속도가 느리다는 단점이 있다. 3년전에 Hadoop을 써 본게 마지막이라 지금은 틀린 내용일 수 있으나 MapReduce 작업을 클러스터의 수백대 노드에 분산 시키고 결과를 취합하는데 기본 비용이 크다. 따라서 OLTP보다는 OLAP 업무에 적합하고, 실시간성 보다는 batch성 업무 도메인에 적합하다.

RDBMS에 대용량을 데이터를 넣고 SELECT문 실행하여 분석하는 데 몇 시간 걸리는 작업을, Hadoop을 을 이용하여 몇 십분 내로 끝낼 수 있다는 정도로 접근하면 편할 것이다.

## CAP 이론

Hadoop 이외에도 수많은 NoSQL이 있고 그 특성은  Hadoop과 다르다. NoSQL 분야에서 유명 CAP 이론을 보면서 어떤 NoSQL 종류가 있고 어떤 업무 도메인에 적합한지 살펴보는 것도 좋다.

http://blog.nahurst.com/visual-guide-to-nosql-systems

## 내맘대로 NoSQL 분류

내 맘대로 NoSQL을 분류해 보자면, 다음과 같다.
- Key-Value store
- Hadoop
- GraphDB
- Document DB
- 검색엔진

NoSQL마다 Data Modeling도 다르고, Data를 검색하는 방법도 다른다. 우선 Hadoop에 대해서 간단히 문서 정리를 해 봤는데 시간이 허락한다면 다른 NoSQL에 대해서도 정리해 보겠다.
