---
layout: post
title: "2개 이상의 잡지를 구독하는 독자의 이름과 독자가 구독하는 잡지 이름 조회하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20257388/getting-specific-results-in-output/20257638

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/2b46d/1

## 질문

다음과 같이, 독자 테이블(people), 잡지 테이블(magazines), 독자의 잡지 구독 정보 테이블(peoplewhosub) 테이블이 존재한다.

    mysql> SELECT * FROM people;
    +-----------+-------------+
    | person_id | person_name |
    +-----------+-------------+
    |         1 | JOEY        |
    |         2 | TOM         |
    |         3 | RICK        |
    +-----------+-------------+
    3 rows in set (0.00 sec)
     
    mysql> SELECT * FROM magazines;
    +-------------+---------------+
    | magazine_id | magazine_name |
    +-------------+---------------+
    | SPT         | Sports        |
    | TEC         | Tech          |
    +-------------+---------------+
    2 rows in set (0.00 sec)
     
    mysql> SELECT * FROM peoplewhosub;
    +-----------+-------------+
    | person_id | magazine_id |
    +-----------+-------------+
    |         1 | SPT         |
    |         1 | TEC         |
    |         2 | SPT         |
    +-----------+-------------+
    3 rows in set (0.00 sec)

예를 들어 JOEY는 Sports와 Tech라는 잡지를 구독 중이며, RICK은 Sports 잡지를 구독 중이다. 2개 이상의 잡지를 구독 중인 사람의 이름과 잡지 이름을 조회하고 싶다. 어떻게 하면 되는가?

{% include adsense-content.md %}

## 답변

우선 정답을 보기 전에 DB 정규화에 대한 이야기를 해 보자. 앞의 예는 n:m 관계를 위한 일반적인 DB 설계이다. 즉, 1명의 사람은 1개 이상의 잡지를 구독할 수 있고, 1개의 잡지는 1명 이상의 사람에게구독될 수 있다. 이런 관계를 n:m 관계라고 하며 사람 테이블과 잡지 테이블 연결을 위한 테이블, 앞의 예에서는 peoplewhosub 테이블이 필요하다. 값 조회를 위해서 3개 테이블을 JOIN 해야 한다. 간혹JOIN에 대한 불신 때문에 반정규화를 하는 경우가 있지만 성능이 아주 느리지 않는 한 앞의 예와 같은 DB 설계를 하는 것이 좋다.

이제 다시 원래 질문으로 돌아오자. 우선 2개 이상의 잡지를 구독 중인 사람의 id는 다음의 질의를 이용하여 얻을 수 있다.

    SELECT person_id
    FROM peoplewhosub
    GROUP BY person_id
    HAVING COUNT(*) > 1;
     
    +-----------+
    | person_id |
    +-----------+
    |         1 |
    +-----------+
    1 row in set (0.00 sec)

질문자가 원하는 것은 사람의 이름과 잡지의 이름인데 다음의 SQL문을 사용하면 된다.

    SELECT p.person_name, m.magazine_id, m.magazine_name
    FROM people p INNER JOIN peoplewhosub ms ON p.person_id = ms.person_id
      INNER JOIN magazines m ON ms.magazine_id = m.magazine_id
    WHERE p.person_id = ms.person_id and ms.magazine_id = m.magazine_id
      AND p.person_id IN (
        SELECT person_id
        FROM peoplewhosub
        GROUP BY person_id
        HAVING COUNT(*) > 1
      );
     
    +-------------+-------------+---------------+
    | person_name | magazine_id | magazine_name |
    +-------------+-------------+---------------+
    | JOEY        | SPT         | Sports        |
    | JOEY        | TEC         | Tech          |
    +-------------+-------------+---------------+
    2 rows in set (0.00 sec)

앞의 SQL이 최상의 정답이라는 이야기는 아니며 더 좋은 방법이 있을 수도 있으니 참고하기 바란다.
