---
layout: post
title: "MySQL Query Cache hit ratio, innodb buffer pool hit ratio"
date: 2014-03-05 
categories: mysql
---

[이전 포스트](/mysql/2014/03/06/mysql-query-cache-prune.html)에서 MySQL Query Cache에 대한 자세한 설명을 했지만, 내용이 길다보니 Query Cache hit ratio을 구하는 방법은 눈에 들어오지 않아서 Query Cache hit ratio 구하는 방법만 따로 정리했다. 더불어 InnoDB buffer pool hit ratio 구하는 방법도 정리했다.

## Query Cache hit ratio

총 수행된 SELECT 개수 중에서 Query Cache에서 결과가 출력된 비율을 말한다. 이 값이 높으면 높을 수록 좋다. 이 값이 낮다면 Query Cache를 끄는 게 성능에 더 좋을 수도 있다. 얼마나 높아야 높은 것인지 얼마나 낮아야 낮은 것인지 기준은 없다.

    Query Cache hit ratio = Qcache_hits / (Qcache_hits + Com_select)

각 항목은 다음과 같이 구할 수 있다.

    mysql> SHOW GLOBAL STATUS LIKE 'Qcache_hits';
    +---------------+---------+
    | Variable_name | Value   |
    +---------------+---------+
    | Qcache_hits   | 1968119 |
    +---------------+---------+
    1 row in set (0.00 sec)
     
    mysql> SHOW GLOBAL STATUS LIKE 'Com_select%';
    +---------------+----------+
    | Variable_name | Value    |
    +---------------+----------+
    | Com_select    | 24736290 |
    +---------------+----------+
 
## InnoDB Buffer Pool hit ratio

InnoDB Buffer pool hit ratio가 낮으면 Disk에서 InnoDB Buffer pool로 올라오는 양이 많다는 이야기다. 이 값이 낮다면 성능이 좋을 수 없다. my.cnf에서 innodb_buffer_pool_size를 늘려주도록 하자. Memory가 부족하면 Memory를 더 구매해자. 요즘 램값 얼마 안 한다.

    innodb buffer pool hit ratio = Innodb_buffer_pool_read_requests / (Innodb_buffer_pool_read_requests + Innodb_buffer_pool_reads)

위의 계산에 필요한 값은 다음과 같이 얻을 수 있다.

    mysql> SHOW GLOBAL STATUS LIKE 'Innodb_buffer_pool%';
    +---------------------------------------+------------+
    | Variable_name                         | Value      |
    +---------------------------------------+------------+
    | Innodb_buffer_pool_pages_data         | 323342     |
    | Innodb_buffer_pool_pages_dirty        | 0          |
    | Innodb_buffer_pool_pages_flushed      | 1886513    |
    | Innodb_buffer_pool_pages_free         | 259315     |
    | Innodb_buffer_pool_pages_misc         | 72699      |
    | Innodb_buffer_pool_pages_total        | 655356     |
    | Innodb_buffer_pool_read_ahead_rnd     | 0          |
    | Innodb_buffer_pool_read_ahead         | 120385     |
    | Innodb_buffer_pool_read_ahead_evicted | 0          |
    | Innodb_buffer_pool_read_requests      | 1051030436 |
    | Innodb_buffer_pool_reads              | 45777      |
    | Innodb_buffer_pool_wait_free          | 0          |
    | Innodb_buffer_pool_write_requests     | 174742167  |
    +---------------------------------------+------------+
    13 rows in set (0.00 sec)

MySQL 5.5 기준으로 출력된 결과이기 때문이며 MySQL의 다른 버전 혹은  Percona 및 MariaDB에서는 값이 약간 다를 수 있지만 계산 식은 위에 나온 것과 동일하다.

