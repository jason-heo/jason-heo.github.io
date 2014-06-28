---
layout: post
title: "조회할 값을 2개 컬럼에서 비교하기"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20209350/mysql-select-row-by-comparing-two-columns/21322502

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/9a9fa/1

## 질문

다음과 같은 messages 테이블이 있다.

    mysql> SELECT * FROM messages;
    +---------------+-----------------+----------+---------------------+
    | sender_userid | receiver_userid | msg_body | date_sent           |
    +---------------+-----------------+----------+---------------------+
    | user1         | user2           | msg1     | 2014-12-01 00:00:00 |
    | user2         | user1           | msg2     | 2014-12-02 00:00:00 |
    | user1         | user2           | msg3     | 2014-12-03 00:00:00 |
    | user2         | user1           | msg4     | 2014-12-04 00:00:00 |
    +---------------+-----------------+----------+---------------------+
    4 rows in set (0.00 sec)

sender_userid가 receiver_userid에게 보낸 메시지가 저장된 테이블이다. user1에 대해서 송신, 수신과 상관없이 가장 최근의 메시지 1개를 조회하려고 한다. 어떻게 하는 것이 좋은가? 내가 시도한 SQL은 다음과 같다.

    SELECT sender_userid, receiver_userid, msg_body, date_sent
    FROM (
        SELECT sender_userid, receiver_userid, msg_body, date_sent
        FROM messages
        WHERE (receiver_userid = 'user1' OR sender_userid = 'user1')
        ORDER BY date_sent DESC
    ) AS MSG
    GROUP BY receiver_userid;
     
    +---------------+-----------------+----------+---------------------+
    | sender_userid | receiver_userid | msg_body | date_sent           |
    +---------------+-----------------+----------+---------------------+
    | user2         | user1           | msg4     | 2014-12-04 00:00:00 |
    | user1         | user2           | msg3     | 2014-12-03 00:00:00 |
    +---------------+-----------------+----------+---------------------+
    2 rows in set (0.00 sec)

{% include adsense-content.md %}

## 답변

질문자가 질문한 내용, 시도한 SQL, 질문자가 채택한 답변이 서로 달라서 이해하기 어려웠던 질문이다. 아래의 SQL을 이용하면 질문자가 의도한 결과를 쉽게 얻을 수 있다.

    SELECT 'user1' AS my_id, sender_userid, receiver_userid, date_sent
    FROM messages
    WHERE (receiver_userid='user1' OR sender_userid='user1')
    ORDER BY date_sent DESC
    LIMIT 1;
     
    +-------+---------------+-----------------+---------------------+
    | my_id | sender_userid | receiver_userid | date_sent           |
    +-------+---------------+-----------------+---------------------+
    | user1 | user2         | user1           | 2014-12-04 00:00:00 |
    +-------+---------------+-----------------+---------------------+
    1 row in set (0.00 sec)

### 질의 개선 (MySQL 5.0 이하인 경우)

서로 다른 컬럼에 대해 OR 연산을 수행하는 경우, 각각의 컬럼에 INDEX되었더라도 INDEX를 활용할 수가 없기 때문에 위의 질의는 속도가 느리다. 성능 개선을 위해서는 귀찮더라도 다음과 같이 UNION을 사용하여 OR를 제거해 주는 것이 좋다.

    SELECT *
    FROM (
        (
            SELECT 'user1' AS my_id, sender_userid, receiver_userid, date_sent
            FROM messages
            WHERE receiver_userid='user1'
            ORDER BY date_sent DESC
            LIMIT 1
        )
     
        UNION ALL
     
        (
            SELECT 'user1' AS my_id, sender_userid, receiver_userid, date_sent
            FROM messages
            WHERE sender_userid='user1'
            ORDER BY date_sent DESC
            LIMIT 1
        )
    ) x
    ORDER BY date_sent DESC
    LIMIT 1;
     
    +-------+---------------+-----------------+---------------------+
    | my_id | sender_userid | receiver_userid | date_sent           |
    +-------+---------------+-----------------+---------------------+
    | user1 | user2         | user1           | 2014-12-04 00:00:00 |
    +-------+---------------+-----------------+---------------------+
    1 row in set (0.00 sec)

