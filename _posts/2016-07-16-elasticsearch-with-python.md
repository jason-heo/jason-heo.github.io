---
layout: post
title: "Elasticsearch와 Python 연동"
categories: "elasticsearch"
---

Rest API만을 이용하여 Elasticsearch(이하 ES)를 사용하는 것은 불편하지만, Python ES API를 이용하면 Elasticsearch를 좀 더 편하게 사용할 수 있다.

본 문서는 Python ES API [공식 메뉴얼](https://elasticsearch-py.readthedocs.io/en/master/api.html)를 참고하면서 사용해본 경험을 정리한 문서이다

Python ES API 설치
-----------------

`import elasticsearch`를 입력했는데, 아래와 같이 모듈을 찾지 못하는 경우 우선 Python ES API를 먼저 설치해야 한다.

```
>>> import elasticsearch
Traceback (most recent call last):
  File "<stdin>", line 1, in <module>
ImportError: No module named elasticsearch
```

Python ES API는 `pip`를 이용하면 쉽게 설치할 수 있다.

```
$ pip install elasticsearch
```

`pip`가 설치되지 않은 경우는 각자 사용 중인 패키지 매니저를 이용하여 설치할 수 있다. 예를 들어 yum을 사용 중이라면

```
$ yum install python-pip
```

을 이용면 되고, 혹은 easy_install을 이용하여

```
$ easy_install pip
```

혹은 Mac 사용자라면  brew로 python을 설치해도 된다.

```
$ brew install python
```

Sample Data Loading
-------------------

Test에 사용할 Sample Data는 [Elasticsearch 문서](https://www.elastic.co/guide/en/elasticsearch/reference/current/_exploring_your_data.html)의 것을 활용하며 포맷은 아래와 같다.

    {
        "account_number": 0,
        "balance": 16623,
        "firstname": "Bradshaw",
        "lastname": "Mckenzie",
        "age": 29,
        "gender": "F",
        "address": "244 Columbus Place",
        "employer": "Euron",
        "email": "bradshawmckenzie@euron.com",
        "city": "Hobucken",
        "state": "CO"
    }

[accounts.zip](https://github.com/bly2k/files/blob/master/accounts.zip?raw=true)을 download 한 뒤 압축을 풀고, 아래의 명령으로 Bulk Loading을 하면 된다.

    curl -XPOST 'localhost:9200/bank/account/_bulk?pretty' --data-binary "@accounts.json"

문서 id로 문서 조회 - get() 함수
--------------

가장 기초 기능으로서 문서 id를 이용하여 문서를 조회하는 방법을 알아보자. shell에서 조회한다면 다음과 같은 명령에 대응될 것이다.

```
$ curl -XGET http://localhost:9200/bank_version1/account/100?pretty
{
  "_index" : "bank_version1",
  "_type" : "account",
  "_id" : "100",
  "_version" : 1,
  "found" : true,
  "_source" : {
    "account_number" : 100,
    "balance" : 29869,
    "firstname" : "Madden",
    "lastname" : "Woods",
    "age" : 32,
    "gender" : "F",
    "address" : "696 Ryder Avenue",
    "employer" : "Slumberia",
    "email" : "maddenwoods@slumberia.com",
    "city" : "Deercroft",
    "state" : "ME"
  }
}
```

shell에서도 조회가 잘 되는데 뭐하러 귀찮게 Python으로 조회하느냐 반문할 수도 있다. ES의 output을 이용하여 뭔가 제어를 하고 싶은 경우가 있는데 curl의 출력 결과는 단순 문자열이기 때문에 후처리를 하기가 어렵다.

본 절에서는 혹시 모를 Python을 전혀 모르는 사용자를 위하여 Python ES API 이야기 외에 Python의 아주 기본적인 내용도 포함하고 있다.

Python을 이용하여 100번 문서를 조회하려면 다음과 같은 code를 작성하면 된다.

```
$ python
Python 2.7.10 (default, Oct 23 2015, 18:05:06)
[GCC 4.2.1 Compatible Apple LLVM 7.0.0 (clang-700.0.59.5)] on darwin
Type "help", "copyright", "credits" or "license" for more information.
>>> import elasticsearch
>>>
>>> es_client = elasticsearch.Elasticsearch("localhost:9200")
>>> doc = es_client.get(index = 'bank_version1', doc_type = 'account', id = '100')
>>>
>>> print doc
{u'_type': u'account', u'_source': {u'city': u'Deercroft', u'firstname': u'Madden', u'lastname': u'Woods', u'age': 32, u'address': u'696 Ryder Avenue', u'employer': u'Slumberia', u'state': u'ME', u'account_number': 100, u'gender': u'F', u'balance': 29869, u'email': u'maddenwoods@slumberia.com'}, u'_index': u'bank_version1', u'_version': 1, u'found': True, u'_id': u'100'}
```

print 결과를 보면 알겠지만, doc 변수의 Type은 String이 아닌 Map 형태이다. email만 출력하고 싶은 경우 다음과 같이 하면 된다.

```
>>> print doc['_source']['email']
maddenwoods@slumberia.com
```

Python의 map을 눈으로 보기 좋게 출력하려면 json 모듈을 사용하면 된다.

```
>>> import json
>>> print json.dumps(doc, indent = 2)
{
  "_type": "account",
  "_source": {
    "city": "Deercroft",
    "firstname": "Madden",
    "lastname": "Woods",
    "age": 32,
    "address": "696 Ryder Avenue",
    "employer": "Slumberia",
    "state": "ME",
    "account_number": 100,
    "gender": "F",
    "balance": 29869,
    "email": "maddenwoods@slumberia.com"
  },
  "_index": "bank_version1",
  "_version": 1,
  "found": true,
  "_id": "100"
}
```

`print doc`보단 훨씬 읽기 좋다.

만약 모든 필드를 조최할 필요없이 `firstname`, `lastname`, `age` 필드만 필요한 경우라면 어떻게 하면 될까? `get()` 함수에 대한 설명은 [여기](https://elasticsearch-py.readthedocs.io/en/master/api.html#elasticsearch.Elasticsearch.get)에서 볼 수 있다. 각자 API Spec을 읽어본 뒤 parameter를 찾아보자.

정답은 `get()` 함수의 `fields` parameter를 활용하는 것이다.

```
>>> print json.dumps(es_client.get(index = 'bank_version1', doc_type = 'account', id = '100', fields = "firstname,lastname,email"), indent = 2)
{
  "_type": "account",
  "_index": "bank_version1",
  "fields": {
    "lastname": [
      "Woods"
    ],
    "email": [
      "maddenwoods@slumberia.com"
    ],
    "firstname": [
      "Madden"
    ]
  },
  "_version": 1,
  "found": true,
  "_id": "100"
}
```

Data Type이 Array로 변경되었는데, 원래 이런 것인지 버그인지는 잘 모르겠다.

한번에 여러 문서 id를 조회하기 - mget() 함수
------------------

조회해야 할 문서 id가 10개라고 하자. `get()` 함수를 10번 호출하는 방법도 있고, `mget()`이라는 함수 (예상할 수 있듯이 `mget()`은 `multiple get`을 의미한다)를 1번만 호출할 수도 있다.

어떤 게 좋을까? 그리고 이유는?

`mget()`이 좋다. 함수를 1번만 호출해서 편하기도 하지만, 성능상의 이점이 훨씬 크다. 이유는 `get()`을 10번 호출하면 Python과 ES 사이에 HTTP 명령이 10번 호출되지만, `mget()`을 사용하는 경우 1번의 HTTP 호출만 있기 때문이다.

`mget()`의 사용 방법은 다음과 같다.

```
>>> print es_client.mget(index = 'bank_version1', doc_type = 'account', body = {'ids': ['100', '101']})
{u'docs': [{u'_type': u'account', u'_source': {u'city': u'Deercroft', u'firstname': u'Madden', u'lastname': u'Woods', u'age': 32, u'address': u'696 Ryder Avenue', u'employer': u'Slumberia', u'state': u'ME', u'account_number': 100, u'gender': u'F', u'balance': 29869, u'email': u'maddenwoods@slumberia.com'}, u'_index': u'bank_version1', u'_version': 1, u'found': True, u'_id': u'100'}, {u'_type': u'account', u'_source': {u'city': u'Manchester', u'firstname': u'Cecelia', u'lastname': u'Grimes', u'age': 31, u'address': u'972 Lincoln Place', u'employer': u'Ecosys', u'state': u'AR', u'account_number': 101, u'gender': u'M', u'balance': 43400, u'email': u'ceceliagrimes@ecosys.com'}, u'_index': u'bank_version1', u'_version': 1, u'found': True, u'_id': u'101'}]}
```

Python을 모는 사람을 위해 좀 더 설명하자면, `mget()`의 결과는 `for`문을 이용하여 탐색할 수 있다.

```
>>> docs = es_client.mget(index = 'bank_version1', doc_type = 'account', body = {'ids': ['100', '101']})
>>> for doc in docs['docs']:
...     print 'firstname is', doc['_source']['firstname']
...
firstname is Madden
firstname is Cecelia
```

문서 검색하기 - search() 함수
-------

`state` 값이 `NY`인 문서는 다음과 같이 검색할 수 있다. code의 양이 길어져서 REPL이 아닌 스크립트로 실행했다.

```
#!/usr/bin/env python

import elasticsearch
import json

es_client = elasticsearch.Elasticsearch("localhost:9200")


docs = es_client.search(index = 'bank_version1',
                       doc_type = 'account',
                       body = {
                           'query': {
                               'match': {
                                   'state': 'NY'
                               }
                           }
                       })

print json.dumps(docs, indent = 2)
```

수행 결과는 다음과 같다

```
{
  "hits": {
    "hits": [
      {
        "_score": 5.199705,
        "_type": "account",
        "_id": "581",
        "_source": {
          ...
          "firstname": "Fuller",
          "state": "NY",
          ...
        },
        "_index": "bank_version1"
      },
      {
        "_score": 5.199705,
        "_type": "account",
        "_id": "464",
        "_source": {
          ...
          "firstname": "Cobb",
          "state": "NY",
          ...
        },
        "_index": "bank_version1"
      },
      ...
```

`docs` 변수를 `for` 문으로 iterate하면 전체 문서를 탐색할 수 있다.

{% include adsense-content.md %}

scroll 기능 활용하기
--------------------

`search()`의 결과가 수백만 건이라고 생각해보자. ES에서 Python으로 return될 문서의 개수가 많기 때문에 응답 시간이 느릴 수 있고, 또한 결과를 모두 Python에 저장하고 있어야 하므로 Memory 부족 현상이 발생할 수 있다.

이러한 경우를 위하여 ES에서는 [Scroll 기능](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-scroll.html)을 제공한다.

Scroll의 작동 방식 최초 search 시에 scroll 기능을 사용하겠다는 것과 몇 개를 fetch해올 것인지를 ES에 전달하면 ES는 match된 모든 Document를 return하지 않고, 요청한 문서 개수만 return한다. 이때 `scroll_id`도 같이 return하는데, Client는 다음 번 요청 시에는 return받은 `scroll_id`만 요청하면 ES는 직전까지 return했던 이후의 Document만 return하는 식이다.

이런 방식이 가능하려면 ES 내부에서는 scroll 정보를 저장해야 하는데, Client는 scroll 생성 시, scroll 정보를 몇 분 유지할 것인지 요청할 수 있다.

Scroll 문서를 읽어보면 알겠지만 사용하기 좀 불편한 기능인지, Python API를 이용하여 쉽게 사용할 수 있다.

```
#!/usr/bin/env python
#-*- encoding: utf8 -*-

import elasticsearch
import json


es_client = elasticsearch.Elasticsearch("localhost:9200")


docs = es_client.search(index = 'bank_version1',
                       doc_type = 'account',
                       body = {
                           'query': {
                               'match': {
                                   'state': 'NY'
                               }
                           }
                       },
                       scroll = '1m',   # scroll 정보를 1분 유지
                       size = 3) # 한번에 fetch해올 문서를 2개로 지정
                                 # 실제 사용 시에는 1,000 정도로 주면 좋다
scroll_id = docs['_scroll_id']

num_docs = len(docs['hits']['hits'])
print "{0} docs retrieved".format(num_docs)

while num_docs > 0:
    docs = es_client.scroll(scroll_id = scroll_id,
                            scroll = '1m')

    num_docs = len(docs['hits']['hits'])
    print "{0} docs retrieved".format(num_docs)
```

수행 결과는 다음과 같다

```
3 docs retrieved
3 docs retrieved
3 docs retrieved
3 docs retrieved
3 docs retrieved
3 docs retrieved
2 docs retrieved
0 docs retrieved
```

`'state': 'NY'` 조건을 만족하는 전체 문서는 총 20개이다. 일반적으로 `size`는 1,000 정도를 주는 것이 좋지만, 여기서는 예를 들기 위해 3으로 설정하였다. 총 20개의 문서를 3개씩 가져오다가 마지막에는 2개를 가져온 뒤 종료한 것을 볼 수 있다.

Bulk Insert
-----------

Bulk Insert란 1번의 API 요청으로 여러 개의 문서를 Insert하는 기능을 말한다. 주로 성능을 위해 Bulk Insert를 사용한다. 1개씩 Insert했을 때 초당 1,000건을 Insert할 수 있다면 Bulk Insert를 이용하면 초당 수만 건도 Insert할 수 있다.

여기서는 `state`가 NY인 임의의 문서 10개를 Bulk로 Insert해보겠다.

```
import elasticsearch
from elasticsearch import helpers

es_client = elasticsearch.Elasticsearch("localhost:9200")

docs = []
for cnt in range(10):
    docs.append({
        '_index': 'bank_version1',
        '_type': 'account',
        '_id': 'new_id_' + str(cnt),
        '_source': {
            'state': 'NY'
        }
    })

elasticsearch.helpers.bulk(es_client, docs)
```

위의 스크립트를 수행한 뒤에, search를 해 보면 새로운 문서가 입력된 것을 볼 수 있을 것이다. (주의: index 설정에 따라 `refresh_interval`이 지난 후에 확인을 해야 입력된 문서가 검색된다.)


기타 - 모든 index 출력하기
------------------

ES에 존재하는 모든 index를 출력할 때 사용하는 방법이다.

```
print es_client.indices.get_alias().keys()
```

작동 원리는 이렇다. 브라우저에서 `http://localhost:9200/_aliases?pretty`를 접속해보면 ES에 존재하는 모든 index와 그들에게 걸린 alias를 출력해 주는데, `get_alias()`의 결과와 동일한다. 여기서 key 부분이 index 이름이므로 `keys()`를 이용하여 key 부분을 출력하도록 했다.

기타 - alias 관리하기
--------------------

ES의 [Alias](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html)를 활용하면, 입수가 완료된 Index를 사용자가 조회하는 Index로 Alias를 거는 등의 작업을 할 수 있다. RDBMS의 Transaction 같은 기능이 없기 때문에 Data의 임수가 완료된 이후에 사용자에게 노출시키는 용도로 활용이 가능하다. 본 예제에서 `bank`가 아닌 `bank_version1`으로 사용한 이유도 물리적인 index와 사용자가 조회하는 index를 분리하기 위함이었다.

다음과 같이 `put_alias()`를 사용하면 된다.

```
es_client.indices.put_alias(index = 'bank_version1', name = 'bank')
```

이후엔 `bank` index로 조회하면 ES는 자동으로 알아서 `bank_version1`의 Data를 출력할 것이다.

기타 - 이외에도 수 많은 API가 존재한다
--------------------------------------

[API 문서](http://elasticsearch-py.readthedocs.io/en/master/api.html)를 꼭 읽어보자. Rest API로 조회했던 많은 기능들을 Python에서도 거의 대부분 할 수 있다.

API에서 제공되는 기능이 없다면? - perform_request()
--------------------------------------------------

그런데 만약 API에서 기능을 찾지 못한다면 어떻게 해야할까? 이때는 `perform_request()`를 이용하면 된다. `perform_request()`는 ES의 임의의 Rest API를 호출하는 기능을 제공한다.

사실 `perform_request()`가 없더라도 HTTP Url을 호출할 수도 있다. 하지만, `perform_request()`를 이용하는 것이 여러 모로 편리하다. (응답 코드 제어라든지, logging이라든지, timeout 설정 및 timeout 발생 시 retry 기능이라든지, 기타 http 옵션 관리라든지...)

필자가 못 찾은 기능 중 하나가 Bulk Insert가 아닌 문서 1개를 Insert하는 API였다. curl을 이용하면 아래와 같은 요청이다.

```
$ curl -XPUT http://localhost:9200/bank_en_ver1/account/new_id2?pretty -d '
{
    "state": "NY"
}
'
{
  "_index" : "bank_en_ver1",
  "_type" : "account",
  "_id" : "new_id2",
  "_version" : 1,
  "_shards" : {
    "total" : 2,
    "successful" : 1,
    "failed" : 0
  },
  "created" : true
}
```

이를 `perform_request()`로 표현하면 다음 code와 같다.

```
#!/usr/bin/env python
#-*- encoding: utf8 -*-

import elasticsearch
from elasticsearch import helpers


es_client = elasticsearch.Elasticsearch("localhost:9200")

(ret_code, response) = es_client.transport.perform_request(method = 'PUT',
                                    url = '/bank_version1/account/new_id2',
                                    body = {
                                            'state': 'NY'
                                    })

if ret_code == 200: # http 응답 코드만 확인한다.
    print "OK"
```

위 코드에서 `ret_code`에는 http 응답 코드만 저장되어 있다. Rest API에 따라서 200이더라도 요청이 실패한 경우가 있으므로 요청하는 API의 응답 결과인 `response`도 같이 확인해야 한다.
