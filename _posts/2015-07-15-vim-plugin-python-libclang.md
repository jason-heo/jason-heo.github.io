---
layout: post
title: "vim plugin 개발 및 libclang에 관한 자료들"
categories: cpp
---

Libclang
========

LLVM에서 제공되는 clang은 3가지 성격의 API를 제공한다.

1. LibTooling
 - C++ API이며, 막강한 기능을 제공한다.
 - `ASTMatcher`, `Rewrite` 기능을 사용하려면 LibTooling을 사용해야 한다.
 - 그런데 LLVM 버전 올라갈 때마다 LibTooling은 많이 변한다. 하위 버전 호환성이 지켜지지 않는다.
 - Python과 연동이 불가능하다.
1. LibClang
 - Stable한 API를 제공한다.
 - Clang 이전 버전과 잘 호환된다.
 - (지금 나에게 있어 젤 중요한 문제인) Python과 Clang을 연동하려면 LibClang을 사용하는 수 밖에 없다;;
1. Clang Plugin
 - 요건 뭔지 모르겠고

LibClang 관련 자료들
-------------------

- http://llvm.org/devmtg/2010-11/Gregor-libclang.pdf
 - 기초적인 사용법
 - 처음 접하는 사람들은 이걸 읽어봐야함

LibTooling 관련 자료들
---------------------

- https://kevinaboos.wordpress.com/2013/07/23/clang-tutorial-part-i-introduction/
- https://kevinaboos.wordpress.com/2013/07/23/clang-tutorial-part-ii-libtooling-example/
 - 여기 방문하면 내가 댓글 남긴 것도 볼 수 있다. 하하.

Python && LibClang
=================

- Parsing C++ in Pyhton with Clang
 - http://eli.thegreenplace.net/2011/07/03/parsing-c-in-python-with-clang/
 - Eli Bendersky는 도대체 뭐하는 사람인지...
 - Stackoverflow에서도 왕성하게 활동하는 듯. http://stackoverflow.com/users/8206/eli-bendersky

- Python으로 C++ code generate하기
 - http://szelei.me/code-generator/
 - 이것도 꼭 읽어봐야 한다.

- comonster
 - https://github.com/sztomi/cmonster
 - LibClang용 Python Wrapper
 - AST Traverse랑 rewrite 기능도 제공한다.
 - 좀 오래된 프로젝트 같은데 어디까지 기능이 지원되려나...
 - LibClang을 배울 시간없으니 이걸 먼저 배워야 할 듯 한데...

VIM Plugin && LibClang
======================

- LibClang을 이용한 C++ Navigation Vim Plugin
 - http://blog.wuwon.id.au/2011/10/vim-plugin-for-navigating-c-with.html
- clang_complete
 - https://github.com/Rip-Rip/clang_complete
 - C/C++ 용 자동 완성 Plugin
- clighter - C/C++ 개발 편의를 위한 Plugin
 - https://github.com/bbchung/clighter
 - Python & LibClang으로 만들어져 있음
 - 다양한 기능을 제공
 - 변수명 rename하는 기능도 제공한다! <= 나한테 젤 필요한 기능

VIM Plugin && Python
====================

- Writeing Vim plugins with Python
 - https://www.youtube.com/watch?v=vMAeYp8mX_M

VIM Plugin
==========

- Your First Vim Plugin
 - https://www.youtube.com/watch?v=lwD8G1P52Sk

개발 환경 셋팅하기
==================

Mac 기준에서 설명

Clang 설치하기
-------------

* 본인의 경우 Xcode 설치할 때 자동으로 설치된 듯 하다.
* LLVM 3.6이 설치되었는데, 더 최신 버전을 설치하고 싶은 경우 다음의 URL을 참고하면 된다.
* http://llvm.org/docs/GettingStarted.html

Python Clang Module 설치
-----------------------

* `pip`로 clang을 설치할 수 있다.

```
$ sudo pip install clang
```

* `pip` 명령이 없는 경우 다음과 같은 명령으로 `pip`부터 설치하자
* 다음 명령 실행

```
$ sudo easy_install pip
```

* Python Clang Module이 제대로 설치되었는지 확인 방법
 * python 실행 후 `import clang.cindex` 입력 시 아무 오류가 없어야 한다.

```
$ python
Python 2.7.6 (default, Sep  9 2014, 15:04:36) 
[GCC 4.2.1 Compatible Apple LLVM 6.0 (clang-600.0.39)] on darwin
Type "help", "copyright", "credits" or "license" for more information.
>>> import clang.cindex
>>> 
```

Parsing Test
------------

* 입력 프로그램: `test.cc`

```cpp
#include <iostream>

class Person
{
public:
    int get_age(){return age_;}
    int age_;
};

int main()
{
    Person kim;
    kim.age_ = 10;
}
```

* python script: `parse.py`

```python
#!/usr/bin/env python

import clang.cindex, asciitree, sys

# Library 경로는 본인 환경마다 다릅니다.
clang.cindex.Config.set_library_path("/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib")

def print_decl(node):
    if (node.kind.is_declaration()):
        if (node.spelling == "Person"):
            print node.spelling
            print node.location

    for child in node.get_children():
        print_decl(child)

index = clang.cindex.Index.create(False)

tu = index.parse("./test.cc")
print_decl(tu.cursor)
```

* 실행해보기: `Person`이 정의된 File과 Line을 출력한다.

        $ ./parse.py
        Person
        <SourceLocation file './test.cc', line 3, column 7
