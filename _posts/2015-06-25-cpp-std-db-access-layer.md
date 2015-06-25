---
layout: post
title: "A Proposal to add a Database1 Access Layer to the Standard Library"
categories: programming
---

C++에서 아쉬운 것 중 하나는 Java의 JDBC 같은 통일된 Database Connector가 없다는 점... 각 DBMS Vendor마다 API를 따로 사용해야 하는 번거로움이 있을 뿐만 아니라 (사실 나야 MySQL만 사용하다보니 그런 번거로움은 아직 못 느꼈음;;) 저수준의 API가지로 DB 질의를 하려면 번거로운 일이 많다.

C++에서도 JDBC 같은 것이 있으면 좋겠다 생각을 했는데, 2014년 1월 18일에 문서 번호 N3886 "A Proposal to add a Database1 Access Layer to the Standard Library"가 Proposal될 듯 하다.

관심있는 분은 http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2014/n3886.pdf 를 읽어보시기 바란다. 이게 C++ 표준 라이브러리에 포함이 되는 것은 아닌 듯 하다. C++ 에서도 이런 움직임이 있다는 것이 중요한 듯...

N3886 이외에도 [N3612][1], [N3515][2], [N3458][3] 같은 것도 있다니 신기하다.

N3886 문서에서 제안된 SELECT 후 결과를 출력하는 code를 발췌하면 다음과 같다.

```cpp
result res{execute(conn, "select * from test_table where AGE < ?", 100)}; 

// dump column names
for (int i = 0; i < res.get_column_count(); ++i)
{
    std::cout << res.get_column_name(i) << "\t";
}
std::cout << std::endl;

// dump data
while(!res.is_eof())
{
    std::cout << cast_to<std::string>(res.get_column_value("ID")) << "\t"
    << value_of<std::string>(res.get_column_value(1)) << "\t"
    << value_of<int>(res["AGE"]) << "\t"
    << value_of<double>(res[3])
    << std::endl;
    res.move_next();
}
```

[1]: http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2013/n3612.pdf
[2]: http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2012/n3415.html
[3]: http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2012/n3458.pdf
