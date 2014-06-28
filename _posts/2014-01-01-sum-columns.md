---
layout: post
title: "MySQL 임의 컬럼 개수 합산"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20072250/find-sum-across-varying-no-of-columns/20072490

## 질문

다음과 같은 테이블이 존재한다.

    value1   value2     value3    value4    value5   constant
    1          2          3           4         5     2
    8          2          8           3         5     2
    1          5          3           4         5     3
    1          2          6           4         5     3

constant 값에 따라 다음과 같은 컬럼의 값을 합산하고자 한다. 좋은 방법이 없는지 궁금하다.

- constant가 2인 경우 : value1 + value2
- constant가 3인 경우 : value1 + value2 + value3
- constant가 4인 경우 : value1 + value 2 + value3 + value4

{% include adsense-content.md %}

## 답변

value 컬럼의 개수가 5개라고 가정한다면 다음과 같은 SQL문을 사용할 수 있다.

```sql
SELECT
  IF(constant = 1, value1,
    IF (constant = 2, value1 + value2,
      IF (constant = 3, value1 + value2 + value3,
        IF (constant = 4, value1 + value2 + value3 + value4,
           value1 + value2 + value3 + value4 + value5
        )
      )
    )
  )
FROM tab;
```

질문자의 경우 총 24개의 value 컬럼이 존재한다고 한다. 이 경우 사람이 직접 위의 IF() 절을 만들기 귀찮으므로 프로그램을 이용하여 만들 수도 있지만 컬럼 개수가 많은 경우 위의 방법은 SQL이 너무길어져서 보기도 안 좋고 속도도 느릴 수 있다. 이 경우 DB 설계를 변경하는 것이 더 좋다고 생각된다.

```sql
CREATE TABLE tbl(id INT, constant INT),
CREATE TABLE value_tbl(tbl_id INT, column_seq INT, value INT);
 
SELECT SUM(value)
FROM tbl t, value_tbl v
WHERE t.id = v.tbl_id
  AND column_seq BETWEEN 1 AND tbl.constant;
```

즉, 24개의 값을 저장하는 경우 24개의 컬럼을 만드는 것이 아니라 24개의 값을 저장할 테이블 (위의 경우 value_tbl)에 저장하는 것이다.
