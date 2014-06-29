---
layout: post
title: "MySQL correlated sub-query 성능 향상 방법"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL
http://stackoverflow.com/questions/20026681/counting-rows-via-php-is-faster-than-count-in-sql/20038081

## 질문

다음과 같은 SQL을 사용 중에 있는데 속도가 느리다. 5초 정도 소요되며 늦는 경우 13초 이상도 걸린다. 동일한 결과를 출력하는 PHP를 작성했는데 이는 0.3초 정도 소요된다. 좀 더 빠르게 할 수 있는방법은 없는가?

### SELECT 문 (5~13초)

```sql
SELECT r.x, r.y FROM `base` AS r
WHERE r.l=50 AND AND r.n<>'name' AND 6 = (
    SELECT COUNT(*)
    FROM surround AS d
    WHERE d.x >= r.x -1 AND d.x <= r.x +1
      AND d.y>=r.y -1 AND d.y<=r.y +1 AND d.n='name'
);
```

### PHP 코드 (0.3초)

```sql
$q="SELECT x,y FROM `base` WHERE l=50 AND n<>'name'";
 
$sr=mysql_query($q);
 
if(mysql_num_rows($sr)>=1)
{
    while($row=mysql_fetch_assoc($sr))
    {
        $q2="SELECT x,y FROM surround WHERE n='name' AND x<=".
            ($row["x"]+1)." AND x>=".($row["x"]-1).
            " AND y<=".($row["y"]+1)." AND y>=".($row["y"]-1)." ";
 
        $sr2=mysql_query($q2);
 
        if(mysql_num_rows($sr2)=6)
        {
            echo $row['x'].','.$row[y].'\n';
        }
    }
}
```

### 테이블 구조

```sql
    CREATE TABLE IF NOT EXISTS `base` (
      `bid` int(12) NOT NULL COMMENT 'Base ID',
      `n` varchar(25) NOT NULL COMMENT 'Name',
      `l` int(3) NOT NULL,
      `x` int(3) NOT NULL,
      `y` int(3) NOT NULL,
      `LastModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      UNIQUE KEY `coord` (`x`,`y`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
     
     
    CREATE TABLE IF NOT EXISTS `surround` (
      `bid` int(12) NOT NULL COMMENT 'Base ID',
      `n` varchar(25) NOT NULL COMMENT 'Name',
      `l` int(3) NOT NULL,
      `x` int(3) NOT NULL,
      `y` int(3) NOT NULL,
      `LastModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      UNIQUE KEY `coord` (`x`,`y`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

### EXPLAIN SELECT의 결과

    id  select_type         table   type    possible_keys   key     key_len    ref     rows    Extra  
    1   PRIMARY             r       range   coord,n         coord   4          NULL    4998    Using where
    2   DEPENDENT SUB-QUERY  d       ALL     coord           NULL   NULL        NULL    57241   Range checked for each record (index map: 0x1)

{% include adsense-content.md %}

## 답변

질문자의 correlated sub-query는 다음과 같은 JOIN 문으로 변경될 수 있다.

```sql
    SELECT r.x, r.y
    FROM `base` AS r, surround AS d
    WHERE r.l=50
      AND r.n <>'name'
      AND d.x >= r.x -1
      AND d.x <= r.x +1
      AND d.y >= r.y -1
      AND d.y <= r.y +1
      AND d.n = 'name'
    GROUP BY r.x, r.y
    HAVING COUNT(*) = 6
```

질문자는 앞의 JOIN문을 이용하여 0.15초로 단축되었다고 한다. 물론 원래의 correlated sub-query와 동일한 결과를 출력하면서 말이다. 5초 걸리던 질의가 0.15초로 단축되었으니 약 33배 빨라졌다.

인터넷 자료를 보면 막연하게 "JOIN은 느리다", "차라리 SELECT를 여러 번 실행하는 것이 좋다", "JOIN은 성능이 안 좋으니 반정규화를 하는 것이 좋다"라는 글을 보게 된다. 일부 맞는 말이기도 하지만, 100% 맞는 말은 아니다. JOIN이 발생하는 이유는 정규화가 잘 되었다는 이야기이며 설계가 잘된 DB라고 볼 수 있다. 대용량인 경우 성능을 위해 어쩔 수 없이 반정규화를 할 수 있지만, 반정규화는 마지막보루로 삼아야 한다.

MySQL에서 correlated sub-query의 성능은 좋지 못하기 때문에 correlated sub-query를 JOIN으로 변경할 수 있는 경우 JOIN으로 변경하는 것이 좋다. sub-query가 느린 것이 아니라 correlated sub-query가 느리다는 이야기이니 오해 없기 바란다. correlated sub-query는 SQL 수행 시 매 row마다 correlated sub-query가 1번씩 수행되기 때문에 JOIN보다 빠를 수 없다.
