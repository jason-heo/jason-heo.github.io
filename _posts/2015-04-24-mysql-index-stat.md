---
layout: post
title: "MySQL InnoDB Index Statistics"
categories: MySQL
---

## INDEX 통계의 중요성

DBMS 성능의 핵심은 Optimizer이며 Optimizer는 최적의 실행 계획을 세우기 위하여 Index 통계에 의존한다. 예를 들어 다음과 같은 SELECT문이 있고, 각각의 컬럼이 독립적인 INDEX로 생성되어 있다고 가정하자.

```sql
CREATE TABLE tab
(
    a INT,
    b INT,
    c INT,
    INDEX (a),
    INDEX (b),
    INDEX (c)
) Engine=InnoDB;

SELECT *
FROM tab
WHERE a = 10 /* a column에 대한 INDEX를 타야 할까?             */
  AND b = 20 /* 혹은 b column에 대해 INDEX를 타는 것이 좋은가? */
  AND c = 30 /* c에 대하 INDEX를 타면 안 된다                  */
```

Optimizer는 a, b, c 3개 INDEX를 선택할 수 있다. 물론 3개 모두 FULL Scan에 비해 효용성이 없다고 판단되는 경우 아무런 INDEX도 사용하지 않을 수 있다.

만약 우리가 Opitimizer라면 Data의 분포를 확인한 뒤 탐색 시간이 제일 적은 INDEX를 선택할 것이다. Optimizer도 마찬가지이다. 각 INDEX별 Data의 분포(이하 INDEX 통계)를 알고 있으며 이를 기반으로 실행 계획을 생성한다.

INDEX 통계는 모든 Data의 분포를 100% 정확하게 반영하지 못한다. INDEX 통계가 잘못된 경우 실행 계획이 잘못 생성되므로서 DBMS의 성능이 떨어지게 된다.

## INDEX 통계를 이용한 Row 개수 추정

INDEX 통계를 이용하면 SELECT문을 수행했을 때 Row가 몇 개나 출력될지 대략적으로 계산해 볼 수 있다. 아쉽게도 MySQL에서는 INDEX 통계를 눈으로 볼 수 있는 방법이 없기 때문에 Row 개수 추정을 MySQL로 실험하긴 어렵다.

대신, PostgreSQL의 문서를 참고하면 대략적으로 Optimzer가 어떻게 Row 개수를 추정하는지 알 수 있다.

http://www.postgresql.org/docs/8.3/static/row-estimation-examples.html

## MySQL 5.5에서 InnoDB의 INDEX 통계 관리 방법

MySQL 데몬을 최초 start 한 뒤 Query를 수행하면 시간이 유난히 많이 걸리는 걸 볼 수 있다. 왜 그런 것일까? (상황에 따라 Disk I/O일 수도 있으나 MySQL Data file을 Memory로 올라와 있더라도 체감적으로 느리다.)

정답은 InnoDB의 INDEX 통계 관리 방법에 있다. InnoDB는 INDEX 통계를 random sampling에 의지한다. 즉, Table이 처음 읽혀질 때, Table에 존재하는 INDEX별로 `innodb_stats_sample_pages` 개수 만큼의 Page를 random sampling한다.

이에 대한 내용은 [MySQL Manual][1]에서 볼 수 있다.


`innodb_stats_sample_pages`은 default value는 8이다. 각자 환경에 따라 다르겠지만, InnoDB Data가 큰 경우 8로는 부족할 수 있다. 이런 경우 random sampling된 통계가 실제 Data와는 왜곡이 있는 경우 실행 Plan이 잘못 생성될 수 있다. 또한 이 값을 늘린다 하더라도 random sampling이기 때문에 sampling될 때마다 plan이 잘못 생성될 수도 있다.

## MySQL 5.5에서 INDEX 통계가 재계산되는 경우

아래와 같은 경우 INDEX 통계가 재계산된다. 통계가 재계산된다고 해서 상황이 더 좋아지는 것은 아니다. 더 안 좋아질 수도 있다.

1. MySQL이 Start하는 경우 최초 1회 random sampling된다.
1. `ANALYZE TABLE`을 수행한 경우 INDEX 통계가 재계산된다.
 - 뭔가 좋아질 것을 기대하고 `ANALYZE TABLE`을 수행하겠지만 항상 좋아진다는 것을 보장할 수 없다. random sampling이므로 더 안 좋은 실행 Plan이 생성될 수도 있다.
1. `innodb_stats_on_metadata=ON`인 경우.
 - information schema에서 Data를 조회하는 경우 `innodb_stats_on_metadata=ON`인 경우 INDEX 통계가 재계산된다.
1. Table의 6.25%에 변화가 있는 경우
 - Table 전체 record의 6.25%에 만큼의 INDEX, UPDATE, DELETE 등 Table에 변화가 있는 경우 MySQL이 자동으로 INDEX 통계를 재계산한다.
 - 이 부분은 MySQL 소스 코드에 하드 코딩되어 있으므로 사람이 개입할 수가 없다.

## MySQL 5.6에서 INDEX 통계 관련 개선 사항

- Table별로 random sampling할 page 개수를 지정할 수 있다.
 - 테이블 생성 시 혹은 ALTER 문으로 `STATS_SAMPLE_PAGES`를 지정할 수 있다.
 - size가 큰 테이블에는 큰 값을, 작은 테이블에는 적은 값을 줄 수 있다.
- 자동 통계 재계산 맊기
 - MySQL 설정 중 [innodb_stats_auto_recalc][2]를 이용하여 자동으로 재계산되는 것을 맊을 수 있다.
- Table의 10%에 변경이 있을 때 재계산한다.
 - 아직까진 10%로 MySQL 소스 코드에 하드 코딩되어 있다.
 - [MySQL 개발자 블로그][3]에서 10%가 적당한지에 대한 투표가 진행 중이다.

## Plan Stability를 향하여.... (MySQL 5.6의 관련 설정)

Plan Stablitity는 동일한 Query에 대해 동일한 Execution Plan이 생성되는 것을 의미한다. MySQL에서는 이를 바람직한 목표로 하고 있다.

본인이 사용 중인 MySQL 5.5에서는 random sampling되는 상황에 따라서 동일 Query의 Plan이 서로 다르게 생성된다. 때로는 너무 말도 안 되는 Plan이 생성되는 바람에 동일한 Query임에도 불구하고 속도 차이가 심하게 난다. 평소엔 0.1초 걸리는 Query가 간혹 5초 이상 걸리기도 하는데 이런 경우 MySQL이 밉기 까지하다. 

MySQL 5.6에서는 INDEX 통계를 persistent하게 유지할 수 있는 옵션을 제공한다. `innodb_stats_persistent`가 그 옵션이며 기본으로 enabled된 옵션이다. 본인도 아직 사용해보진 않았지만 MySQL이 restart되더라도 한번 sampling된 통계는 계속 유지되는 듯 하다.

물론 `innodb_stats_auto_recalc`는 기본 값이 `ON`이므로 10%의 변화가 있을 때는 자동으로 재계산된다.

이에 대한 자세한 내용은 [MySQL 5.6 메뉴얼][4]에서 볼 수 있다.

[1]: https://dev.mysql.com/doc/refman/5.5/en/innodb-statistics-estimation.html
[2]: http://dev.mysql.com/doc/refman/5.6/en/innodb-parameters.html#sysvar_innodb_stats_auto_recalc
[3]: http://mysqlserverteam.com/some-bits-about-index-statistics-in-innodb/
[4]: http://dev.mysql.com/doc/refman/5.6/en/innodb-persistent-stats.html

{% include mysql-reco.md %}
