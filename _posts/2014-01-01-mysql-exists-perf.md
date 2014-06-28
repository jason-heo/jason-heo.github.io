---
layout: post
title: "MySQL EXISTS 성능 향상"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20064923/query-execution-is-too-long-to-load/20065832

## 질문

다음과 같은 SQL을 사용 중인데 속도가 느리다. 더 빠르게 할 수 있는 방법이 있는가?

```sql
SELECT
  i.ID
FROM item_tb i
WHERE
  i.coID = 'xxxx'
  AND i.isProduct = '1'
  AND i.isBom = '0'
  AND NOT EXISTS(
    SELECT
      s.ID
    FROM
      stocks_tb s
    WHERE
      i.ID = s.itemID
      AND s.brID = 'yyy'
 )
```

{% include adsense-content.md %}

## 답변

다음과 같은 LEFT JOIN이 더 빠르다.

```sql
SELECT
  i.ID
FROM item_tb i LEFT JOIN stocks_tb s ON i.ID = s.itemID
WHERE
  i.coID = 'xxx'
  AND i.isProduct = '1'
  AND i.isBom = '0'
  AND s.brID = 'yyy'
  AND s.itemID IS NULL;
```

NOT EXISTS나 IN 등의 sub-query는 사람이 생각하는 것을 SQL으로 표현하기에 쉽다. 예를 들어 "특정 레코드를 조회하고 싶은데, 대신 이 컬럼의 값은 특정 조건을 만족하면 안 된다"는 생각은 NOT EXISTS로 쉽게 표현할 수 있기 때문에 사용자들은 NOT EXISTS, IN 등을 자주 사용하는 듯 하다. 하지만, 일반적으로 NOT EXISTS, EXISTS, IN, NOT IN 등의 sub-query는 동일 결과를 출력하는sub-query없는 JOIN보다 성능이 느리다. 또한 EXISTS, NOT EXISTS는 correlated sub-query를 사용할 가능성이 높으므로 성능이 더 안 좋을 수 있다.

따라서 sub-query를 사용하기 전에는 JOIN으로 변환할 수 있는지 생각해 보도록 하자.
