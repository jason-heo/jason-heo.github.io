---
layout: post
title: "MySQL MAX 값을 동일 테이블에 INSERT하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20343239/1093-you-cant-specify-target-table-bookings-for-update-in-from-clause/20343381

## 질문

다음과 같이 bookings 테이블의 MAX(expire_date)를 구한 뒤 5일을 더한 값을 다시 bookings 테이블에 INSERT하려고 한다.

    mysql> INSERT INTO bookings (book_id, member_id, expire_date)
        -> VALUES  (1, 3, (SELECT Max(expire_date)
        ->               FROM   bookings
        ->               WHERE  member_id = 1) + 5
        ->         );
     
    ERROR 1093 (HY000): You can't specify target table 'bookings' for update in FROM clause

그런데 보다시피 에러가 발생하고 있다.

{% include adsense-content.md %}

## 답변

다음과 같이 INSERT INTO SELECT 문으로 변경하면 된다.

```sql
INSERT INTO bookings
    (book_id, member_id, expire_date)
SELECT
    1,3, MAX(expire_date) + INTERVAL 5 DAY
FROM bookings
WHERE member_id = 1;
```


{% include mysql-reco.md %}
