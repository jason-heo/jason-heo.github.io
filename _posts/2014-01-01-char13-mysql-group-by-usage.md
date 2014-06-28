---
layout: post
title: "MySQL GROUP BY 사용 시 주의점"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/19929595/mysql-sum-and-inner-join/19929748

## 질문

아래와 같은 SQL문을 수행 중인데 결과가 이상하다.

```sql
SELECT P.Name, P.Playernr, SUM(F.Amount)
FROM FINES F
INNER JOIN PLAYERS P ON F.Playernr = P.Playernr
GROUP BY  P.Playernr
```

{% include adsense-content.md %}

## 답변

MySQL에서 GROUP BY는 표준 SQL과 다르게 작동하기 때문에 헷갈리는 부분이며, Stackoverflow에도 이와 관련하여 질문이 매일 반복하여 올라오고 있다. 개인적인 생각으로는 MySQL에서 이와 관련하여 문법 검사를 엄격하게 하는 것이 좋겠으나 이미 잘못된 문법으로 GROUP BY를 사용하는 레거시(Legacy) 어플리케이션이 많기 때문에 쉽지도 않은 일이다. 무엇이 문제일까? 동일 SQL문을Oracle에서 실행시키면 다음과 같은 에러가 발생한다.

    ORA-00979: not a GROUP BY expression

결론적으로 질문자가 문의한 SQL은 다음과 같이 변경되어야 정확하다.

```sql
SELECT P.Name, P.Playernr, SUM(F.Amount)
FROM FINES F
INNER JOIN PLAYERS P ON F.Playernr = P.Playernr
GROUP BY  P.Name, P.Playernr
```

GROUP BY를 사용하는 경우, SELECT할 수 있는 컬럼은 GROUP BY에 나열된 컬럼과 SUM(), COUNT() 같은 집계 함수(Aggregation Function)으로 한정된다. 질문자의 경우 "GROUP BY P.Playernr"로 GROUP BY를 하였으나 "SELECT P.Name, P.Playernr, SUM(F.Amount)"와 같이 GROUP BY에 사용되지 않은 "P.Playernr"을 SELECT하려고 했기 때문에 결과가 이상하게 출력된 것이다. Oracle은 이러한 경우 "not a GROUP BY expression"이라는 오류 메시지를 출력해 주었다.

물론 MySQL에서도 설정 값 변경을 통해 잘못된 GROUP BY를 사용하는 경우 에러를 발생하도록 할 수 있지만, 이 옵션이 기본으로 비활성화되어 있으며, 이 옵션의 존재를 아는 사용자도 드물다. 다음의 예처럼 sql_mode의 값을 "ONLY_FULL_GROUP_BY"로 변경하면 잘못된 GROUP BY를 사용하는 것을 방지할 수 있다.

```sql
mysql> SET sql_mode = 'ONLY_FULL_GROUP_BY';
Query OK, 0 rows affected (0.00 sec)
 
mysql> SELECT P.Name, P.Playernr, SUM(F.Amount)
    -> FROM FINES F
    -> INNER JOIN PLAYERS P ON F.Playernr = P.Playernr
    -> GROUP BY  P.Playernr;
ERROR 1055 (42000): 'db_name.P.Name' isn't in GROUP BY
```

GROUP BY 이외에도 MySQL은 ANSI 표준을 지키지 않는 경우가 많은데 sql_mode를 조절하여 작동 방식을 사용자가 지정할 수 있다. 자세한 것은http://dev.mysql.com/doc/refman/5.5/en/server-sql-mode.html 를 방문하여 찾아보기 바란다.
