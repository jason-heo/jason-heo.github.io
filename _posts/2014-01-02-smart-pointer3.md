---
layout: post
title: "[Modern C++] 스마트 포인터 3 - Smart Pointers to boost your code"
date: 2014-03-16 
categories: cpp
---

원문 : http://www.codeproject.com/Articles/8394/Smart-Pointers-to-boost-your-code

역자주 : Boost의 Smart Pointer에 대한 좋은 내용이라 생각됩니다.

---

스마트 포인터를 이용하여 C++ 개발을 매우 간단하게 할 수 있다. 가장 좋은 점은 C#, VB 같은 언어처럼 메모리 관리를 자동으로 할 수 있다는 점이다.

## 스마트 포인터란 무엇인가?

"스마트"라는 이름에서 알 수 있듯이,

> 스마트 포인터는 일반 포인터처럼 행동하는 C++ 객체이다. 더불어 객체가 더 이상 사용되지 않을 때 자동으로 delete된다.

C++에서 자원 관리는 매우 복잡하기 때문에, "더 이상 사용되지 않을 때"라는 말은 정의하기 어렵다. 스마트 포인터의 종류는 여러 가지인데 일반적인 시나리오에서 대부분 사용될 수 있다. 물론 단순히 객체를 delete하는 것만이 아니라 다른 일도 할 수 있지만 이런 응용은 본 튜토리얼의 범위를 벗어난다.

많은 라이브러리들이 스마트 포인터를 제공하며 서로 장단점이 다르다. 본 튜토리얼에서는 Boost 라이브러리를 사용하 것이다. Boost에서는 다음과 같은 스마트 포인터를 사용할 수 있다.

| 종류 | 설명 |
| ---  | ---  |
|shared_ptr<T>|T 객체가 사용 중인지 더 이상 사용하지 않는지 판단하기 위하여 참조 카운트 방식을 사용한다. shared_ptr은 generic하다.|
|scoped_ptr<T>|변수의 범위를 벗어나는 순간 자동으로 삭제되는 스마트 포인터이다. 대입 연산을 할 수 없다. 보통의 포인터에 비해 속도 저하가 없다.|
|intrusive_ptr<T>|shared_ptr처럼 참조 카운트를 사용하는 방식이지만, 속도는 shared_ptr보다 빠르다. 단 T 타입 자체에 참조 카운트를 위한 기법이 포함되어 있어야 한다.|
|weak_ptr<T>|shared_ptr과 함께 사용되어 순환 참조 현상을 방지하는데 사용된다.|
|shared_array<T>|shared_ptr과 비슷하지만 T 타입의 배열을 저장하는데 사용된다.|
|scoped_array<T>|scoped_ptr과 비슷하지만 T 타입의 배열을 저장하는데 사용된다.|

우선 가장 단순한 것부터 살펴보자

## boost::scoped_ptr<T>

scoped_ptr은 Boost에서 제공되는 스마트 포인터 중에서 가장 간단한 스마트 포인터이다. 포인터 객체 변수가 범위(scope)를 벗어 날 때 자동으로 삭제된다.

### 일반 포인터를 사용하는 예
```cpp
void Sample1_Plain()
{
  CSample * pSample(new CSample);
 
  if (!pSample->Query() )
  // just some function...
  {
    delete pSample;
    return;
  }
 
  pSample->Use();
  delete pSample;
 
}
```


### scoped_ptr을 사용하는 예

```cpp
#include "boost/smart_ptr.h"
 
void Sample1_ScopedPtr()
{
  boost::scoped_ptr<CSample> 
             samplePtr(new CSample);
 
  if (!samplePtr->Query() )
  // just some function...
    return;    
 
  samplePtr->Use();
 
}
```

일반 포인터를 사용할 때는 함수 종료 전에 항상 포인터를 delete해야 한다. 프로그램이 복잡하고 다양한 상황을 고려한다면 실수를 할 가능성이 많다. 반면 scoped_ptr을 사용하는 경우는 함수가 정상 종료되든 Exception이 발생하든 포인터 객체는 항상 delete된다.

