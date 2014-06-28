---
layout: post
title: "boost::assign::map_list_of 사용법 (C++에서 하드 코딩을 쉽게 제거하기)"
date: 2014-03-21 21:34:00
categories: cpp
---

## C++에서 숫자와 문자열을 매핑하기
제목을 **boost::assign::map_list_of 사용법**이라고 적고, 부제로 **C++에서 하드 코딩을 쉽게 제거하기**라고 하였다. 그렇다. `map_list_of`를 사용하는 곳은 여러 곳이 있겠지만 본인은 하드 코딩을 제거할 때 map 변수를 쉽게 초기화하는 용도로 사용하려고 한다.

출력할 문자열을 코드내에 직접 적는 것는 하드 코딩은 좋은 습관이 아니다. 그러나 개발이 빠르고, 디버깅도 용이(?)하기 때문인지 급할 때는 그냥 하드 코딩을 마구 남발하고 있었는데, 본인도 앞으로는 가급적 하드 코딩을 줄이려고 노력 중이다.

하드 코딩을 제거할 수 있는 다양한 방법이 있겠지만 본인은 `boost::assign::map_list_of`를 이용하려고 한다.

```cpp
#include <iostream>
#include <map>
#include <boost/assign/list_of.hpp>
 
typedef enum {
    SEOUL,
    YONGIN,
    BUNDANG
} CITY;
 
int main()
{
 
    std::map<CITY, std::string> city_name_map = boost::assign::map_list_of
        (SEOUL, "서울특별시")
        (YONGIN, "경기도 용인시")
        (BUNDANG, "성남시 분당구");
 
    std::cout << city_name_map[SEOUL] << std::endl; // "서울특별시" 출력
 
    return 0;
}
```

`city_name_map`이 const이면 `find()->second`를 사용해야하기 때문에 좀 불편하긴하다.


## C에서는 어떻게 하는게 좋을까?

C에서 된다면 C++에서도 되기 때문에 꼭 C언어 만의 이야기는 아닐 것이다.

STL의 map을 사용하지 않고 배열을 바로 접근하기 때문에 이 방법이 속도가 더 빠를 수 있다. 단, enum을 만들고 배열을 만들어서 매핑 시켜주는 단계가 귀찮고 실수할 수도 있기 때문에 난 `boost::assign::map_list_of`를 사용하려고 한다.

```cpp
typedef enum {
    SEOUL,
    YONGIN,
    BUNDANG
} CITY;
 
const char* city_name_map[] = {
    "서울특별시",
    "경기도 용인시",
    "성남시 분당구"
};
 
printf("%s\n", city_name_map[SEOUL]);
```

