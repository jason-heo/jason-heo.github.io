---
layout: post
title: "MySQL 중복 레코드 관리 방법 (INSERT 시 중복 키 관리 방법 (INSERT IGNORE, REPLACE INTO, ON DUPLICATE UPDATE))"
date: 2014-03-05
categories: mysql
---

Test에 사용된 MySQL 버전

- MySQL 5.5
- MySQL 8.0

### 1. 개요

MySQL에는 아래 3가지 방법을 이용하여 중복 레코드를 관리할 수 있다.

1. `INSERT IGNORE ...`
1. `REPLACE INTO ...`
1. `INSERT INTO ... ON DUPLICATE UPDATE`

각 방법의 특징을 요약하면 다음과 같다.

|방법|특징|
|---|---|
|`INSERT IGNORE ...`|최초 입수된 레코드가 남아 있음<BR>최초 입수된 레코드의 AUTO_INCREMENT 값은 변하지 않음|
|`REPLACE INTO ...`|최초 입수된 레코드가 삭제되고, 신규 레코드가 INSERT됨<BR>AUTO_INCREMENT의 값이 변경됨|
|`INSERT INTO ... ON DUPLICATE UPDATE`|INSERT IGNORE의 장점 포함함<BR>중복 키 오류 발생 시, 사용자가 UPDATE될 값을 지정할 수 있음|

### 2. 사전 조건

중복 레코드 관리를 위해선 테이블에 `PRIMARY KEY` 혹은 `UNIQUE INDEX`가 필요하다.

본 예제에서는 아래와 같은 `person` 테이블을 이용하여 설명한다.

```sql
CREATE TABLE person
(
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(20),
  address VARCHAR(255),
  PRIMARY KEY (id),
  UNIQUE INDEX (name) -- 중복 검사용 필드
);
```

`person` 테이블에서 PRIMARY KEY로 지정된 `id` 필드는 AUTO_INCREMENT를 위해 만든 필드이며, 본 문서에는 `name` 필드를 이용하여 중복을 검사한다.

만약 기존에 만들어진 테이블에 PRIMARY KEY 혹은 UNIQUE INDEX를 추가하려면 아래의 SQL을 이용하면 된다.

```sql
-- PRIMARY 추가하는 방법
ALTER TABLE person ADD PRIMARY KEY (name)

-- UNIQUE INDEX를 추가하는 방법
ALTER TABLE person ADD UNIQUE INDEX (name)
```


### INSERT IGNORE

`INSERT IGNORE`는 중복 키 에러가 발생했을 때 신규로 입력되는 레코드를 무시하는 단순한 방법이다.

다음의 예를 보면 중복 키 에러가 발생했을 때 INSERT 구문 자체는 오류가 발생하지 않고, 대신'0 row affected'가 출력된 것을 볼 수 있다.

```sql
mysql> INSERT IGNORE INTO person VALUES (NULL, 'James', 'Seoul');
Query OK, 1 row affected (0.00 sec)

mysql> INSERT IGNORE INTO person VALUES (NULL, 'Cynthia', 'Yongin');
Query OK, 1 row affected (0.00 sec)

mysql> INSERT IGNORE INTO person VALUES (NULL, 'James', 'Seongnam');
Query OK, 0 rows affected (0.00 sec)

mysql> INSERT IGNORE INTO person VALUES (NULL, 'Cynthia', 'Seoul');
Query OK, 0 rows affected (0.00 sec)

mysql> INSERT IGNORE INTO person VALUES (NULL, 'James', 'Incheon');
Query OK, 0 rows affected (0.00 sec)
```

SELECT의 결과는 2건만 존재한다.

```sql
mysql> SELECT * FROM person;
+----+---------+---------+
| id | name    | address |
+----+---------+---------+
|  1 | James   | Seoul   |
|  2 | Cynthia | Yongin  |
+----+---------+---------+
2 rows in set (0.00 sec)
```

`James`의 주소가 최초 입력한 `Seoul`이다. 즉, 최초에 입력된 레코드가 남아 있는 걸을 볼 수 있다.

