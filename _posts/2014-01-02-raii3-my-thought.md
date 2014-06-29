---
layout: post
title: "[Modern C++] RAII 3 : 나의 생각..."
date: 2014-03-09 
categories: cpp
---

주말 동안 RAII에 대한 공부를 했다.

- [위키피디어 문서 번역 보기](/cpp/2014/03/08/raii1.html)
- [위키북스 문서 번역 보기](/cpp/2014/03/09/raii2-wikibooks.html)

RAII 개념을 사용하더라도 자원 누수가 없는 프로그램을 개발할 수는 있다. 하지만, 꼼꼼하게 관리를 해야만 할 것이다. 예를 들어 new로 동적 메모리를 할당 받은 경우 함수 종료 시에는 항상 delete를 호출하여 메모리를 해제해야만 메모리 누수가 없다. 함수의 로직이 단순한 경우 new/delete 짝을 맞추기는 쉽지만 프로그램이 복잡해 질 수록 메모리 관리는 어렵게 된다. 예를 들어 다음과 같이 MySQL에 질의하는 함수를 보자.

```cpp
void get()
{
    Student* person = new Student();
     
    if (mysql_query(...) != 0)
    {
        // 질의 실패 시 person 메모리 해제
        delete person;
        return;
    }
 
    if (mysql_store_result(...) != 0)
    {
        // ResultSet 저장 실패 시 person 메모리 해제
        delete person;
        return;
    }
 
    delete person;
}
```

사용하는 자원이 몇 개 안 되고, 오류 발생 상황도 적은 경우는 개발자가 잘 신경을 쓰면 어느 정도 자원 누수를 막을 수 있다. 하지만, 관리하는 자원이 많고 오류 처리 상황이 많은 경우 메모리 누수를 없애기 위해선 많은 신경을 써야 하며 `delete person;`처럼 메모리 관리하는 코드가 중복될 것이다.

하지만 RAII를 이디엄을 적용한 스마트 포인터를 사용한다면 메모리 관리에서 자유롭게 된다. 함수 종료 시 RAII 객체의 소멸자가 호출되면서 자동으로 delete가 호출되기 때문이다. Modern C++의 다음 차례는 스마트 포인터에 대한 내용이다.
