---
layout: post
title: "Wordpress 사용자 프로필 테이블 조회하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20343802/mysql-select-on-row-values/20343891

## 질문

다음과 같이 Wordpress 사용자 프로필 테이블이 존재한다.

    mysql> SELECT user_id, field_id, value FROM wp_bp_xprofile_data;
    +---------+----------+-----------------+
    | user_id | field_id | value           |
    +---------+----------+-----------------+
    |     123 |        1 | Cindy           |
    |     123 |       73 | North America   |
    |     123 |      270 | Female          |
    |     123 |      354 | California City |
    |     222 |        1 | Heo             |
    |     222 |       73 | South Korea     |
    |     222 |      270 | Male            |
    |     222 |      354 | Seoul           |
    +---------+----------+-----------------+
    8 rows in set (0.00 sec)

총 2명의 사용자 (123과 222)가 존재한다. 사용자는 총 4개의 프로필을 가질 수 있다. field_id 값은 다음을 의미한다.

- 1 : 이름
- 73 : 거주 국가 이름
- 270 : 성별
- 354 : 거주 도시 이름

즉, 123 사용자는 "이름이 Cindy이며, 여성이고 North America의 California 시에 살고 있다"는 것을 알 수 있다. 앞의 정보를 이용하여 "여성이면서 California City에 살고 있는 사용자"를 검색하고 싶다.

{% include adsense-content.md %}

## 답변

Wordpress는 CMS(Content Management System)으로서 홈페이지 구축 도구로 사용된다. 우리나라에서 XE를 많이 사용하는 것처럼 외국에서 홈페이지 제작 시에 Wordpress를 많이 사용하는 듯 하다. Stackoverflow에도 Wordpress 관련 질문이 많이 올라오고 있다. 질문자가 원하는 것은 다음과 같이 SELF JOIN을 통해서 구할 수 있다.

    SELECT t1.user_id, t1.value, t2.value
    FROM wp_bp_xprofile_data t1
      INNER JOIN wp_bp_xprofile_data t2 ON t1.user_id = t2.user_id
    WHERE t1.field_id = 270 AND t1.value = 'Female'
    AND t2.field_id = 354 AND t2.value = 'California City';
     
    +---------+--------+-----------------+
    | user_id | value  | value           |
    +---------+--------+-----------------+
    |     123 | Female | California City |
    +---------+--------+-----------------+
    1 row in set (0.00 sec)

t1 테이블은 여성을 검색하고, t2 테이블은 California City에 거주 중인 사용자를 검색한 뒤, 두 테이블을 JOIN함으로서 여성이면서 California City에 거주 중인 사용자를 검색하였다.

### 질문자가 채택한 답변

질문자가 채택한 답변은 다음과 같다.

    SELECT user_id FROM wp_bp_xprofile_data t
    WHERE (field_id = 270 AND value = 'Female') OR
          (field_id = 354 AND value = 'California City')
    GROUP BY user_id
    HAVING COUNT(*) = 2;
     
    +---------+
    | user_id |
    +---------+
    |     123 |
    +---------+
    1 row in set (0.00 sec)

답변자도 언급했지만, 앞의 방법은 GROUP BY를 이용하기 때문에 user_id 이외의 컬럼 값을 조회하기 위해서는 다음과 같이 복잡해 지는 것을 언급하고 있다.

    SELECT
      user_id,
      max(case when field_id = 270 then value end) nameOfPerson,
      max(case when field_id = 354 then value end) cityName
    FROM wp_bp_xprofile_data t
    GROUP BY user_id
    HAVING SUM(
      (field_id = 270 AND value = 'Female') +
      (field_id = 354 AND value = 'California City')
    ) = 2;
     
    +---------+--------------+-----------------+
    | user_id | nameOfPerson | cityName        |
    +---------+--------------+-----------------+
    |     123 | Female       | California City |
    +---------+--------------+-----------------+
    1 row in set (0.00 sec)

### EAV (Entity-Attribute-Value) Model

Wordpress에서 사용자 프로필을 저장하는 데이터 모델링을 EAV Model이라고 한다. 1명 사용자의 프로필을 다음과 같이 1개의 레코드와 여러 개의 컬럼에 저장할 수도 있다.

    mysql> SELECT * FROM wp_bp_xprofile_data;
    +---------+--------+--------+-----------------+---------------+
    | user_id | gender | name   | city            | county        |
    +---------+--------+--------+-----------------+---------------+
    |     123 | Cindy  | Female | California City | North America |
    |     222 | Heo    | Male   | Seoul           | South Korea   |
    +---------+--------+--------+-----------------+---------------+
    2 rows in set (0.00 sec)

질문자가 원하는 것도 다음과 같이 쉽게 구할 수 있다.

    SELECT *
    FROM wp_bp_xprofile_data
    WHERE gender = 'Female' AND city = 'California City';
     
    +---------+--------+-------+-----------------+---------------+
    | user_id | gender | name  | city            | county        |
    +---------+--------+-------+-----------------+---------------+
    |     123 | Female | Cindy | California City | North America |
    +---------+--------+-------+-----------------+---------------+

EAV Model은 특수한 용도에서는 꼭 필요하지만 일반적인 경우에는 피해야 할 안티 패턴(Anti Pattern)이다. EAV를 사용하는 것이 최적인 경우는 위키피이아에서 설명되어 있으니 관심 있는 독자는 읽어보기 바란다.

Wordpress 같은 CMS에서는 사이트 관리자가 사용자 프로필을 설정하도록 하는 기능을 제공한다. 사이트마다 필요한 사용자 프로필 값이 다르기 때문에 미리 컬럼으로 만들어 놓을 수는 없다. 따라서Wordpress는 EAV Model로 사용자 프로필을 저장하고 있다.
