---
layout: post
title: "MySQL sub-query를 이용한 DELETE 오류 시 수정 방법"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20119211/mysql-delete-rows-with-sub-query/20119336

## 질문

다음과 같은 DELETE문을 작성했는데 "ERROR 1064 (42000): You have an error in your SQL syntax" 에러가 발생한다. 무엇이 문제인가?

```sql
DELETE FROM LINKS t1
WHERE EXISTS (
    SELECT *
    FROM LINKS t2
    WHERE t2.cntid = t1.cntid
        AND t2.title = t1.title
        AND t2.lnkid > t1.lnkid
);
```

{% include adsense-content.md %}

## 답변

질문자의 경우 `"DELETE FROM LINKS t1..."` 처럼 LINKS를 t1으로 alias하였다. 이것이 문제이다. 문제를 단순화하기 위하여 다음과 같은 DELETE문만 수행해 보았다.

    mysql> DELETE FROM LINKS t1;
    ERROR 1064 (42000): You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 't1' at line 1

보다시피 테이블을 1개만 지정했더라도 alias를 한 경우는 다음처럼 "DELETE"와 "FROM" 사이에 테이블 이름을 적어야 한다.

    mysql> DELETE t1 FROM LINKS t1;
    Query OK, 1 row affected (0.00 sec)

질문자의 원래 질의는 다음과 같이 수정되어야 한다.

```sql

DELETE t1 FROM LINKS t1
WHERE EXISTS (
    SELECT *
    FROM LINKS t2
    WHERE t2.cntid = t1.cntid
      AND t2.title = t1.title
      AND t2.lnkid > t1.lnkid
);
```

하지만 앞의 변환된 DELETE문을 실행하면 다음과 같은 오류를 보게 된다.

    ERROR 1093 (HY000): You can't specify target table 't1' for update in FROM clause

DELETE될 테이블이 correlated sub-query에 사용되었기 때문이다.

결론은 다음과 같이 일반적인 JOIN을 이용한 DELETE문으로 변경되어야 한다.

```sql
DELETE t1
FROM LINKS t1, LINKS t2
WHERE t2.cntid = t1.cntid
    AND t2.title= t1.title
    AND t2.lnkid > t1.lnkid
```

질문자의 경우 위와 같이 변경하여 DELETE문은 수행되었지만 삭제될 레코드가 수백만 건이라서 DELETE하는데 시간이 많이 걸린다고 하였다. 이 경우 DELETE를 SELECT로 변경한 뒤 EXPLAIN SELECT를 이용하여 INDEX는 잘 생성되어 있는지, Optimizer가 JOIN 순서는 잘 결정했는지 확인해 보는 것이 좋다.
