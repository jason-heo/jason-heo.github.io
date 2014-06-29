---
layout: post
title: "MySQL의 IN() v.s. EXISTS v.s. INNER JOIN 성능 비교"
date: 2014-05-28 
categories: mysql
---

## 연재 시리즈

- MySQL IN subquery 성능. IN sub query는 가급적 사용을 피하자
- IN() v.s. EXISTS v.s INNER JOIN <= 현재 글
- INNER JOIN v.s. EXISTS 어떤 것이 언제 더 빠른가

## 일러두기

- 아래의 실험음 모두 MySQL 5.5 기반으로 실험되었습니다.
- MySQL Query Cache는 Off로 설정하고 실험되었습니다.
- Test에 사용된 Data는 TPC-H용 데이터입니다.
- TPC-H용 데이터를 MySQL에 로딩하는 것은 본인의 블로그에서 보실 수 있습니다.

## IN() v.s. EXISTS v.s INNER JOIN

많은 경우 IN(), EXISTS, INNER JOIN은 상호 변환이 가능하면서도 성능에 차이가 있다.

### 질의

- 10번 국가에 속한 고객의 주문 건수를 구하라

### 실험 결과

|종류|수행 시간(초)|
|INNER JOIN|0.06|
|IN|3.33|
|EXISTS|5.06|

### INNER JOIN
```sql
SELECT COUNT(*)
FROM orders INNER JOIN customer
  ON orders.o_custkey = customer.c_custkey
WHERE customer.c_nationkey = 10;
```

### IN()

```sql
SELECT COUNT(*)
FROM orders
WHERE o_custkey IN (
  SELECT c_custkey FROM
  customer WHERE c_nationkey = 10
);
```

### EXISTS

```sql
SELECT COUNT(*)
FROM orders
WHERE EXISTS (
  SELECT 1 FROM customer
  WHERE c_nationkey = 10 
  AND c_custkey = orders.o_custkey
);
```
 
## NOT IN v.s. NOT EXISTS v.s. LEFT OUTER JOIN

### 질의

- 고객 아이디가 존재하지 않는 주문 건수

TCP-H의 orders 테이블을 약간 가공하여 고객 id가 존재하지 않는 주문을 만들었다. (여러분들도 FOREIGN KEY 없이 테이블 설계하면 이런 현상이 충분히 발생할 수 있습니다.) 이런 잘못된 데이터를 찾는 질의이다.

### 실험 결과

|종류|수행 시간(초)|
|---|---|
|`LEFT JOIN`|1.21|
|`IN ()`|2.96|
|`EXISTS`|4.67|

### LEFT JOIN

```sql
SELECT COUNT(*)
FROM orders t1 LEFT JOIN customer t2
  ON t1.o_custkey = t2.c_custkey
WHERE t2.c_custkey IS NULL;
```

### NOT IN

```sql
SELECT COUNT(*)
FROM orders
WHERE o_custkey NOT IN (
  SELECT c_custkey FROM customer
);
```

### NOT EXISTS

```sql
SELECT COUNT(*)
FROM orders
WHERE NOT EXISTS (
  SELECT 1 FROM customer
  WHERE c_custkey = orders.o_custkey
);
```

## INNER JOIN v.s. EXISTS

위의 실험 자료만 보면 INNER JOIN이 EXISTS보다 빠른 것으로 나오지만, 항상 그런 것은 아니다. EXISTS가 INNER JOIN보다 빠른 경우가 있는데 이것은 추후 살펴보기로 하겠다.

## 참고 자료

본인과 비슷한 실험을 한 블로그가 이미 존재한다. 다음의 블로그를 찾아서 보도록 하자.

- http://explainextended.com/2009/09/15/not-in-vs-not-exists-vs-left-join-is-null-sql-server/
- http://explainextended.com/2009/09/18/not-in-vs-not-exists-vs-left-join-is-null-mysql/
- http://explainextended.com/2010/05/27/left-join-is-null-vs-not-in-vs-not-exists-nullable-columns/
