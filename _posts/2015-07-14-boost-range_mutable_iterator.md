---
layout: post
title: "custom container에서 BOOST_FOREACH 사용하는 방법"
categories: cpp
---

Modern Style의 C++를 늦게 배우다보니 ㅠㅠ [BOOST_FOREACH](http://mysqlguru.github.io/cpp/2014/12/13/boost-foreach-implementation.html)를 작년에 첨 알게 되어 잘 사용 중이다. (이렇게 편리한 것을 왜 이제 알았을까)

gcc 버전이 높다면 std의 `for()`를 사용해도 되겠지만 아직 나의 개발 환경은 `BOOST_FOREACH`를 사용할 수 밖에 없는 상황...

남들이 잘 만들어 놓은 Container들은 `BOOST_FOREACH`에서 쉽게 사용이 가능한데, 도대체 내가 unordered_map 등을 한번 Wrapping 해 놓은 Class들은 어떻게 해야 `BOOST_FOREACH`와 사용 가능한지 알 수가 없었다...

귀찮아서 그냥있다가 동료도 고민 중이길래 같이 검색 좀 했더니 다음과 같이 아주 쉽게 되었다...

`boost::range_mutable_iterator`와 `boost::range_const_iterator`만 정의해 주면 된다!!

```cpp
#include <iostream>
#include <tr1/unordered_map>

#include <boost/foreach.hpp>


class MyData
{
public:
    typedef std::tr1::unordered_map<int, int> int_int_map_t;
    typedef int_int_map_t::value_type value_type;

    int_int_map_t m_data;
    void insert(int k, int v)
    {
        m_data[k] = v;
    }

    int_int_map_t::iterator begin()
    {
        return m_data.begin();
    }

    int_int_map_t::iterator end()
    {
        return m_data.end();
    }

    int_int_map_t::const_iterator begin() const
    {
        return m_data.begin();
    }

    int_int_map_t::const_iterator end() const
    {
        return m_data.end();
    }


};

namespace boost
{
    template<>
    struct range_mutable_iterator<MyData>
    {
        typedef MyData::int_int_map_t::iterator type;
    };

    template<>
    struct range_const_iterator<MyData>
    {
        typedef MyData::int_int_map_t::const_iterator type;
    };
} // end of namespace boost


int main(int argc, char* argv[])
{
    MyData t;

    t.insert(1, 10);
    t.insert(2, 20);

    BOOST_FOREACH (const auto& vt, t)
    {
        std::cout << vt.first << ":" << vt.second << std::endl;
    }

}
```
