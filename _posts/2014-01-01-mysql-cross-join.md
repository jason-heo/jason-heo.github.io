---
layout: post
title: "MySQL Cross JOIN"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20214848/cross-product-of-columns/20214908

## SQLFiddle URL

http://sqlfiddle.com/#!2/9264d7/2/0

## 질문

다음과 같은 테이블이 존재한다.

    SELECT * FROM test;
     
    +------+------+-------+--------+
    | name | age  | spent | gender |
    +------+------+-------+--------+
    | De   |   26 |    10 | M      |
    | FU   |   27 |    29 | F      |
    +------+------+-------+--------+
    2 rows in set (0.00 sec)

이때 아래와 같은 결과를 얻고 싶은데 어떻게 하면 될까?

    +------+------+-------+--------+
    | name | age  | spent | gender |
    +------+------+-------+--------+
    | De   |   26 |    10 | M      |
    | FU   |   26 |    10 | M      |
    | De   |   27 |    10 | M      |
    | FU   |   27 |    10 | M      |
    | De   |   26 |    29 | M      |
    ...

{% include adsense-content.md %}

## 답변

질문자의 의도가 명확하지는 않다. 유추하기로는 각 컬럼의 UNIQUE한 값들의 조합을 알고 싶은 것 같았다. 다음과 같은 SQL을 제안했고 질문자가 원하던 결과를 출력한다는 피드백을 받았다.

    SELECT *
    FROM
    (
        SELECT DISTINCT name
        FROM test
    ) x CROSS JOIN
    (
        SELECT DISTINCT age
        FROM test
    ) y CROSS JOIN
    (
        SELECT DISTINCT spent
        FROM test
    ) z CROSS JOIN
    (
        SELECT DISTINCT gender
        FROM test
    ) a;
     
    +------+------+-------+--------+
    | name | age  | spent | gender |
    +------+------+-------+--------+
    | De   |   26 |    10 | M      |
    | FU   |   26 |    10 | M      |
    | De   |   27 |    10 | M      |
    | FU   |   27 |    10 | M      |
    | De   |   26 |    29 | M      |
    | FU   |   26 |    29 | M      |
    | De   |   27 |    29 | M      |
    | FU   |   27 |    29 | M      |
    | De   |   26 |    10 | F      |
    | FU   |   26 |    10 | F      |
    | De   |   27 |    10 | F      |
    | FU   |   27 |    10 | F      |
    | De   |   26 |    29 | F      |
    | FU   |   26 |    29 | F      |
    | De   |   27 |    29 | F      |
    | FU   |   27 |    29 | F      |
    +------+------+-------+--------+
    16 rows in set (0.00 sec)
 
x, y, z, a 같은 inlinew view 1개마다 각 컬럼의 UNIQUE한 값을 추출한 뒤에 모든 테이블을 CROSS JOIN 하면 질문자가 원한 결과를 얻을 수 있다.

{% include mysql-reco.md %}