지역 객체 및 클래스 멤버 변수에 사용하는 것이 좋다. 또한 지연된 인스턴스화 (Delayed Instantiation), PIMPL 구현, RAII에서 사용되는 것이 좋다.

STL 컨테이너의 원소로 사용되는 것은 좋지 않다. 또한 동일한 객체를 여러 포인터가 가리킬 때도 사용하지 않는 것이 좋다.

`scoped_ptr`의 성능은 일반 포인터의 성능보다 약간 느린 정도이다. 

## 참조 카운트 포인터

참조 카운트 포인터는 객체를 가리키는 포인터의 개수를 추적하는 방법이다. 객체를 가리키는 포인터가 1개인데 해당 포인터가 삭제될 때에만 객체가 소멸된다. Boost에서 제공되는 참조 카운트 방식의 스마트 포인터는 shared_ptr이다. 이름에서 알 수 있듯이 여러 개의 포인터가 1개의 객체를 가리킬 수 있다. 다음의 예를 보자.


```cpp
void Sample2_Shared()
{
  // (A) CSample 인스턴스를 만든다. 이때 참조 카운트 값은 1이다.
  boost::shared_ptr<CSample> mySample(new CSample); 
  printf("The Sample now has %i references\n", mySample.use_count()); // 1이 출력됨
 
  // (B) 동일 객체를 가리키는 포인터를 다시 생성한다.
  boost::shared_ptr<CSample> mySample2 = mySample; // 2가 출력된다.
  printf("The Sample now has %i references\n", mySample.use_count());
 
  // (C) 첫 번째 포인터를 NULL로 설정한다.
  mySample.reset(); 
  printf("The Sample now has %i references\n", mySample2.use_count());  // 1이 출력된다.
 
  // mySample2의 범위를 벗어나는 순간 CSample 객체도 소멸된다.
  // when mySample2 goes out of scope
}
```

(A)는 새로운 CSample 인스턴스를 힙 영역에 생성하는 코드이다. mySample이 해당 객체를 가리키고 있다. 그림으로 표현하면 다음과 같다.

![img1](/images/posts/cpp/sp-image001.gif)

mySample을 mySample2에 대입하는 경우 2개의 포인터가 동일 객체를 포인트하게 되며 참조 카운트 값은 2이다.

![img2](/images/posts/cpp/sp-image002.gif)

첫 번째 포인터를 reset()하더라도 (일반 포인터를 사용할 때 p = NULL;과 비슷하다) CSample 인스턴스의 참조 카운트는 1이기 때문에 힙에 계속 존재하게 된다.

![img3](/images/posts/cpp/sp-image003.gif)

mySampl2가 변수의 범위를 벗어날 때, CSample 인스턴스의 참조 카운드가 0이 되면서 소멸된다.

![img4](/images/posts/cpp/sp-image004.gif)

앞의 예는 CSample을 가리키는 shared_ptr이 2개보다 많더라도 마찬가지이며, 1개의 함수 안에서만 작동하는 것도 아니다. shared_ptr의 사용처는 다음과 같다.

- 컨테이너의 원소로 사용
- PIMPL(pointer-to-implementation) 이디엄에 사용
- RAII(Resource Acquisition Is Initialization) 이디엄에 사용
- 구현과 인터페이스를 분리하는데 사용

(참고. PIMPL 혹은 RAII에 대해서 잘 모르겠다면 C++ 책을 보길 바란다. C++ 에서 아주 중요한 개념이며 프로그래머들이 꼭 알고 있어야 한다. 일부 경우에서 스마트 포인터는 이러한 것을 구현할 수 있는 유일한 방법이다.)

### 중요한 특징

`boost::shared_ptr`의 구현은 다른 라이브러리에서의 스마트 포인터 구현과 다른 특징을 가지고 있다.

