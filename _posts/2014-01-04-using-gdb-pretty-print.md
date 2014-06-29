---
layout: post
title: "gdb에서 Object의 내용을 이쁘게 print하기"
date: 2014-04-08 
categories: programming
---

## 예제 소스 코드

다음과 같은 Class와 프로그램이 있다고 가정하자.

```cpp
#include <iostream>
#include <sstream>
#include <typeinfo>
 
class Person
{
public:
    int age_;
    char gender_;
    std::string name_;
    std::string address_;
};
 
int main(int argc, char* argv[])
{
    Person p;
 
    p.age_ = 10;
    p.gender_ = 'M';
    p.name_ = "My Name";
    p.address_ = "Seoul, S. Korea";
 
    std::cout << p.age_ << std::endl;
}
```
 
## GDB의 기본 출력

GDB에서 `print p`를 해 보면 다음과 같이 출력된다.

    (gdb) print p
    $1 = {age_ = 10, gender_ = 77 'M', name_ = "My Name", address_ = "Seoul, S. Korea"}

간단한 class이기 때문에 보는데 어렵지 않지만, class가 좀 복잡해 지면 객체의 내용을 확인하기 어렵다.

## 이쁘게 출력하는 방법

다음의 내용을 gdb에서 입력하거나 `~/.gdbinit` 파일에 저장하자.

    set print pretty on
    set print object on
    set print static-members on
    set print vtbl on
    set print demangle on
    set demangle-style gnu-v3
    set print sevenbit-strings off

그 뒤에 동일하게 print p를 해 보면 다음 처럼 이쁘게 출력된 결과를 볼 수 있다.

    (gdb) print p
    $1 = {
      age_ = 10,
      gender_ = 77 'M',
      name_ = "My Name",
      address_ = "Seoul, S. Korea"
    }
