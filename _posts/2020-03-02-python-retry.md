---
layout: post
title: "Python에서 함수 retry하기"
categories: "python"
---

{% include python.md %}

함수를 retry할 경우가 종종 생긴다. (예를 들어 A 시스템의 데이터를 읽어가는데, A 시스템에서 데이터 생성이 늦어지는 경우 생성이 완료될 때까지 retry를 하는 경우 등)

그런데 retry 로직 때문에 정작 주요 비지니스 로직이 지저분해지는 경우가 있다.

https://codereview.stackexchange.com/a/188544/32442 답변에 있는 Fancy stuff한 retry code를 아주아주 약간 수정해보았다.

```python
import functools
import logging
import random
import time

logging.basicConfig()
logger = logging.getLogger('retry_test')
logger.setLevel(logging.INFO)

def retry(total_try_cnt=5, sleep_in_sec=5, retryable_exceptions=()):
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            for cnt in range(total_try_cnt):
                logger.info(f"trying {func.__name__}() [{cnt+1}/{total_try_cnt}]")

                try:
                    result = func(*args, **kwargs)
                    logger.info(f"in retry(), {func.__name__}() returned '{result}'")

                    if result: return result
                except retryable_exceptions as e:
                    logger.info(f"in retry(), {func.__name__}() raised retryable exception '{e}'")
                    pass
                except Exception as e:
                    logger.info(f"in retry(), {func.__name__}() raised {e}")
                    raise e

                time.sleep(sleep_in_sec)
            logger.info(f"{func.__name__} finally has been failed")
        return wrapper
    return decorator

@retry(total_try_cnt=3, sleep_in_sec=3, retryable_exceptions=(OSError, ValueError))
def my_method():
    rand = random.randint(0, 9)
    logger.info(f"rand={rand}")

    if rand % 3 == 0:
        return True
    elif rand % 3 == 1:
        return False
    else:
        raise int("a")

my_method()
```

수행 결과 (2회째 성공)

```
$ python3 retry.py
INFO:retry_test:trying my_method() [1/3]
INFO:retry_test:rand=1
INFO:retry_test:in retry(), my_method() returned 'False'
INFO:retry_test:trying my_method() [2/3]
INFO:retry_test:rand=0
INFO:retry_test:in retry(), my_method() returned 'True'
```

수행 결과 (3회 모두 실패)

```
$ python3 retry.py
INFO:retry_test:trying my_method() [1/3]
INFO:retry_test:rand=1
INFO:retry_test:in retry(), my_method() returned 'False'
INFO:retry_test:trying my_method() [2/3]
INFO:retry_test:rand=8
INFO:retry_test:in retry(), my_method() raised retryable exception 'invalid literal for int() with base 10: 'a''
INFO:retry_test:trying my_method() [3/3]
INFO:retry_test:rand=4
INFO:retry_test:in retry(), my_method() returned 'False'
INFO:retry_test:my_method finally has been failed
```
