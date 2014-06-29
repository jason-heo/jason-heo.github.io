---
layout: post
title: "FOREIGN KEY 제약 조건을 위배하는 레코드 찾기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20391652/find-which-rows-where-foreign-key-constraint-fail/20391825

## 질문

이미 존재하는 테이블에 FOREIGN KEY를 추가하는 경우 다음과 같은 오류가 발생한다.

    SQLSTATE[23000]: Integrity constraint violation: 1452 Cannot add or update a child row: a foreign key constraint fails

child 테이블에 존재하는 레코드 중에서 parent에 존재하지 않는 값을 reference하는 레코드가 존재하기 때문인데, 이런 레코드를 찾아서 삭제하려고 한다. 쉽게 찾을 수 있는 방법은 없는가?

{% include adsense-content.md %}

## 답변

LEFT JOIN을 이용하면 쉽게 찾을 수 있다. 우선 질문자가 테이블 스키마를 언급하지 않았기 때문에 다음과 같은 테이블이 존재한다고 가정하겠다.

    parent (parent_id (PK), name)
    child (child_id, name, parent_id (FK));

앞과 같은 스키마에서는 다음과 같은 LEFT JOIN으로 FOREIGN KEY 조건을 위배하는 레코드를 찾을 수 있다.

    SELECT child.child_id, child.parent_id
    FROM child LEFT JOIN parent ON child.parent_id = parent.parent_id
    WHERE parent.parent_id IS NULL;

물론 NOT IN, NOT EXISTS를 사용해도 동일한 결과를 얻을 수 있다.

    SELECT child_id, parent_id
    FROM child
    WHERE parent_id NOT IN (SELECT parent_id FROM parent);
 
    SELECT child_id, parent_id
    FROM child
    WHERE NOT EXISTS (SELECT 1 FROM parent WHERE parent.parent_id = child.parent_id);

