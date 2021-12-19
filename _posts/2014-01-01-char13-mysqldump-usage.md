---
layout: post
title: "mysqldump 사용법"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20015675/importing-mysql-dump-without-overwriting-existing-data/20015743

## 질문

mysqldump로 dump받은 파일을 열어 보면 "USE db_name"이 저장되어 있다. 이 파일을 복구하는 경우 기존 DB의 내용이 지워지고 테이블을 새로 만들게 되는데, 다른 DB 명을 지정할 방법은 없는가? dump된 파일 내용은 다음과 같다.

    Host: localhost     Database: db
    -------------------------------------------
    Server version      5.5.34-0ubuntu0.13.04.1
     
    [SET a bunch of things]
    Current Database: 'db'
    CREATE DATABASE /*!32312 IF NOT EXISTS*/ 'db' /*!40100 DEFAULT CHARACTER SET latin1 */;
    USE 'db';
    DROP TABLE IF EXISTS `FINES`;
    CREATE TABLE `FINES` (
    ...

{% include adsense-content.md %}

## 답변

기존에 dump되어 있는 파일을 이용하여 복구하는 경우 dump 파일에 명시된 DB가 아닌 다른 DB에 복구하는 기능은 MySQL에 없다. dump 파일이 text 파일이기 때문에 에디터로 열어서 수정하는 것도방법이지만 일반적으로 dump 파일은 크기가 크기 때문에 에디터로 수정은 어려울 수 있으며 이 경우 sed 같은 유틸리티를 이용해서 수정해야 한다.

mysqldump로 DB 내용을 dump할 때, database를 여러 개 지정하는 경우는 질문자처럼 dump 받은 파일에 DB이름이 입력되어 있다. mysqldump은 다음과 같이 3가지 방식으로 사용할 수 있는데 각방식의 특정이 잘 파악하여 사용자의 목적에 맞게 사용할 수 있도록 하자.

- mysqldump [OPTIONS] database [tables]
- mysqldump [OPTIONS] --databases [OPTIONS] DB1 [DB2 DB3...]
- mysqldump [OPTIONS] --all-databases [OPTIONS]

이중 `--databases` 혹은 `--all-databases`를 사용하는 경우 복수 개의 database를 dump받을 수 있기 때문에 dump 파일 안에는 "USE db_name"이 포함되게 된다.

옵션 종류에 따라서 옵션 뒤에 나오는 문자열을 해석하는 방법이 다르다.

    $ mysqldump a b c
a는 DB명, b와 c는 a DB에 존재하는 테이블 명을 의미한다. b 혹은 c가 a DB에 존재하지 않는 경우 dump 자체가 실패한다.

    $ mysqldump --databases a b c

a, b, c는 모두 DB명을 의미한다. 복수 개의 DB를 dump 받기 때문에 dump 받은 파일에는 "USE a", "USE b"와 같이 DB 지정을 위한 USE 구문이 삽입된다.

    $ mysqldump --all-databases a b c

위와 같은 사용은 mysqldump에서 오류를 출력한다. --all-databases 옵션은 MySQL에 존재하는 DB를 dump받겠다는 것을 의미하므로 옵션 뒤에 DB명이나 테이블 명이 나열될 수 없다.

{% include mysql-reco.md %}
