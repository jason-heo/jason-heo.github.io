---
layout: post
title: "`delete` 시에는 `NULL` 검사를 할 필요가 없다"
categories: programming
---

`delete` 시에는 `NULL` 검사를 할 필요없다. `free()`를 사용하던 시절에나 `NULL` 검사를 했었지, `delete`에서는 검사가 필요없다.

```cpp
    Cat* p1 = NULL;
    if (p1 != NULL) // 이 검사를 꼭 필요해다.
    {
        free(p1);
    }

    Cat* p2 = NULL;
    if (p2 != NULL) // delete를 사용할 때는 이 검사는 필요없다
    {
        delete p2;
    }

    Cat* p3 = NULL;
    delete p3;     // 바로 이렇게 직접 delete를 호출해도 아무 문제없다
```
