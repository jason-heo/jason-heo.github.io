---
layout: post
title: "MySQL GROUP BY를 이용하여 최대 값을 가진 레코드 가져오기 (중복 값 허용 시)"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20329351/get-entire-row-when-group-an-id-in-mysql/20329527#20329527

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/d7fe7/6

## 질문

다음과 같은 테이블과 데이터가 존재한다.

    mysql> SELECT * FROM objects;
    +---------+-------+--------+---------+
    | product | name  | points | message |
    +---------+-------+--------+---------+
    |       1 | Joe   |      4 | M1      |
    |       2 | Steve |     15 | M2      |
    |       2 | Loui  |     17 | M3      |
    |       3 | Larry |     10 | M4      |
    |       3 | Harry |     10 | M5      |
    |       3 | Hank  |      8 | M6      |
    +---------+-------+--------+---------+
    6 rows in set (0.00 sec)

product 그룹 내에서 'MAX(points)'를 소유한 레코드 정보를 출력하고자 한다. 단, product = 3인 레코드의 MAX(points)는 10인데 Larry와 Harry가 이를 만족하고 있다. 이렇게 중복된 경우는 1건만 출력되어야 하며 어떤 것이 출력되든 1개만 출력되면 된다.

{% include adsense-content.md %}

## 답변

기본적으로 앞의 질문 중 MAX(credits)를 구하는 질문과 동일하지만, 이 경우 MAX(points)의 값이 동일 그룹 내에서 중복된다는 차이점이 있다.

복잡할 수 있으나 우선 원하는 결과를 출력하는 SQL을 보자. 물론 아래의 방법 말고 더 좋은 방법이 있을 수 있다.

    SELECT z.product, z.name, z.message, z.m_p
    FROM
    (
        SELECT IF(@prev = ord.product, 0, 1) is_first_appear,
            @prev := ord.product,
            ord.*
        FROM (
            SELECT o.product, o.name, o.message, x.m_p
            FROM (
                SELECT product, MAX(points) m_p
                FROM objects
                GROUP BY product
            ) x INNER JOIN objects o
                 ON x.product = o.product AND x.m_p = o.points
            ORDER BY o.product
        ) ord, (SELECT @prev := 0) init
    ) z
    WHERE
        is_first_appear = 1;
     
    +---------+-------+---------+------+
    | product | name  | message | m_p  |
    +---------+-------+---------+------+
    |       1 | Joe   | M1      |    4 |
    |       2 | Loui  | M3      |   17 |
    |       3 | Larry | M4      |   10 |
    +---------+-------+---------+------+
    3 rows in set (0.00 sec)

자, 그러면 SQL을 하나 씩 풀어서 설명해 보자.

### x table

    각각의 product별 최대 points를 구하는 질의이다.

    SELECT product, MAX(points) m_p
    FROM objects
    GROUP BY product;
    ord table

ord 테이블까지는 어렵지 않을 것이라 생각된다. "ORDER BY o.product"를 이용하여 JOIN 결과를 product 순으로 정렬하였다.

    SELECT o.product, o.name, o.message, x.m_p
    FROM (
        SELECT product, MAX(points) m_p
        FROM objects
        GROUP BY product
    ) x INNER JOIN objects o
         ON x.product = o.product AND x.m_p = o.points
    ORDER BY o.product;
     
    +---------+-------+---------+------+
    | product | name  | message | m_p  |
    +---------+-------+---------+------+
    |       1 | Joe   | M1      |    4 |
    |       2 | Loui  | M3      |   17 |
    |       3 | Harry | M5      |   10 |
    |       3 | Larry | M4      |   10 |
    +---------+-------+---------+------+
    4 rows in set (0.00 sec)

### init table

MySQL의 user variable를 사용하기 위해 변수 값을 초기화하는 테이블이다.

    (SELECT @prev := 0) init
    z table

SELECT 절이 핵심이다. @prev 변수는 초기에 0의 값이 할당되었으며 레코드를 이동할 때마다 현재 레코드의 product 값으로 재할당된다. ord 테이블은 product로 정렬이 되었으므로, 동일한product는 연속적으로 출력될 것이다. 이를 @prev 값과 비교한다. @prev 값과 ord.product의 값이 서로 다르다면 product의 값이 앞의 레코드와 다르다는 이야기이고 이를 이용하여 그룹 내 첫 번째 출력된 product인지 판단한다.

    SELECT IF(@prev = ord.product, 0, 1) is_first_appear,
        @prev := ord.product,
        ord.*
    FROM (
        SELECT o.product, o.name, o.message, x.m_p
        FROM (
            SELECT product, MAX(points) m_p
            FROM objects
            GROUP BY product
        ) x INNER JOIN objects o
             ON x.product = o.product AND x.m_p = o.points
        ORDER BY o.product
    ) ord, (SELECT @prev := 0) init
     
    +-----------------+----------------------+---------+-------+---------+------+
    | is_first_appear | @prev := ord.product | product | name  | message | m_p |
    +-----------------+----------------------+---------+-------+---------+------+
    |               1 |                    1 |       1 | Joe   | M1      |    4 |
    |               1 |                    2 |       2 | Loui  | M3      |   17 |
    |               1 |                    3 |       3 | Larry | M4      |   10 |
    |               0 |                    3 |       3 | Harry | M5      |   10 |
    +-----------------+----------------------+---------+-------+---------+------+
    4 rows in set (0.00 sec)

### 최종 결과

최종적으로 WHERE 절에서 is_first_appear 컬럼의 값이 1인 레코드만 출력하면 된다.

user variable을 이용하여 선언적 언어인 SQL에 약간의 절차적인 느낌을 줄 수 있다. 앞으로도 user variable을 이용한 재미있는 SQL이 예가 몇 번 더 보게 될 것이다.


{% include mysql-reco.md %}
