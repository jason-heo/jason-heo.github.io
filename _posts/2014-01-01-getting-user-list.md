---
layout: post
title: "사용자 목록 구하기"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20137395/how-to-get-user-list/20137466

## 질문

다음과 같은 3개의 테이블이 존재한다.

    User(userid, username, email)
    Site_A(userid, username, status)
    Site_B(userid, username, status)

User 테이블에 존재하는 userid 중 Site_A 혹은 Site_B에 존재하지 않는 userid 목록을 얻고 싶다.

{% include adsense-content.md %}

## 답변

`User` 중 `Site_A`에 존재하지 않는 구문은 NOT IN을 이용하면 쉽게 구할 수 있으나 역시 성능을 고려한다면 `LEFT JOIN`을 사용하는 것이 좋다. 그런데 질문자는 Site_B에 존재하지 않는 User도 알고싶어하므로 2개의 질의를 UNION으로 묶으면 쉽게 구할 수 있는 문제이다.

```sql
SELECT  u.username
FROM User u LEFT JOIN Site_A USING(username)
WHERE Site_A.username IS NULL
 
UNION
 
SELECT  u.username
FROM User u LEFT JOIN Site_B USING(username)
  LEFT JOIN Site_B USING(username)
WHERE Site_B.username IS NULL
```
