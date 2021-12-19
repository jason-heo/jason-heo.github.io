---
layout: post
title: "MySQL Natural sort (제목으로 정렬하기)"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20215619/sorting-varchar-column/20215692

## 질문

다음과 같은 데이터가 있다. `ORDER BY title`로 정렬하는 경우 원하는 결과가 출력되지 않고 있다.

    mysql> SELECT * FROM chapter ORDER BY title;
    +------------+
    | title      |
    +------------+
    | Chapter 1  |
    | Chapter 11 |
    | Chapter 12 |
    | Chapter 2  |
    | Chapter 3  |
    | Chapter 4  |
    +------------+
    6 rows in set (0.00 sec)

내가 원하는 결과는 다음과 같다. 어떻게 할 수 있는가?

    +------------+
    | title      |
    +------------+
    | Chapter 1  |
    | Chapter 2  |
    | Chapter 3  |
    | Chapter 4  |
    | Chapter 11 |
    | Chapter 12 |
    +------------+

{% include adsense-content.md %}

## 답변

`ORDER BY title`로는 질문자가 원하는 결과를 얻을 수 없다. 그렇다고 MySQL이 잘못된 결과를 출력한 것은 아니다. 질문자가 원하는 정렬을 `NATURAL SORT`라고 한다. 필자가 알기로는 MySQL에서 NATURAL SORT를 완벽하게 하는 방법은 없는 것으로 알고 있다.

질문자의 데이터에서는 다음의 SQL로 원하는 결과를 얻을 수는 있다.

    SELECT title
    FROM chapter
    ORDER BY LENGTH(title), title;
     
    +------------+
    | title      |
    +------------+
    | Chapter 1  |
    | Chapter 2  |
    | Chapter 3  |
    | Chapter 4  |
    | Chapter 11 |
    | Chapter 12 |
    +------------+
    6 rows in set (0.00 sec)

역순 정렬도 무리 없이 된다.

    SELECT title
    FROM chapter
    ORDER BY LENGTH(title) DESC, title DESC;
     
    +------------+
    | title      |
    +------------+
    | Chapter 12 |
    | Chapter 11 |
    | Chapter 4  |
    | Chapter 3  |
    | Chapter 2  |
    | Chapter 1  |
    +------------+
    6 rows in set (0.00 sec)

본 질문의 경우 모든 title이 동일한 "Chapter"라는 문자열로 시작했기 때문에 NATUAL SORT가 잘 작동하였다.

NATURAL SORT의 다른 방법으로는 title에서 "Chapter" 문자를 없애버린 뒤에 남아 있는 숫자 부분을 INT형으로 CAST한 뒤에 정렬하는 방법도 있으나 SQL이 복잡해진다는 단점이 있다. 그러나만약 숫자가 0 이상의 양수가 아닌 음수를 포함하는 경우는 방금 설명한 것과 같이 문자열을 모두 제거한 뒤 숫자 부분만 가지고 정렬하도록 해야 할 것이다.

### 완벽한 NATURAL SORT는 불가능하다

본 질문의 예에서 사용된 데이터는 포맷이 일정하기 때문에 NATURAL SORT가 잘 되었다. 다시 한번 말하지만, NATURAL SORT를 항상 쉽게 할 수 있는 것은 아니다. 오름차순으로 정렬이 잘 되더라도내림차순으로는 정렬이 안 되는 경우도 있다. 완벽한 NATURAL SORT가 필요한 경우는 정렬을 위한 필드를 사용하는 것이 좋다.

예를 들어, "터미네이터 1", "터미네이터 2"과 같이 정렬하기 쉬운 제목도 있지만, 맷 데이먼의 "본 시리즈"의 경우는 "본 아이덴터티", "본 슈프리머시"와 같이 영화 제목을 정렬에 사용할수 없다. 영화 테이블에는 "개봉일" 필드가 존재할 것이므로 개봉일을 정렬 키로 사용하는 것이 좋다. 마찬가지로 음반의 경우 발매일이 존재할 것이다. 정렬을 위한 필드가 없는 경우는 정렬을 위한 필드를 추가적으로 생성한 뒤에 관리자가 제목 별로 정렬 값을 할당해 주는 것이 좋다.

{% include mysql-reco.md %}
