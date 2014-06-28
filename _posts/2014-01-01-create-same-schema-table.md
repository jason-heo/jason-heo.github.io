---
layout: post
title: "특정 DB에 존재하는 모든 테이블을 다른 DB에 동일한 구조로 생성하기"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20207294/how-to-copy-a-mysql-database-schema-using-c/20208107

## 질문

TableTemplate라는 DB가 존재한다. 이 DB에는 여러 개의 테이블이 있다. 테이블에 레코드는 존재하지 않는다. 신규 User가 생성되면 해당 User가 소유한 DB에 TableTemplate에 존재하는 모든테이블을 동일한 구조로 자동으로 생성하고자 한다.

    > TableTemplate (DB)
        + FirstTable    (table)
        + SecondTable  (table)
          ...

와 같은 구조일 때, UserA가 생성되면 UserA_DB에 위와 동일한 테이블을 생성하려고 한다.

    > UserA_DB (DB)
        + FirstTable    (table)
        + SecondTable  (table)
          ...

{% include adsense-content.md %}

## 답변

특정 테이블과 동일한 구조로 새로운 테이블을 생성하는 것은 `CREATE TABLE ... LIKE tbl_name`을 사용하면 된다. 다음의 예는 TableTemplate의 FirstTable과 동일한 구조의 테이블을UserA_DB의 FirstTable에 생성하는 예이다.

    mysql> SHOW CREATE TABLE TableTemplate.FirstTable\G
    *************************** 1. row ***************************
           Table: FirstTable
    Create Table: CREATE TABLE `FirstTable` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `person_id` varchar(20) DEFAULT NULL,
      `skill_id` varchar(20) DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8
    1 row in set (0.00 sec)
     
    mysql> CREATE TABLE UserA_DB.FirstTable LIKE TableTemplate.FirstTable;
    Query OK, 0 rows affected (0.00 sec)
     
    mysql> SHOW CREATE TABLE UserA_DB.FirstTable\G
    *************************** 1. row ***************************
           Table: FirstTable
    Create Table: CREATE TABLE `FirstTable` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `person_id` varchar(20) DEFAULT NULL,
      `skill_id` varchar(20) DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8
    1 row in set (0.00 sec)

그렇다면 특정 DB에 있는 모든 테이블을 생성하기 위해서는 어떻게 하는 것이 좋을까? 다양한 방법이 있겠지만, 여기서는 다음과 같이 information_schema를 제안해 본다.

    mysql> SELECT CONCAT("CREATE TABLE UserA_DB.", TABLE_NAME,
        ->   " LIKE TableTemplate.", TABLE_NAME) AS cr_sql
        -> FROM information_schema.TABLES
        -> WHERE TABLE_SCHEMA = 'TableTemplate';
    +------------------------------------------------------------------+
    | cr_sql                                                           |
    +------------------------------------------------------------------+
    | CREATE TABLE UserA_DB.FirstTable LIKE TableTemplate.FirstTable   |
    | CREATE TABLE UserA_DB.SecondTable LIKE TableTemplate.SecondTable |
    | CREATE TABLE UserA_DB.ThirdTable LIKE TableTemplate.ThirdTable   |
    +------------------------------------------------------------------+
    3 rows in set (0.00 sec)

information_schema를 이용하여 TableTemplate에 존재하는 모든 테이블에 대해 "CREATE TABLE ... LIKE"를 생성하는 SQL문이 반환되었다. 독자의 응용프로그램에서 위의 SELECT 문을실행한 뒤에 결과를 반복하면서 실행시켜주면 된다.

참고 : 테이블 생성과 데이터 복사를 동시에

앞의 예는 동일한 구조의 테이블을 생성하기만 하였는데 데이터 복사까지 하려면 "CREATE TABLE ... SELECT"를 이용하면 된다.

    mysql> CREATE TABLE t SELECT * FROM person_skill;
    Query OK, 12 rows affected (0.00 sec)
    Records: 12  Duplicates: 0  Warnings: 0
     
    mysql> SHOW CREATE TABLE t\G
    *************************** 1. row ***************************
           Table: t
    Create Table: CREATE TABLE `t` (
      `id` int(11) NOT NULL DEFAULT '0',
      `person_id` varchar(20) DEFAULT NULL,
      `skill_id` varchar(20) DEFAULT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8
    1 row in set (0.00 sec)

단, 테이블 생성 후 "SHOW CREATE TABLE"에서 보는 것과 같이 INDEX는 자동으로 생성되지 않으므로 사용자가 직접 INDEX를 생성해 주어야 한다. 어차피 2번에 나눠서 해야 하는 것이라면 "CREATE TABLE ... LIKE"로 테이블을 생성한 뒤, "INSERT INTO ... SELECT"를 이용하여 데이터를 복사할 수 있다. 하지만, 대용량인 경우 INDEX 없이 데이터를 INSERT한 후에 INDEX를 생성하는 것이속도가 빠르다.

`CREATE TABLE SELECT`의 장점은 일반적인 SELECT를 사용할 수 있기 때문에 원하는 컬럼만 생성할 수 있고, 원하는 레코드만 복사할 수 있다는 점이다. JOIN을 사용하여 보다 복잡한 데이터도 복사할 수 있다. 다음의 예를 보자.

    mysql> CREATE TABLE new_tbl
        -> SELECT t1.person_id AS pid, t1.skill_id AS sid
        -> FROM person_skill t1 INNER JOIN person_skill t2
        ->   ON t1.person_id = t2.person_id
        -> WHERE t1.skill_id = 'skill A'
        ->   AND t2.skill_id = 'skill B'
        ->   AND t1.id != t2.id;
    Query OK, 4 rows affected (0.00 sec)
    Records: 4  Duplicates: 0  Warnings: 0
     
    mysql>
    mysql> SELECT * FROM new_tbl;
    +---------+---------+
    | pid     | sid     |
    +---------+---------+


