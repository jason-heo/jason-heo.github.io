---
layout: post
title: "중복 설정된 친구 관계 찾기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20266184/delete-query-for-two-way-friendship-mysql/20266323

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/b1024c/1

## 질문

다음과 같은 테이블과 데이터가 존재한다.

    mysql> SELECT * FROM friendship;
    +-----+--------------+------------+
    | num | from_user_id | to_user_id |
    +-----+--------------+------------+
    |   1 | user2        | user1      |
    |   2 | user15       | user1      |
    |   3 | user14       | user1      |
    |   4 | user1        | user14     |
    |   5 | user5        | user1      |
    |   6 | user1        | user5      |
    |   7 | user2        | user5      |
    +-----+--------------+------------+
    7 rows in set (0.00 sec)

`from_user_id`와 `to_user_id`가 서로 친구 관계인 것을 의미하며, (from_user_id, to_user_id) = (user1, user2) 혹은 (user2, user1) 레코드는 user1과 user2가 서로 친구인 것을 의미한다. from과 to는 누가 먼저 친구 신청을 했느냐를 의미할 뿐이다. 그런데 실수로 친구관계가 중복으로 입력된 것들이 있다. 예를 들어 num이 3인 레코드와 4인 레코드는 user1과 user14가 친구인 것을 나타내고 있으므로 중복된 친구 관계이다.

이렇게 중복된 친구 관계를 찾고 싶다.

{% include adsense-content.md %}

## 답변

SELF JOIN을 이용하여 중복되는 친구 관계를 구할 수 있다.

    SELECT t1.num, t1.from_user_id, t1.to_user_id
    FROM friendship AS t1 JOIN friendship AS t2
      WHERE t1.from_user_id = t2.to_user_id
        AND t2.from_user_id = t1.to_user_id;
     
    +-----+--------------+------------+
    | num | from_user_id | to_user_id |
    +-----+--------------+------------+
    |   4 | user1        | user14     |
    |   3 | user14       | user1      |
    |   6 | user1        | user5      |
    |   5 | user5        | user1      |
    +-----+--------------+------------+
    4 rows in set (0.00 sec)

t1의 from_user_id가 t2의 to_user_id이면서, t1의 to_user_id가 t2의 from_user_id인 레코드를 찾는 SELF JOIN이다.

{% include mysql-reco.md %}
