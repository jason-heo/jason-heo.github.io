---
layout: post
title: "LEFT OUTER JOIN과 RIGHT OUTER JOIN을 헷갈리는 문제"
date: 2014-03-05 21:34:00
categories: mysql
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

{% include adsense-content.md %}

## 답변

LEFT JOIN을 사용할 때 좌우 테이블의 위치를 헷갈렸다. 질문자의 경우 아래와 같이 city 테이블을 왼쪽에 위치시켜야 한다. 또한 "keyword_id = 781" 조건을 ON 구문에 위치시켜야 한다.

```sql
SELECT city.city_name
FROM city LEFT JOIN keywords ON city.city_name = keywords.city_name
  AND keywords.keyword_id = 781
WHERE keywords.keyword_id IS NULL;
```

### 왜 LEFT JOIN일까?

필자도 자주 헷갈리는 것 중 하나가 LEFT JOIN을 사용할 때 JOIN 조건을 만족하지 않아도 출력되어야 할 테이블을 왼쪽에 두어야 하는지 오른쪽에 두어야 하는지에 대한 것이다. 이는 JOIN을 NESTED LOOP JOIN으로 변경하면 이해가 쉽다.

RDB의 JOIN을 연산하는 방법은 기본적으로 NESTED LOOP JOIN을 사용한다. JOIN 성능을 위해 MERGE JOIN, HASH JOIN을 사용할 수 있지만, 기본은 NESTED LOOP JOIN이며 MySQL 5.5에서는NESTED LOOP JOIN만 지원한다.

예를 들어, "tab1 INNER JOIN tab2"를 JOIN하는 의사 코드(pseudo code)는 다음과 같다.

```
$tab1 = [1, 2, 3, 4, 5];
$tab2 = [3, 4, 5, 6, 7];
 
foreach ($left_row in $tab1)
{
    foreach ($right_row in tab2)
    {
        if ($left_row == $right_row)
        {
            echo ($left_row, $right_row);
        }
    }
}
```

즉, 2중 LOOP를 수행하는 방식이 NESTED LOOP JOIN이다. 이때 2개 테이블 중 안쪽(inner)에 놓을 테이블과 바깥쪽(outer)에 놓을 테이블은 Optimizer에 의해 결정된다.

그렇다면 "tab1 LEFT JOIN tab2"를 수행하는 NESTED LOOP JOIN은 어떻게 생겼을까? 이 LEFT JOIN의 결과는 tab1에 존재하는 모든 레코드가 출력되어야 한다. 이를 위해선 tab1이 NESTED LOOP JOIN의 바깥쪽(outer)에 위치해야만 한다.

```
$tab1 = [1, 2, 3, 4, 5];
$tab2 = [3, 4, 5, 6, 7];
 
foreach ($left_row in $tab1)
{
    $matched = false;
 
    foreach ($right_row in tab2)
    {
        if ($left_row == $right_row)
        {
            echo ($left_row, $right_row);
            $matched = true;
        }
    }
 
    if ($matched == false)
    {
        echo ($left_row, NULL);
    }
}
```

LEFT JOIN은 LEFT OUTER JOIN의 줄임말이다. NESTED LOOP JOIN과 LEFT JOIN의 출력 결과를 잘 생각해 본다면, LEFT JOIN의 왼쪽 테이블이 NESTED LOOP JOIN의 바깥쪽에 위치해야 한다는 것을 알 수 있을 것이므로 LEFT JOIN과 RIGHT JOIN을 헷갈리는 일이 줄어들 것이라 생각된다.

### 왜 keyword_id = 781 조건을 ON 구문에 넣었을까?

다음은 INNER JOIN, LEFT JOIN, RIGHT JOIN의 결과를 보여주는 예이다.

![relational algebra](/images/posts/mysql/relational-algebra.PNG)

각 결과를 잘 생각해 보면 "keyword_id = 781"이 ON 절에 와야 하는 이유를 알 수 있을 것이다. 참고로 `FULL JOIN`은 MySQL에서 지원하지 않으며 `LEFT JOIN`과 `RIGHT JOIN`을 `UNIONq하여 이용하여 `FULL JOIN`을 흉내 낼 수 있다.
