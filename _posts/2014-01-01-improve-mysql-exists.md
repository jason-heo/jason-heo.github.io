---
layout: post
title: "MySQL EXISTS 성능 향상"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20392611/is-there-any-other-way-to-write-this-query/20392902

## 질문

다음과 같은 EXISTS를 이용한 SELECT문을 수행 중인데 수행 시간이 18분이나 된다. 좀 더 빠르게 할 수 있는 방법은 없는가?

    SELECT COUNT(*) FROM psverify_interaction_numeric_ip_address a
    WHERE EXISTS (
      SELECT 1 FROM Xwalk_GeoLiteCity_Blocks
      WHERE startIpNum <= a.numeric_ip_address AND endIpNum >= a.numeric_ip_address
    );
     
    +----------+
    | COUNT(*) |
    +----------+
    |      240 |
    +----------+
    1 row in set (18 min 2.00 sec)

{% include adsense-content.md %}

## 답변

MySQL에서 correlated sub-query는 대부분 속도가 느리다. 다음과 같이 INNER JOIN으로 바꾸는 것이 좋겠다.

```sql
SELECT COUNT(a.id)
FROM psverify_interaction_numeric_ip_address a
INNER JOIN Xwalk_GeoLiteCity_Blocks b
  ON b.startIpNum <= a.numeric_ip_address AND b.endIpNum >= a.numeric_ip_address
```
