---
layout: post
title: "MySQL Query Cache는 동시성에 약하다. (Query Cache의 올바른 이해)"
date: 2014-05-20 
categories: mysql
---

## Query 수행 전/후로 Global Lock을 사용한다.

MySQL Query Cache을 사용하면 Query 수행 전에는 Query Cache에 해당 Query가 존재하는지 조회를 한다. 또한 Query 종료 후 Query의 결과를 Query Cache에 저장한다. 다음의 SHOW PROFILE 결과를 보자.

    mysql> show profile;
    +--------------------------------+----------+
    | Status                         | Duration |
    +--------------------------------+----------+
    | starting                       | 0.000034 |
    | Waiting for query cache lock   | 0.000006 |
    | checking query cache for query | 0.000038 |
    | checking permissions           | 0.000010 |
    | Opening tables                 | 0.000018 |
    | System lock                    | 0.000011 |
    | Waiting for query cache lock   | 0.000023 |
    | init                           | 0.000031 |
    | optimizing                     | 0.000011 |
    | statistics                     | 0.000046 |
    | preparing                      | 0.000017 |
    | executing                      | 0.000005 |
    | Sending data                   | 0.000022 |
    | end                            | 0.000007 |
    | query end                      | 0.000006 |
    | closing tables                 | 0.000009 |
    | freeing items                  | 0.000009 |
    | Waiting for query cache lock   | 0.000005 |
    | freeing items                  | 0.000015 |
    | Waiting for query cache lock   | 0.000005 |
    | freeing items                  | 0.000004 |
    | storing result in query cache  | 0.000006 |
    | logging slow query             | 0.000005 |
    | cleaning up                    | 0.000004 |
    +--------------------------------+----------+
    24 rows in set (0.00 sec)

Query 수행 전후로 `Waiting for query cache lock`을 볼 수 있으며 Lock을 획득한 뒤에는 다른 thread는 MySQL Query Cache를 사용할 수 없으므로 Lock을 선점한 thread가 Lock을 해제할 때까지 대기해야 한다.

## 동시성이 많을 때 Query Cache on/off 비교

동시성이 많을 때 동일 Query를 Query Cache를 사용했을 때와 사용하지 않았을 때의 시간 비교이다.

## Query Cache를 사용할 때

    mysql> show profile;
    +--------------------------------+----------+
    | Status                         | Duration |
    +--------------------------------+----------+
    | starting                       | 0.000017 |
    | Waiting for query cache lock   | 0.006523 |  <= 여기를 주목
    | checking query cache for query | 0.000010 |
    | checking privileges on cached  | 0.000003 |
    | checking permissions           | 0.000006 |
    | sending cached result to clien | 0.000010 |
    | logging slow query             | 0.000003 |
    | cleaning up                    | 0.000003 |
    +--------------------------------+----------+
    8 rows in set (0.00 sec)

Lock을 획득할 때까지 0.0065초가 소요되었다.

## 동일 Query를 동일 상황에서 Query Cache 사용하지 않았을 때

    mysql> show profile;
    +----------------------+----------+
    | Status               | Duration |
    +----------------------+----------+
    | starting             | 0.000071 |
    | checking permissions | 0.000005 |
    | Opening tables       | 0.000015 |
    | System lock          | 0.000007 |
    | init                 | 0.000031 |
    | optimizing           | 0.000011 |
    | statistics           | 0.000093 |
    | preparing            | 0.000023 |
    | Creating tmp table   | 0.000026 |
    | Sorting for group    | 0.000005 |
    | executing            | 0.000002 |
    | Copying to tmp table | 0.000074 |
    | Sorting result       | 0.000017 |
    | Sending data         | 0.000013 |
    | end                  | 0.000003 |
    | removing tmp table   | 0.000018 |
    | end                  | 0.000003 |
    | query end            | 0.000004 |
    | closing tables       | 0.000006 |
    | freeing items        | 0.000020 |
    | logging slow query   | 0.000002 |
    | cleaning up          | 0.000003 |
    +----------------------+----------+
    22 rows in set (0.00 sec)

Query Cache를 사용했을 때보다 단계를 많아졌지만 시간을 누적해 보면 Query Cache를 사용하지 않는 것이 더 빠르다.

## Query Cache의 올바른 이해

실험을 해 보면 `SHOW GLOBAL STATUS like 'Qcache_lowmem_prunes';` 의 값이 0을 넘어서는 시점에 Locking 비용이 커지는 듯 하다. 동시 Thread 개수가 많더라도 Pruning이 발생하지 않으면 Locking 비용이 적으나 Pruning이 발생 중일 때는 동시 Thread가 적더라도 Locking 비용이 크다. 이를 종합하여 몇 가지 조언을 하며 글을 맺겠다.

- Query Cache가 사용되면 성능 테스트에 더 많은 노력을 기울여야 한다.
 - 테스트 시나리오에는 Query Cache가 Full 되었을 때 성능 변화도 관찰하는 것이 좋다.
- query_cache_type = ON으로 설정한 후
 - 가급적 가벼운 질의에는 SELECT SQL_NO_CACHE 를 사용하도록 하자
 - 무거운 질의는 SQL_NO_CACHE를 붙이지 않고 평소처럼 사용하면 된다.

비슷한 방법으로 `query_cache_type = DEMAND`로 설정한 뒤, 무거운 질의만 `SELECT SQL_CACHE`로 사용할 수도 있으나 이 방법은 `query_cache_type = ON`으로 놓고 SELECT SQL_NO_CACHE로 사용할 때와 다른 점이 있다. 이 내용은 다음에 다시 정리하겠다.
