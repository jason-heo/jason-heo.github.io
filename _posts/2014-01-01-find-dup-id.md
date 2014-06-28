---
layout: post
title: "복수 개의 값을 가진 id 찾기"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20207719/is-there-a-better-way-to-do-this-query/20208002

## 질문

다음과 같이 인물(person_id)가진 기술(skill_id) 데이터가 존재한다.

mysql> SELECT * FROM person_skill;
+----+-----------+----------+
| id | person_id | skill_id |
+----+-----------+----------+
|  1 | person1   | skill A  |
|  2 | person1   | skill B  |
|  3 | person1   | skill C  |
|  4 | person2   | skill A  |
|  5 | person2   | skill B  |
|  6 | person3   | skill B  |
|  7 | person3   | skill C  |
|  8 | person4   | skill A  |
|  9 | person4   | skill A  |
| 10 | person4   | skill B  |
| 11 | person5   | skill B  |
| 12 | person5   | skill B  |
+----+-----------+----------+
12 rows in set (0.00 sec)

"skill A"와 "skill B"를 동시에 소유한 person_id를 찾으려고 하는데 좋은 방법은 없는가?

{% include adsense-content.md %}

## 답변

당연한 이야기이지만 다음과 SELECT문은 작동을 하지 않는다. 간혹 다음과 같은 SELECT문을 사용하는 사람이 있기 때문에 적어 보았다.

    SELECT *
    FROM person_skill
    WHERE skill_id = 'skill A'
      AND skill_id = 'skill B';
     
    Empty set (0.00 sec)

그렇다면 어떤 질의를 이용해야 할까?

### SELF JOIN을 이용하기

우선 다음과 같이 SELF JOIN을 활용할 수 있다.

    SELECT t1.person_id
    FROM person_skill t1 INNER JOIN person_skill t2
      ON t1.person_id = t2.person_id
    WHERE t1.skill_id = 'skill A'
      AND t2.skill_id = 'skill B'
      AND t1.id != t2.id;
     
    +-----------+
    | person_id |
    +-----------+
    | person1   |
    | person2   |
    | person4   |
    | person4   |
    +-----------+
    4 rows in set (0.00 sec)

person4가 2번 나온 것은 person4가 "skill A"를 2개 소유했기 때문이며 DISTINCT를 넣어서 제거할 수 있다. 위의 SELF JOIN에서 t1은 "skill A"를 소유한 인물을 검색하고 있으며, t2는"skill B"를 검색하고 있다. t1과 t2를 SELF JOIN하여 t1과 t2의 person_id가 같은 레코드를 찾는 SELECT문이다. SELF JOIN에 익숙하지 않으면 약간 헷갈릴 수도 있으나 잘 풀어서 생각해보면어렵지는 않다.

### GROUP BY를 활용하기

위의 SELF JOIN이 어렵다면 GROUP BY를 이용하여 같은 문제를 풀 수도 있다. 단, 아래 SELECT문결과를 보면 알겠지만 SELF JOIN에 비해 제약 조건이 있긴 하다.

    SELECT person_id
    FROM person_skill
    WHERE skill_id IN ('skill A', 'skill B')
    GROUP BY person_id
    HAVING SUM(skill_id IN ('skill A', 'skill B')) = 2;
     
    +-----------+
    | person_id |
    +-----------+
    | person1   |
    | person2   |
    | person5   |
    +-----------+
    3 rows in set (0.01 sec)

