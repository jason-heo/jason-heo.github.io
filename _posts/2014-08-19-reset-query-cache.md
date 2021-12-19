---
layout: post
title: "MySQL Query Cache를 빠르게 비우기"
categories: mysql
---

## 들어가며...

MySQL Query Cache는 동시성이 약하다. Query Cache 영역은 Lock이 필요할 때 Global Lock을 사용되며 Lock이 걸린 상태에서 다른 Thread들은 대기를 하는 수 밖에 없다.

이러한 문제로 인해 MySQL Query Cache 크기 ([query_cache_size][1])를 크게 늘리기 어렵다.

## Query Cache 클 때의 단점

### 동시 수행 Thread가 많을 때 Lock 대기 시간이 많다

Global Lock 문제로 1개 Thread가 Lock을 점유한 경우 다른 모든 Thread는 대기를 하는 수 밖에 없다. Lock 시간은 query_cache_size가 큰 경우 더 오래 걸린다. 
이에 대한 자세한 실험과 극복 방법은 [예전에 올린 포스트][2]에서 볼 수 있으며, 이를 극복하는 방법도 설명되어 있다.
본인은 이런 방법을 이용하여 query_cache_size를 1GB로 사용 중이다.

### Query Cache를 비우는데 시간이 오래 걸림

Global Lock의 문제는 앞의 포스트를 이용하여 어느 정도 극복을 했는데 가끔 `RESET QUERY CACHE`를 수행할 때 1분 이상 소요될 경우가 있다.

```sql
mysql> RESET QUERY CACHE;
Query OK, 0 rows affected (1 min 30.55 sec)
```

`RESET QUERY CACHE`는 자주 하는 일은 아니고 서비스 중에 Query Cache를 비울 일음 많지 않으니 별 문제는 아닐 수 있다.

### Replication이 늦어질 수 있다.

이 부분이 좀 큰 문제이다. Replication에 의해 변경되는 테이블과 관련된 Query를 Query Cache에서 Invalidation하는데 시간이 많이 걸린다. 본인이 실험한 결과 1GB의 Query Cache를 사용하는 경우 Replication될 binlog가 1MB 밖에 안 되더라도 Invalidation하는데만 5분이 걸렸다.

## Query Cache를 빠르게 Flush하는 Tip!

`query_cache_size`를 크게 사용 중인데 `RESET QUERY CACHE`가 느리거나 Replication이 알 수 없이 느릴 때 `query_cache_size`를 재설정하면 아주 빠르게 Query Cache를 비울 수 있다.

이는 `DELETE TABLE` 보다 `TRUNCATE TABLE`이 빠른 것과 비슷한 원리이다.

우선 다음 결과를 보자. 총 1GB 중에서 약 170MB를 사용 중이다.

```sql
mysql> SHOW GLOBAL STATUS LIKE 'Qc%';
+-------------------------+-----------+
| Variable_name           | Value     |
+-------------------------+-----------+
| Qcache_free_blocks      | 1         |
| Qcache_free_memory      | 833819048 |
| Qcache_hits             | 79520264  |
| Qcache_inserts          | 7882323   |
| Qcache_lowmem_prunes    | 1201502   |
| Qcache_not_cached       | 7446228   |
| Qcache_queries_in_cache | 133904    |
| Qcache_total_blocks     | 267820    |
+-------------------------+-----------+
8 rows in set (0.00 sec)
```

Query Cache 내용을 Flush하기 위해서 다음과 같이 `query_cache_size`를 재설정 한다. (`SUPER` 권한이 있어야 한다.)

```sql
mysql> SET GLOBAL query_cache_size = 1024 * 1024 * 1000;
Query OK, 0 rows affected (0.05 sec)
```

다음 결과에서 보는 것 처럼 Query Cache 내용이 Flush된 것을 볼 수 있다.

```sql
mysql> SHOW GLOBAL STATUS LIKE 'Qc%';
+-------------------------+------------+
| Variable_name           | Value      |
+-------------------------+------------+
| Qcache_free_blocks      | 1          |
| Qcache_free_memory      | 1048557680 |
| Qcache_hits             | 11850448   |
| Qcache_inserts          | 6427063    |
| Qcache_lowmem_prunes    | 0          |
| Qcache_not_cached       | 3150558    |
| Qcache_queries_in_cache | 0          |
| Qcache_total_blocks     | 1          |
+-------------------------+------------+
8 rows in set (0.00 sec)
```

## 비고

위에서 말한 `query_cache_size`가 클 때의 단점은 모두 Query Cache가 꽉 차있는 경우에 발생한다. 성능 측정을 할 때는 꼭 Query Cache가 Full 되는 상황에서도 성능을 측정해야 한다.


[1]: http://dev.mysql.com/doc/refman/5.5/en/server-system-variables.html#sysvar_query_cache_size
[2]: http://mysqlguru.github.io/mysql/2014/05/20/mysql-query-cache2.html

{% include mysql-reco.md %}
