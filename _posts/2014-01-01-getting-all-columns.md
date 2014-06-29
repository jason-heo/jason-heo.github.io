---
layout: post
title: "특정 DB 내에 존재하는 모든 컬럼 목록을 한번에 조회하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20217452/mysql-fields-definition/20217618

## 질문

특정 DB 내에 존재하는 모든 테이블의 모든 컬럼을 1번에 조회하고자 한다. 어떻게 하는 것이 좋은가?

{% include adsense-content.md %}

## 답변

다음과 같이 information_schema를 이용하여 쉽게 구할 수 있다.

    SELECT  TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION,
      DATA_TYPE, IS_NULLABLE
    FROM    INFORMATION_SCHEMA.COLUMNS
    WHERE   TABLE_SCHEMA = 'db_name'
    ORDER BY TABLE_NAME, ORDINAL_POSITION;
     
    +------------+-------------+------------------+-----------+-------------+
    | TABLE_NAME | COLUMN_NAME | ORDINAL_POSITION | DATA_TYPE | IS_NULLABLE |
    +------------+-------------+------------------+-----------+-------------+
    | a          | a           |                1 | int       | YES         |
    | b          | a           |                1 | int       | YES         |
    | test       | name        |                1 | varchar   | YES         |
    | test       | age         |                2 | int       | YES         |
    | test       | spent       |                3 | int       | YES         |
    | test       | gender      |                4 | char      | YES         |
    +------------+-------------+------------------+-----------+-------------+

`ORDINAL_POSITION`은 테이블 내 컬럼이 정의된 순서를 의미한다.
