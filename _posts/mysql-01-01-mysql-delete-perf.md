---
layout: post
title: "MySQL DELETE 성능 (IN sub-query를 JOIN으로 변경하기)"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/19803704/select-sub-query-cause-mysql-server-to-be-unresponsive/19803782#19803782

## 질문

다음과 같은 DELETE 문을 사용 중인데 속도가 느리다. 속도를 향상 시킬 수 있는 방법은 없는가?

```sql
DELETE FROM item_measurement
WHERE measurement_id IN (
   SELECT id AS measurement_id
   FROM invoice_item
   WHERE invoice_id = 'A3722'
)
```

{% include adsense-content.md %}

## 답변

다음과 같은 MULTIPLE DELETE 형식으로 변경하는 것이 좋다.

```
DELETE item_measurement
FROM item_measurement m, invoice_item i
WHERE m.measurement_id = (-i.id)
  AND i.invoice_id = 'A3722'
```

또한 다음과 같은 INDEX가 있으면 좋다

```sql
ALTER TABLE item_measurement ADD INDEX(measurement_id);
ALTER TABLE invoice_item ADD INDEX(invoice_id, id);
```

DELETE문의 조건을 SELECT로 쉽게 변환할 수 있는데, 이를 통해서 DELETE가 느린 경우 INDEX를 잘 타고 있는지, JOIN 순서는 올바른지 판단할 수 있다. 예를 들어 위의 질문자의 DELETE 문은 다음과같이 SELECT로 변환할 수 있다.

```sql
SELECT COUNT(*)
FROM item_measurement
WHERE measurement_id IN (
   SELECT (-id) AS measurement_id
   FROM invoice_item
   WHERE invoice_id = 'A3722'
);
```

이를 통해 얻을 수 있는 장점은

1. EXPLAIN SELECT를 통해서 실행 계획을 확인할 수 있다.
1. 대략적인 시간을 예측할 수 있다.
1. DELETE될 레코드가 몇 개인지, 그 대상은 무엇인지 알 수 있다.

와 같다. EXPLAIN SELECT를 통하여 적적할 INDEX를 타고 있는지, Optimizer가 JOIN 순서를 잘 선택했는지 알 수 있다. 또한 DELETE 수행 시간은 최소한 동일 조건의 SELECT보다 클 것이므로 대략적인 DELETE 시간도 예측할 수 있다.
