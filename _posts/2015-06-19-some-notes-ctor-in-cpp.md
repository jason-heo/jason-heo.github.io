---
layout: post
title: "C++ constructor에 관한 논쟁 2가지"
categories: programming
---

정답은 없겠다만...

## 1) Constructor에서 Exception을 throw해도 되는가?

- 일부 의견: Constructor에서 Exception이 Throw되면, Desctructor가 호출되지 않는다.  따라서 Memory Leak이 발생할 확율이 높기 때문에 ctor에서는 `throw Exception`을 하지 말라는 의견이 있다.
- 나의 의견: smart pointer만 잘 사용한다면 Memory Leak은 큰 문제가 안 된다. 쓰고 싶으면 써도 무방하다.

## 2) Constructor에서는 아무 일도 하지 않는 것이 좋다?

- 일부 의견: Constructor에서 아무 일도하지 말아라. initialize 함수를 만들어라...
- 나의 의견: Class만 잘 설계되었다면 즉, SRP(Single Responsibility Principle)과 DI(Dependency Injection)이 잘 설계되었다면 생성자에서 일을 하는 것이 좋다고 생각된다.

## 참고 자료

1: http://codereview.stackexchange.com/questions/59242/throw-exception-in-constructor-bad-or-good-practice
2: http://stackoverflow.com/questions/7048515/is-doing-a-lot-in-constructors-bad

