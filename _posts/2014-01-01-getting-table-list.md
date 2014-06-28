---
layout: post
title: "MySQL 테이블 목록 구하기 (MySQL information schema)"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20183322/mysql-show-tables-query/20183336

## 질문

다음과 같은 질의를 이용하여 'table1'로 시작하는 모든 테이블 목록을 구하고 있다.

    SHOW TABLES LIKE 'table1%'

그런데, 'table1_blank' 처럼 'table1_'로 시작하는 테이블은 제외를 하고 싶은데 방법은 없는가?

{% include adsense-content.md %}

## 답변

MySQL에서 "SHOW TABLES"를 이용하여 테이블 목록을 쉽게 구할 수 있다. 그런데 너무 쉽게 테이블 목록을 구할 수 있다 보니 information schema의 존재를 모르는 사용자가 많은 듯 하다. 우선 질문자가 원하는 것은 SHOW TABLES를 이용하더라도 다음과 같이 구할 수 있으며 다른 답변자가 답변을 했다.

```sql
SHOW TABLES
FROM `dbname`
WHERE `Tables_In_dbname` LIKE 'table1%'
  AND `Tables_In_dbname` NOT LIKE 'table1\_%';
```

질문자가 채택한 답변은 위의 "SHOW TABLES"이지만, 필자는 다음과 같이 information_schema를 이용하는 것을 제안했었다.

```sql
SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'your database name'
   AND TABLE_NAME LIKE 'table1%'
   AND TABLE_NAME NOT LIKE 'table1\_%';
```

약간은 어려워 보일 수 있지만, information_schema를 이용하면 MySQL에 존재하는 다양한 정보를 쉽게 얻을 수 있다. information_schema DB에는 많은 테이블이 존재하는데 그 중 TABLES 테이블은 MySQL에 존재하는 테이블에 대한 정보를 얻을 수 있는 테이블이다. TABLES 테이블에는 다음과 같은 컬럼이 존재한다.

    mysql> desc information_schema.TABLES;
    +-----------------+---------------------+------+-----+---------+-------+
    | Field           | Type                | Null | Key | Default | Extra |
    +-----------------+---------------------+------+-----+---------+-------+
    | TABLE_CATALOG   | varchar(512)        | NO   |     |         |       |
    | TABLE_SCHEMA    | varchar(64)         | NO   |     |         |       |
    | TABLE_NAME      | varchar(64)         | NO   |     |         |       |
    | TABLE_TYPE      | varchar(64)         | NO   |     |         |       |
    | ENGINE          | varchar(64)         | YES  |     | NULL    |       |
    | VERSION         | bigint(21) unsigned | YES  |     | NULL    |       |
    | ROW_FORMAT      | varchar(10)         | YES  |     | NULL    |       |
    | TABLE_ROWS      | bigint(21) unsigned | YES  |     | NULL    |       |
    | AVG_ROW_LENGTH  | bigint(21) unsigned | YES  |     | NULL    |       |
    | DATA_LENGTH     | bigint(21) unsigned | YES  |     | NULL    |       |
    | MAX_DATA_LENGTH | bigint(21) unsigned | YES  |     | NULL    |       |
    | INDEX_LENGTH    | bigint(21) unsigned | YES  |     | NULL    |       |
    | DATA_FREE       | bigint(21) unsigned | YES  |     | NULL    |       |
    | AUTO_INCREMENT  | bigint(21) unsigned | YES  |     | NULL    |       |
    | CREATE_TIME     | datetime            | YES  |     | NULL    |       |
    | UPDATE_TIME     | datetime            | YES  |     | NULL    |       |
    | CHECK_TIME      | datetime            | YES  |     | NULL    |       |
    | TABLE_COLLATION | varchar(32)         | YES  |     | NULL    |       |
    | CHECKSUM        | bigint(21) unsigned | YES  |     | NULL    |       |
    | CREATE_OPTIONS  | varchar(255)        | YES  |     | NULL    |       |
    | TABLE_COMMENT   | varchar(2048)       | NO   |     |         |       |
    +-----------------+---------------------+------+-----+---------+-------+

컬럼 이름을 보면 대략적인 의미는 파악할 수 있을 것이라 생각되며, DB명에 대한 컬럼이 없어 보이는데, TABLE_SCHEMA가 DB명을 저장하는 컬럼이다.
