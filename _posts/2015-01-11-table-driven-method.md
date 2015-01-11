---
layout: post
title: "Table Driven Method 소개"
categories: programming
---

"논리를 코드로 표현하지 말고 Table에 표현하라" 이 방법을 Table Driven Method라고 부른다. 우선 간단한 예부터 살펴보고 객체 지향적 방법보다 Table Driven Method가 더 좋은 예를 보려고 한다.

## 간단한 예

Table Driven Method는 앞에 적었다시피 풀어야할 문제를 **논리**가 아닌 **Table로 표현**한다. 이게 무슨 말인고 하니..

월(月)을 숫자로 입력받아 문자열로 출력하는 함수를 보자. 이를 논리로 풀어내면 다음과 같이 복잡해진다.

```cpp
void print_month_name(int month)
{
    if (month == 1) std::cout << "Jan";
    if (month == 2) std::cout << "Feb";
    ...
    if (month == 12) std::cout << "Dec";
}
```

`switch case`를 사용하든 `if-else`를 사용하든 코드가 복잡해질 수 밖에 없다. 이를 Table을 이용하면 다음과 같이 깔끔해 진다.

```cpp
void print_month_name(int month)
{
    char* month_names[] = {"Jan", "Feb", ..., "Dec"};

    std::cout << month_names[month-1];
}
```

무척 깔끔해졌다. `month_names` 변수가 함수 호출시마다 만들어지지만 이는 다른 방법으로 우회할 수 있으니 문제가 안 된다. 배열에 대한 접근 비용도 O(1) 밖에 안 되므로 문제가 되지 않는다.

## 좀 더 복잡한 예

위의 예는 많은 사람들이 아는 것이고 크게 어렵지 않다. 이번엔 좀 더 복잡한 예를 Table Driven Method로 멋지게 풀어보자.

우선 다음과 같이 "선수 성적 조회" 프로그램이 있다고 보자. 클라이언트는 서버에 선수 이름을 요청하면 서버는 선수의 타입 별로 성적 정보를 출력한다. 예를 들어 보면 다음과 같다.

```
클라이언트                         서버
     |                               |
     |  "박찬호" 선수 성적 요청        |
     | ----------------------------> |
     |                               |
     |  "방어율", "승패" 출력          |
     | <---------------------------  |
     |                               |
     |                               |
     |  "이승엽" 선수 성적 요청        |
     | ----------------------------> |
     |                               |
     |  "홈런", "타율" 출력            |
     | <---------------------------- |
     |                               |
     |                               |
```

선수의 종류별로 출력해야할 정보가 다르다. 이를 보고 OOP 적 관점에서 생각해보면 "Factory Method Pattern"을 생각하겠지만, Table Driven Method를 이용하여 Factory Method보다 더 쉽게 풀 수 있다.


```cpp
class AbstractPlayer
{
    abstract void print_score() = 0;
};

class Tusoo : AbstractPlayer // Tusoo = 투수.. 영어가 짧아서....
{
    void print_score
    {
        // 투수 성적 출력
    }
}

class Taja : AbstractPlayer // 상동..
{
    void print_score
    {
        // 타자 성적 출력
    }
}

enum PLAYER_TYPE {TUSOO, TAJA, MAX_TYPE}

void print_score(const std::string& player_id)
{
    AbstractPlayer* player_map[MAX_TYPE];   // --------+
                                            //         |
    player_map[TUSOO] = new Tusoo();        //         |--> 테이블 만들기
    player_map[TAJA] = new Taja();          // --------+

    PLAYER_TYPE t = get_type(player_id);

    player_map[t]->print_score();    // 실제 선수 타입별 print_score()가 호출됨
}
```

Factory Method처럼 상속을 활용하는 것은 동일하다. [Clean Code][1]의 저자 Robert C. Martin은 복잡한 `if-else`나 `switch-case` 구문을 Factory Method에 한해서만 허용한다고 한다. Table Driven Method의 경우 이러한 Factory Method 자체도 필요없다.

`get_type()`을 잘못 만들게 되면 Factory Function과 비슷해질 수 있다.

[1]: http://www.yes24.com/24/goods/11681152
