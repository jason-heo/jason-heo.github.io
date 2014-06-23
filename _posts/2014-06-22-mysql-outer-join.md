---
layout: post
title: "LEFT OUTER JOIN과 RIGHT OUTER JOIN을 헷갈리는 문제"
date: 2014-06-05 21:34:00
categories: MySQL
---

# 안내

본 문서는 블로그의 운영자인 본인이 Stackoverflow에 올린 답변을 정리한 글입니다. Stackoverflow에 올린 답변 중 한국에 있는 다른 개발자들에게도 도움이 될만한 Q&A를 보기 쉽게 정리했습니다. 가능한 경우는 SQLFiddle에 샘플 데이터도 같이 올려서 실습도 해 볼 수 있도록 하였습니다. 또한 전체 Q&A를 묶어서 PDF 파일로도 배포하고 있습니다. 방문해 주시는 많은 분들에게 도움이 되었으면 좋겠습니다.

# Stackoverflow URL

http://stackoverflow.com/questions/19799547/mysql-difference-between-two-tables/19802310#19802310

# SQLFiddle URL

http://www.sqlfiddle.com/#!2/aa721/1

# 질문

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
