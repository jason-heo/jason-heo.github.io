---
layout: post
title: "팁 2 - MySQL에서 전문 검색 (MySQL FullText Search)"
date: 2014-03-05 21:34:00
categories: mysql
---

`search_test` 테이블에는 약 1천만 건의 레코드가 존재하며 value 컬럼은 LONGTEXT이고 INDEX가 걸려 있는 테이블이다.

## LIKE 'keyword%'

컬럼이 INDEX되었을 때 컬럼의 prefix로 검색하는 것은 INDEX 스캔을 할 수 있기 때문에 속도가 매우 빠르다.

    SELECT SQL_NO_CACHE COUNT(*)
    FROM search_test
    WHERE value LIKE 'robert%';
     
    +----------+
    | COUNT(*) |
    +----------+
    |     3441 |
    +----------+
    1 row in set (0.00 sec)

INDEX 스캔이기 때문에 속도는 빠르지만, prefix search는 전문 검색이라고 할 수 없다.

## LIKE '%keyword%'

infix search를 사용하면 전문 검색을 할 수 있지만 역시 INDEX를 활용하지 못하기 때문에 속도가 느리다.

    SELECT SQL_NO_CACHE COUNT(*)
    FROM search_test
    WHERE value LIKE '%robert%';
     
    +----------+
    | COUNT(*) |
    +----------+
    |     5034 |
    +----------+
    1 row in set (9.73 sec)

## REGEXP()

정규 표현식을 이용하여 전문 검색을 할 수도 있다. 그러나 역시 Full scan을 해야 하므로 속도가 느리다.

    SELECT SQL_NO_CACHE COUNT(*)
    FROM search_test
    WHERE value REGEXP ('robert');
     
    +----------+
    | COUNT(*) |
    +----------+
    |     5034 |
    +----------+
    1 row in set (9.16 sec)

## MATCH AGAINST

MySQL에서 지원하는 전문 검색 기능을 활용한 결과이다. 0.02초 밖에 걸리지 않았다.

    SELECT SQL_NO_CACHE COUNT(*)
    FROM search_test
    WHERE MATCH(value)  AGAINST('*robert*' IN BOOLEAN MODE)
     
    +----------+
    | COUNT(*) |
    +----------+
    |     3505 |
    +----------+
    1 row in set (0.02 sec)

검색 결과 개수가 3,505개로서 앞선 결과와 개수가 다르다는 것에 주의하기 바란다. MATCH AGAINST를 사용하는 경우 MySQL은 문자열을 미리 색인 해두어야 하는데, 이 색인 때문에 LIKE의 결과와 달라질 수 있다. LIKE나 REGEXP를 활용하는 방법은 'robert' 바로 앞 뒤에 어떠한 문자가 나오던지 검색이 된다. 하지만, 'robert' 앞 뒤에는 공백 문자나 기호가 있어야만 'robert'라는 단어가 색인이 될 것이다. MATCH AGAINST 사용 시에는 색인된 개별 단어 사이에서 LIKE 'keyword%'처럼 prefix search를 한다고 생각하면 쉽다.

MATCH AGAINT는 MySQL 5.5 이하에서는 MyISAM에서만 사용할 수 있으며, MySQL 5.6부터는 InnoDB에서도 사용할 수 있다.

## 기타 전문 검색 방법

최근엔 검색 엔진과 MySQL을 연동하기도 쉬워졌다. MySQL과 연동할 수 있는 검색 엔진으로 ElasticSearch, Sphinx, Solr과 같은 것들이 있다. 필자가 직접 사용해 보지는 않았지만, Sphinx와 MySQL을 연동하는 튜토리얼을 읽어보니 매우 좋은 기능 같았다. 한글도 무리 없이 검색할 수 있었고, 검색 속도 또한 빨랐다.
