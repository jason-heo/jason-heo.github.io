---
layout: post
title: "INSERT INTO SELECT 사용 시 AUTO_INCREMENT 컬럼 값 지정하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

다음과 같은 INSERT 문을 이용하여 SELECT의 결과를 테이블에 INSERT하고 있다.

```sql
INSERT INTO tab
SELECT LAST_INSERT_ID(), B.id, C.name, C.address,
FROM sample_table C JOIN other_table B ON B.phoneNumber = C.phoneNumber;
```

첫 번째 컬럼이 AUTO_INCREMENT 컬럼인데 위와 같이 SELECT 시에 `LAST_INSERT_ID()`를 지정했는데도 자동 증가된 값이 INSERT되지 않고 있다. `LAST_INSERT_ID()`는 계속하여 1만 반환하고있다. 무엇이 문제인가?

## 질문

{% include adsense-content.md %}

## 답변

`LAST_INSERT_ID()` 함수는 가장 최근에 AUTO_INCREMENT 컬럼에 할당되었던 값을 반환하는 함수일 뿐, 앞으로 할당될 값을 반환하는 함수가 아니다. INSERT 시에 AUTO_INCREMENT 컬럼에 NULL값을 부여하여 생성되는 매 레코드마다 새로운 값이 할당되도록 할 수 있다.

```sql
INSERT INTO A
SELECT NULL, B.id, C.name, C.address
FROM sample_table C JOIN other_table B ON B.phoneNumber = C.phoneNumber;
```

혹은 INSERT 문에 컬럼 명을 명시적으로 적되, AUTO_INCREMENT 컬럼 명만 제외하는 것도 방법이다.

```sql
INSERT INTO A (bid, name, address)
SELECT B.id, C.name, C.address
FROM sample_table C JOIN other_table B ON B.phoneNumber = C.phoneNumber;
```
