---
layout: post
title: "1:n JOIN에서 중복된 레코드 제거하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20206488/fetching-data-from-three-different-tables-but-it-is-duplicating-records/20206570

## 질문

아래 SELECT문과 같이 3개의 테이블을 JOIN하고 있다. 테이블들의 관계가 1:n이기 때문에 레코드가 출력되는데, 중복을 제거할 수 있는 방법은 없는가?

```sql
SELECT c.categoriestype, s.SubCatName,
  p.productname, p.productprice, p.id, p.productimage,
  p.productthumbnail
FROM tbl_ProCategories c, tbl_ProSubCategories s, tbl_products p
WHERE p.subcat_id = s.cat_id
  AND p.cat_id = c.id
  AND c.id = s.cat_id
LIMIT 0 , 30
```

{% include adsense-content.md %}

## 답변

다음과 같이 DISTINCT를 붙여서 쉽게 중복을 제거할 수 있다.

```sql
SELECT DISTINCT c.categoriestype, s.SubCatName,
  p.productname, p.productprice, p.id, p.productimage, p.productthumbnail
FROM tbl_ProCategories c, tbl_ProSubCategories s, tbl_products p
WHERE p.subcat_id = s.cat_id
  AND p.cat_id = c.id
  AND c.id = s.cat_id
LIMIT 0 , 30
```

DISTINCT가 함수인 줄 알고 DISTINCT(col)과 같이 함수처럼 사용하는 경우가 있는데 엄밀히 말하면 DISTINCT는 함수가 아니다. 중복을 제거할 컬럼이 1개인 경우는 함수 표현처럼 사용해도 에러가발생하지 않는다. 다음 예를 보자

    mysql> SELECT * FROM test;
    +------+------+
    | a    | b    |
    +------+------+
    |    1 |    1 |
    |    1 |    1 |
    |    2 |    2 |
    +------+------+
    3 rows in set (0.00 sec)
     
    mysql> SELECT DISTINCT(a) FROM test;
    +------+
    | a    |
    +------+
    |    1 |
    |    2 |
    +------+
    2 rows in set (0.00 sec)

`DISTINCT(a)` 처럼 함수 형식으로 사용해도 중복 제거가 잘 되었다. 하지만 a, b 컬럼을 동시에 중복 제거하는 경우 다음과 같이 DISTINCT(a, b)를 입력하면 에러가 발생하게 된다.

    mysql> SELECT DISTINCT(a, b) FROM test;
    ERROR 1241 (21000): Operand should contain 1 column(s)

DISTINCT를 함수라고 생각하기 보다는 "SELECT DISTINCT"를 묶어서 중복을 제거하는 SELECT라고 인식하는 것이 좋다. 다음과 같이 사용하면 복수 개의 컬럼에 대해서도 중복을 제거할 수 있다.

    mysql> SELECT DISTINCT a, b FROM test;
    +------+------+
    | a    | b    |
    +------+------+
    |    1 |    1 |
    |    2 |    2 |
    +------+------+
    2 rows in set (0.00 sec)

참고로 MySQL 매뉴얼에 따르면 우리가 평소 무심코 사용하는 SELECT는 사실 "SELECT ALL"과 같이 "ALL"이 생략된 것이다. 따라서 다음과 같은 질의도 사용 가능하다. "SELECT ALL"을 생각해본다면 "SELECT DISTINCT"의 사용법도 이해가 될 것이다.

    mysql> SELECT ALL a, b FROM test;
    +------+------+
    | a    | b    |
    +------+------+
    |    1 |    1 |
    |    1 |    1 |
    |    2 |    2 |
    +------+------+
    3 rows in set (0.00 sec)

