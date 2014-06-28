---
layout: post
title: "MySQL 문자열 검색 후 검색 문자열 위치에 따른 정렬"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20328657/like-mysql-query-order-by-search-word/20328956

## SQLFiddle URL

http://sqlfiddle.com/#!2/84faa/1

## 질문

다음과 같은 테이블과 데이터가 존재한다.

    mysql> SELECT * FROM tbl;
    +---------------------+--------+
    | loc_name            | loc_id |
    +---------------------+--------+
    | Dubai Marina M      |     11 |
    | MariM M Residance M |     12 |
    | Hoteli Marine       |     13 |
    +---------------------+--------+
    3 rows in set (0.00 sec)

loc_name 내에 'M'을 포함한 모든 레코드를 출력하되, 정렬 순서는 loc_name에서 M이 앞에 위치한 것을 먼저 출력하고 싶다.

{% include adsense-content.md %}

## 답변

다음 SQL문을 사용하면 된다.

    SELECT loc_name, POSITION('M' IN loc_name) AS pos
    FROM tbl
    WHERE loc_name LIKE '%M%'
    ORDER BY POSITION('M' IN loc_name) ASC;
     
    +---------------------+------+
    | loc_name            | pos  |
    +---------------------+------+
    | MariM M Residance M |    1 |
    | Dubai Marina M      |    7 |
    | Hoteli Marine       |    8 |
    +---------------------+------+
    3 rows in set (0.00 sec)

`POSITION(x IN str)` 함수는 "x가 str 내에 존재하는 첫 번째 위치를 출력"하는 함수이다. str 내에 x가 존재하지 않으면 0을 반환한다.

### 문자열 포함 횟수로 정렬하기

질문자의 질문에 포함되지는 않았지만, 검색 시 문자열을 포함한 횟수로 정렬할 수도 있다.

    SELECT loc_name,
      (LENGTH(loc_name) - LENGTH(REPLACE(loc_name, 'M', ''))) / LENGTH('M') AS occurrence
    FROM tbl
    WHERE loc_name LIKE '%M%'
    ORDER BY (LENGTH(loc_name) - LENGTH(REPLACE(loc_name, 'M', ''))) / LENGTH('M') DESC;
     
    +---------------------+------------+
    | loc_name            | occurrence |
    +---------------------+------------+
    | MariM M Residance M |     4.0000 |
    | Dubai Marina M      |     2.0000 |
    | Hoteli Marine       |     1.0000 |
    +---------------------+------------+
    3 rows in set (0.00 sec)

SQL을 풀어서 설명하면 다음과 같다.

- `LENGTH(loc_name)` : 원문 문자열 길이
- `REPLACE(loc_name, 'M', '')` : 원본 문자열에서 'M'을 삭제함
- `LENGTH(REPLACE(..))` : 원몬 문자열에서 'M'을 삭제한 문자열의 길이
- `(LENGTH(loc_name) - LENGTH(REPLACE())` : 삭제된 문자열의 길이
- `(LENGTH(...) - LENGTH(REPLACE(...)) / LENGTH('M')` : 'M'을 포함한 횟수

2개 이상의 문자로 검색할 것을 대비하여 LENGTH('M')로 나누기를 하였다.
