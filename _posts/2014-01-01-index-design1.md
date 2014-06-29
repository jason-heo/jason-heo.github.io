---
layout: post
title: "MySQL SELECT 성능 향상을 위한 INDEX 설계"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20396795/how-can-i-optimize-this-mysql-query-please/20396888

## 질문

다음과 같은 SELECT문을 수행 중이다. 속도가 느린데 최적화 방법은 없는가?

```sql
SELECT a.providerName AS Assureur, b.insuranceType AS Produit, c.votedFor,
  c.votedFor2, c.email, c.comment, c.modified
FROM insuranceproviders AS a, insurancetypes AS b, insurancevotes AS c
WHERE a.id = c.providerId
  AND b.id = c.insTypeId
```

{% include adsense-content.md %}

## 답변

다음과 같은 INDEX를 생성하라.

```
ALTER TABLE insuranceproviders ADD INDEX(id);
ALTER TABLE insurancetypes ADD INDEX (providerID, insTypeId);
ALTER TABLE insurancevotes ADD INDEX(insTypeId);
```

그리고 JOIN도 다음과 같이 명시적으로 INNER JOIN으로 변경하는 것이 좋다. 다음과 같은 SQL이 질문자의 SQL에 비해 가독성이 좋다. 즉, 테이블간 JOIN이 어떤 컬럼으로 수행되는지 명시적으로 알 수있으며 이는 MySQL Optimizer가 최적화를 하는데도 도움을 주는 것으로 알고 있다.

```sql
SELECT a.providerName AS Assureur, b.insuranceType AS Produit,
    c.votedFor, c.votedFor2, c.email, c.comment, c.modified
FROM insuranceproviders AS a
    INNER JOIN insurancetypes AS b ON a.id = c.providerID
    INNER JOIN  insurancevotes AS c ON b.id = c.insTypeId
```

