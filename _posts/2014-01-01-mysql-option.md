---
layout: post
title: "팁 3 - mysql 옵션"
date: 2014-03-05 
categories: mysql
---

## 출력 결과에서 '|'를 제거하기 (-s)

일반적으로 mysql에서 SQL을 실행하면 다음과 같이 테이블 형식으로 결과가 출력된다.

    mysql> SELECT * FROM p;
    +----+---------------+
    | id | products_name |
    +----+---------------+
    |  1 | a             |
    |  2 | b             |
    |  4 | d             |
    |  5 | ed            |
    |  7 | gs            |
    |  8 | d             |
    |  9 | f             |
    | 11 | f             |
    +----+---------------+
    8 rows in set (0.00 sec)
 
사람이 눈으로 보기에는 쉽지만 SQL의 출력 결과를 프로그램으로 가공해야 할 때는 '|'로 포맷팅된 출력물은 귀찮기만 할 것이다. 이때 -s (silent의 약자임) 옵션을 이용하면 '|'를 없앨 수 있다.

    $ mysql db_name -s
     
    mysql> SELECT * FROM p;
    id      products_name
    1       a
    2       b
    4       d
    5       ed
    7       gs
    8       d
    9       f
    11      f

## 컬럼명 없애기 (-N)

-N 옵션을 이용하면 SQL의 결과에서 컬럼 명도 제거할 수 있다.

    $ mysql  db_name -N
    mysql> SELECT * FROM p;
    +----+------+
    |  1 |    a |
    |  2 |    b |
    |  4 |    d |
    |  5 |   ed |
    |  7 |   gs |
    |  8 |    d |
    |  9 |    f |
    | 11 |    f |
    +----+------+
    8 rows in set (0.00 sec)

## shell에서 SQL 입력받기 (-e)

-e 옵션을 이용하여 실행할 SQL을 입력받을 수 있다.

    $ mysql db_name -e "SELECT * FROM p"
    +----+---------------+
    | id | products_name |
    +----+---------------+
    |  1 | a             |
    |  2 | b             |
    |  4 | d             |
    |  5 | ed            |
    |  7 | gs            |
    |  8 | d             |
    |  9 | f             |
    | 11 | f             |
    +----+---------------+

## 테이블 형식 출력을 강제하기

다음과 같이 "echo SQL | mysql" 처럼 유닉스의 파이프를 이용하여 SQL을 실행할 수도 있다.

    $ echo "SELECT * FROM p" | mysql db_name
    id      products_name
    1       a
    2       b
    4       d
    5       ed
    7       gs
    8       d
    9       f
    11      f

그런데 결과에서 '|'가 제거되었다. mysql 옵션 중 -t를 이용하면 우리가 평소 봤던 테이블 형식으로 출력된다.

    $ echo "SELECT * FROM p" | mysql db_name  -t
    +----+---------------+
    | id | products_name |
    +----+---------------+
    |  1 | a             |
    |  2 | b             |
    |  4 | d             |
    |  5 | ed            |
    |  7 | gs            |
    |  8 | d             |
    |  9 | f             |
    | 11 | f             |
    +----+---------------+

{% include mysql-reco.md %}
