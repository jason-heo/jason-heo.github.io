---
layout: post
title: "[Modern C++] 스마트 포인터 4 - boost::shared_ptr의 구현"
date: 2014-03-16 21:34:00
categories: cpp
---

역자 주 : [앞선 문서](/cpp/2014/03/17/smart-pointer3.html) 를 보면 `boost::shared_ptr`은 일반 포인터에 대비하여 크기가 약간 크다는 단점이 나와 있다. 이번 문서에서는 `boost::shared_ptr`의 구현을 살펴 볼 것인데 그 원인도 알 수 있다. 참고로 `boost::shared_ptr`의 크기가 일반 포인터 보다 큰 것이 싫다면 싫다면 `boost::intrusive_ptr`을 사용하면 된다.

---

단순한 스마트 포인터를 구현하는 것은 쉬운 일이다. 하지만 대부분의 컴파일러를 지원하는 스마트 포인터를 만드는 것은 쉽지 않다. 또한 Exception 안전한 스마트 포인터를 만드는 것은 더욱 어렵다. boost::shared_ptr은 이 모든 것을 고려하여 구현되었는데 본 문서에서는 boost::shared_ptr이 어떻게 구현되었는지 살펴 볼 것이다.

우선 클래스 선언이 필요하다. 스마트 포인터는 템플릿이다.

```cpp
template<typename T> class shared_ptr {
```

생성자는 다음과 같이 생겼다.

```cpp
explicit shared_ptr(T* p =0) : px(p)
{
  // fix: prevent leak if new throws
  try
  {
    pn = new long(1);
  }  
  catch (...)
  {
    checked_delete(p); throw;
  }
}
```

생성자에서는 쉽게 까먹을 수 있는 2가지를 검사하고 있다. 첫 번재는 생성자가 explicit으로 선언되어 있다는 점이다. (역자주:인자가 1개인 생성자가 explicit이 아니면 컴파일러에 의해 암묵적 형변환이 발생할 수 있다.) 두 번째가 더 중요한 점인데 참고 카운터 변수 할당이 try-catch 블럭으로 보호되고 있다는 점이다. 그렇지 않은 경우 참조 카운트 변수 할당이 실패하는 경우 제대로 작동하지 못할 것이다.

```cpp
~shared_ptr()
{
  dispose();
}
```

소멸자도 중요한 업무를 담당한다. 참조 카운트 값이 0이 되는 경우 포인트되는 객체를 소멸시키는 업무인데, 소멸자는 이 업무를 dispose() 메소드에게 위임한다.

```cpp
void dispose()
{
  if (?*pn == 0)
  {
    checked_delete(px);
    delete pn;
  }
}
```

코드에서 볼 수 있듯이 참조 카운트 변수 값(pn)이 1 감소되고 있다. 참조 카운트 값이 0이 되는 순간 포인트되는 객체(px)를 안전하게 소멸시킨다. 또한 참조 카운트 변수(pn)도 delete한다.

그렇다면 check_deleted()는 어떤 일을 할까? Boost.utility에서 찾을 수 있는 간단한 함수이며 포인터가 완전한 타입을 표현하도록 한다(?). 당신이 만든 스마트 포인터에도 이런 것이 있는가?

대입 연산자를 살펴 보자.

```cpp
template<typename Y> shared_ptr& operator=
  (const shared_ptr<Y>& r)
{
  share(r.px,r.pn);
  return *this;
}
```

대입 연산자는 멤버 템플릿이다. 만약 템플릿이 아니라면 다음과 같은 2가지 일이 발생할 수 있다.

만약 파라미터화된 복사 생성자 (parameterized copy constructor)가 없다면 Base = Derived;는 작동하지 않을 것이다.
파라미터화된 복사 생성자가가 존재한다면 대입 연산은 작동할 것이다. 하지만 이는 불필요한 임시 smart_ptr 객체를 생성하게 된다.
이러한 이유로 자신만의 스마트 포인터를 만드는 것은 좋지 않다.

대입 연산자의 실제 업무는 share()가 담당한다.

```cpp
void share(T* rpx, long* rpn)
{
  if (pn != rpn)
  { // Q: why not px != rpx?
    // A: fails when both == 0
    ++*rpn; // done before dispose() in case
            // rpn transitively dependent on
            // *this (bug reported by Ken Johnson)
    dispose();
    px = rpx;
    pn = rpn;
  }
}
```

자기 자신을 대입하는 것을 방지하기 위하여 참조 카운터 값을 비교한다.

```cpp
shared_ptr(const shared_ptr& r) : // never throws
  px(r.px)
{
  ++*(pn = r.pn);
}
```

위의 코드는 템플릿화된 복사 생성자이다.

다음 코드는 대입 연산자와 복사 생성자의 비-템플릿 버전이다.

```cpp
shared_ptr(const shared_ptr& r) : // never throws
  px(r.px)
{
  ++*(pn = r.pn);
}
shared_ptr& operator= (const shared_ptr& r)
{
  share(r.px,r.pn);
  return *this;
}
```

reset() 함수는 포인트되는 객체를 리셋하는 함수이다. 스마트 포인터 변수의 범위를 벗어나기 전에 포인트되는 객체를 삭제할 때 혹은 캐시 값을 무효화(invalidate)할 때 사용된다.

{% include adsense-content.md %}

```cpp
void reset(T* p=0)
{
  // fix: self-assignment safe
  if ( px == p ) return;  
 
  if (?*pn == 0) 
  {
    checked_delete(px);
  }
  else
  { 
    // allocate new reference 
    // counter
    // fix: prevent leak if new throws
    try {
      pn = new long;
    }  
    catch (...)
    {
      // undo effect of ?*pn above to
      // meet effects guarantee
      ++*pn;  
      checked_delete(p);
      throw;
    } // catch
  } // allocate new reference counter
 
  *pn = 1;
  px = p;
} // reset
```

잠재적으로 발생할 수 있는 누수 및 Exception 안전을 고려하여 구현되어 있다.

다음은 스마트 포인터를 "스마트"하게 하는 연산자들이다.

```cpp
// never throws
T& operator*() const
{
  return *px;
}
 
// never throws
T* operator->() const
{
  return px;
}
 
// never throws
T* get() const
{
  return px;
}  
```

종종 스마트 포인터를 T*로 형변환하는 연산자를 지원하는 스마트 포인터가 있는데 이는 좋은 생각이 아니다. Andrei Alexandrescu가 한 말이 있다. "당신의 스마트 포인터가 바보같은 포인터처럼 행동한다면 당신의 스마트 포인터는 스마트 하지 않다."

마지막으로 편의를 위한 함수도 제공된다.

```cpp
long use_count() const
{
  return *pn;
}  // never throws
 
bool unique() const   
{
  return *pn == 1;
}  // never throws
```

Boost.smart_ptr에는 이 외에도 많은 기능을 제공하지만 이 문서의 범위를 벗어난다.
