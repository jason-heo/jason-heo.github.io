---
layout: post
title: "boost::disjoint_sets 사용방법"
categories: cpp
---

다음과 같은 골치 아픈 문제가 있었는데 `boost::disjoint_sets`를 쓰니 한방에 해결이 된다.

## `Disjoint Set`이란

한국말로 하면 '교집합이 없는 집합'으로 해석할 수 있다. 이런 자료구조 설명에 약하니 [위키 문서][1]를 참고하세요.

## `boost::disjoint_sets` 사용방법

역시 [Stackoverflow][2]에서 사용 방법을 찾을 수 있었다. 애석하게도 우리 같은 일반인들은 [boost 메뉴얼][3]만 봐서는 사용 방법을 알기 어렵다;;

다음 예제는 위의 Q&A를 보고 좀 더 쉽게 변경해 본 예이다.

```cpp
#include <iostream>
#include <vector>
#include <boost/pending/disjoint_sets.hpp>
#include <boost/unordered/unordered_set.hpp>

int main(int argc, char* argv[])
{
    typedef std::vector<int> VecInt;
    typedef std::vector<std::pair<int, int> > VecPair;

    VecPair winners_losers;

    // 2 = 3 = 4 = 10
    winners_losers.push_back(std::pair<int, int>(3, 2));
    winners_losers.push_back(std::pair<int, int>(3, 4));
    winners_losers.push_back(std::pair<int, int>(10, 2));

    // 5 = 6 = 8
    winners_losers.push_back(std::pair<int, int>(5, 6));
    winners_losers.push_back(std::pair<int, int>(5, 8));
    
    // size를 얼마를 줘야하는지 모르겠다.
    // 넘 적게 주면 seg. fault 발생
    VecInt rank(winners_losers.size() * winners_losers.size());
    VecInt parent(winners_losers.size() * winners_losers.size());

    boost::disjoint_sets<int*, int*> ds(&rank[0], &parent[0]);

    // create singletons
    for (VecPair::const_iterator it = winners_losers.begin()
         ; it != winners_losers.end(); ++it)
    {
        ds.make_set(it->first);
        ds.make_set(it->second);
    }

    // union
    for (VecPair::const_iterator it = winners_losers.begin()
         ; it != winners_losers.end(); ++it)
    {
        ds.union_set(it->first, it->second);
    }

    // 결과 출력
    for (VecPair::const_iterator it = winners_losers.begin()
         ; it != winners_losers.end(); ++it)
    {
        std::cout << it->first << " => " << ds.find_set(it->second) << std::endl;
    }

    return 0;
}
```

실행 예는 다음과 같다

```
3 => 2
3 => 2
10 => 2
5 => 6
5 => 6
```

{% include adsense-content.md %}

## 기본 예제의 한계 및 한계 극복하기

위에서 사용된 기본 예제는 아쉽게도 `int` 형 밖에 처리를 못 한다. primitive data type 중 `long` 형도 처리할 수 없고, 당연히 좀 더 복잡한 자료 구조, 즉, struct나 Class는 사용할 수 없다.

이를 극복하기 위한 방법도 있는 듯 하다.

[이 방법][4]은 `std::map`을 사용하여 data type을 매핑시켜주는 듯 하다. data type이 `long` 처럼 primitive한 경우 사용 가능한 듯...

[다른 방법][5]으로는 Class 멤버 변수에 저장하고자 하는 변수를 만들고, Class에는 몇 가지 Operator를 overloading하는 듯 한데 관심있는 분은 직접 테스트하고 그 결과를 알려주면 고맙겠다.


[1]: http://en.wikipedia.org/wiki/Disjoint-set_data_structure
[2]: http://stackoverflow.com/questions/3738537/implementing-equivalence-relations-in-c-using-boostdisjoint-sets
[3]: http://www.boost.org/doc/libs/1_56_0/libs/disjoint_sets/disjoint_sets.html
[4]: http://stackoverflow.com/questions/4134703/understanding-boostdisjoint-sets
[5]: http://janoma.cl/post/using-disjoint-sets-with-a-vector/