또한 AUTO_INCREMENT 컬럼의 값이 1, 2인 것에 주목하라.

MySQL에서 AUTO_INCREMENT는 식별키 용도로 많이 사용하는데, 중복 발생 여부에 따라 식별 키가 변경되는 경우 여러 가지 불편한 점이 생긴다.

`INSERT IGNORE`에서는 AUTO_INCREMENT의 값이 변경되지 않는다는 장점이 있다.

{% include adsense-content.md %}

### REPLACE INTO

`REPLACE INTO`는 중복이 발생되었을 때 기존 레코드를 삭제하고 신규 레코드를 INSERT하는 방식이다.

`person` 테이블을 drop 후 다시 생성한 뒤에 아래의 레코드를 입수해보자.

```sql
mysql> REPLACE INTO person VALUES (NULL, 'James', 'Seoul');
Query OK, 1 row affected (0.00 sec)

mysql> REPLACE INTO person VALUES (NULL, 'Cynthia', 'Yongin');
Query OK, 1 row affected (0.00 sec)

mysql> REPLACE INTO person VALUES (NULL, 'James', 'Seongnam');
Query OK, 2 rows affected (0.00 sec)

mysql> REPLACE INTO person VALUES (NULL, 'Cynthia', 'Seoul');
Query OK, 2 rows affected (0.00 sec)

mysql> REPLACE INTO person VALUES (NULL, 'James', 'Incheon');
Query OK, 2 rows affected (0.00 sec)
```

세번째 레코드를 입수할 때부터는 '2 rows affected'가 출력되었다. (`INSERT IGNORE`에서는 '0 rows affected'가 출력되었었다)

```sql
mysql> SELECT * FROM person;
+----+---------+---------+
| id | name    | address |
+----+---------+---------+
|  4 | Cynthia | Seoul   |
|  5 | James   | Incheon |
+----+---------+---------+
2 rows in set (0.00 sec)
```

id가 4, 5로 변하였다. 또한 James의 주소가 "Incheon"으로 변경되었다.

이를 '2 rows affected'와 함께 종합적으로 판단한다면 "REPLACE INTO"는 다음과 같이 작동하는 것을 알 수 있다.

- 중복 키 오류 발생 시 기존 레코드를 삭제 -> 첫 번째 레코드가 affected되었음
- 이후 새로운 레코드를 입력 -> 두 번째 레코드가 affected되었음

그래서 '2 rows affected'가 출력되었다.

새로운 레코드가 입력되면서 AUTO_INCREMENT 컬럼의 값이 매번 새롭게 발급되었다.

"REPLACE INTO"는 그다지 좋은 방법이 아닌데 앞서 이야기한 것처럼 AUTO_INCREMENT는 레코드를 식별할 수 있는 id로 사용되는데 중복 발생 시 id가 변경되기 때문이다.

{% include adsense-content.md %}

### ON DUPLICATE UPDATE

`ON DUPLICATE UPDATE`의 장점은 중복 키 오류 발생 시 사용자가 원하는 값을 직접 설정할 수 있다는 점이다.

우선 기본적인 사용 방법을 보자.

마찬가지로 테이블을 drop후 새로 생성했다.

```sql
mysql> INSERT INTO person VALUES (NULL, 'James', 'Seoul')
           ON DUPLICATE KEY UPDATE address = VALUES(address);
Query OK, 1 row affected (0.00 sec)

mysql> INSERT INTO person VALUES (NULL, 'Cynthia', 'Yongin')
           ON DUPLICATE KEY UPDATE address = VALUES(address);
Query OK, 1 row affected (0.00 sec)

mysql> INSERT INTO person VALUES (NULL, 'James', 'Seongnam')
           ON DUPLICATE KEY UPDATE address = VALUES(address);
Query OK, 2 rows affected, 1 warning (0.01 sec)

mysql> INSERT INTO person VALUES (NULL, 'Cynthia', 'Seoul')
           ON DUPLICATE KEY UPDATE address = VALUES(address);
Query OK, 2 rows affected, 1 warning (0.00 sec)

mysql> INSERT INTO person VALUES (NULL, 'James', 'Incheon')
           ON DUPLICATE KEY UPDATE address = VALUES(address);
Query OK, 2 rows affected, 1 warning (0.01 sec)
```