- shared_ptr<T>는 불완전한 타입을 사용할 수 있다.
 - shared_ptr<T>를 선언하거나 사용할 때 T는 "불완전한 타입"일 수 있다. 즉, class T;처럼 forward declaration을 해도 사용할 수 있다. (역자주:선언(declaration)과 정의(definition)에 대해서 알고 있어야 이해가 될 듯 하다) 컴파일러는 포인터를 dereference할 때만 T 타입에 대한 모든 것을 알면된다.
- shared_ptr<T>는 어떠한 타입과도 사용될 수 있다.
 - T 타입에 대한 어떠한 요구 사항도 없다.
- shared_ptr<T>는 사용자가 정의한 삭제객체를 지원한다.
 - delete p;가 아닌 임의의 삭제용 객체를 지원한다.
- 임의의 형변환
 - U*가 T*로 형변환이 가능하다면 shared_ptr\<U\>는 shared\_ptr\<T>로 형 변환이 가능하다.
- shared_ptr은 "쓰레드 안전"하다
 - 이는 장점이라기 보다는 그렇게 작동하도록 된 디자인 선택이지만 다중 쓰레드 프로그램에서 필수적이며 부하가 적다.
- 다양한 플랫폼을 지원하며 피어 리뷰(Peer-review)를 통해서 검증되어 있다.

## shared_ptr을 컨테이너에 사용한 예

STL 컨테이너를 포함한 많은 컨테이너 클래스는 list나 vector에 객체가 삽입될 때 객체를 복사하는 방법으로 객체를 컨터이너에 저장한다. 하지만 객체의 복사 비용이 비싼 경우 (혹은 복사 연산이 불가능한 경우) 일반적인 해결책은 컨테이너에 포인터를 저장하는 방법이다.

```cpp
std::vector<CMyLargeClass *> vec;
vec.push_back( new CMyLargeClass("bigString") );
```

하지만 호출하는 쪽에서 메모리 관리를 해야 한다. (역자 주: vec가 삭제될 때 프로그래머가 명시적으로 매 원소마다 delete를 해 주어야 한다.) shared_ptr을 사용하는 경우는 다음과 같다.

```cpp
typedef boost::shared_ptr<CMyLargeClass>  CMyLargeClassPtr;
std::vector<CMyLargeClassPtr> vec;
vec.push_back( CMyLargeClassPtr(new CMyLargeClass("bigString")) );
```

일반 포인터를 사용하는 것과 큰 차이는 보이지 않지만, vec가 소멸될 때 벡터 안에 저장된 객체들은 자동으로 삭제된다. 물론 벡터의 원소를 참조하는 다른 포인터가 있는 경우 해당 원소는 삭제되지 않는다. 이는 다음의 예에서 볼 수 있다.

```cpp
void Sample3_Container()
{
  typedef boost::shared_ptr<CSample> CSamplePtr;
 
  // (A) CSample을 저장하는 벡터 생성
  std::vector<CSamplePtr> vec;
 
  // (B) 3개의 원소를 삽입
  vec.push_back(CSamplePtr(new CSample));
  vec.push_back(CSamplePtr(new CSample));
  vec.push_back(CSamplePtr(new CSample));
 
  // (C) anElement가 2번재 원소를 포인트하고 있다.
  CSamplePtr anElement = vec[1];
 
  // (D) 벡터 소멸
  vec.clear();
 
  // (E) 2번째 원소는 여전히 접근 가능하다.
  anElement->Use();
  printf("done. cleanup is automatic\n");
 
  // (F) anElement 변수의 범위가 종료될 때 2번째 원소는 자동으로 소멸된다.
}
```

## Boost 스마트 포인터를 정확히 사용하기 위해 알고 있어야 하는 것

드물지만 스마트 포인터를 사용할 때 뭔가 잘못되는 경우도 있다. Boost에서 스마트 포인터 구현은 이러한 일을 방지하도록 명시적으로 구현되어 있다. 스마트 포인터를 안전하게 사용하기 위해 다음의 규칙을 기억하자.

