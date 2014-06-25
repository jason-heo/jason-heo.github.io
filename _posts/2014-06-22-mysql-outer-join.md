---
layout: post
title: "LEFT OUTER JOIN과 RIGHT OUTER JOIN을 헷갈리는 문제"
date: 2014-06-05 21:34:00
categories: MySQL
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/19799547/mysql-difference-between-two-tables/19802310#19802310

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/aa721/1

## 질문

```sql
mysql> SELECT * FROM keywords;
+------------+-----------+
| keyword_id | city_name |
+------------+-----------+
|        781 | NYC       |
|     266855 | NYC       |
|     266856 | NYC       |
|     266857 | NYC       |
|     266858 | NYC       |
|     266859 | NYC       |
+------------+-----------+
6 rows in set (0.00 sec)
 
 mysql> SELECT * FROM city;
 +-------------+
 | city_name   |
 +-------------+
 | NYC         |
 | Jersey City |
 | San Jose    |
 | Albany      |
 +-------------+
 4 rows in set (0.00 sec)
```
