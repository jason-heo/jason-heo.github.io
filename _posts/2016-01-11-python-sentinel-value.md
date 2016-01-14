---
layout: post
title: "Python에서의 Sentinel Value"
categories: programming
---

오.. 역시 Pythoninc way는 깔끔하고 좋다
출처: http://www.youtube.com/watch?v=OSGv2VnC0go&t=12m27s

일반적인 C style code
-----------------

```python
blocks = []
while True:
    block = f.read(32)
    if block == '':
        break;
    blocks.append(block)
```

pythonic way
------------

```python
blocks = []
for block in iter(partial(f.read, 32), ''):
    blocks.append(block)
```
