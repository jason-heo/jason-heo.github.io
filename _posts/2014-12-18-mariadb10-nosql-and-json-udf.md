---
layout: post
title: "MariaDB 10의 NoSQL 기능과 MySQL의 Json 관련 UDF"
categories: MySQL
---

MariaDB 10에서는 Dynamic Column이라는 이름으로 NoSQL적인 기능을 제공하고 있다. 개념 자체는 간단한데 BLOB 컬럼에 JSON을 저장한 뒤, JSON에서 특정 컬럼의 값을 조회한다던가 특정 컬럼을 추가/삭제하는 기능을 제공한다.

간단한 예는 [MariaDB Blog][1]와 [Manual][2]에서 볼 수 있다. 과거에 조사할 때는 Index 를 걸 수는 없는 것으로 기억한다. (지금도 그런 듯 하다)

기능 자체는 간단하고 Index scan도 사용할 수 없지만, 특정 용도에서는 유용하게 사용될 수 있을 것 같다.

## MariaDB Dynamic Column 사용법

간단한 사용 방법을 보자. 아래 예제는 `mysql>` 이라고 보이지만, client만 mysql일 뿐 실제는 MariaDB 10에서 실행한 결과이다.

### 테이블 생성 및 데이터 입력

```sql
mysql> CREATE TABLE person(id INT, dynamic_cols BLOB);
Query OK, 0 rows affected (0.00 sec)

mysql> INSERT INTO person VALUES (1, COLUMN_CREATE('name', 'heo', 'age', 11));
Query OK, 1 row affected (0.01 sec)

mysql> INSERT INTO person VALUES (2, COLUMN_CREATE('name', 'kim', 'age', 22));
Query OK, 1 row affected (0.00 sec)
```

컬럼명은 아무것이나 해도 상관없다.

### 데이터 조회

```sql
mysql> SELECT id, dynamic_cols FROM person;
+------+---------------------------+
| id   | dynamic_cols              |
+------+---------------------------+
|    1 |         agename!heo |
|    2 |         agename,!kim |
+------+---------------------------+
2 rows in set (0.00 sec)
```

데이터가 깨져보이는데 이는 Dynamic Column의 자료가 binary 형태로 저장되기 때문이다. 그래서 컬럼 타입을 `BLOB`으로 해야 한다.

다음과 같이 `COLUMN_JSON()` 함수를 이용하여 JSON 형식으로 볼 수 있다.

```
mysql> SELECT id, COLUMN_JSON(dynamic_cols) FROM person;
+------+---------------------------+
| id   | COLUMN_JSON(dynamic_cols) |
+------+---------------------------+
|    1 | {"age":11,"name":"heo"}   |
|    2 | {"age":22,"name":"kim"}   |
+------+---------------------------+
2 rows in set (0.00 sec)
```

JSON의 특정 key 값만 조회하고 싶은 경우 `COLUMN_GET()` 함수를 이용하면 된다.

```sql
mysql> SELECT id, COLUMN_GET(dynamic_cols, 'name' AS CHAR) FROM person;
+------+------------------------------------------+
| id   | COLUMN_GET(dynamic_cols, 'name' AS CHAR) |
+------+------------------------------------------+
|    1 | heo                                      |
|    2 | kim                                      |
+------+------------------------------------------+
2 rows in set (0.00 sec)
```

### 컬럼 추가

기존 값에 원하는 key를 축할 수 있다. `COLUMN_ADD()` 함수를 이용하면 된다.

```sql
mysql> UPDATE person SET dynamic_cols = COLUMN_ADD(dynamic_cols, 'gender', 'M') WHERE id = 1;
Query OK, 1 row affected (0.00 sec)
Rows matched: 1  Changed: 1  Warnings: 0

mysql> SELECT id, COLUMN_JSON(dynamic_cols) FROM person;
+------+--------------------------------------+
| id   | COLUMN_JSON(dynamic_cols)            |
+------+--------------------------------------+
|    1 | {"age":11,"name":"heo","gender":"M"} |
|    2 | {"age":22,"name":"kim"}              |
+------+--------------------------------------+
2 rows in set (0.00 sec)
```

## MySQL에서의 유사 기능

이 정도의 기능은 MySQL의 UDF(User Defined Function)으로 구현할 수 있으며 이미 구현되어 있다.

자세한 것은 [여기][3]를 참고해보세요~

[1]: https://mariadb.com/blog/sql-or-nosql-both-mariadb-10
[2]: https://mariadb.com/kb/en/mariadb/documentation/nosql/dynamic-columns/
[3]: http://blog.ulf-wendel.de/2013/mysql-5-7-sql-functions-for-json-udf/