person4는 결과에서 제외되었고, "skill A"를 2개 가진 person5는 결과에 포함되었다. 이렇게 중복이 허용된 경우에는 GROUP BY는 적용이 불가능하지만, JOIN이 없기 때문에 경우에 따라서는SELF JOIN보다 빠를 수도 있다. 물론 최적화가 어려운 GROUP BY가 사용되었으므로 독자의 시스템에 도입하는 경우는 똑 성능 테스트를 하고 도입하도록 하자. GROUP BY가 사용되었으므로 조회할 필드가 person_id 이외에 다른 컬럼이 더 있는 경우는 좀 까다롭다는 단점도 있다. 또한 필자의 테스트 결과 'skill A'와 'skill B'를 모두 만족하는 레코드가 적지만, 'skill A' 혹은 'skill B'를 만족하는 레코드가 많은 경우 "GROUP BY"할 레코드가 많으므로 SELF JOIN을 사용했을 때보다 느렸다. 반대로 'skill A' 혹은 'skill B'를 만족하는 레코드가 개수가 적은 경우는 SELF JOIN보다GROUP BY가 다소 빨랐으나 이때는 2 경우 모두 검색할 양이 얼마 안 되기 때문에 시간 차이는 크지 않았다.

앞 SELECT문의 핵심은 HAVING SUM 부분이다. SUM 안에 있는 "skill_id IN ('skill A', skill B')"는 비교문으로서 1 혹은 0을 반환한다. 다음과 같이 GROUP BY HAVING SUM을 없애고실행하면 이해가 쉽다.

    SELECT person_id, skill_id, skill_id IN ('skill A', 'skill B')
    FROM person_skill;
     
    +-----------+----------+------------------------------------+
    | person_id | skill_id | skill_id IN ('skill A', 'skill B') |
    +-----------+----------+------------------------------------+
    | person1   | skill A  |                                  1 |
    | person1   | skill B  |                                  1 |
    | person1   | skill C  |                                  0 |
    | person2   | skill A  |                                  1 |
    | person2   | skill B  |                                  1 |
    | person3   | skill B  |                                  1 |
    | person3   | skill C  |                                  0 |
    | person4   | skill A  |                                  1 |
    | person4   | skill A  |                                  1 |
    | person4   | skill B  |                                  1 |
    | person5   | skill B  |                                  1 |
    | person5   | skill B  |                                  1 |
    +-----------+----------+------------------------------------+
    12 rows in set (0.00 sec)

"skill_id IN ('skill A', 'skill B')"를 GROUP BY person_id에 사용하면 다음 결과와 같다.

    SELECT person_id, SUM(skill_id IN ('skill A', 'skill B'))
    FROM person_skill
    GROUP BY person_id;
     
    +-----------+-----------------------------------------+
    | person_id | SUM(skill_id IN ('skill A', 'skill B')) |
    +-----------+-----------------------------------------+
    | person1   |                                       2 |
    | person2   |                                       2 |
    | person3   |                                       1 |
    | person4   |                                       3 |
    | person5   |                                       2 |
    +-----------+-----------------------------------------+
    5 rows in set (0.00 sec)

이제 SUM()에 대한 조건을 넣어야 하므로 HAVING 절에 조건을 넣어서 SUM()의 값이 2인 것만 선택하면 제일 처음의 SELECT문이 된다.

### 응용 질문

