---
layout: post
title: "ElasticSearch의 Bulk Loading with Python"
categories: programming
---

ElasticSearch를 사용하기에 이른다...
---------------------------

최근 들어 분석 업무를 위해 `GROUP BY` 연산이 빠른 DB를 찾아야 했는데 이리 저리 자료를 찾다보니 ElasticSearch의 Aggregation 속도가 장난 아니게 빠르다는 이야기를 들었다. 예를 들어 동일한 장비에서 동일한 자료에 대한 Aggregation 속도가 MySQL에서는 1분 걸리던 것이 ElasticSearch에서는 1.x 초 걸렸다는 것이다.

무슨 마법이 있길래 이렇게 빠를 수 있는 것인가... 어쨌든 의심반 기대반으로 ElasticSearch를 설치해봤다.

설치 및 사용기는 별도의 포스트로 올리기로 하고 암튼 결과는 대만족이다! 미라클~ 원더풀~

LogStash는 Bulk Load를 지원하지 않았다.
---------------------------------------

흔히 말하는 ELK Stack에서 L은 Logstash인데 이것도 완전 신세경이었다. 그런데 나의 경우 기존에 존재하는 Data를 ElasticSearch (이하 ES)로 빠르게 로딩을 해야 했는데 아쉽게도 LogStash가 Bulk Load를 지원하지 않았다.

여기서 말하는 Bulk Load는 일반적인 의미의 대량 입력이 아니라 ES가 지원하는 [Bulk API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html)를 의미한다.

ES의 Bulk Load
-------------

여기서 말하는 Bulk Load라 함은 1번의 HTTP PUT 요청으로 n개의 Document를 색인 요청하는 것을 의미한다. 즉, 100개의 문서를 색인할 때 1건 씩 100번을 요청하는 것 보다 100개의 문서를 1번에 요청하는 것을 의미하는데 당연히 후자의 성능이 빠르다.

그런데 문제는 ES 문서가 친절하지 않다는 것이다ㅠㅠ

Python으로 Bulk Loading하기
-----------------------

하지만 답은 언제나 구글과 Stackoverflow에 있었다. 나처럼 Bulk API 사용에 어려움이 있는 사람이 [질문](http://stackoverflow.com/questions/20288770/how-to-use-bulk-api-to-store-the-keywords-in-es-by-using-python)을 했었고, 다음과 같이 쉬운 방법으로 Bulk Load를 수행할 수 있었다. (출처는 위의 질문을 클릭하면 된다.)

```python
from datetime import datetime

from elasticsearch import Elasticsearch
from elasticsearch import helpers

es = Elasticsearch()

actions = []

for j in range(0, 10):
    action = {
        "_index": "tickets-index",
        "_type": "tickets",
        "_id": j,
        "_source": {
            "any":"data" + str(j),
            "timestamp": datetime.now()
            }
        }

    actions.append(action)

if len(actions) > 0:
    helpers.bulk(es, actions)
```

HTTP 429 에러 처리하기
----------------

그렇다고 해서 Logstash에 비해서 아주 드라마틱하게 성능이 향상되진 않았다. 동시 입수 process 개수를 많이 늘릴 수록 qps가 많이 증가되긴 하는데, 동시 요청 개수가 일정 이상 증가하면 HTTP 429 error가 발생한다.

429 error에 대응하기 위해선 이번 HTTP 요청에서 실패한 문서들을 다시 요청보내거나 하는 등의 특별 처리가 필요하다. Bulk 요청이 아니라 1개 씩만 보냈을 때 429 error가 발생하면 해당 요청을 다시 보내면 끝이지만, Bulk Load 시에는 n개를 요청하고, 그 중 일부가 fail 나기 때문에 좀 귀찮은 작업이 필요했다.

나는 429 error 없을 정도의 process 개수만큼으로 줄여서 보냈는데 이렇게 해도 충분히 빠르다 생각해서 429 error 처리는 하지 않았다. 나중에 시간나면 처리해야지 (라고 적고 아마 안 할 가능성도 있음)
