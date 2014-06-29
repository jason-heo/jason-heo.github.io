---
layout: post
title: "mysqldump 파일에서 information_schema 제거하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20208399/remove-information-schema-from-sql-dump/20208492

## 질문

mysqldump로 dump 받은 파일의 크기가 40GB이다. 이 파일 안에는 information_schema DB도 함께 저장되어 있다. dump 파일에서 information_schema에 관련된 테이블을 모두 지우고 싶은데 파일 크기가 40GB가 넘다 보니 일반적인 에디터로는 수정이 불가능하다. 좋은 방법이 없을까?

{% include adsense-content.md %}

## 답변

이미 dump를 받은 상황이기 때문에 dump 받은 파일을 직접 수정하는 방법 밖에 없어 보인다. 필자의 경우 Linux의 egrep 명령을 이용하길 제안했다.

    $ egrep -v '(GLOBAL_VARIABLES|CHARACTER_SETS|COLLATIONS)' dump.sql > new_dump.sql

`grep`과 `egrep`의 `-v` 옵션은 invert의 약자로서 해당 문자열을 포함하지 않는 line만 출력하라는 옵션이다. egrep은 grep을 확장시킨 명령으로서 정규 표현식을 패턴으로 지정할 수 있다. 정규 표현식 `(A|B|C)`는 `A 혹은 B 혹은  C`를 의미한다.

앞의 예에서는 3개의 테이블을 지정했지만, 실제로는 information_schema DB에 존재하는 모든 테이블을 나열해야 한다. MySQL 5.5 기준으로 information_schema에는 40개의 테이블이 존재한다.