### Rule 1 : 대입 및 유지 (Assign and Keep)

new로 객체를 생성하자 마자 스마트 포인터 객체를 생성해야 한다. T* p = new T; shared_ptr<T> sp(p); 처럼 객체 생성과 스마트 포인터 생성을 나누기 보다는 shared_ptr<T> sp(new T) 처럼 사용해야 한다.

### Rule 2 : _ptr<T>는 T*가 아니다

TBD

### Rule 3 : 순환 참조가 없어야 한다

순환 참조가 발생하는 경우 객체는 영원히 삭제되지 않는다. 이를 방지하기 위해 Boost는 weak_ptr을 제공한다.

### Rule 4 : 임시 shared_ptr이 없어야 한다

함수에 shared_ptr을 전달하기 위하여 임시 변수를 만들지 말고, 지역 변수를 만들어야 한다. boost::shared_ptr 문서에 나온 다음의 good과 bad 함수를 보자.

```cpp
void f(shared_ptr<int>, int);
int g();
 
void ok()
{
    shared_ptr<int> p( new int(2) );
    f( p, g() );
}
 
void bad()
{
    f( shared_ptr<int>( new int(2) ), g() );
}
```

{% include adsense-content.md %}
 
## 순환 참조

참조 카운트는 자원 관리를 하는데 편한 기법이다. 하지만 1가지 단점이 있는데 바로 순환 참조가 발생되는 경우 자원이 자동으로 해제되지 않는다는 점이다. 순환 참조는 컴퓨터가 발견하기 어렵다. 다음의 예를 보자.

```cpp
struct CDad;
struct CChild;
 
typedef boost::shared_ptr<CDad>   CDadPtr;
typedef boost::shared_ptr<CChild> CChildPtr;
 
 
struct CDad : public CSample
{
   CChildPtr myBoy;
};
 
struct CChild : public CSample
{
  CDadPtr myDad;
};
 
// a "thing" that holds a smart pointer to another "thing":
 
CDadPtr   parent(new CDadPtr); 
CChildPtr child(new CChildPtr);
 
// deliberately create a circular reference:
parent->myBoy = child; 
child->myDad = dad;
 
 
// resetting one ptr...
child.reset();
```

parent는 CDad 객체를 참조하고 있는데 그 객체는 CChild 객체를 참조하고 있다. 이를 그림으로 표현하면 다음과 같다.

![img5](/images/posts/cpp/sp-image005.gif)

위의 상황에서 `parent.reset();`을 호출하는 순간 우리는 2개의 객체에 접근할 수 있는 방법이 없게 된다. 따라서 메모리 누수가 발생하게 되고 CDad나 CChild가 중요한 자원을 보유하고 있었다면 이는 메모리 누수보다 더 안 좋은 상황에 직면하게 될 것이다.

이런 순환 참조는 shared_ptr보다 더 좋은 스마트 포인터가 있다고 하더라도 해결될 수 없는 문제이다. 순환 참조를 없애는데는 다음과 같은 방법이 있다.

1. 마지막 참조 객체를 해제할 때 직접 순환을 끊어 버린다.
1. Dad의 생명 주기가 Child보다 긴 것이 확실하다면 Child는 Dad에 대해 스마트 포인터가 아닌 일반 포인터를 이용하여 가리킨다.
3 boost::weak_ptr을 사용한다.

1과 2는 완벽한 방법은 아니지만 스마트 포인터 라이브러리에서 Boost의 weak_ptr 같은 포인터를 제공하지 않는 이상 1과 2의 방법을 사용하는 수 밖에 없다. weak_ptr에 대해서 좀 더 자세히 알아보도록 하자.

## 순환 참조를 끊기 위한 weak_ptr 사용법

### Strong 참조 v.s. Weak 참조

