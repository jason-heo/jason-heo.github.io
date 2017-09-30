---
layout: post
title: "python dict의 value로 정렬하기"
categories: "programming"
---

자주 사용하지 않는 것은 맨날 까먹는다.

```
>>> dict = {'a': {'name': 'zzz'}, 'b': {'name': 'aaa'}}

>>> sorted(dict.items(), key = lambda (k, v): v['name'])
[('b', {'name': 'aaa'}), ('a', {'name': 'zzz'})]
```
