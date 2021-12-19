---
layout: post
title: "같은 테이블에서 비슷한 속성을 가진 레코드 찾기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20169824/finding-entries-with-similar-attributes-in-same-table-in-mysql/20170190

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/4dca1/1

## 질문

다음과 같은 데이터는 각각의 person이 어떤 취미(hobby)를 어느 정도로 좋아하는지(enjoyment)를 나타낸다.

    mysql> SELECT * FROM tbl;
    +------+----------+--------+-----------+
    | id   | hobby    | person | enjoyment |
    +------+----------+--------+-----------+
    |    1 | soccer   | john   |        10 |
    |    2 | soccer   | jake   |         5 |
    |    3 | baseball | john   |         3 |
    |    4 | baseball | nate   |         5 |
    |    5 | baseball | jordan |         2 |
    |    6 | tennis   | john   |         3 |
    |    7 | tennis   | nate   |         7 |
    |    8 | chess    | john   |        10 |
    |    9 | chess    | nate   |         3 |
    +------+----------+--------+-----------+
    9 rows in set (0.00 sec)

예를 들어 john은 soccer를 10점 만큼 좋아한다는 것을 나타낸다. 특정 인물이 입력되었을 때 그 사람과 가장 유사한 취미를 가진 사람을 찾고 싶다. 어떻게 하면 될까? 예를 들어 john은 soccer,baseball, tennis, chess 총 4개의 취미를 가지고 있다. 이 경우 john의 취미 중 3개를 nate가 좋아하므로 nate가 선택되면 된다.

{% include adsense-content.md %}

## 답변

SNS가 많이 퍼지면서 Stackoverflow에도 친구 목록 얻기 등 SNS에 관련된 질문이 많이 올라오고 있다. 위의 질문도 넓은 의미에서는 SNS에 관련되어 보인다. 질문자가 원하는 것은 다음과 같이 SELF JOIN을 통해서 얻을 수 있다.

    SELECT t_other.person, COUNT(*) AS cnt
    FROM tbl AS t_john, tbl AS t_other
    WHERE t_john.person = 'john'
      AND t_other.hobby = t_john.hobby
      AND t_other.person != 'john'
    GROUP BY t_other.person
    ORDER BY cnt DESC;
     
    +--------+-----+
    | person | cnt |
    +--------+-----+
    | nate   |   3 |
    | jake   |   1 |
    | jordan |   1 |
    +--------+-----+
    3 rows in set (0.00 sec)

{% include mysql-reco.md %}
