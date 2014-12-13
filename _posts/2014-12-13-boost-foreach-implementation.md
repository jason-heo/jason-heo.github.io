---
layout: post
title: "boost::disjoint_sets 사용방법"
categories: cpp
---

`BOOST_FOREACH`는 비교적 늦게 알게 되었다. C++을 첨 접한 것은 1998년이지만 학교 숙제 정도만 했었고 본격적으로 C++로 개발하게 된 뒤에도 너무 C 스타일로만 개발을 했다;;

## `BOOST_FOREACH` 사용 전 code

`BOOST_FOREACH`가 없을 때 vector의 원소를 출력하려면 다음과 같이 해야 한다.

```cpp
#include <iostream>
#include <vector>

int main()
{
    std::vector<int> vi;

    vi.push_back(1);
    vi.push_back(2);
    vi.push_back(3);

    for (std::vector<int>::const_iterator it = vi.begin(); it != vi.end(); ++it)
    {
        std::cout << *it << std::endl;
    }

    return 0;
}
```

돌아가는데는 전혀 문제없는 코드지만 가독성이 떨어진다. Java나 PHP 등의 언어는 자체적으로 `for` 문이나 `foreach`를 지원하지만 C++에서는 지원하지 않는다. 물론 C++11에서는 지원하고 있으나, 모든 컴파일러가 C++11을 지원하지 않는다.

이런 경우 `boost::FOREACH`를 사용하면 좋다.

## `BOOST_FOREACH`를 사용한 code

```cpp
#include <iostream>
#include <vector>
#include <boost/foreach.hpp>

int main()
{
    std::vector<int> vi;

    vi.push_back(1);
    vi.push_back(2);
    vi.push_back(3);

    BOOST_FOREACH(const int& i, vi)
    {
        std::cout << i << std::endl;
    }

    return 0;
}
```

이 얼마나 깔끔한 코드인가? 

## `BOOST_FOREACH`는 어떻게 구현되었을까?

[Stackoverflow에 올라온 답변][1]을 보면 다음과 같은 define을 왜 안 쓰냐고 반문한 사람이 있다.

```
#define foreach(iter_type, iter, collection) \
 for (iter_type iter = collection.begin(); iter != collection.end(); ++iter)
```

`BOOST_FOREACH`도 define으로 정의되어 있긴하지만 위처럼 간단하진 않다. 우선 `BOOST_FOREACH`는 vector, list 같은 stl container 이외에 array도 지원을 한다. 하지만 위의 `foreach`는 array를 처리할 수 없다. 또한 `collection`이 expression인 경우에 위험할 수 있다. collection이 `for` 문에서 2번 나오기 때문에 expression이 의도치 않게 여러 번 수행될 수 있기 때문이다.

`BOOST_FOREACH`의 구현에 대한 문서는 다음 article을 읽어보는 것이 좋겠다. 본인도 100% 이해하진 못한 내용이며 C++11에서는 `std::for`를 사용할 수 있으므로 지금시점에서 `BOOST_FORECH`의 구현을 하는 것이 중요하냐고 생각할지 모르지만, `BOOST_FOREACH`의 구현을 배움으로서 C++에 대해 깊이 있는 내용을 배울 수 있다.

- http://cplusplus.bordoon.com/boost_foreach_techniques.html
- http://www.artima.com/cppsource/foreach.html (이 글은 `BOOST_FOREACH` 개발자가 작성한 문서이다. 학생 때 수업을 들으면서 이런 것을 만들었다니 대다나다.)

[1]: http://stackoverflow.com/questions/969496/access-iterator-in-boost-foreach-loop/1858516#comment43273978_1858516
