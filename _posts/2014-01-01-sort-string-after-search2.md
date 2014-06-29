---
layout: post
title: "MySQL 문자열 검색에서 검색 적합도 순으로 정렬하기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20413033/most-relevent-results-php-mysql/20414071

## 질문

다음과 같은 PHP 코드를 이용하여 검색을 하고 있다.

```php
$search = $_GET['search'];
$key_terms = explode(" ", $search);
$query = "SELECT * FROM sentence WHERE";
 
foreach ($key_terms as $each)
{
    if($each != '')
    {
        $i++;
 
        if($i == 1)
        {
            $query .= " keywords LIKE '%$each%' ";
        }
        else
        {
            $query .= "OR keywords LIKE '%$each%' ";
        }
    }
}
 
$result_query = mysql_query($query);
```

검색 결과 출력을 적합도(Relevance Score) 순으로 정렬하고자 하는데 어떻게 하는 것이 좋은가.

{% include adsense-content.md %}

## 답변

우선 앞의 PHP 코드는 공백으로 구분된 단어들을 단어간 OR 연산으로 keywords 컬럼에서 검색하는 코드이다. 예를 들어, 사용자가 "이효리 한예슬 전지현"을 입력하면 다음과 같은 SQL문을 생성한다.

```sql
SELECT *
FROM sentence
WHERE keywords LIKE '%이효리%'
  OR keywords LIKE '%한예슬%'
  OR keywords LIKE '%전지현%';
```

질문자는 적합도 순으로 정렬을 하고 싶다고 했지만 정작 본인이 생각하는 적합도에 대해 설명하지 않았다. Relevance Score라고 하는 적합도는 검색 시 문서가 사용자 의도를 얼마나 잘 내포하는지 척도이다. 적합도를 이용하여 랭킹을 계산한 뒤 랭킹 순위에 따라 문서가 출력된다. 적합도를 어떻게 계산하는지가 검색 엔진의 핵심 중의 하나라고 할 수 있다. 하지만 MySQL의 LIKE를 이용하여 검색할 때적합도를 정의할 방법은 많지 않다. 앞에서 검색 문자열이 문서 내에 앞에 위치한 것을 먼저 출력하는 것도 하나의 적합도가 될 수 있다. 본 질문에서는 다음과 같이 2가지 적합도를 계산해 보고자 한다.

### 중복 제거 단어 포함 횟수

```sql
SELECT keywords,
  (keywords LIKE '%이효리%') + (keywords LIKE '%한예슬%') + (keywords LIKE '%전지현%') AS score
FROM sentence
WHERE keywords LIKE '%이효리%'
  OR keywords LIKE '%한예슬%'
  OR keywords LIKE '%전지현%'
ORDER BY score DESC;
```

keyword가 '이효리', '한예슬', '전지현'을 포함한 횟수를 적합도로 계산하였다. (keywords LIKE '%이효리%')는 참, 거짓을 반환하는 표현식이다. keywords가 '이효리'를 포함했으면 1을 반환하고,'이효리'를 포함하지 않았다면 0을 반환한다. 이 적합도는 문서가 '이효리'를 여러 번 포함하더라도 1만 계산 된다. 3개 단어를 모두 포함한 문서는 3점 만점을 얻게 된다.

### 중복 허용 단어 포함 횟수

```sql
SELECT keywords,
  (
    ((LENGTH(keywords) - LENGTH((REPLACE(keywords, '이효리', '')))) / LENGTH('이효리'))
    + ((LENGTH(keywords) - LENGTH((REPLACE(keywords, '한예슬', '')))) / LENGTH('한예슬'))
    + ((LENGTH(keywords) - LENGTH((REPLACE(keywords, '전지현', '')))) / LENGTH('전지현'))
  ) AS score
FROM sentence
WHERE keywords LIKE '%이효리%'
  OR keywords LIKE '%한예슬%'
  OR keywords LIKE '%전지현%'
ORDER BY score DESC
```
 
검색어가 문자열 내에 포함된 횟수를 계산하는 방법을 이용하였다. 문서가 '이효리'를 3번 포함하고, '한예슬', '전지현'을 전혀 포함하지 않았더라도 3점의 적합도를 얻는다. '이효리', '한예슬', '전지현'을 각각 한번씩 포함하더라도 3점의 적합도를 얻는다.

