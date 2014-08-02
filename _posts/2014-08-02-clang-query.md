---
layout: post
title: "clang-query를 이용하여 ASTMatcher 테스트하기"
categories: cpp
---

## ASTMatcher

며칠 전에 `Clang AST`를 출력하는 방법에 대해 간단히 [포스팅][1]을 했었다. `Clang ASTMatcher`를 이용하면 전체 AST 중에서 원하는 sub tree를 검색할 수 있다. grep으로 텍스트 검색을 하면 문자열 매치만 검색할 수 있지만 ASTMatcher를 이용하면 프로그램 문맥을 이해하고 특정 영역만 검색 할 수 있다.

변수명 치환의 경우도  sed를 이용하여 변경하는 경우 내가 원하지 않는 변수까지도 치환될 수 있으나 ASTMatcher와 Clang Rewrite 기능을 활용하면 sed로 치환하는 것보다 안전하다.

## clang-query

본인도 아직은 Clang 초보인데다가 관련 자료를 찾기가 어려워서 [ASTMatcher 레퍼런스 메뉴얼][2]을 보면서 테스중이었는데, 내가 원하는 영역을 찾는 ASTMatcher를 만들기 어려웠다. 가급적 발견의 재미를 위해 혼자서 찾아 해결해 보려했지만 결국 포기하고 부족한 영어로 [Stackoverflow에 문의][3]를 올렸다.

그리고 답변에서 `clang-query`라는 툴을 알려줬는데 이게 물건이다.!! ASTMatcher를 사용하려면 매번 컴파일을 해야 하는데 컴파일 시간도 많이 걸리고 컴파일 에러 발생해도 뭔 소리인지 무엇 때문에 오류났는지 알 수가 없다.

`clang-query`를 이용하면 interactive한 화면에서 컴파일 과정 없이 ASTMatcher를 빠르게 테스트할 수 있다.

## clang-query install

`clang-query`는 Clang 설치 시 기본으로 설치되지 않는다. `Clang Extra Tools`라는 Package에 포함되어 있는데 별도로 설치를 해야 한다. 설치 방법은 [이 문서][4]를 읽으면 된다.

[1]: /cpp/2014/07/29/clang-ast.html
[2]: http://clang.llvm.org/docs/LibASTMatchersReference.html
[3]: http://stackoverflow.com/questions/25051049/how-can-i-get-all-constructor-initializer-using-clang-astmatcher
[4]: http://clang.llvm.org/docs/ClangTools.html

## clang-query 사용법

우선 다음과 같은 소스를 테스트에 사용했다.

```cpp
class Person
{

public:
    Person()
    {
        m_age = 0;
        m_gender = UNKNOWN;
    }

    Person(int age) : m_age(age)
    {
    }

    Person(int age, char gender) : m_age(age), m_gender(gender)
    {
    }

    int get_age()
    {
        return m_age;
    }

    void set_age(int age)
    {
        m_age = age;
    }
}
```

나는 생성자만 검색을 하고 싶은데 생성자 중에서 멤버 변수 초기화 코드가 있는 생성자만 검색하려고 한다. `Person(int age) : m_age(age)` 같은 코드 말이다.

어떤 ASTMatcher를 사용해야하는지 몰라서 한참을 고민했는데 다음 `clang-query` 사용 화면을 보면 ASTMatcher과 어떤 code에서 matct되었는지 그 결과까지 한눈에 볼 수 있다.

```
$ clang-query  ex03-input.cc --
clang-query> match constructorDecl(forEachConstructorInitializer(ctorInitializer().bind("ctorInitializer")))

Match #1:

/user/jsheo/clang-libtooling-example/ex03-input.cc:12:23: note:
      "ctorInitializer" binds here
    Person(int age) : m_age(age)
                      ^~~~~~~~~~
/user/jsheo/clang-libtooling-example/ex03-input.cc:12:5: note:
      "root" binds here
    Person(int age) : m_age(age)
    ^~~~~~~~~~~~~~~~~~~~~~~~~~~~

Match #2:

/user/jsheo/clang-libtooling-example/ex03-input.cc:16:48: note:
      "ctorInitializer" binds here
    Person(int age, char gender) : m_age(age), m_gender(gender)
                                               ^~~~~~~~~~~~~~~~
/user/jsheo/clang-libtooling-example/ex03-input.cc:16:5: note:
      "root" binds here
    Person(int age, char gender) : m_age(age), m_gender(gender)
    ^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Match #3:

/user/jsheo/clang-libtooling-example/ex03-input.cc:16:36: note:
      "ctorInitializer" binds here
    Person(int age, char gender) : m_age(age), m_gender(gender)
                                   ^~~~~~~~~~
/user/jsheo/clang-libtooling-example/ex03-input.cc:16:5: note:
      "root" binds here
    Person(int age, char gender) : m_age(age), m_gender(gender)
    ^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
3 matches.
```

총 3군데에서 match된 것과 소스의 어디에서 match되었는지도 출력을 해 준다.