Strong 참조는 객체가 참조되는 동안 객체가 살아 있도록 유지한다. boost::shared_ptr은 strong 참조처럼 동작한다. 반대로 weak 참조는 살아 있도록 유지하지 않는다. 객체가 살아 있는 동안만 참조를 한다.

일반 C++ 포인터는 weak 참조와 비슷하다. 하지만 포인터만 가지고 있는 경우 당신은 객체가 여전히 살아 있는지 알 수 있는 방법이 없다.

boost::weak_ptr<T>는 weak 참조처럼 행동하는 스마트 포인터이다. 필요한 경우 weak_pr로부터 shared_ptr을 얻을 수 있다. (객체가 삭제된 경우 NULL이 반환될 수도 있다.) 물론 strong 포인터는 사용하고 난 뒤에 바로 해제가 되어야 한다. 위의 예에서 우리는 1개의 포인터를 weak 참조가 되도록 선택할 수 있다.

```cpp
struct CBetterChild : public CSample
{
  weak_ptr<CDad> myDad;
 
  void BringBeer()
  {
    shared_ptr<CDad> strongDad = myDad.lock(); // request a strong pointer
    if (strongDad)                      // is the object still alive?
      strongDad->SetBeer();
    // strongDad is released when it goes out of scope.
    // the object retains the weak pointer
  }
};
```
 
## intrusive_ptr - shared_ptr의 가벼운 버전

shared_ptr은 일반 포인터보다 편리하다. 하지만 이런 편리는 공짜가 아닌데, shared_ptr은 일반 포인터보다 크기가 크다. 또한 shard_ptr에 저장된 객체는 참조 카운트 값을 저장하고 있어야 하며 삭제자 (deleter)도 들고 있어야 한다. 이런 점은 대부분 무시해도 좋을 정도 이다.

intrusive_ptr은 일장일단(trade-off)를 제공하는데 가장 가벼운 참조 카운트 방식의 스마트 포인터이지만, 스마트 포인터가 가리키는 클래스에서 참조 카운트 기능을 제공해야 한다. 그렇기 때문에 크기가 작고 성능이 빠르다.

intrusive_ptr을 사용하기 위해서 2개의 함수 intrusive_ptr_add_ref와 intrusive_ptr_release를 정의해야 한다. 다음의 예에서 설명된다.

```cpp
#include "boost/intrusive_ptr.hpp"
 
// forward declarations
class CRefCounted;
 
 
namespace boost
{
    void intrusive_ptr_add_ref(CRefCounted * p);
    void intrusive_ptr_release(CRefCounted * p);
};
 
// My Class
class CRefCounted
{
  private:
    long    references;
    friend void ::boost::intrusive_ptr_add_ref(CRefCounted * p);
    friend void ::boost::intrusive_ptr_release(CRefCounted * p);
 
  public:
    CRefCounted() : references(0) {}   // initialize references to 0
};
 
// class specific addref/release implementation
// the two function overloads must be in the boost namespace on most compilers:
namespace boost
{
 inline void intrusive_ptr_add_ref(CRefCounted * p)
  {
    // increment reference count of object *p
    ++(p->references);
  }
 
 
 
 inline void intrusive_ptr_release(CRefCounted * p)
  {
   // decrement reference count, and delete object when reference count reaches 0
   if (--(p->references) == 0)
     delete p;
  } 
} // namespace boost
```

위의 예는 단순한 구현을 보여주는 예이므로 쓰레드 안전하지 않다. 일반적인 패턴을 보여주기 위한 목적으로 코드를 보기 바라며 더 좋은 구현은 다른 문서를 찾아 보기 바란다.

## scoped_array와 shared_array

scoped_ptr과 shared_ptr과 거의 동일하지만 operator new[]로 할당된 배열을 저장하는데 사용된다. operator []가 오버로드 되어 있고 일반 배열처럼 사용 가능하다. 하지만, 최초 할당 시에 배열의 길이가 지정되어야 한다.
