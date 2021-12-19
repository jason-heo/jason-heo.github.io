---
layout: post
title: "MySQL Query Rewrite에 대한 간략한 소개"
categories: MySQL
---

MySQL 5.7부터 Query Rewrite 기능을 사용할 수 있는 듯 하다. 아직은 자료 조사 중이며 앞으로 1주일 동안은 Query Rewrite 기능을 주로 찾아볼 계획이다. 물론 다음 주 오픈할 서비스가 있어서 시간이 날진 모르지만 틈틈히 조사해볼 계획이다.

### Query Rewrite 기능이란?

말 그대로 MySQL에 입력되는 Query를 중간에 intercept하여 Rewrite하는 기능이다. 예를 들어보자.

> 솔루션을 하나 샀는데, 여기서 수행되는 SELECT문에서 INDEX를 잘못 사용하고 있다. `table1`이라는 테이블에 idx_a를 항상 타면 좋겠는데, 외부 솔루션이고 소스코드도 없어서 어떻게 할 방도가 없다.

이런 경우 Query Rewrite 기능을 사용하여 해결할 수 있다. MySQL에서 수행되는 Query 중에 `table`에 대해서 SELECT하는 Query를 캡쳐한 뒤 `FORCE INDEX(idx_a)`를 지정하면 된다.

pre query rewrite와 post query rewrite 2가지 방식이 있으며 Query가 Parsing되기 전이냐 후냐에 따라 pre와 post로 나뉜다.

### pre query rewrite

pre query rewrite는 Query 문자열을 char*로 입력받은 후 새로운 Query를 char* 형태로 return해주는 query rewrite이다. 패턴 매칭으로 rewrite할 수 있으므로 learning curve가 가파르진 않지만 Query를 제대로 분석하기엔 어려울 수 있다. (즉, 초기에 배울 게 별로 없지만 Query를 제대로 분석하려면 Parse Tree가 필요하다)

### post query rewrite

여기서 post는 Query Parsing 후를 의미한다. 따라서 pre 와 다르게 입력도 Query Parse Tree가 입력된다. 그렇기 때문에 post query rewrite를 다루기 위해선 배워야 할 것이 많다.

나는 post query rewrite에 관심이 있으며, 앞으로 1주일 간 공부를 해볼 계획이다.

### 참고 자료

[Write Yourself a Query Rewrite Plugin: Part 1](http://mysqlserverteam.com/write-yourself-a-query-rewrite-plugin-part-1/)
[Write Yourself a Query Rewrite Plugin: Part 2](http://mysqlserverteam.com/write-yourself-a-query-rewrite-plugin-part-2/)

{% include mysql-reco.md %}
