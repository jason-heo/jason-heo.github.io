---
layout: post
title: "JOIN에서 중복된 레코드 제거하기"
date: 2014-03-05 
categories: mysql
---

test에 사용된 MySQL 버전: 8.0

(일반적인 방법이기 때문에 5.x에서도 적용된다)

## 목차

- [1. 개요](#1-개요)
- [2. 예제 데이터](#2-예제-데이터)
- [3. 문제 상황](#3-문제-상황)
- [4. 중복 찾기](#4-중복-찾기)
  - [4-1) 방법 1: SELECT DISTINCT를 사용하는 방법](#4-1-방법-1-select-distinct를-사용하는-방법)
  - [4-2) 참고: DISTINCT에 대한 이해](#4-2-참고-distinct에-대한-이해)
  - [4-3) 방법 2: JOIN 전에 중복을 제거하기](#4-3-방법-2-join-전에-중복을-제거하기)

## 1. 개요

1:n 관계의 테이블을 JOIN하면 동일한 결과의 레코드가 n개 출력된다.

중복을 제거하는 방법은 아래와 같이 두 가지 방법이 있다.

1. 최종 SELECT 결과에서 `SELECT DISTINCT`를 이용하는 방법
    - 장점: 간단한다
    - 단점: 속도가 느리다
1. JOIN 전에 중복을 미리 제거하는 방법
    - 장점: 속도가 빠르다
    - 단점: 복잡하다

## 2. 예제 데이터

아래와 같은 테이블 두 개를 이용하였다. `job` 테이블에 중복된 레코드가 없어야 정상이겠지만 예를 위해 중복 레코드를 입력하였다.

- `person` 테이블
    ```sql
    CREATE TABLE person
    (
        id INT NOT NULL AUTO_INCREMENT,
        name VARCHAR(255),
        PRIMARY KEY(id)
    );

    INSERT INTO person (name) VALUES ('Kim');
    INSERT INTO person (name) VALUES ('Heo');
    ```
- `job` 테이블
    ```sql
    CREATE TABLE job
    (
        id INT NOT NULL AUTO_INCREMENT,
        person_name VARCHAR(255),
        job_name VARCHAR(255),
        PRIMARY KEY(id)
    );

    INSERT INTO job (person_name, job_name) VALUES ('Kim', 'Programmer');
    INSERT INTO job (person_name, job_name) VALUES ('Kim', 'Programmer');

    INSERT INTO job (person_name, job_name) VALUES ('Heo', 'Can Opener');
    INSERT INTO job (person_name, job_name) VALUES ('Heo', 'Can Opener');
    INSERT INTO job (person_name, job_name) VALUES ('Heo', 'Can Opener');
    ```

## 3. 문제 상황

아래 JOIN 결과를 보면 중복된 레코드가 출력된 것을 볼 수 있다.

```sql
SELECT
    person.id, person.name, job.job_name
FROM person INNER JOIN job
    ON person.name = job.person_name;
+----+------+------------+
| id | name | job_name   |
+----+------+------------+
|  1 | Kim  | Programmer |
|  1 | Kim  | Programmer | <= 중복 레코드
|  2 | Heo  | Can Opener |
|  2 | Heo  | Can Opener | <= 중복 레코드
|  2 | Heo  | Can Opener | <= 중복 레코드
+----+------+------------+
5 rows in set (0.00 sec)

```

## 4. 중복 찾기
이런 중복을 어떻게 제거할 수 있을까?

### 4-1) 방법 1: SELECT DISTINCT를 사용하는 방법

개요에서 설명한 것처럼 `SELECT DISTINCT`를 사용하면 중복을 매우 쉽게 제거할 수 있다.

```sql
SELECT DISTINCT -- DISTINCT를 추가
    person.id, person.name, job.job_name
FROM person INNER JOIN job
    ON person.name = job.person_name;

+----+------+------------+
| id | name | job_name   |
+----+------+------------+
|  1 | Kim  | Programmer |
|  2 | Heo  | Can Opener |
+----+------+------------+
2 rows in set (0.00 sec)
```

`SELECT`에 `DISTINCT`만 추가했는데 중복이 제거되었다.

매우 쉬운 방법이지만 레코드 수가 많은 경우 성능이 느리다는 단점이 있다.

### 4-2) 참고: DISTINCT에 대한 이해

간혹 `DISTINCT`를 함수처럼 사용하는 걸 볼 수 있다.

```sql
SELECT DISTINCT(job_name)
FROM job;

+------------+
| job_name   |
+------------+
| Programmer |
| Can Opener |
+------------+
2 rows in set (0.00 sec)
```

그런데 두 개 이상의 필드를 `DISTINCT()` 안에 넣으면 에러가 발생한다.

```sql
SELECT DISTINCT(person_name, job_name)
FROM job;

ERROR 1241 (21000): Operand should contain 1 column(s)
```

`DISTINCT`를 함수라고 생각하지 않고 `SELECT DISTINCT`로 생각하는 것이 필드 개수와 상관없이 사용할 수 있으므로 편하다.

```sql
SELECT DISTINCT person_name, job_name
FROM job;

+-------------+------------+
| person_name | job_name   |
+-------------+------------+
| Kim         | Programmer |
| Heo         | Can Opener |
+-------------+------------+
2 rows in set (0.00 sec)
```

{% include adsense-content.md %}

### 4-3) 방법 2: JOIN 전에 중복을 제거하기

앞서 말한 것처럼 `SELECT DISTINCT`는 간단하지만 성능이 느리다.

성능을 위해서는 JOIN 전에 중복을 제거하는 것이 좋다. (물론 더 좋은 것은 중복이 없도록 테이블 설계를 잘 하는 것이다)

'JOIN 전에 중복 제거'는 다음과 같이 inline view를 사용하면 아주 어렵지 않다.

```sql
SELECT person.id, person.name, job.job_name
FROM person INNER JOIN (
    -- 중복 제거를 위한 inline view
    SELECT DISTINCT person_name, job_name
    FROM job
) AS job ON person.name = job.person_name;

+----+------+------------+
| id | name | job_name   |
+----+------+------------+
|  1 | Kim  | Programmer |
|  2 | Heo  | Can Opener |
+----+------+------------+
2 rows in set (0.00 sec)
```

언뜻보면 복잡해보일 수 있있고 복잡하니깐 성능이 더 느려보일 수 있는데 일반적으로 레코드 수가 많은 경우 JOIN 전에 중복을 제거해서 1:1 JOIN으로 바꾸는 것이 훨씬 빠르다.

물론 WHERE 절에 조건이 존재 여부, index 존재 여부, 한쪽 테이블의 크기가 엄청 적은 경우 등등 많은 시나리오가 있으니 성능 비교를 하는 것이 좋겠다.

{% include mysql-reco.md %}
