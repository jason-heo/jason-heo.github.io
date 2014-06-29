---
layout: post
title: "MySQL INDEX 종류"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/19927620/types-of-indexing-in-mysql/19928261

## 질문

MySQL에서 지원되는 INDEX 종류를 알고 싶다. 더불어 Clustered INDEX와 Non-Clustered INDEX는 무엇인가?

{% include adsense-content.md %}

## 답변

MySQL Manual을 방문해 보면, 테이블 생성 시 지정할 수 있는 INDEX 타입을 다음과 같이 설명하고 있다.

    index_type:
    USING {BTREE | HASH}

문법만 보면 BTREE와 HASH 2가지를 지원하는 듯 하지만, 이는 문법 호환성을 위해 문법적으로만 허용할 뿐 MySQL은 MyISAM과 InnoDB에서는 BTREE만 지원한다. 즉, HASH INDEX를 지정하더라도테이블 생성은 성공하지만, 내부적으로는 BTREE로 생성된다. 아래 예를 보자.

```sql
mysql> CREATE TABLE test (col1 INT, INDEX(col1) USING HASH) ENGINE=InnoDB;
Query OK, 0 rows affected (0.00 sec)
 
mysql> SHOW CREATE TABLE test\G
*************************** 1. row ***************************
       Table: test
Create Table: CREATE TABLE `test` (
  `col1` int(11) DEFAULT NULL,
  KEY `col1` (`col1`) USING HASH
) ENGINE=InnoDB DEFAULT CHARSET=utf8
1 row in set (0.00 sec)
```

SHOW CREATE TABLE의 결과만 본다면 HASH INDEX로 생성이 된 듯 하지만 InnoDB는 HASH INDEX를 지원하지 않는다. 저장 엔진 별로 지원되는 INDEX 종류는 정해져 있으며 다음과 같다.

|저장 엔진|지원되는 INDEX|
|---|---|
|MySQL|BTREE|
|InnoDB|BTREE|
|Memory/HEAP|HASH, BTREE|
|NDB|HASH, BTREE|

참고로 HASH INDEX는 "column = value"처럼 equal 검색을 빠르게 수행할 수 있으나, 대소 비교 및 정렬에서는 사용할 수가 없다. 우리가 일반적으로 사용하는 BTREE는 equal 연산은 HASH INDEX보단 약간 느리지만 대소 비교 및 정렬에서도 인덱스를 활용할 수 있다.

Clustered INDEX는 BTREE, HASH같은 자료 구조에 대한 이야기는 아니며, INDEX로 찾은 레코드의 데이터를 어떻게 찾아갈지에 대한 주제이다. 궁금한 독자는 http://xster.tistory.com/135 를 방문해보기 바란다. 참고로 InnoDB에서 PRIMARY KEY는 Clustered INDEX이다.
