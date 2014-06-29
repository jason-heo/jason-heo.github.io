---
layout: post
title: "30개의 값을 가진 user_id 조회하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20437139/how-do-i-pull-large-number-of-multiple-rows-from-one-table/20437192

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/2648d/1

## 질문

다음과 같은 테이블과 샘플 데이터가 존재한다.

    mysql> SELECT * FROM tbl;
    +---------+----------+
    | user_id | skill_id |
    +---------+----------+
    |       1 |       10 |
    |       1 |       11 |
    |       1 |       12 |
    |       1 |       13 |
    |       2 |       10 |
    |       2 |       12 |
    |       2 |       13 |
    |       3 |       15 |
    |       3 |       16 |
    |       4 |       10 |
    |       5 |       45 |
    |       5 |       46 |
    +---------+----------+
    12 rows in set (0.00 sec)

앞의 예는 샘플 데이터이고 실제로는 더 많은 데이터가 존재하는데, 1개의 user_id는 수십 개의 skill_id를 가질 수 있다. (user_id, skill_id)는 중복되지 않는다. 이런 데이터가 존재할 때, 임의로 주어진 복수 개의 skill_id를 모두 갖는 user_id를 찾고 싶다. skill_id는 최대 수십 개도 주어질 수 있다.

{% include adsense-content.md %}

## 답변

skill_id의 개수가 적다면 SELF JOIN으로 풀어도 될 문제이지만, 질문자의 경우는 skill_id의 개수가 30개도 넘는다고 한다. 따라서 다음과 같이 GROUP BY HAVING으로 푸는 것이 좋겠다. 다음은 문제를 축소하여 10, 12, 13 즉, 3개의 skill_id를 갖는 user_id를 찾는 SQL 문이다.

    SELECT user_id
    FROM tbl
    WHERE skill_id IN (10, 12, 13)
    GROUP BY user_id
    HAVING COUNT(*) = 3;
     
    +---------+
    | user_id |
    +---------+
    |       1 |
    |       2 |
    +---------+
    2 rows in set (0.00 sec)


