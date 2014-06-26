---
layout: post
title: "[Modern C++] Smart Pointer 2 - 스마트 포인터란 무엇이며, 왜 사용해야 하는가"
date: 2014-03-15 21:34:00
categories: cpp
---

원문 - http://ootips.org/yonat/4dev/smart-pointers.html

**역자 주** - 스마트 포인터 자체에 대한 설명, 장점, 왜 사용해야 하는지 등에 대한 설명이 잘 되어 있다. 단, 문서에 예로 등장하는 counted_ptr, cow_ptr 등은 문서 작성자가 직접 개발한 라이브러리이다. C++ 표준 라이브러리도 아니고 boost에 속한 것도 아닌 것이 아쉽다. 또한 std::auto_ptr은 더이상 사용되지 않는다. 문서에 나온 counted_ptr은 boost에 shared_ptr로 존재한다. **따라서 문서에 나온 code는 직접 따라 하기 보다는 스마트 포인터의 개념을 익히는 용도로 문서를 읽기 바란다.**

## 스마트 포인터란 무엇인가?
스마트 포인터는 일반 포인터처럼 생겼지만, 일반 포인터보다는 똑똑한 객체이다. 이는 무엇을 의미하는가?

일반 포인터처럼 보이기 위해서 스마트 포인터는 포인터가 가진 인터페이스를 가져야 한다. 포인터 값이 접근하기 위한 operator *와 포인터에 접근하기 위한 operator ->를 지원해야 한다. 서로 다른 무엇인가와 동일하게 보이는 객체를 Proxy 객체 혹은 그냥 Proxy라고 부른다. [Proxy 패턴](http://rampages.onramp.net/~huston/proxy.html)과 사용처는 The Design Pattern과 [Pattern Oriented Software Architecture](http://www.amazon.com/dp/0471958697/?tag=ootips)에 설명되어 있다.

일반 포인터보다 똑똑하기 위하여 스마트 포인터는 일반 포인터가 하지 못하는 일을 할 수 있어야 한다. C++ (혹은 C) 프로그램에서 가장 빈번히 발생하는 버그는 포인터와 메모리 관리에 관련되어 있다. dangling 포인터, 메모리 누수, 할당 실패 등에 관련된 버그들이다. 스마트 포인터를 이용하여 이런 버그를 줄일 수 있다.

스마트 포인터의 가장 단순한 예는 std::auto_ptr이다. auto_ptr은 C++ 표준 라이브러리에 포함되어 있으며 <memory> 헤더 파일에서 찾을 수 있다. 혹은 [Scott Meyers의 구현](http://cseng.aw.com/book/related/0,3833,020163371X+80,00.html)을 봐도 된다. auto_ptr의 구현 중 일부는 다음과 같다.

```cpp
template <class T> class auto_ptr
{
    T* ptr;
public:
    explicit auto_ptr(T* p = 0) : ptr(p) {}
    ~auto_ptr()                 {delete ptr;}
    T& operator*()              {return *ptr;}
    T* operator->()             {return ptr;}
    // ...
};
```

위의 코드에서 볼 수 있듯이 auto_ptr은 일반 포인터를 감싸는 단순한 클래스이다. auto_ptr은 포인터에 대한 연산(dereferencing, indirection)을 포인터에 전달한다. (역주:dereferencing은 *ptr, indirection은 ptr->func()) 스마트 포인터가 똑똑하다는 것은 소멸자에 있다. 소멸자가 포인터를 delete하는 역할을 담당한다. 일반 포인터를 사용하는 경우 다음과 같이 명시적으로 delete를 해야 하지만,

```cpp
void foo()
{
    MyClass* p(new MyClass);
    p->DoSomething();
    delete p;
}
```

auto_ptr를 사용한다면 다음과 같이 delete를 명시적으로 호출할 필요가 없다.

```cpp
void foo()
{
    auto_ptr<MyClass> p(new MyClass);
    p->DoSomething();
}
```

{% include adsense-content.md %}

## 왜 사용해야 하는가?

스마트 포인터의 종류는 여러 가지이며 사용되는 곳과 이유가 서로 다르다. 이 장에서는 C++에서 스마트 포인터를 사용하는 공통적인 이유에 대해서 알아보고자 한다.

### 적은 버그

### 자동 청소
앞의 코드에서 볼 수 있듯이 스마트 포인터를 사용함으로서 코드의 크기를 줄일 수 있다. 하지만 이는 중요한 이유가 아니다. 스마트 포인터를 사용함으로서 버그를 줄일 수 있는 것이 더 중요한 이유이다. 스마트 포인터를 사용할 때는 delete를 명시적으로 호출할 필요가 없으므로 메모리 해제에 대해서는 잊어도 된다.

### 자동 초기화
또 다른 잊어도 되는 것은 NULL로 초기화하지 않아도 되는 것이다. 디폴트 생성자가 자동으로 하기 때문이다.

### Dangling 포인터
일반 포인터를 사용하면서 자주 겪는 실수는 Dangling 포인터인데, 이는 이미 삭제된 객체를 가리키고 있는 포인터를 의미한다. 다음의 코드가 이런 상황에 대한 예이다.

```cpp
MyClass* p(new MyClass);
MyClass* q = p;
delete p;
p->DoSomething();   // Watch out! p is now dangling!
p = NULL;           // p is no longer dangling
q->DoSomething();   // Ouch! q is still dangling!
```

auto_ptr을 사용할 때 auto_ptr이 복사되는 경우 자동으로 NULL로 설정된다.

```cpp
template <class T>
auto_ptr<T>& auto_ptr<T>::operator=(auto_ptr<T>& rhs)
{
    if (this != &rhs) {
        delete ptr;
        ptr = rhs.ptr;
        rhs.ptr = NULL;
    }
    return *this;
}
```

auto_ptr이 아닌 다른 스마트 포인터들은 복사가 될 때 다른 행동을 한다. 예를 들어 다른 스마트 포인터에서는 q = p 문을 실행할 때 q와 p가 동일한 스마트 포인터인 경우 다음과 같은 전략을 취할 수도 있다.

- 새로운 복사본 생성: p가 가리키고 있는 객체에 대한 복사본을 새로 생성한 다음 q가 포인트하도록 하는 전략이다. copied_ptr.h에서 찾을 수 있다.
- 소유권 이전: p와 q가 동일한 객체를 포인트하도록 하지만 해제에 관한 소유권을 p에서 q로 이전하는 전략이다. owned_ptr.h에서 찾을 수 있다.
- 참조 카운트 (Reference counting): 동일한 객체를 포인트하고 있는 스마트 포인터의 개수를 유지하는 전략이다. 참조 카운트가 0이 되는 순간 객체를 삭제한다. q = p 문은 p가 포인트하고 있는 객체의 카운트 값을 1 증가 시킨다. 이 스마트 포인터는 counted_ptr.h에서 찾을 수 있다.
- 참조 연결(Reference Linking): 참조 카운트 방식과 비슷하다. 동일한 객체를 포인트하는 모든 스마트 포인터를 circular doubly linked list로 연결해 놓는다. linked_ptr.h에서 찾을 수 있다.
- Copy on write: 참조 카운트 혹은 참조 연결 방식은 포인트되고 있는 객체가 수정되지 않는 동안 사용된다. 수정이 필요할 때 새로운 복사본을 생성한 뒤에 복사본에 수정하는 방식이다. 이 전략은 cow_ptr.h에서 찾을 수 있다.

위의 기법들은 dangling 포인터와의 싸움에서 도움을 줄 수 있다. 각 기법마다 장단점이 있기 때문에 다양한 상황에서 어떤 스마트 포인터를 사용해야 하는지는 이 문서의 아래 부분에 설명되어 있다.

### Exception 안전

다음과 같은 간단한 예를 보자.


```cpp
void foo()
{
    MyClass* p(new MyClass);
    p->DoSomething();
    delete p;
}
```

만약 DoSomething() 메소드 호출 시 Exception이 발생되는 경우 어떤 일이 생길까? DoSomething() 이후에 있는 코드는 수행되지 않으므로 delete p;도 호출되지 않는다. 메모리 누수는 당연한 것이며 만약 MyClass의 소멸자에서 잠금 해제 등을 해야 하는 경우, 소멸자가 호출되지 않으므로서 잠금이 영원히 해제되지 않을 수도 있다.

하지만, 스마트 포인터를 사용하는 경우 p의 scope를 벗어 나는 경우 자동으로 소멸자가 호출되게 된다. 정상적인 함수 호출 흐름에 의한 함수 종료든 Exception이 발생하였든 소멸자는 항상 호출된다.

물론 일반 포인터를 사용하더라도 안전한 코드를 작성할 수 있다. 다음과 같은 함수를 작성할 수 있지만 스마트 포인터가 있는데 굳이 어렵게 코딩할 필요는 없다.

```cpp
void foo()
{
    MyClass* p;
    try {
        p = new MyClass;
        p->DoSomething();
        delete p;
    }
    catch (...) {
        delete p;
        throw;
    }
}
```

게다가 if나 for가 많은 경우 코드가 얼마나 복잡해질지, delete가 항상 호출될지 상상해 보라.

### 가비지 컬랙션 (Garbage Collection)

일부 언어들은 자동 가비지 컬랙션 기능을 제공하지만 C++은 그렇지 못하다. 스마트 포인터는 가비지 컬렉션 용도로 사용될 수 있다. 가장 단순한 가지비 컬렉션 기법은 참조 카운트 혹은 참조 연결 방법이다. 스마트 포인터를 이용하는 경우 이러한 가비지 컬렉션을 좀 더 정교하게 구현할 수 있다. 좀 더 자세한 정보는 가비지 컬렉션 FAQ를 참조하라.

### 효율성

스마트 포인터는 가용한 메모리를 좀 더 효율적으로 사용할 수 있게 하며 할당, 해제 시간을 단축 시킬 수 있다.

메모리을 좀 더 효율적으로 사용할 수 있는 일반적인 전략은 COW(Copy on Write) 방법이다. 즉, 1개의 객체가 수정되지 않는 동안 여러 COW 포인터가 해당 객체를 가리킬 수 있도록 하되, 해당 객체가 수정되는 경우 (이때를 "write"라고 한다) COW 포인터가 객체를 복사(Copy)한 후 복사 본을 수정하는 방법이다. 일반적으로 표준 string 클래스도 COW 방식을 사용하여 구현된다.


```cpp
string s("Hello");
 
string t = s;       // t and s point to the same buffer of characters
 
t += " there!";     // a new buffer is allocated for t before
                    // appending " there!", so s is unchanged.
```

객체가 할당되거나 운용되는 환경에 대해 일부 가정을 세울 수 있는 경우 최적화된 할당 계획이 가능하다. 예를 들어, 모든 객체의 크기가 동일하다거나 단일 쓰레드에서만 사용될 수 있다고 하는 등의 가정 말이다. 이런 가정이 있다면 클랙스에 특화된 new와 delete를 이용하여 최적화된 할당 계획을 구현할 수 있지만, 스마트 포인터를 사용하는 경우 이런 가정 자체를 무시할 수 있다. 따라서 서로 다른 운용 환경이나 응용 프로그램이 변경된다 하더라도 클래스 전체의 코드를 변경하지 않더라도 항상 최적화된 할당 계획을 만들 수 있다.

### STL 컨테이너

STL은 일반화(generic)와 효율성에 주안점을 두고 설계되었다. 이를 위해 STL 컨테이너는 객체의 값을 저장한다. 예를 들어 Base 클래스의 객체를 저장하는 STL 컨테이너가 있다면 Base를 상속받아 생성된 클래스의 객체는 해당 STL 컨테이너에 저장될 수 없다.

```cpp
class Base { /*...*/ };
class Derived : public Base { /*...*/ };
 
Base b;
Derived d;
vector<Base> v;
 
v.push_back(b); // OK
v.push_back(d); // error
```

서로 다른 클래스를 저장하기 위해 어떻게 해야 하는가? 가장 간단한 방법은 포인터를 저장하는 방법이다. 

```cpp
vector<Base*> v;
 
v.push_back(new Base);      // OK
v.push_back(new Derived);   // OK too
 
// cleanup:
for (vector<Base*>::iterator i = v.begin(); i != v.end(); ++i)
    delete *i;
```

하지만 이 방법의 단점은 포인트 되는 객체를 명시적으로 delete해야 한다는 점이다. Exception이 발생하는 경우 delete가 호출되지 않을 수 있다. 하지만 스마트 포인터를 사용하는 경우 자동으로 메모리 해제가 된다.

```cpp
vector< linked_ptr<Base> > v;
v.push_back(new Base);      // OK
v.push_back(new Derived);   // OK too
 
// cleanup is automatic
```
 
스마트 포인터는 자동으로 청소되기 때문에 프로그래머가 직접 포인트되는 객체를 delete할 필요가 없다.

## 어디에 사용해야 하는가?

아직도 헷갈린다면 다음 내용이 도움될 것이다.

### 지역 변수

표준 auto_ptr은 가장 단순한 스마트 포인터이다. 특별한 요구 사항이 없는 경우 지역 변수에서는 auto_ptr을 사용하는 것이 좋다. (역자 주 : boost::scoped_ptr 혹은 std::unique_ptr을 사용해야 한다.)

### 클래스 멤버 변수

auto_ptr을 클래스 멤버 변수로 사용할 수도 있지만, 객체가 복사되는 경우 해당 스마트 포인터 지역 변수는 NULL로 설정되는 문제가 있다.

```cpp
class MyClass
{
    auto_ptr<int> p;
    // ...
};
 
MyClass x;
// do some meaningful things with x
MyClass y = x; // x.p now has a NULL pointer
```

auto_ptr 대신 copied pointer를 사용하면 문제가 해결된다. 주의할 것이 있는데, 참조 카운트나 참조 연결 방식의 스마트 포인터를 사용하는 경우 y의 멤버 변수 값을 변경하면 x의 멤버 변수 값도 변경된다는 점이다. 따라서 메모리 사용량을 줄이고 싶다면 COW 포인터를 사용하면 된다.

### STL 컨테이너

TBD

### 명시적 소유권 전환

함수의 인자로 포인터를 전달 받을 때 포인터의 소유권 또한 전달 받고 싶은 경우가 있다. Taligent의 프로그램 설계 안내에서는 이러한 함수에는 "adopt"를 사용하길 제안하고 있다. 소유권을 전달받은 스마트 포인터를 함수 인자로 사용하는 경우, 명시적으로 소유권이 이전된다는 것을 알기 쉽게 된다.

### Big Objects

객체가 메모리 공간을 많이 차지하는 경우 COW 포인터를 사용하여 공간을 절약할 수 있다. 이런 방식을 이용하여 객체가 변경되지 않는 한 여러 포인터에 의해 공유되고, 필요한 경우(내용이 변경되는 경우)에만 객체가 복사되도록 할 수 있다. 이런 공유는 참조 카운트 혹은 참조 연결 방법을 이용하여 구현될 수 있다.

### 요약

- 지역 변수 : auto_ptr
- 클래스 멤버 변수 : copied pointer
- STL Containers : 가비지 컬렉션이 되는 포인터 (예: 참조 카운트, 참조 연결)
- 명시적 소유권 이전 : Owned pointer
- 큰 객체 : Copy on write

## 결론

스마트 포인터는  C++에서 안전하고 효율적인 코드를 작성하는데 유용하다. 스마트 포인터에 대한 정확한 이해와 지식을 가지고 있어야 적절한 곳에 사용될 수 있을 것이다. 스마트 포인터에 대한 완벽한 이해를 위해서 Alexandrescu가 집필한 Modern C++ Design의 스마트 포인터에 대한 챕터를 읽어보길 바란다.

내가 만든 스마트 포인터를 당신의 코드에 사용해도 된다. Boost에도 스마트 포인터가 존재하며 테스트가 잘 되어 있고 활발하게 관리되어 있다. 당신의 요구사항을 만족하는 경우 먼저 Boost의 스마트 포인터를 사용해 보라.

Copyright 1999 by Yonat Sharon  http://ootips.org/yonat/4dev/smart-pointers.html

