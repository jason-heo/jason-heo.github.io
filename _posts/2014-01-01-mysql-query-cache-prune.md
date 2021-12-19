---
layout: post
title: "MySQL Query Cache hit ratio, Qcache_lowmem_prunes의 의미"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20259249/what-is-query-cache-prunes-per-day-in-mysql/20259502#20259502

## 부가 URL

http://www.percona.com/files/presentations/MySQL_Query_Cache.pdf

## 질문

MySQL로 서비스를 운영 중인데 성능이 느려지기 시작했다. MySQL 설정 변경을 위해서 MySQLTuner를 설치해서 모니터링 중이다. 그런데 MySQLTuner가 다음의 경고 메시지를 출력하였다.

    Query cache prunes per day: 57482

의미하는 바가 무엇인지, 성능에 영향을 미치는 것인지 궁금하다.

{% include adsense-content.md %}

## 답변

우선 "Query cache prunes per day"의 의미를 파악해 보자. "prune"은 "나무 가지치기를 하다", "불필요한 부분을 정리하다"의 의미이다. "per day"이므로 하루 동안 Query Cache에서 삭제된 Cache 가 57,482개라는 것을 의미한다. 1일은 86,400초 이므로 약 1.5초마다 Query Cache에 저장된 내용이 1개씩 삭제되고 있다는 것을 의미한다. MySQLTuner 소스 코드를 확인했는데 다음과 같이 계산하고 있었다.

Query Cache가 prune되는 이유는 Query Cache가 꽉 찼기 때문이다. 새로운 Cache 항목이 Query Cache로 입력될 때 Query Cache가 꽉 차있다면 LRU (Least Recently Used) 기법을 이용하여오래된 Cache 항목을 삭제한다. 이를 pruning이라고 한다. pruning 된 횟수는 `SHOW GLOBAL STATUS`를 이용하여 얻을 수 있다.

    mysql> SHOW GLOBAL STATUS LIKE 'Q%';
    +-------------------------+----------+
    | Variable_name           | Value    |
    +-------------------------+----------+
    | Qcache_free_blocks      | 1        |
    | Qcache_free_memory      | 16733288 |
    | Qcache_hits             | 1291736  |
    | Qcache_inserts          | 1301133  |
    | Qcache_lowmem_prunes    | 15214    |
    | Qcache_not_cached       | 10415252 |
    | Qcache_queries_in_cache | 195      |
    | Qcache_total_blocks     | 113      |
    | Queries                 | 15142148 |
    | Questions               | 15139850 |
    +-------------------------+----------+

"Qc"로 시작하는 상태 값이 Query Cache에 관련된 상태 값이며 "Qcache_lowmem_prunes"가 MySQL 시작 후 Query Cache가 pruning된 횟수를 저장하는 상태 값이다. 참고로 MySQLTuner의소스를 보면 "Query cache prunes per day"는 다음과 같이 계산하고 있다.

    $mycalc{'query_cache_prunes_per_day'} = int($mystat{'Qcache_lowmem_prunes'} / ($mystat{'Uptime'}/86400));

'Uptime'은 MySQL이 시작된 이후의 시간이며 단위는 초(second)이다. MySQL 상태 값은 "FLUSH STATUS"를 이용하여 초기화 될 수 있으니 엄밀히 따져서는 일별 pruning 횟수라고 할 수 없으므로 참고하기 바란다.

다시 원래의 질문으로 돌아오자. pruning이 많다는 이야기는 Query Cache가 꽉 차있다는 것을 의미하므로 Query Cache의 크기를 늘려 주는 것이 좋다. my.cnf에서 [mysqld] 항목에query_cache_size를 늘리면 된다.

    [mysqld]
    query_cache_size = 512M

최적의 크기는 독자의 서버 환경마다 다를 것으로 생각된다.

### Query Cache Hit ratio 계산하기

일반적으로 Hit ratio는 `(Hit 횟수 / (Hit 횟수 + Miss 횟수)`로 계산할 수 있다. MySQL 상태 변수로는 다음과 같다.

    Qcache_hits / (Qcache_hits + Com_select)

Qcache_hits는 Query Cache를 Hit한 횟수를 의미하며, Com_select는 'Command select'의 약어인데 Query Cache Miss가 발생하여 실제로 SELECT 문이 수행된 횟수를 의미한다.

### Query Cache는 항상 유리한가?

그렇지 않다. Query Cache Hit ratio가 낮은 경우(절대적인 수치 얼마가 '낮다'라고 대답하기는 어렵다)는 아예 Query Cache 기능 자체를 꺼두는 것이 빠를 수도 있다. 이런 경우는 일반적으로 테이블에WRITE가 많은 경우 즉, INSERT, UPDATE, DELETE 등이 많은 경우이다. 왜일까?

SELECT 문이 실행될 때 Query Cache에 존재하는지 검사를 해야 하고, Miss인 경우 SELECT문 수행 후 SELECT문의 결과를 Query Cache에 저장해야 하는데, 이때마다 Query Cache에 Global Lock이 걸리기 때문에 Lock을 건 Connection 이외에 다른 Connection은 Lock이 풀릴 때까지 대기를 해야 하기 때문이다. Query Cache Hit ratio가 높은 경우는 이 비용이 SELECT를 하는 것보다 낮을 수 있지만 Query Cache Hit ratio가 높은 경우는 Query Cache에 대한 대기 시간이 SELECT문을 수행하는 것보다 더 느릴 수 있다.

다음의 예는 MySQL의 프로파일 기능을 이용하여 SELECT시에 발생하는 Query Cache Lock을 확인해본 예이다. MySQL 프로파일 기능은 MySQL 5.1.24부터 사용할 수 있다.

    mysql> SET PROFILING = 1;
    Query OK, 0 rows affected (0.00 sec)
     
    mysql> SELECT * FROM a;
    Empty set (0.00 sec)
     
    mysql> SHOW PROFILE;
    +--------------------------------+----------+
    | Status                         | Duration |
    +--------------------------------+----------+
    | starting                       | 0.000024 |
    | Waiting for query cache lock   | 0.000007 |
    | checking query cache for query | 0.000027 |     
    | checking permissions           | 0.000010 |
                    ....
    | Waiting for query cache lock   | 0.000022 |
                    ....
    | Sending data                   | 0.000029 |
    | end                            | 0.000009 |
                    ....
    | Waiting for query cache lock   | 0.000007 |
    | freeing items                  | 0.000010 |
    | Waiting for query cache lock   | 0.000007 |
    | freeing items                  | 0.000005 |
    | storing result in query cache  | 0.000009 |
    | logging slow query             | 0.000008 |
    | cleaning up                    | 0.000007 |
    +--------------------------------+----------+
    24 rows in set (0.00 sec)

{% include mysql-reco.md %}
