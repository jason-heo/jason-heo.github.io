---
layout: post
title: "MySQL 집계함수에서 NULL의 의미"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/19763806/mysql-ignores-null-value-when-using-max/20119925

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/df63a0/2

## 질문

다음과 같은 데이터가 있다.

    ID   DATE    
    1     '2013-01-26'
    1     NULL
    1     '2013-03-03'     
    2     '2013-02-23'     
    2     '2013-04-12'     
    2     '2013-05-02'

앞의 데이터를 이용하여 각 ID별로 MAX(DATE) 값을 알고 싶은데 NULL 값을 다른 값보다 더 큰 값으로 인식하고 한다. 즉, 나는 다음과 같은 결과를 얻고 싶다.

    ID   DATE    
    1     NULL   
    2     '2013-05-02'

다음과 같은 SELECT문을 수행해 봤지만 원하는 결과를 얻을 수 없었다.

```sql
SELECT ID, MAX(DATE)
FROM test
GROUP BY ID
```

어떻게 하면 내가 원한 결과를 얻을 수 있는가?

{% include adsense-content.md %}

## 답변

질문자가 원한 결과에 대한 대답은 이미 다른 답변자가 해결책을 제시해 주었으며 다음과 같다.

```sql
SELECT id,
  CASE WHEN MAX(date IS NULL) = 0 THEN MAX(date) ELSE NULL END AS date
FROM tbl
GROUP BY id;
```

"DATE IS NULL"은 1 혹은 0을 반환하기 때문에 NULL 값을 포함하지 않은 그룹에 대해서 MAX(DATE IS NULL)은 0을 반환하여 MAX(DATE) 값이 출력된다. NULL 값을 포함한 그룹에서는MAX(DATE IS NULL)은 1을 반환하기 때문에 ELSE 부분에 있는 NULL이 출력된다. 위의 답변 자체 만으로도 훌륭한 팁이라 생각된다.

위의 답변은 필자가 답변을 달기 전에 이미 답변이 달렸었고, 필자는 RDB에서 집계 함수를 사용할 때 NULL은 계산되지 않는 예를 보여주려고 한다. 다음과 같은 데이터가 존재한다고 가정하자.

    mysql> SELECT * FROM null_test;
    +------+
    | c    |
    +------+
    | NULL |
    |    1 |
    |    2 |
    |    3 |
    +------+
    4 rows in set (0.00 sec)

이때 COUNT(*)와 COUNT(c)의 차이는 어떻게 될까? 위에서 말했다시피 집계 함수는 NULL을 제외하고 계산하므로 다음 결과와 같인 차이가 발생한다.

    mysql> SELECT COUNT(c) FROM null_test;
    +----------+
    | COUNT(c) |
    +----------+
    |        3 |
    +----------+
    1 row in set (0.00 sec)

COUNT(c)의 경우 컬럼 명을 명시적으로 입력했기 때문에 컬럼 값이 NULL인 것은 COUNT에서 제외되었다.  COUNT(*)를 수행하면 NULL도 포함하게 되므로 3이 아닌 4가 출력된다.

    mysql> SELECT COUNT(*) FROM null_test;
    +----------+
    | COUNT(*) |
    +----------+
    |        4 |
    +----------+
    1 row in set (0.00 sec)

AVG()는 평균 값을 계산하는 함수인데 AVG(c)의 결과에서도 NULL은 제외된다. 이때 주의할 점은 나눗셈 연산에서 분자의 값에서 NULL이 제외됨은 물론이고, 분모에 있는 레코드 개수에서도 NULL인값이 제외된다는 점이다. 따라서 AVG(c)는 다음과 같이 2가 출력된다.

    mysql> SELECT AVG(c) FROM null_test;
    +--------+
    | AVG(c) |
    +--------+
    | 2.0000 |
    +--------+
    1 row in set (0.00 sec)

"(1+2+3) / 4"가 아닌 "(1+2+3) / 3"이 결과로 출력되었다. COUNT()는 집계 함수 중에서도 자주 사용되는 함수이므로 꼭 위의 사항을 기억하고 있어야 한다.
