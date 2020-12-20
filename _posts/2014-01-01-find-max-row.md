---
layout: post
title: "MySQL GROUP BY를 이용하여 최대 값을 가진 레코드 가져오기"
date: 2014-03-05 
categories: mysql
---

Test에 사용된 MySQL 버전: 8.0.22

그런데 아주 일반적인 기능이라서 MySQL 5은 물론 그 이전 버전에서도 작동한다.

## Data set

email이 중복되었지만, name은 서로 다른 레코드가 저장되어 있다.

```sql
CREATE TABLE user
(
  email VARCHAR(50),
  name VARCHAR(50),
  score INT
);

INSERT INTO user VALUES ('user1@gmail.com', 'smith', 25);
INSERT INTO user VALUES ('user1@gmail.com', 'kim', 30);
INSERT INTO user VALUES ('user2@hotmail.com', 'another', 25);
INSERT INTO user VALUES ('user2@hotmail.com', 'ben', 62);
INSERT INTO user VALUES ('user3@yahoo.com', 'joe', 2);
INSERT INTO user VALUES ('user3@yahoo.com', 'lee', 5);
INSERT INTO user VALUES ('user4@hotmail.com', 'barbra', 6);
INSERT INTO user VALUES ('user5@comcast.net', 'rob', 8);

mysql> SELECT * FROM user;
+-------------------+---------+-------+
| email             | name    | score |
+-------------------+---------+-------+
| user1@gmail.com   | smith   |    25 |
| user1@gmail.com   | kim     |    30 | <= 위의 레코드와 email은 동일하지만 name이 다르다
| user2@hotmail.com | another |    25 |
| user2@hotmail.com | ben     |    62 |
| user3@yahoo.com   | joe     |     2 |
| user3@yahoo.com   | lee     |     5 |
| user4@hotmail.com | barbra  |     6 |
| user5@comcast.net | rob     |     8 |
+-------------------+---------+-------+
8 rows in set (0.00 sec)
```

## email별 max score 조최하기

```sql
mysql> SELECT email, MAX(score) as max_sscore
       FROM user
       GROUP BY  email;
+-------------------+------------+
| email             | max_sscore |
+-------------------+------------+
| user1@gmail.com   |         30 |
| user2@hotmail.com |         62 |
| user3@yahoo.com   |          5 |
| user4@hotmail.com |          6 |
| user5@comcast.net |          8 |
+-------------------+------------+
5 rows in set (0.00 sec)
```

max score를 갖는 `name`을 구하기 위해서는 앞 SQL의 결과를 다시 한번 JOIN해야 한다.

```sql
mysql> SELECT user.email, user.name, user.score
       FROM(
           SELECT email, MAX(score) as max_score
           FROM user
           GROUP BY email
       ) t1 INNER JOIN user ON t1.email = user.email AND t1.max_score = user.score;
+-------------------+--------+-------+
| email             | name   | score |
+-------------------+--------+-------+
| user1@gmail.com   | kim    |    30 |
| user2@hotmail.com | ben    |    62 |
| user3@yahoo.com   | lee    |     5 |
| user4@hotmail.com | barbra |     6 |
| user5@comcast.net | rob    |     8 |
+-------------------+--------+-------+
5 rows in set (0.00 sec)
```

단, 가정이 있는데 동일 그룹 내에서 서로 다른 `name`은 서로 다른 `score` 값을 가진다는 가정을 만족해야 한다. 그렇지 않은 경우 `MAX(score)`의 값이 중복된 개수만큼 출력이 된다.
