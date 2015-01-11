---
layout: post
title: "if (boolean_variable == false) vs if (!boolean_variable)"
categories: programming
---

## Boolean Test 방식

프로그램 분기를 위하여 boolean test를 많이 사용한다. 코드 가독성 측면에서 다음 code 중 어떤 것을 선호하는가? 변수의 type은 boolean이라 가정한다.

1. `if (andrew.is_late == false)`
1. `if (!andrew.is_late)`

본인의 경우 그동안 1번째 방법을 선호했는데 그 이유는

1. 변수의 data type을 알 수 있다.
 * 즉, `== false`를 보면 `is_late`가 boolean type인 것을 알 수 있다.
1. 어순상 !이 변수보다 뒤에 오는 것이 맞을 듯 하다.
 * 즉, `andrew가 지각하지 *않은* 경우`로 읽힐 수 있다.

와 같은 이유에서였다.

그런데 같은 팀에서 개발하는 분들은 2번 방식을 선호하길래 다른 사람들은 어떤 방식을 선호하는지 조사해 봤다.

검색을 해 보니 이런 [토론 글][1]이 있었다. 결론은 2번 방식이 선호되는 것이다. (글 중간에 "코딩이 짧기 때문에"는 무시해도 된다.) 2번 방식이 선호되는 것은 *영어 문장에 어울리는 코딩이기 때문*이다.

즉, `if (!andrew.is_late)`는 `if andrew is *not* late`로 읽히기 때문에 영어 문화권에서 가독성이 좋다. 반면 `if (andrew.is_late == false)`는 'if andrew is late is false`로 읽히기 때문에 가독성이 떨어진다.

한국어는 영어와 어순이 다르기 때문에 다음과 같은 문법이 있으면 좋을 듯도 하다.

- `if (andrew.is_late not)`
(적어 놓고 보니 그닥 가독성이 좋진 않은 듯)

암튼 2번 방식이 선호되며 이에 대한 내용은 "Code Complete"의 19장 "제어와 관련된 일반적인 문제"의 19.2 절 에 소개되어 있다.

## boolean이 아닌 다른 data type에서는?

boolean type의 경우 영어 `if (!boolean_var)`가 영어 문장과 비슷하기 때문에 선호되고 있다. 하지만, 다음과 같은 boolean이 아닌 타입은 어떨까?

1. `if (andrew.age)` vs `if (andrew.age != 0)`
1. `if (andrew.name)` vs `if (andrew.name != NULL)` (name이 pointer라고 가정)

"Code Complete"에 의하면 이 경우 후자 방식 즉, `0`이나 `NULL`과 직접 비교하는 것이 좋다고 한다.

## 더 중요한 것은 일관성

여러 사람이 코딩을 하는 경우 어떤 방식이 되었든 일관성을 지키며 코딩하는 것이 좋다. 일관성을 위해 규칙을 세우는 것이 중요한 데 그 규익은 종교적 신념이 아닌 경우는 많이 사용하는 쪽으로 정하거나 혹은 권위있는 책에서 제안하는 방법이 좋다. 일하다보면 별로 중요한 것도 아닌데, 내가 옳으니 하면서 싸우는 경우를 많이 보게 된다. 어떻게 결정해도 무방한 것은 다투지 말고 남들이 많이 하는데로 정해보자.

[1]: http://programmers.stackexchange.com/questions/136908/why-use-boolean-variable-over-boolean-variable-false
