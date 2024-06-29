---
layout: post
title: "Github Copilot은 어떻게 작동하는가?"
categories: "llm"
---

### 1)

Github Copilot을 직접 사용해본 것은 아니지만 사용해본 사람들의 소감이나 demo 영상을 보면 개발자의 생산성을 크게 향상시켜주고 있다.

나도 사용해보고 싶지만 업무 환경에서 Copilot을 사용할 수 없어서 local 환경 (open source plugin & sLLM)을 구축한 뒤 사용해봤는데 소문으로 듣고 기대하던 Copilot과 너무 달라서 당황을 했었다.

그런데 생각해보니 code auto completion 구현이 쉬운 일이 아니었다.

단순히 웹 화면에 "오늘 날짜 알아오는 함수 작성해줘"를 물어보는 것과 다른 수준이다.

예를 들어 다음과 같은 함수를 작성하고 나머지 구현을 맡긴다고 해보자.

```
def sortByAge(people: list[Person]): list[Person]
```

`Person` class에 어떤 variable과 method가 있는지 알아야 올바른 코드를 생성할 것이다.

그렇다면 `Person` class의 정의도 prompt에 같이 전달해야한다. 즉, `Person` class 정의를 찾아야 하는 문제로 귀결된다.

그외, 이미 code base에 존재하는 function을 이용해서 신규 code를 구현해야한다면 해당 library의 definition을 찾아서 같이 전달해야한다.

class 및 function의 definition이 편집 중인 파일에 있을 수도 있고 아닐 수도 있다. 이런 정보를 어떻게 잘 찾아서 prompt에 전달해 줄 수 있을까?

### 2)

이 정보가 궁금하여 몇 가지 조사한 내용을 기록해본다.

- [How GitHub Copilot is getting better at understanding your code](https://github.blog/2023-05-17-how-github-copilot-is-getting-better-at-understanding-your-code/)
  - github blog 글, 2023-05-17
  - 많은 ML 연구자들이 연구를 했다고 한다
  - 간단히 요약하면 다음 그림과 같다고 한다
  - <img style="width: 100%; max-width: 686px;" src="/images/2024/06/29/copilot-01.png" />
- [Inside GitHub: Working with the LLMs behind GitHub Copilot](https://github.blog/2023-05-17-inside-github-working-with-the-llms-behind-github-copilot/)
  - github blog 글, 2023-05-17 (Copilot 관련 글을 하루에 두건이라 발행했다)
- [Copilot Internals](https://thakkarparth007.github.io/copilot-explorer/posts/copilot-internals.html)
  - thakkarparth007.github.io
  - Copilot plugin을 reverse engineering을 하여 작동 방식을 분석한 글
  - 관련 github: https://github.com/thakkarparth007/copilot-explorer
