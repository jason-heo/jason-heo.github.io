---
layout: post
title: "이전, 이후 레코드 찾기 (MySQL HANDLER)"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20277168/how-i-get-the-preview-product-and-next-product-in-mysql/20277284

## SQLFiddle URL

http://www.sqlfiddle.com/#!2/d7066/1

## 질문

다음과 같은 테이블이 있다.

    mysql> SELECT * FROM p;
    +----+---------------+
    | id | products_name |
    +----+---------------+
    |  1 | a             |
    |  2 | b             |
    |  4 | d             |
    |  5 | ed            |
    |  7 | gs            |
    |  8 | d             |
    |  9 | f             |
    | 11 | f             |
    +----+---------------+
    8 rows in set (0.00 sec)

주어진 id의 전후 레코드를 조회하고 싶다. 예를 들어 현재 id가 7인 경우, id 7의 전 레코드인 5와 후 레코드인 8을 조회하면 된다. 어떻게 할 수 있는가? id는 AUTO_INCREMENT 컬럼이므로 증가하기만 한다.

{% include adsense-content.md %}

## 답변

우선 질문자가 채택한 답변은 다음과 같다.

    SELECT t2.type, t2.id, t1.products_name
    FROM p t1 INNER JOIN
    (
      SELECT 'prev_row' AS type, MAX(id) AS id FROM p WHERE id < 7
      UNION ALL
      SELECT 'next_row' AS type, MIN(id) AS id FROM p WHERE id > 7
    ) t2 ON t1.id = t2.id;
     
    +----------+------+---------------+
    | type     | id   | products_name |
    +----------+------+---------------+
    | prev_row |    5 | ed            |
    | next_row |    8 | d             |
    +----------+------+---------------+
    2 rows in set (0.00 sec)

MIN, MAX를 이용하여 이전/이후 레코드의 id를 검색한 뒤 UNION으로 묶은 후 원본 테이블과 JOIN하여 테이블의 다른 컬럼을 조회하는 방법이다. 이 자체로 좋은 답변이라고 생각되지만 필자는 다른방법을 제안하고자 한다.

MySQL에서 제공하는 특별한 기능인 HANDLER를 이용하는 방법이다. HANDLER는 MySQL 테이블에 직접 접근할 수 있는 인터페이스이다. MySQL 에서만 제공하는 방법이기 때문에 별로 좋은 방법은 아닐 수 있다. 본 질문의 경우 표준 SQL로도 충분히 구할 수 있으므로 굳이 HANDLER를 이용할 이유는 없다. 본 답변에서는 HANDLER의 기본적인 사용 방법을 설명하고자 한다.

HANDLER는 열기(OPEN), 읽기(READ), 닫기(CLOSE) 단계로 수행된다. 최초 읽기 단계에서 읽을 때 사용할 INDEX를 지정할 수 있다. 이 단계에서는 INDEX의 값을 명시하여 내가 조회하고자 하는 레코드로 바로 이동할 수 있다. 아래의 예는 테이블을 "READ `PRIMARY` = (7)"을 이용하여 id=7인 레코드를 읽기 시작한다. "READ NEXT"를 통하여 다음 레코드를 읽고 있으며 "READ PREV 1, 1"을 2개 이전의 레코드를 읽는다. 이 레코드가 id=7인 레코드의 이전 레코드이다.

### HANDLER OPEN

    mysql> HANDLER p OPEN;
    Query OK, 0 rows affected (0.00 sec)
    HANDLER READ

    mysql> HANDLER p READ `PRIMARY` = (7);
    +----+---------------+
    | id | products_name |
    +----+---------------+
    |  7 | gs            |
    +----+---------------+
    1 row in set (0.00 sec)

### HANDLER READ NEXT

    mysql> HANDLER p READ `PRIMARY` NEXT;
    +----+---------------+
    | id | products_name |
    +----+---------------+
    |  8 | d             |
    +----+---------------+
    1 row in set (0.00 sec)

### HANDLRE READ PREV

    mysql>  HANDLER p READ `PRIMARY` PREV LIMIT 1,1;
    +----+---------------+
    | id | products_name |
    +----+---------------+
    |  5 | ed            |
    +----+---------------+

### HANDLER CLOSE

    mysql> HANDLER p CLOSE;
    Query OK, 0 rows affected (0.00 sec)
