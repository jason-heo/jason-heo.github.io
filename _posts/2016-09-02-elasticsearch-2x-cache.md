---
layout: post
title: "Elasticsearch 2.x의 Query Cache에 관하여"
categories: "elasticsearch"
---

본 글은 Elasticsearch (이하ES) 2.x의 Query Cache에 대해 정리한 글이다.

ES를 사용하면서 RDBMS와 개념이 약간 다른 것 때문에 헷갈리는 게 있는데, 그 중에서도 제일은 Query Cache라 생각한다.

ES의 Cache 종류
--------------

ES에는 다음과 같은 2개의 Cache가 존재한다. (이외에도 더 많은 종류가 있을 수 있으나 Query에 관련된 Cache는 2가지)

- [Node Query Cache](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-cache.html)
- [Shard Request Cache](https://www.elastic.co/guide/en/elasticsearch/reference/current/shard-request-cache.html)

흔히 생각하기엔 무릇 Query Cache라 함은 Query의 output을 저장한다고 생각하게 되는데 Node Query Cache는 그런 용도가 아니었습니다.

Node Query Cache
----------------

Node Query Cache는 용어가 헷갈리기 때문에 이해하기 어려웠다. ES의 DSL(Domain Specific Language)는 다음과 같이 생겼다.
```
{
    "query": {
        "match": {
            "text": "iphone"
        }
    },
    "aggregations": {
        "sample": {
            "sampler": {
                "shard_size": 200,
                "field" : "user.id"
            },
            "aggregations": {
                "keywords": {
                    "significant_terms": {
                        "field": "text"
                    }
                }
            }
        }
    }
}
```

DSL이 `query`와 `aggregations` 2개 부분으로 나뉜 것을 볼 수 있다. 여기서 "Query Cache"라고 생각하면 `query`와 `aggregations`를 포함한 전체 DSL의 결과가 Cache된다고 생각하기 쉽다. 하지만, Node Query Cache에서 말하는 Query란 DSL 전체가 아니라 DSL 안의 `query`를 의미한다. 따라서, query의 결과만 저장되게 되며 agregation은 매번 수행된다.

### Node Query Cache에 저장되는 내용

Query Cache에는 뭐가 저장될가? 이를 이해하기 위해선 ES의 질의 처리 방법을 이해해야 한다. [Internal Filter Operation](https://www.elastic.co/guide/en/elasticsearch/guide/current/_finding_exact_values.html#_internal_filter_operation) 문서에 자세히 설명되어 있다. 요약을 해보자면, query에는 field들의 조건이 나열되는데, 각각의 조건들은 bitset을 만들고, 이 bitset들끼리 intersect를 해서 최종적인 document의 id를 얻게 된다.

(확실치 않으나) Node Query Cache에는 이들 Bit Set이 저장되는 것으로 보인다. (개별 조건들의 bitset이 cache되는지, 모든 조건들을 만족하는 bitset이 cache되는지는 확실치 않지만, 개인적인 생각으로는 개별 조건의 bit set이 cache될 것 같다)


Shard Request Cache
-------------------

Shard Request Cache를 이해하기 위해서 ES Query가 어떤 식으로 수행되는지 이해해야 한다.

![ES Query](https://www.elastic.co/guide/en/elasticsearch/guide/current/images/elas_0901.png)
(출처: https://www.elastic.co/guide/en/elasticsearch/guide/current/_query_phase.html)

user가 입력한 query는 coordination node가 받아서 각 shard가 처리한 뒤에 다시 coordinator가 집계한다. 이때 shard가 요청받은 DSL의 output을 cache하는 것이 Shard Request Cache이며, Aggregation 결과도 Cache가 된다. (즉, 우리가 흔히 생각하는 query cache이다)

Cache의 key는 DSL 문자열이다. 따라서 같은 조건의 json이더라도 필드 순서가 바뀌면 cache를 탈 수 없다. Cache의 Value는 DSL의 output이 저장된다.

### Cache 조건

- DSL의 top level에 `size=0`만 cache된다.
- `now`를 사용하는 query는 cache가 안 된다.
- `hits`는 cache하지 않는다.


Cache별 활성 여부 및 default size
--------------------------------

- Node Query Cache는 기본으로 활성화되어 있으며 기본값은 heap size의 10%이다.
- Shard Request Cache는 ES 2.x까지는 disabled되어 있으며 default value는 heap size의 1%이다. ES 5.0부터는 기본으로 enabled되었다.

### Shard Request Cache 활성화 시키기

- index 단위로 활성화 여부를 지정할 수 있다.
```
 curl -XPUT localhost:9200/my_index -d'
  {
    "settings": {
      "index.requests.cache.enable": true
    }
  }
  '
```
- enabling caching per request
	- disabled되었더라도 `_search?request_cache=true`처럼 질의하면 cache된다.
	- 반대로 enabled되었더라도 `_search?request_cache=false`를 주면 cache되지 않는다. 

Cache 사용량 모니터링
--------------------

- index 별 cache 통계: http://localhost:9200/_stats/request_cache,query_cache?pretty&human
- 특정 index의 cache 통계: http://localhost:9200/index_name/_stats?pretty&human
- node별 cache 통계: http://localhost:9200/_nodes/stats/indices/request_cache,query_cache?pretty&human
