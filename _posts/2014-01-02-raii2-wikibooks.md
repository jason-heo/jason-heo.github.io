---
layout: post
title: "[Modern C++] RAII 2 : 위키북스 번역 문서"
date: 2014-03-08 21:34:00
categories: cpp
---

RAII에 대해서 위키피디어 문서를 [번역](/cpp/2014/03/08/raii1.html) 했었는데, RAII에 대한 위키피디어 문서가 별로 좋지를 못했다. RAII를 사용했을 때와 사용하지 않았을 때의 코드 비교가 있으면 좋겠는데..

위키북스에 있는 문서의 예가 더 좋아서 다시 번역해보려고 한다.

## Resource Acquisition is Initialization

RAII 기법은 다중 쓰레드 어플리케이션에서 쓰레드 lock을 제어하는데 자주 사용된다. RAII를 사용하는 다른 예는 C++의 파일 스트림 같은 파일 처리이다. 객체의 생성자에서 파일이 열리고, 객체가 소멸될 때 파일이 닫히게 된다. C++에서 지역 객체는 스택에 생성되므로 파일 접근에 대한 처리에 C++ Scope 기법이 사용된다.

RAII를 이용하여 클래스 소멸자가 자원을 해제하는 것을 보장할 수 있다. 이는 다른 언어의 *finally* 키워드와 비슷하다. 이렇게 하여 에러를 방지할 수 잇으며 자원 해제에 대한 업무를 자동화시킬 수 있다.

또한 아래 예에서 볼 수 있는 것처럼 RAII는 예외 안전성을 보장할 수 있다. RAII는 자원 누수를 방지하는데 사용될 수 있다. try/catch를 남발하지 않아도 된다.

new를 이용하여 동적으로 할당된 메모리의 소유권 또한 RAII로 제어될 수 있다. 이를 위하여 C++ 표준 라이브러리에서는 auto_ptr을 정의한다. 더불어 공유된 객체의 생명주기도 공유-소유권 개념을 사용하여 관리될 수 있으며 Boost 라이브러리의 `boost::shared_ptr` 혹은 Loki Library의 `Loki::SmartPtr`을 사용할 수 있다.

## RAII를 이용한 파일 처리 예

다음의 RAII 클래스는 C 표준 라이브러이 파일 시스템 콜을 감싸는 가벼운 클래스이다.

```cpp
#include <cstdio>
  
// exceptions
 class file_error { } ;
 class open_error : public file_error { } ;
 class close_error : public file_error { } ;
 class write_error : public file_error { } ;
  
 class File
 {
 public:
     File( const char* filename )
         :
         m_file_handle(std::fopen(filename, "w+"))
     {
         if( m_file_handle == NULL )
         {
             throw open_error() ;
         }
     }
  
     ~File()
     {
         std::fclose(m_file_handle) ;
     }
  
     void write( const char* str )
     {
         if( std::fputs(str, m_file_handle) == EOF )
         {
             throw write_error() ;
         }
     }
  
     void write( const char* buffer, std::size_t num_chars )
     {
         if( num_chars != 0
             &&
             std::fwrite(buffer, num_chars, 1, m_file_handle) == 0 )
         {
             throw write_error() ;
         }
     }
  
 private:
     std::FILE* m_file_handle ;
  
     // copy and assignment not implemented; prevent their use by
     // declaring private.
     File( const File & ) ;
     File & operator=( const File & ) ;
 } ;
```

RAII 클래스는 다음과 같이 사용된다.

```cpp
void example_with_RAII()
{
  // open file (acquire resource)
  File logfile("logfile.txt") ;
  
  logfile.write("hello logfile!") ;
  // continue writing to logfile.txt ...
  
  // logfile.txt will automatically be closed because logfile's
  // destructor is always called when example_with_RAII() returns or
  // throws an exception.
}
```

{% include adsense-content.md %}

## RAII 없이 파일 처리하는 예

RAII를 사용하지 않는 경우는 자원 누수를 방지하기 위해서는 다음과 같이 코드가 복잡하게 된다. try/catch를 이용하여 Exception이 발생할 때마다 명시적으로 `std::fclose()`를 호출하는 것을 볼 수 있다.

```cpp
void example_without_RAII()
{
  // open file
  std::FILE* file_handle = std::fopen("logfile.txt", "w+") ;
  
  if( file_handle == NULL )
  {
    throw open_error() ;
  }
  
  try
  {
  
    if( std::fputs("hello logfile!", file_handle) == EOF )
    {
      throw write_error() ;
    }
  
    // continue writing to logfile.txt ... do not return
    // prematurely, as cleanup happens at the end of this function
  }
  catch(...)
  {
    // manually close logfile.txt
    std::fclose(file_handle) ;
  
    // re-throw the exception we just caught
    throw ;
  }
  
  // manually close logfile.txt 
  std::fclose(file_handle) ;
}
```

`example_without_RAII()`는 만약 `fopen()`과 `fclose()`가 Exception을 발생할 수 있다면 더욱 복잡하게 될 것이다. 하지만, `example_with_RAII()`는 아무런 영향이 없다.

RAII 이디엄의 기초는 file 클래스가 `FILE*` 같은 유한한 자원을 은닉화한다는 것이다. 이는 함수 종료 시 자원이 자동으로 해제되는 것을 보장한다. 게다가 file 인스턴스는 올바른 로그 파일이 유효하다는 것을 보장한다. (파일이 열릴 수 없는 경우 Exception을 발생시킨다.)

예외가 발생하는 경우 example_without_RAII()에는 더 큰 문제가 존재한다. 만약 2개 이상의 자원을 사용하는 경우에 자원 할당 과정에서 Exception이 발생하는 경우이다. 이 때 catch 블럭에서 어떤 자원이 할당을 성공했고, 어떤 자원이 할당에 실패했는지 알 수 있는 일반적인 방법이 없다. 할당에 성공한 자원을 해제하기가 어렵다. RAII는 이를 쉽게  할 수 있는데 첫 째, 지역 변수들은 생성된 순서의 역순으로 소멸이 되기 때문이며 둘째, 완전히 생성된 경우 (생성자 안에서 Exception이 발생하지 않은 경우)에만 소멸자가 호출되기 때문이다. 따라서 example_without_RAII()는 이러한 상황에 대해 예외적인 코딩을 하지 않는 이상 example_with_RAII()보다 안전하기 힘들다.

example_with_RAII()는 자원 관리를 명시적으로 해야 하는 번거로움으로 자유롭게 한다. 많은 함수에서 File 클래스를 사용하는 경우 전체 코드 크기를 줄일 수도 있고, 프로그램의 정확성을 높힐 수 있다.

example_without_RAII()는 Java와 같은 비RAII 언어에서 자원 관리 하는 방법과 비슷하다. Java의 try-finally 블럭이 자원 해제를 정확하도록 하면서도, 프로그래머가 여전히 번거로운 작업을 해야 한다. 
