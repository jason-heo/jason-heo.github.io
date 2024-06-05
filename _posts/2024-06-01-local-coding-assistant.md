---
layout: post
title: "local model 에서 수행 가능한 무료 coding assistant plugin"
categories: "llm"
---

앞서 [여기](/llm/2024/05/26/llm-and-end-of-programming.html)에서도 이야기했지만 github Copilot 같은 도구를 사용하지 않으면 구석기 시대 개발자가 될 것이다. 철로 무장한 철기 시대 개발자와의 싸움에서 질 수 밖에 없고, 거대한 공룡이 멸망한 것처럼 나도 멸망할 수 있다.

다만 아쉬운 점은, 보안상 Copilot을 업무용으로 사용할 수 없다는 점.

그래서 reddit에서 "local model를 지원하는 coding 보조 도구"에 대해 찾아본 후 간단히 정리해본다.

아래 중에서 ContinueDev가 괜찮은 듯 하여 요걸 테스트해볼 예정이다

무료 도구들

- ContinueDev
  - https://github.com/continuedev/continue
  - https://plugins.jetbrains.com/plugin/22707-continue
- Tabby
  - https://github.com/TabbyML/tabby
  - https://plugins.jetbrains.com/plugin/22379-tabby

다음과 같은 것도 찾았는데 유료인 것 같다. 일부는 local llm을 지원하는 것도 같고 아닌 것도 같다.

- CodeGPT
  - (요건 유료인지 아닌지 확실치 않다)
  - https://codegpt.co/
  - https://plugins.jetbrains.com/plugin/21056-codegpt
- Codeium
  - reddit 댓글을 보면 Codeium이 제일 많이 언급되고 있다
    - 다만 local llm에서 사용하는 방법이 있는 것 같은데 복잡해보인다
    - 그리고 유료 같다
  - https://codeium.com/
  - https://plugins.jetbrains.com/plugin/20540-codeium-ai-autocomplete-and-chat-for-python-js-java-go--
- Cursor
  - https://www.cursor.com/
  - intelliJ plugin이 존재하는지 모르겠다
  - reddit에 [Cursor.sh is Amazing](https://www.reddit.com/r/ChatGPT/comments/18jbxar/cursorsh_is_amazing/) 같은 글도 있고 여러 글에 좋다고 댓글이 달려있다
  - "Coplit과 차이가 뭐냐"라는 질문에 다음과 같은 글이 달려 있다
    - > so many friggin ways. copilot x is barely conversational it's more focused on suggestions -- small recommendations to lines of codes even when you chat with it. cursor's implementation is much more holistic and can be taken in any direction while maintaining stellar context.
