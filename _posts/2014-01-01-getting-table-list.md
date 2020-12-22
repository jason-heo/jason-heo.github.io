---
layout: post
title: "MySQL 테이블 목록 구하기 (SHOW TABLES, information schema)"
date: 2014-03-05 
categories: mysql
---

테스트에 사용된 MySQL 버전: 8.0.22

하지만 MySQL 5.x 버전에서도 사용 가능하다.

### 목차

- [`SHOW TABLES`를 이용하는 방법](#show-tables를-이용하는-방법)
- [information schema를 이용하는 방법](#information-schema를-이용하는-방법)

### `SHOW TABLES`를 이용하는 방법

`SHOW TABLES`는 표준 SQL이 아니지만 사용하기 편하다는 장점이 있다.

- 현재 사용 중인 DB의 테이블 목록 조회하기
```sql
mysql> SHOW TABLES;
+-----------------+
| Tables_in_jsheo |
+-----------------+
| bbs_article     |
| bbs_list        |
| job             |
| person          |
| test            |
+-----------------+
5 rows in set (0.05 sec)
```
- 특정 테이블 패턴만 조회하기: `bbs`로 시작하는 테이블만 조회
```sql
mysql> SHOW TABLES LIKE 'bbs%';
+------------------------+
| Tables_in_jsheo (bbs%) |
+------------------------+
| bbs_article            |
| bbs_list               |
+------------------------+
2 rows in set (0.01 sec)
```
- 복잡한 조건을 아래처럼 지정할 수 있다
```sql
mysql> SHOW TABLES WHERE Tables_in_jsheo LIKE 'bbs%'
         AND Tables_in_jsheo != 'bbs_list';
+-----------------+
| Tables_in_jsheo |
+-----------------+
| bbs_article     |
+-----------------+
1 row in set (0.00 sec)
```
- 다른 DB의 테이블 목록 조회하기
```sql
mysql> SHOW TABLES FROM service_db;
+----------------------+
| Tables_in_service_db |
+----------------------+
| config               |
| file                 |
| user                 |
+----------------------+
3 rows in set (0.00 sec)
```

### information schema를 이용하는 방법

information schema 약간 어려워 보일 수 있지만 표준이기 때문에 익혀두면 다른 RDBMS를 사용할 때도 사용할 수 있다.

```sql
mysql> SELECT TABLE_NAME
       FROM information_schema.TABLES
       WHERE TABLE_SCHEMA = 'jsheo'
         AND TABLE_NAME LIKE 'bbs%'
         AND TABLE_NAME != 'bbs_list';
+-------------+
| TABLE_NAME  |
+-------------+
| bbs_article |
+-------------+
1 row in set (0.00 sec)
```

information_schema를 이용하면 MySQL에 존재하는 다양한 정보를 쉽게 얻을 수 있다. information_schema DB에는 많은 테이블이 존재하는데 그 중 TABLES 테이블은 MySQL에 존재하는 테이블에 대한 정보를 얻을 수 있는 테이블이다. TABLES 테이블에는 다음과 같은 컬럼이 존재한다.

아래 SQL 수행 결과는 결과는 MySQL 버전에 따라 약간씩 다를 수 있다.

```sql
mysql> DESC information_schema.TABLES;
+-----------------+--------------------------------------------------------------------+------+-----+---------+-------+
| Field           | Type                                                               | Null | Key | Default | Extra |
+-----------------+--------------------------------------------------------------------+------+-----+---------+-------+
| TABLE_CATALOG   | varchar(64)                                                        | YES  |     | NULL    |       |
| TABLE_SCHEMA    | varchar(64)                                                        | YES  |     | NULL    |       |
| TABLE_NAME      | varchar(64)                                                        | YES  |     | NULL    |       |
| TABLE_TYPE      | enum('BASE TABLE','VIEW','SYSTEM VIEW')                            | NO   |     | NULL    |       |
| ENGINE          | varchar(64)                                                        | YES  |     | NULL    |       |
| VERSION         | int                                                                | YES  |     | NULL    |       |
| ROW_FORMAT      | enum('Fixed','Dynamic','Compressed','Redundant','Compact','Paged') | YES  |     | NULL    |       |
| TABLE_ROWS      | bigint unsigned                                                    | YES  |     | NULL    |       |
| AVG_ROW_LENGTH  | bigint unsigned                                                    | YES  |     | NULL    |       |
| DATA_LENGTH     | bigint unsigned                                                    | YES  |     | NULL    |       |
| MAX_DATA_LENGTH | bigint unsigned                                                    | YES  |     | NULL    |       |
| INDEX_LENGTH    | bigint unsigned                                                    | YES  |     | NULL    |       |
| DATA_FREE       | bigint unsigned                                                    | YES  |     | NULL    |       |
| AUTO_INCREMENT  | bigint unsigned                                                    | YES  |     | NULL    |       |
| CREATE_TIME     | timestamp                                                          | NO   |     | NULL    |       |
| UPDATE_TIME     | datetime                                                           | YES  |     | NULL    |       |
| CHECK_TIME      | datetime                                                           | YES  |     | NULL    |       |
| TABLE_COLLATION | varchar(64)                                                        | YES  |     | NULL    |       |
| CHECKSUM        | bigint                                                             | YES  |     | NULL    |       |
| CREATE_OPTIONS  | varchar(256)                                                       | YES  |     | NULL    |       |
| TABLE_COMMENT   | text                                                               | YES  |     | NULL    |       |
+-----------------+--------------------------------------------------------------------+------+-----+---------+-------+
21 rows in set (0.00 sec)
```

컬럼 이름을 보면 대략적인 의미는 파악할 수 있을 것이라 생각된다. 언뜻보면 테이블이 속한 DB name에 대한 컬럼이 없어 보이는데, `TABLE_SCHEMA`가 DB name을 저장하는 컬럼이다.

information schema에는 다음과 같은 테이블들이 있다. (MySQL 8.0.22 기준)

```sql
mysql> SHOW TABLES FROM information_schema;
+---------------------------------------+
| Tables_in_information_schema          |
+---------------------------------------+
| ADMINISTRABLE_ROLE_AUTHORIZATIONS     |
| APPLICABLE_ROLES                      |
| CHARACTER_SETS                        |
| CHECK_CONSTRAINTS                     |
| COLLATION_CHARACTER_SET_APPLICABILITY |
| COLLATIONS                            |
| COLUMN_PRIVILEGES                     |
| COLUMN_STATISTICS                     |
| COLUMNS                               |
| COLUMNS_EXTENSIONS                    |
| ENABLED_ROLES                         |
| ENGINES                               |
| EVENTS                                |
| FILES                                 |
| INNODB_BUFFER_PAGE                    |
| INNODB_BUFFER_PAGE_LRU                |
| INNODB_BUFFER_POOL_STATS              |
| INNODB_CACHED_INDEXES                 |
| INNODB_CMP                            |
| INNODB_CMP_PER_INDEX                  |
| INNODB_CMP_PER_INDEX_RESET            |
| INNODB_CMP_RESET                      |
| INNODB_CMPMEM                         |
| INNODB_CMPMEM_RESET                   |
| INNODB_COLUMNS                        |
| INNODB_DATAFILES                      |
| INNODB_FIELDS                         |
| INNODB_FOREIGN                        |
| INNODB_FOREIGN_COLS                   |
| INNODB_FT_BEING_DELETED               |
| INNODB_FT_CONFIG                      |
| INNODB_FT_DEFAULT_STOPWORD            |
| INNODB_FT_DELETED                     |
| INNODB_FT_INDEX_CACHE                 |
| INNODB_FT_INDEX_TABLE                 |
| INNODB_INDEXES                        |
| INNODB_METRICS                        |
| INNODB_SESSION_TEMP_TABLESPACES       |
| INNODB_TABLES                         |
| INNODB_TABLESPACES                    |
| INNODB_TABLESPACES_BRIEF              |
| INNODB_TABLESTATS                     |
| INNODB_TEMP_TABLE_INFO                |
| INNODB_TRX                            |
| INNODB_VIRTUAL                        |
| KEY_COLUMN_USAGE                      |
| KEYWORDS                              |
| OPTIMIZER_TRACE                       |
| PARAMETERS                            |
| PARTITIONS                            |
| PLUGINS                               |
| PROCESSLIST                           |
| PROFILING                             |
| REFERENTIAL_CONSTRAINTS               |
| RESOURCE_GROUPS                       |
| ROLE_COLUMN_GRANTS                    |
| ROLE_ROUTINE_GRANTS                   |
| ROLE_TABLE_GRANTS                     |
| ROUTINES                              |
| SCHEMA_PRIVILEGES                     |
| SCHEMATA                              |
| SCHEMATA_EXTENSIONS                   |
| ST_GEOMETRY_COLUMNS                   |
| ST_SPATIAL_REFERENCE_SYSTEMS          |
| ST_UNITS_OF_MEASURE                   |
| STATISTICS                            |
| TABLE_CONSTRAINTS                     |
| TABLE_CONSTRAINTS_EXTENSIONS          |
| TABLE_PRIVILEGES                      |
| TABLES                                |
| TABLES_EXTENSIONS                     |
| TABLESPACES                           |
| TABLESPACES_EXTENSIONS                |
| TRIGGERS                              |
| USER_ATTRIBUTES                       |
| USER_PRIVILEGES                       |
| VIEW_ROUTINE_USAGE                    |
| VIEW_TABLE_USAGE                      |
| VIEWS                                 |
+---------------------------------------+
79 rows in set (0.00 sec)
```
