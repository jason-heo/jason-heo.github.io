---
layout: post
title: "const X* const p를 해석하는 방법"
categories: programming
---

# 들어가며

다음과 같이 변수가 `Const X* const`로 선언된 경우 해당 변수는 다른 변수를 point할 수 없다.

```cpp
    Cat guru, bally;

    const Cat* const p1 = &bally;

    p1 = &guru; // 요 부분이 문제.
```

g++ 4.8.3에서는 컴파일 시 다음과 같은 에러 메시지를 출력한다.

```
error: assignment of read-only variable ‘p1’
     p1 = &guru;
```

2개의 const 중 앞의 `const`는 `p1`가 point하는 변수의 내용을 변경할 수 없는 것을 의미하고 2번째 `const`는 `p1`이 다른 변수를 point하지 못하도록 한다. 즉, 다음과 같이 2번째 `const`를 없애면 `p1`은 다른 변수를 point할 수 있다.

```cpp
    Cat guru, bally;

    const Cat* p1 = &bally;

    p1 = &guru;
```

그런데 이게 꽤나 헷갈리다. 다음과 같은 code는 동작을 할까?

```cpp
    Cat guru, bally;

    Cat const * p1 = &bally;

    p1 = &guru;
```

혹은 다음 code는 어떤가?

```cpp
    Cat guru, bally;

    Cat * const p1 = &bally;

    p1 = &guru;
```

## `const X* const`를 해석하는 방법

[C++ FAQ][1]를 읽다가 알게된 사실인데, '오른쪽에서 왼쪽 방향'으로 해석하면 이해가 쉽다.

예를 들어, 

- `const X* p`: `p`는 pointer 변수인데 X type을 point하며 이 변수는 상수(`const`)이다. 즉, pointer 자체가 `const`는 아니므로 `p`는 다른 변수를 point할 수 있다.
- `X* const p`: `p`는 상수 pointer 변수이며 `X` type을 point한다. '상수 pointer'이므로 다른 변수를 point할 수 없다. `X`에는 `const`가 아니므로 `p`가 point하는 변수의 내용은 변경할 수 있다.
- `X const * p`:  `p`는 `non-const` pointer이며, `const X`를 point한다. 따라서 `p`는 다른 변수를 point할 수 있지만, `p`가 point하는 변수는 수정할 수 없다.

이 처럼 `const X* const p`를 right-to-left로 해석하면 `p` 변수의 특성을 이해하기 쉽다.

[1]: https://isocpp.org/wiki/faq/const-correctness#const-ptr-vs-ptr-const