person_id가 여러 개의 skill을 가질 수 있다. 그런데, "skill A"와 "skill B" 2개만 가진 person_id를 찾는 방법은 무엇일까? 문제를 간단히 하기 위해 다음과 같이 중복 데이터가 없는 경우를한정한다. [SQLFiddle](http://www.sqlfiddle.com/#!2/ab967/1)은 여기서 확인할 수 있다.

    SELECT * FROM person_skill;
     
    +----+-----------+----------+
    | id | person_id | skill_id |
    +----+-----------+----------+
    |  1 | person1   | skill A  |
    |  2 | person1   | skill B  |
    |  3 | person1   | skill C  |
    |  4 | person2   | skill A  |
    |  5 | person2   | skill B  |
    |  6 | person3   | skill B  |
    |  7 | person3   | skill C  |
    |  8 | person4   | skill A  |
    |  9 | person4   | skill B  |
    | 10 | person5   | skill B  |
    +----+-----------+----------+
    10 rows in set (0.00 sec)

우선 GROUP BY를 이용하여 다음과 같이 SELECT하는 방법이 있다.

    SELECT person_id, SUM(skill_id IN ('skill A', 'skill B'))
    FROM person_skill
    GROUP BY person_id
    HAVING SUM(skill_id IN ('skill A', 'skill B')) = COUNT(*)
      AND COUNT(*) = 2;
     
    +-----------+-----------------------------------------+
    | person_id | SUM(skill_id IN ('skill A', 'skill B')) |
    +-----------+-----------------------------------------+
    | person2   |                                       2 |
    | person4   |                                       2 |
    +-----------+-----------------------------------------+
    2 rows in set (0.00 sec)

SUM의 의미는 앞에서 설명이 되었으며, COUNT(*)는 GROUP의 전체 레코드를 의미하므로 GROUP의 모든 레코드가 "skill A", "skill B"를 만족해야 한다는 조건을 넣었다. "COUNT(*) = 2"는 skill_id를 "skill A"혹은 "skill B"를 1개만 가진 person_id를 제거하는 역할을 한다.

잘 보면 알겠지만, 위의 SELECT문에는 WHERE 절이 없다. GROUP 전체에 대한 조건을 검사해야 하므로 WHERE 절에서 skill_id 조건을 넣을 수 없다. 전체 레코드를 대상으로 GROUP BY를 수행해야하므로  위의 SELECT 문은 성능이 좋을 수 없다.

성능을 고려하는 방법은 앞의 SELECT문에 SELF JOIN을 이용한 inline view를 넣는 방법이다. 앞의 SELECT문도 잘 작동하지만, 성능을 고려한다면 복잡해 보이더라도 다음의 SELECT문을 이용하는 것이 좋다.

    SELECT t1.person_id, SUM(t1.skill_id IN ('skill A', 'skill B'))
    FROM person_skill t1 INNER JOIN (
        SELECT t1.person_id
        FROM person_skill t1 INNER JOIN person_skill t2
          ON t1.person_id = t2.person_id
        WHERE t1.skill_id = 'skill A'
          AND t2.skill_id = 'skill B'
          AND t1.id != t2.id
    ) t2 ON t1.person_id = t2.person_id
    GROUP BY t1.person_id
    HAVING SUM(t1.skill_id IN ('skill A', 'skill B')) = 2
      AND COUNT(*) = 2;
     
    +-----------+--------------------------------------------+
    | person_id | SUM(t1.skill_id IN ('skill A', 'skill B')) |
    +-----------+--------------------------------------------+
    | person2   |                                          2 |
    | person4   |                                          2 |
    +-----------+--------------------------------------------+
    2 rows in set (0.00 sec)

처음의 data set으로 돌아와서, 중복이 허용된 경우는 DISTINCT로 중복을 제거한 뒤 다시 GROUP BY HAVING을 써야 해서 좀 더 복잡해 진다. 더 좋은 방법이 있을 수도 있지만 필자가 제안하는 방법은 다음과 같다. [SQLFiddle](http://www.sqlfiddle.com/#!2/97263/2)에서 직접 실행해 볼 수 있다.

    SELECT x.person_id, SUM(x.skill_id IN ('skill A', 'skill B'))
    FROM
    (
        SELECT DISTINCT t1.person_id, skill_id
        FROM person_skill t1 INNER JOIN (
            SELECT DISTINCT t1.person_id
            FROM person_skill t1 INNER JOIN person_skill t2
              ON t1.person_id = t2.person_id
            WHERE t1.skill_id = 'skill A'
              AND t2.skill_id = 'skill B'
              AND t1.id != t2.id
        ) t2 ON t1.person_id = t2.person_id
    ) x
    GROUP BY x.person_id
    HAVING SUM(x.skill_id IN ('skill A', 'skill B')) = 2 AND COUNT(*) = 2;
     
    +-----------+-------------------------------------------+
    | person_id | SUM(x.skill_id IN ('skill A', 'skill B')) |
    +-----------+-------------------------------------------+
    | person2   |                                         2 |
    | person4   |                                         2 |
    +-----------+-------------------------------------------+
    2 rows in set (0.00 sec)
