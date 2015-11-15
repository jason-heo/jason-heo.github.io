---
layout: post
title: "SQL on Elasticseach Cli"
categories: elasticsearch
---

Git Repository 바로 가기: https://github.com/mysqlguru/sql-on-es-cli


Elasticsearch에서 Query를 수행하려면 DSL을 작성해야 하는데, RDBMS만 오래 다뤄서 그런가 DSL에 전혀 익숙해지지가 않았다. NoSQL 계역에서도 "SQL On XXX" SQL을 올리는 것이 사용자 친화적이라서 "SQL On Elasticsearch"를 찾아 봤는데 elasticsearch-SQL](https://github.com/NLPchina/elasticsearch-sql/)가 검색되었다.

오... DSL을 사용했던 것보다 역시 SQL을 사용할 수 있으니 편하다... 그런데, `elasticsearch-SQL`이 Web Frontend 밖에 없다보니 CUI를 더 편하게 사용하는 나로서는 이것도 약간 불편해서, CLI 방식의 SQL on ES를 찾아 봤는데 없더라.

그래서 Python으로 간단히 만들어 봤다.

하는 일은 별거 없고, 그냥 사용자로부터 SQL을 입력받아서 `elastisearch-SQL`에 요청 보낸 후 Json 형식을 Table로 출력하는 것만 한다. (Aggregation 결과 출력 때문에 좀 고생했는데, 코딩 잘 하는 사람은 이것도 쉽게 했을 듯)

출력 결과는 다음과 같다

```
SQL> SELECT zipcode, area_depth1 FROM jsheo LIMIT 5
| _id                  | _type   | area_depth1 | zipcode |
|----------------------|---------|-------------|---------|
| AVELB3nkuplgpa0tYzi6 | zipcode |  Gangwon-do |  210821 |
| AVELB3npuplgpa0tYzi8 | zipcode |  Gangwon-do |  210823 |
| AVELB3nruplgpa0tYzi9 | zipcode |  Gangwon-do |  210823 |
| AVELB3nzuplgpa0tYzjB | zipcode |  Gangwon-do |  210822 |
| AVELB3n2uplgpa0tYzjD | zipcode |  Gangwon-do |  210824 |

5 rows printed
2044 docs hitted (0.007 sec)
```
