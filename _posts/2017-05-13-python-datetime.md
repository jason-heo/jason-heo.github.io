---
layout: post
title: "Python 날짜 연산"
categories: "programming"
---

맨날 까먹어서 정리

```
>>> from datetime import datetime
>>> from datetime import timedelta
>>>
... yesterday = datetime.now() - timedelta(days=1)
>>>
... yesterday.strftime('%Y-%m-%dT%H:%M:%S+09:00')
'2017-05-12T10:22:28+09:00'
```
