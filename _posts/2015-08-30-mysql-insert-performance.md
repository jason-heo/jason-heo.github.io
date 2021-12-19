---
layout: post
title: "MySQL INSERT 방법별 성능 측정"
categories: MySQL
---

Slideshare에 처음으로 자료를 올려봤다. 요즘은 장황한 글 설명보다는 pt 형식으로 중요 항목만 빠르게 적고 읽는 게 좋아보이네...

http://www.slideshare.net/mysqlguru/mysql-insert-performance-test

결과 요약
---------

1. prepared statement는 성능 향상 효과가 적다.
 - 나의 테스트로는 10%
 - 그래도 SQL Injection을 가장 효과적으로 피하는 방법이니 사용하는 것은 좋음
1. 입수 시에는 트랜잭션을 켜고 입수하는 것이 좋다.
 - 트랜잭션 양이 너무 많다면 중간 중간 commit 해주는 것이 좋다.
 - 요걸 `commit-interval`이라고 부른다.
1. 뭐니뭐니해도 입수 시간이 제일 빠른 것은 `MULTI INSERT`이다.
 - 예: 다음과 같이 3개의 Record를 1개의 INSERT 구문으로 표현할 수 있다.
 - `INSERT INTO person (name, age) VALUES ('heo', 10), ('lee', 12), ('kim', 13)`

{% include mysql-reco.md %}
