---
layout: post
title: "MySQL IN 성능 향상 (INNER JOIN으로 변경하기)"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20311929/mysql-query-optimization/20311945

## 질문

다음과 같은 SQL문을 사용 중인데 속도가 너무 느리다.

```sql
SELECT SUM(earnings)
FROM earnings
WHERE account_number IN
    (SELECT account_number FROM users WHERE referred_by = 500);
```

빠르게 할 수 있는 방법은 없는가?

{% include adsense-content.md %}

## 답변

다음과 같은 INNER JOIN으로 변경하는 것이 좋다.

```sql
SELECT SUM(earnings.earnings)
FROM earnings INNER JOIN users USING(account_number)
WHERE users.referred_by = 500;
```

또한 다음의 INDEX가 존재해야 한다.

```sql
ALTER TABLE users ADD INDEX (referred_by, account_number);
ALTER TABLE earnings ADD INDEX (account_number);
```
