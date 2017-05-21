---
layout: post
title: "python setuptools upgrade 중 max recursion 오류 해결하기"
categories: "programming"
---

문제 상황
==

Apache Impala의 Python library를 설치하려고 하면 아래와 같은 에러가 발생했다.

```
$ sudo pip install impyla
Collecting impyla
  Using cached impyla-0.14.0.tar.gz
    Complete output from command python setup.py egg_info:

    The required version of setuptools (>=3.4.4) is not available,
    and can't be installed while this script is running. Please
    install a more recent version first, using
    'easy_install -U setuptools'.

    (Currently using setuptools 0.9.8 (/usr/lib/python2.7/site-packages))

    ----------------------------------------
Command "python setup.py egg_info" failed with error code 2 in /tmp/pip-build-f7IO4s/impyla/
```

그래서 `easy_install -U setuptools`를 실행하면 이번엔 `RuntimeError: maximum recursion depth exceeded while calling a Python object` 오류가 발생한다.

```
$ sudo easy_install -U setuptools
  ...
  ...
  File "/usr/lib/python2.7/site-packages/pkg_resources.py", line 1958, in find_distributions
    importer = get_importer(path_item)
  File "/usr/lib64/python2.7/pkgutil.py", line 394, in get_importer
    importer = ImpImporter(path_item)
RuntimeError: maximum recursion depth exceeded while calling a Python object
```

해결하기
==

[Stackoverflow 답변](http://stackoverflow.com/questions/31273332/pip-install-upgrade-sqlalchemy-gives-maximum-recursion-depth-exceeded/31273772#31273772)을 참고해서 해결했다

```
pip install --upgrade distribute 
```