('2 rows affected'의 의미는 아래 내용을 읽고 각자 생각해보자)

SELECT 결과를 보자.

```sql
mysql> SELECT * FROM person;
+----+---------+---------+
| id | name    | address |
+----+---------+---------+
|  1 | James   | Incheon |
|  2 | Cynthia | Seoul   |
+----+---------+---------+
2 rows in set (0.00 sec)
```

이번에는 `id` 값이 최초 입수된 레코드의 값 그대로이다. 하지만 `address`의 값이 마지막에 입수한 레코드로 변경되어 있다.

`INSERT INTO ... ON DUPLICATE KEY UPDATE`의 장점은 중복 발생 시 필드의 값을 내 맘대로 UPDATE할 수 있다는 점이다.

`id` 필드만 놓고 보면 `INSERT IGNORE`와 동일하지만 `address`의 값이 변경된 것이 `INSERT IGNORE`와 `INSERT INTO ... ON DUPLICATE KEY UPDATE`의 차이점이다.

`ON DUPLICATE KEY UPDATE`를 이용하면 재미있는 것들을 할 수 있다. 중복 레코드가 총 몇 번이나 입수되었는지를 기록해보자.

이를 위해 `inserted_cnt` 필드를 추가하였다.

```sql
CREATE TABLE person
(
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(20),
  address VARCHAR(255),
  inserted_cnt INT, -- 레코드가 몇 번 입수되었는지 확인용 필드
  PRIMARY KEY (id),
  UNIQUE INDEX (name)
);
```

```sql
mysql> INSERT INTO person VALUES (NULL, 'James', 'Seoul', 1)
           ON DUPLICATE KEY UPDATE inserted_cnt = inserted_cnt + 1;
Query OK, 1 row affected (0.00 sec)

mysql> INSERT INTO person VALUES (NULL, 'Cynthia', 'Yongin', 1)
           ON DUPLICATE KEY UPDATE inserted_cnt = inserted_cnt + 1;
Query OK, 1 row affected (0.00 sec)

mysql> INSERT INTO person VALUES (NULL, 'James', 'Seongnam', 1)
           ON DUPLICATE KEY UPDATE inserted_cnt = inserted_cnt + 1;
Query OK, 2 rows affected (0.00 sec)

mysql> INSERT INTO person VALUES (NULL, 'Cynthia', 'Seoul', 1)
           ON DUPLICATE KEY UPDATE inserted_cnt = inserted_cnt + 1;
Query OK, 2 rows affected (0.00 sec)

mysql> INSERT INTO person VALUES (NULL, 'James', 'Incheon', 1)
           ON DUPLICATE KEY UPDATE inserted_cnt = inserted_cnt + 1;
Query OK, 2 rows affected (0.00 sec)
```

SELECT를 해 보면 `inserted_cnt`에는 해당 중복 값이 몇 번 INSERT 시도가 되었는지 기록되어 있을 것이다.

```sql
mysql> SELECT * FROM person;
+----+---------+---------+--------------+
| id | name    | address | inserted_cnt |
+----+---------+---------+--------------+
|  1 | James   | Seoul   |            3 |
|  2 | Cynthia | Yongin  |            2 |
+----+---------+---------+--------------+
2 rows in set (0.00 sec)
```

`inserted_cnt` 필드의 값을 보면 알겠지만, 레코드가 몇 번 입수되었는지 저장되어 있다.

주의해야 할 점은 새로온 레코드의 값으로 UPDATE하고자 할 때는 항상 "VALUES(column)"과 같이 `VALUES()로 감싸야 한다는 점이다. 만약 다음과 같이 `VALUES()` 없이 필드명만 사용하는 것은 기존 레코드의 컬럼 값을 의미하게 된다.

```sql
INSERT INTO person VALUES (NULL, 'James', 'Incheon')
    ON DUPLICATE KEY UPDATE address = address;
```

따라서 `address`의 값이 UPDATE되지 않는다.
