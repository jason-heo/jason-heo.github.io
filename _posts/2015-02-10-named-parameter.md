---
layout: post
title: "Named parameter Idiom"
categories: programming
---

## Named Parameter란?

[C++ Faq][1]를 읽다가 [Named Parameter Idiom][2]를 알게 되었다. Named parameter를 지원하지 않는 C/C++, Java 계열에서는 함수 호출 시 parameter 위치가 parameter의 의미를 결정한다. 따라서 함수를 호출할 때 몇 번째 parater가 어떤 것을 의미하는지 알고 있어야 한다. 예를 들어 다음의 함수를 보자.

    make_point(10, 5);

`make_point()`는 점(Point)를 생성하며 Point는 x, y 좌표로 구성이 된다. 그렇다면 위의 함수만을 보고 10이 `x`인지 `y`인지 알 수 있는가?

`Named Parameter`를 지원하는 Object-C (나는 한번도 못 써봤음)에서는 다음과 같이 함수를 호출할 때 parameter의 이름과 값을 동시에 지정할 수 있다고 한다. (출처 : [위키피디어][3])

```
[window addNewControlWithTitle:@"Title"
                     xPosition:20
                     yPosition:50
                         width:100
                        height:50
                    drawingNow:YES];
```

## Named Parameter Idiom

C/C++, Java에서는 언어 자체적으로 `Named Parameter`를 지원하지 않으므로 다음과 같은 Idiom을 통해서 비슷한 효과를 볼 수 있다. (출처 : [C++ Faq - Named Parameter Idiom][2])

```cpp
File f = OpenFile("foo.txt")
           .readonly()
           .appendWhenWriting();
```

`File` 객체에 대한 설정을 생성자의 parameter로 넘기게 되면 parameter 개수도 많고, 실수할 가능성이 많다. `Named Parameter Idiom`을 이용하면 parameter 위치를 기억할 필요가 없고, 헷갈림도 방지할 수 있다.

여기서 말하는 "헷갈림"이란, 다음과 같다. 예를 들어 다음과 같은 `OpenFile()`이 있다고 할 때, 

    File f = OpenFile("foo.txt", true, false);

parameter가 3개 밖에 안 되지만, true가 read_only에 대한 것은지 append에 대한 true인지 알기가 어렵다.

실제 Faq에 나온 예는 위의 것보다 설정하는 parameter가 많다. 다음 전체 예이다.

```cpp
File f = OpenFile("foo.txt")
           .readonly()
           .createIfNotExist()
           .appendWhenWriting()
           .blockSize(1024)
           .unbuffered()
           .exclusiveAccess();
```

## 명확성을 위한 다른 방법

### 1) enum 활용하기

`Named Parameter`든 뭐든 그들의 목표는 code의 가독성을 높히고 실수를 방지하는 것이다. boolean 변수는 `true/false` 대신 `enum`을 활용하는 것이 좋다.

```cpp
enum ENUM_READ_ONLY = {READ_ONLY, READ_WRITE};
enum ENUM_APPEND = {APPEND, TRUNCATE};

File f = OpenFile("foo.txt", READ_ONLY, TRUNCATE};
```

이렇게 하면 가독성이 `true/false`로 전달되는 것보다 가독성이 높아지고 실수를 미연에 방지할 수 있다.

### 2) Class 활용

다음과 같이 parameter로 전달할 내용들을 모두 1개의 Class로 만들어도 된다.

```cpp
class FileOption
{
public:
    bool read_only_;
    bool append_;
};


FileOpen option;
option.read_only_ = true;
option.append_ = false;

File f = OpenFile("foo.txt", option);
```

위의 2 방법을 사용하는 경우, 만들어야 할 `enum` 혹은 `class`가 많아지는 단점이 있다.

## 결론

`Named Parameter Idiom`이든 `enum`이든 `class`든 서로 장단점이 있다고 생각된다.

[1]: https://isocpp.org/faq
[2]: https://isocpp.org/wiki/faq/ctors#named-parameter-idiom
[3]: http://en.wikipedia.org/wiki/Named_parameter
