---
layout: post
title: "Impala Python에서 cursor open 시 TBufferedTransport 오류 발생"
categories: "programming"
---

문제 상황
==

```
from impala.dbapi import connect

conn = connect(host='hostname', port=21050)

cursor = conn.cursor()
```

위의 code를 실해하면 `conn.cursor()` 부분에서 다음과 같은 Exception이 발생한다.

```
AttributeError: 'TBufferedTransport' object has no attribute 'trans'
```

해결하기
==

[Impyla Issue](https://github.com/cloudera/impyla/issues/235)를 참고하여 해결 가능

```
$ sudo pip install thrift==0.9.3
Collecting thrift==0.9.3
  Downloading thrift-0.9.3.tar.gz
Installing collected packages: thrift
  Found existing installation: thrift 0.10.0
    Uninstalling thrift-0.10.0:
      Successfully uninstalled thrift-0.10.0
  Running setup.py install for thrift ... done
Successfully installed thrift-0.9.3
```
