---
layout: post
title: "MySQL COUNT의 값이 예상과 다른 문제"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20393685/sub-query-in-inner-join-mysql/20394180

## 질문

다음과 같은 SQL을 수행하고 있다.

```sql
SELECT actor_role.actor_id, COUNT(*) AS cnt
FROM movie INNER JOIN actor_role ON movie.movie_id = actor_role.movie_id
GROUP BY actor_role.actor_id
ORDER BY COUNT cnt;
```

movie 테이블은 영화 정보를 저장하는 테이블이며, 앞의 SQL에는 보이지 않지만, actor는 영화 배우 테이블이다. 영화와 영화배우는 n:m 관계이기 때문에 영화화 영화 배우를 연결하기 위한 테이블actor_role이 존재한다.

따라서 위의 SQL문은 영화배우를 영화에 많이 출연한 순으로 출력하는 SELECT문이 된다. 그런데 1명의 배우가 1개의 영화에 여러 역할로 출연할 수 있다. 이러한 경우도 1개의 영화로 계산하고자 하는데 앞의 SQL은 그러한 경우도 여러 영화에 출연한 것으로 계산하고 있다. 여러 시도를 해 봤지만 잘 안 된다. 방법이 없을까?

{% include adsense-content.md %}

## 답변

질문자가 Stackoverflow에 올린 SQL이 좀 복잡하고 설명도 어려운 듯 하지만, 우리나라의 2003년작 "클래식"을 생각하면 된다. 클래식에서 영화배우 손예진은 "주희"와 "지혜" 캐릭터를 소화했다. 필자도본 글을 정리하면서 알게 되었지만 위키피디아에는 이렇게 1명의 배우가 1개의 영화에 여러 캐릭터를 연기한 것에 대해서도 정리되어 있다. 손예진과 클래식 예도 위키피디아서 찾았다.

이러한 경우 앞의 예에서 actor_role에 손예진이 영화 클래식에 출연한 정보가 2건의 레코드로 저장되게 된다. COUNT(*)로 출연 영화를 계산하면 손예진이 2개의 영화에 출연한 것으로 계산되지만,질문자는 1개의 영화에 출연한 것으로 계산하고 싶다는 이야기이다.

질문에 대한 설명이 길었지만, 답은 의외로 간단하다. COUNT(*)은 모든 레코드의 개수인데 우리는 영화 개수만 계산하면 되므로 COUNT(*)를 COUNT(DISTINCT movie.movie_id)로 수정하면 된다.

```sql
SELECT actor_role.actor_id, COUNT(DISTINCT movie.movie_id) AS cnt
FROM movie INNER JOIN actor_role ON movie.movie_id = actor_role.movie_id
GROUP BY actor_role.actor_id
ORDER BY COUNT cnt;
```
