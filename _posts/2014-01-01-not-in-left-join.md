---
layout: post
title: "MySQL NOT IN을 LEFT JOIN으로 변환하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20087399/remove-a-sub-query-and-want-a-single-query/20087453

## 질문

다음과 같은 SELECT문을 사용하는데 sub-query를 삭제하고 싶다

```sql
SELECT a1.`active_id` 
FROM active_table a1 
WHERE a1.`active_id`  NOT IN (
    SELECT a2.`active_id`
    FROM view a2
    GROUP BY a2.active_id
      AND DATEDIFF(NOW(),  active_date) > 9
  )
```

{% include adsense-content.md %}

## 답변

```sql
SELECT a1.`active_id` 
FROM active_table a1 LEFT JOIN `view` a2 USING(active_id)
WHERE a2.`active_id` IS NULL
  AND DATEDIFF(now(), active_date) > 9
```

NOT IN은 LEFT JOIN과 IS NULL을 이용하여 sub-query를 제거할 수 있다.

{% include mysql-reco.md %}
