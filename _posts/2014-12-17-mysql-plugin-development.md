---
layout: post
title: "MySQL Query Rewrite에 대한 간략한 소개"
categories: MySQL
---

MySQL Query Rewrite Plugin 기능을 공부하려고 했는데, 이것도 MySQL Plugin의 일종이다보니 우선 MySQL Plugin의 개념에 익숙해져야 겠더라.

이 부분은 자료 찾기가 어렵지만 다행이도 MySQL Plugin을 만든 개발자들이 집필한 [MySQL 5.1 Plugin Development][1]라는 책이 있다. MySQL 5.1 기준으로 만들어졌으나 이런 류의 프로그램은 갑작스레 바뀔 수 없으므로 5.1 기준으로 작성된 내용이더라도 5.7에서 무리없이 적용 가능하다.

문제는 책 값이 너무 비싸다는 것! Amazon에서 무료 배송으로 40달러이며 Yes24에서 4만 9천원에 판매중이다;;

하지만 어둠의 경로에서 pdf 파일을 구할 수 있다.

Plugin 종류별로 할 수 있는 일이 다른데, 이들의 특성을 파악하는 것이 중요하다. 그리고 Plugin에 전달되는 파라미터들의 자료 구조를 보면서 MySQL Internal에 대해 공부할 수 있는 장점도 있다. 예를 들어 Inforation Schema Plugin에서는 condition pushdown을 위해 `WHERE` 절을 표현하는 `Item` class에 대한 내용이 나온다. 이를 보면서 MySQL이 WHERE 절을 어떻게 저장하는지 엿볼 수 있다.

아직은 본인도 다 읽진 못했지만 읽어본 내용까지 정리해 본다.

## deamon plugin

- 단독으로 실행
- install 하자마자 실행됨
- heartbeat 등으로 사용할 수 있음
- MySQL variable 및 status에 접근 가능함

## information schema plugin

- 단복으로 사용할 수 있으나 다른 plugin과 같이 사용됨
- information schema에 접근 가능
- 새로운 Engine 만들 때 information schema plugin을 이용하여 Engine 정보를 남길 수 있음
- 그래서 위에서 다른 plugin과 같이 사용된다고 했음
- `ST_SCHEMA_TABLE`에서 `fields_info`와 `fill_table` 함수를 구현하면 됨
- is plugin의 목적이 MySQL 서버 상태를 user에게 알리기 위한 plugin을 만드는 것이 목적임
- 따라서 is plugin에서는 MySQL Internal에 접근을 할 수 있는 항목이 많음
- 일반적으로 sql/ 디렉터리에 있는 header file의 자료를 접근할 수 있으며,
- 간혹 mysys/ 디렉터리의 데이터 접근도 가능

- condition pushdown
 - SELECT문의 WHERE 절을 표현
 - WHERE 절이 plugin까지 내려오면 조건에 맞는 rows만 생성할 수 있다. => 성능 향상 가능
 - (비슷한 개념으로 index condition pushdown이 있는데 이건 나중에 기회되면 설명)
 - condition은 `Item` 객체의 tree인데, 기본 class도 복잡하고, `Item class`의 계층도 수백개나 된다.
 - 하지만 MySQL의 core 중 하나이다. 그도 그럴 것이 WHERE 절을 표현하는 것이기 때문에 엄청 복잡할 듯 하다.

# fulltext parser plugin

- fulltext에서 사용할 term extractor 지정
- MySQL FTS에서 한글 같은 경우 n-gram 방식으로 term들을 저장한다. 따라서 저장 용량도 늘어나고 어휘 분석도 제대로 안 될 수 있다.
- 요즘 오픈소스 형태소 분석기가 있으므로 이것과 fulltext parse plugin을 연동하면 좋을 듯 하다.

## Storage Engine Plugin

- 신규 Storage Engine 개발 시 사용
- 아직 읽어보진 않았지만 이 부분이 재미있을 듯 하다.

[1]: http://www.amazon.com/MySQL-Plugin-Development-Sergei-Golubchik/dp/1849510601
