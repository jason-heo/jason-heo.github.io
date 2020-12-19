---
layout: post
title: "MySQL Foreign key 사용 시 주의 사항 (Can't create table (errno: 150))"
date: 2014-03-05 
categories: mysql
---

MySQL에서 FOREIGN KEY를 설정하려면 몇 가지 규칙을 알고 있어야 한다. FOREIGN KEY 생성을 할 때 이런 조건을 만족하지 못하는 경우 errno: 150 만을 출력하기 때문에 어디가 문제인지 한눈에 알기 어렵다.

우선 정상적인 CASE부터 살펴보자.

## 목차

- [1. 정상적인 FOREIGN KEY 설정 예](#1-정상적인-foreign-key-설정-예)
- [2 FOREIGN KEY 를 설정하기 위한 조건](#2-foreign-key-를-설정하기-위한-조건)
- [3. 잘못된 예제들](#3-잘못된-예제들)
  - [3-1) 문자셋을 잘못 지정한 예](#3-1-문자셋을-잘못-지정한-예)
  - [3-2) ON DELETE SET NULL 지정 시 child.parent_id를 NOT NULL로 지정한 경우](#3-2-on-delete-set-null-지정-시-childparent_id를-not-null로-지정한-경우)
  - [3-3) Data Type이 다른 경우 1](#3-3-data-type이-다른-경우-1)
  - [3-4) Data Type이 다른 경우 2](#3-4-data-type이-다른-경우-2)

## 1. 정상적인 FOREIGN KEY 설정 예

예를 들기 위해 parent, child 테이블이 있다고 하겠다. 다음과 같은 테이블 구조가 FOREIGN KEY를 달기 위한 올바른 구조이다.

```sql
DROP TABLE IF EXISTS child;
DROP TABLE IF EXISTS parent;

CREATE TABLE parent
(
  id INT NOT NULL,
  PRIMARY KEY(id)
) ENGINE = InnoDB;

CREATE TABLE child
(
  id INT NOT NULL,
  parent_id INT NOT NULL,
  PRIMARY KEY(id),
  INDEX(parent_id),
  FOREIGN KEY(parent_id) REFERENCES parent(id)
) ENGINE = InnoDB;
```

## 2 FOREIGN KEY 를 설정하기 위한 조건

parent, child 테이블 및 각 컬럼은 다음의 조건을 만족해야 한다.

- parent, child 테이블은 모두 InnoDB여야 한다.
- parent.id, child.parent_id는 동일한 컬럼 타입이어야 한다.
- parent.id는 PRIMARY KEY여야 한다.
- child.parent_id는 INDEX 여야 한다.
- parent.id가 문자열인 경우 character set이 동일해야 한다.
- ON DELETE SET NULL인 경우 child.parent_id는 NOT NULL이면 안 된다.
 
## 3. 잘못된 예제들

### 3-1) 문자셋을 잘못 지정한 예

다음을 보고 무엇이 잘못되었는지 알 수 있겠는가?

```sql
DROP TABLE IF EXISTS child;
DROP TABLE IF EXISTS parent;

CREATE TABLE parent
(
  id VARCHAR(10) NOT NULL,
  PRIMARY KEY(id)
) ENGINE = InnoDB character set=utf8;

CREATE TABLE child
(
  id INT NOT NULL,
  parent_id VARCHAR(10) NOT NULL,
  PRIMARY KEY(id),
  INDEX(parent_id),
  FOREIGN KEY(parent_id) REFERENCES parent(id)
) ENGINE = InnoDB character set=latin1;
```

CHARACTER SET을 잘못 지정했으므로 child 테이블 생성은 실패할 것이다.

### 3-2) ON DELETE SET NULL 지정 시 child.parent_id를 NOT NULL로 지정한 경우

FOREIGN KEY 에러 중 대부분은 쉽게 발견할 수 있지만, 아래 문제는 원인을 발견하기는 약간 어렵다.

```sql
DROP TABLE IF EXISTS child;
DROP TABLE IF EXISTS parent;

CREATE TABLE parent
(
  id VARCHAR(10) NOT NULL,
  PRIMARY KEY(id)
) ENGINE = InnoDB;

CREATE TABLE child
(
  id VARCHAR(10) NOT NULL,
  parent_id VARCHAR(10) NOT NULL,
  PRIMARY KEY(id),
  INDEX(parent_id),
  FOREIGN KEY(parent_id) REFERENCES parent(id) ON DELETE SET NULL
) ENGINE = InnoDB;
```

`child.parent_id`는 NOT NULL로 선언되어있다. 그런데 FOREIGN KEY 설정 시 ON DELETE SET NULL을 지정했는데 이는 컬럼 설정 시 NOT NULL에 위배되므로 FOREIGN KEY를 만들 수 없다.

### 3-3) Data Type이 다른 경우 1

parent.id가 INT이면 child.parent_id 컬럼도 INT형이어야 한다. BIGINT거나 SMALLINT이어도 안 된다. parent.id가 CHAR(10)인데 child.parent_id가 VARCHAR(10)이면 안 된다. 다음의 예를 보자.

```sql
DROP TABLE IF EXISTS child;
DROP TABLE IF EXISTS parent;

CREATE TABLE parent
(
  id CHAR(10) NOT NULL,
  PRIMARY KEY(id)
) ENGINE = InnoDB;

CREATE TABLE child
(
  id INT NOT NULL,
  parent_id VARCHAR(10) NOT NULL,
  PRIMARY KEY(id),
  INDEX(parent_id),
  FOREIGN KEY(parent_id) REFERENCES parent(id)
) ENGINE = InnoDB;
```

### 3-4) Data Type이 다른 경우 2

숫자형인 경우 SIGNED, UNSIGNED도 동일해야 한다. 다음은 이 규칙을 벗어 나기 때문에 FOREIGN KEY 설정을 할 수 없다.

```sql
DROP TABLE IF EXISTS child;
DROP TABLE IF EXISTS parent;

CREATE TABLE parent
(
  id INT NOT NULL,
  PRIMARY KEY(id)
) ENGINE = InnoDB;

CREATE TABLE child
(
  id INT NOT NULL,
  parent_id INT UNSIGNED NOT NULL,
  PRIMARY KEY(id),
  INDEX(parent_id),
  FOREIGN KEY(parent_id) REFERENCES parent(id)
) ENGINE = InnoDB;
```
