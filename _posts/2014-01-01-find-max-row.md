---
layout: post
title: "MySQL GROUP BY를 이용하여 최대 값을 가진 레코드 가져오기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20289068/mysql-unique-email-with-user-that-has-most-currency/20289211

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/2f2c00/1

## 질문

다음과 같은 데이터가 존재한다.

mysql> SELECT * FROM tbl;
+-----------------+---------+---------+
| email           | uname   | credits |
+-----------------+---------+---------+
| 824@hotmail.com | barbra  |       6 |
| 123@gmail.com   | smith   |      25 |
| 123@gmail.com   | smithy  |      30 |
| abc@hotmail.com | another |      25 |
| def@comcast.net | rob     |       8 |
| abc@hotmail.com | ben     |      62 |
| ijk@yahoo.com   | jeb     |       2 |
| ijk@yahoo.com   | joe     |       5 |
+-----------------+---------+---------+
8 rows in set (0.00 sec)
보는 바와 같이 email 컬럼에 중복된 email이 저장되어 있는데 중복된 email의 uname은 서로 다르다. 모든 회원들에게 뉴스레터를 발송하려고 하는데 중복된 email 주소에는 1회만 email을 전송하려고 한다. email별로 credits이 가장 큰 값을 가진 uname과 email을 조회하고 싶다.

예를 들어 'smith'와 'smithy'가 123@gmail.com을 소유했는데, smithy의 credits가 smith보다 더 크므로 smithy를 선택하면 된다.

{% include adsense-content.md %}

## 답변

email별로 최대 credits는 다음과 같이 쉽게 구할 수 있다.

    SELECT email, MAX(credits) as credits
    FROM tbl
    GROUP BY  email;
    +-----------------+---------+
    | email           | credits |
    +-----------------+---------+
    | 123@gmail.com   |      30 |
    | 824@hotmail.com |       6 |
    | abc@hotmail.com |      62 |
    | def@comcast.net |       8 |
    | ijk@yahoo.com   |       5 |
    +-----------------+---------+
    5 rows in set (0.00 sec)

최대 credits를 갖는 uname을 구하기 위해서는 앞 SQL의 결과를 다시 한번 JOIN해야 한다.

    SELECT tbl.email, tbl.uname, tbl.credits
    FROM(
        SELECT email, MAX(credits) as credits
        FROM tbl
        GROUP BY  email
    ) x INNER JOIN tbl ON x.email = tbl.email AND tbl.credits = x.credits;
     
    +-----------------+--------+---------+
    | email           | uname  | credits |
    +-----------------+--------+---------+
    | 824@hotmail.com | barbra |       6 |
    | 123@gmail.com   | smithy |      30 |
    | def@comcast.net | rob    |       8 |
    | abc@hotmail.com | ben    |      62 |
    | ijk@yahoo.com   | joe    |       5 |
    +-----------------+--------+---------+
    5 rows in set (0.00 sec)

단, 동일 그룹 내에서 서로 다른 uname은 서로 다른 credits를 가진다는 가정을 만족해야 한다. 동일 그룹에서 MAX(credits)의 값이 중복된 경우는 중복된 개수만큼 출력이 된다.
