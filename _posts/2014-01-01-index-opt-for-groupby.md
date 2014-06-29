---
layout: post
title: "MySQL GROUP BY 성능 최적화를 위한 INDEX 설계"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20298486/is-it-good-to-add-index-on-column-used-in-group-by-clause/20298503

## 질문

다음과 같은 SQL문을 실행 중이다. 성능 향상을 위해 어떤 INDEX를 생성하는 것이 좋은가?

```sql
SELECT x, SUM(y)
FROM tbl
WHERE z >= 100
GROUP BY x
ORDER BY SUM(y) DESC;
```

{% include adsense-content.md %}

## 답변

필자의 경험으로는 최적화에서 가장 어려운 것이 GROUP BY가 아닐까 생각된다. 그나마 다행인 것은 앞의 SQL 문은 구조가 단순하기 때문에 다음과 같은 INDEX 추가만으로도 성능 향상을 기대할 수 있다.

```sql
ALTER TABLE tbl ADD INDEX (z, x)
```

GROUP BY를 빠르게 하기 위해선 GROUP BY에 사용된 컬럼 값으로 정렬이 되어 있어야 한다. GROUP BY는 동일한 값을 동일한 그룹으로 묶겠다는 것이므로 정렬이 되어 있으면 그 자체로 이미 그룹핑이 완료되었기 때문이다. 따라서 정렬를 위해 GROUP BY에 사용되는 컬럼을 INDEX로 생성했다.

`INDEX(z, x)`와 같이 복합 키 INDEX를 생성한 이유는 `WHERE z >= 100` 조건에 의해 우선 z 컬럼으로 검색을 한 뒤에 x 컬럼으로 GROUP BY를 하기 때문이다.

좀 더 개선할 여지가 남아 있다. "SUM(y)"를 SELECT 중이지만, INDEX에는 y 컬럼의 값이 존재하지 않다. MySQL은 y 컬럼의 값을 얻기 위해 Data 파일을 접근해야만 하는데 만약 다음과 같은 INDEX를 생성한다면 SELECT문 수행 시 필요한 모든 값은 INDEX에만 존재하기 때문에 성능이 더 빠를 것이다. 물론 INDEX를 추가할 때마다 디스크 용량을 많이 차지하는 단점이 있으므로 꼭 필요한 INDEX인지는 잘 생각하길 바란다.
