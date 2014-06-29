---
layout: post
title: "You can't specify target table 'table_name' for update in FROM clause"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20297204/update-all-recored-below-current-record-given-by-id/20297303

SQLFiddle URL

http://www.sqlfiddle.com/#!9/79067/1

## 질문

다음과 같은 UPDATE문을 실행하면 오류가 발생한다.

    mysql> UPDATE tbl SET score = score - 1
        -> WHERE score > (SELECT score FROM tbl WHERE id = 1);
    ERROR 1093 (HY000): You can't specify target table 'tbl' for update in FROM clause

해결할 수 있는 방법은 무엇인가?

{% include adsense-content.md %}

## 답변

MySQL에서는 테이블을 UPDATE할 때 WHERE 절에 사용된 sub-query에 동일한 테이블 이름이 나오면 안 된다. 필자의 생각으로는 correlated sub-query만 아니면 될 것 같지만 MySQL은 이를 허용하지 않고 있다.

### 내가 쓴 답변

필자는 다음과 같이 sub-query가 아닌 MULTIPLE UPDATE를 이용한 SQL을 제안했다.

    UPDATE tbl t1, tbl t2
    SET t2.score = t2.score -1
    WHERE t2.score > t1.score
      AND t1.id = 1;

### 다른 사람의 답변

우선 질문자가 채택한 답변은 다음과 같다. sub-query를 한번 더 감싸서 inline view로 만드는 방법이다. SELECT의 결과가 inline view에 저장되게 되므로 원래 테이블과 의존성이 끊겨지는 듯 하다. (MySQL 좀 바보 같은 느낌?)

    UPDATE tbl
    SET score = score -1
    WHERE score > (
        SELECT t.score FROM (SELECT score FROM tbl WHERE id = 1) t
    );

### user variable 사용하기

다음은 sub-query의 결과를 변수에 저장하는 방법이다.

    SET @score = (SELECT score FROM tbl WHERE id = 1);
    UPDATE tbl SET score = score - 1 WHERE score > @score;

