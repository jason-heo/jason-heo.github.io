---
layout: post
title: "RAG는 long context에 의해 멸망할 것인가?"
categories: "llm"
---

### 1)

Llama 3 70B의 context length는 8k이다. 이 정도면 적지 않은 size이고 본인의 용도로 사용하는데 아직 불편함이 없었다.

그러던 중 reddit에서 [LLama-3-70B with 1048k context length + big thanks to LocalLLama-Community](https://www.reddit.com/r/LocalLLaMA/comments/1ckc7k6/llama370b_with_1048k_context_length_big_thanks_to/)라는 글을 읽어보니 Llama 3를 기반으로 context length를 1M (즉, 1백만개)로 늘린 model이 있다는 것을 알았다.

그런데 생각을 해보니 context length가 매우 크다면 문서 전체를 prompt에 입력할 수 있으니 RAG가 무의미한 것 아닌가 하는 생각이 들었다.

참고 RAG는 만능이 아니다. RAG에 관한 발표 자료가 많은데 자료만 보면 RAG를 이용하여 내부 지식에 쉽게 답변을 얻을 수 있을 것 같다. 하지만 RAG를 직접 구현해보면 원하는 답을 얻는 것이 매우 어려운 일이라는 걸 알게 될 것이다. 이에 대해 좀 더 궁금한 분은 youtube의 [R.A.G. 우리가 절대 쉽게결과물을 얻을 수 없는 이유](https://www.youtube.com/watch?v=NfQrRQmDrcc)를 참고해보자.

그런데 만약 LLM의 context length가 증가하여 1백만개가 된다면 A4 수백 페이지에 해당하는 내용을 prompt에 입력을 할 수 있다. 이렇게 입력된 prompt에서 LLM이 원하는 정보를 잘 검색한다면 RAG는 불필요할 것이다.

그래서 관련 내용을 검색해보니 X에서 Dr. Yao Fu라는 사람이 쓴 [이런 글](https://x.com/Francis_YAO_/status/1759962812229800012)을 찾게 되었다.

요약을 해보면 Yao Fu가 몇 칠 전에 "long context will replace RAG"라는 내용으로 글을 작성했었고, 이에 대해 사람들이 반박을 하다보니 Yao Fu가 재반박한 내용이다.

<img style="width: 100%; max-width: 580px;" src="/images/2024/05/12/rag-01.png" />

### 2)

정말 RAG가 없어질지 궁금하여 1M context length의 품질 측면에서 테스트를 해봤다. (품질이 궁금한 것이기 때문에 현재 단계에서는 속도와 비용은 관심 밖이다)

우선 포탈에서 임의의 뉴스 본문 10개를 모은 후 token 수를 계산해보니 1만 token 정도가 나왔다. 그리고 본문에 있는 내용에 대해 질의를 해봤다. (예를 들어 뉴스 본문에 "AAA와 BBB가 회의를 했다"는 내용이 있었다면 나는 "AAA와 대화한 사람은?"이라는 질문을 해봤다)

사용된 model은 앞서 말한 [Llama-3-70B-Instruct-Gradient-1048k](https://huggingface.co/gradientai/Llama-3-70B-Instruct-Gradient-1048k)이다.

품질은 어땠을까? 아쉽게도 품질이 많이 낮았다. Llama-3-70B-Instruct-Gradient-1048k model의 문제인가 하고 동일한 질문을 Gemini 무료 버전에서 실행을 해봤는데 이건 Llama-3-70B-Instruct-Gradient-1048k 보다 품질이 더 안 좋았다.

### 3)

Yao Fu의 내용에 대해 비판적인 글들도 여러 개 찾을 수 있다.

- [LLM’s ability to find needles in a haystack signifies the death of RAG?](https://medium.com/@infiniflowai/llms-ability-to-find-needles-in-a-haystack-signifies-the-death-of-rag-0414d8818463)
- [Will Retrieval Augmented Generation (RAG) Be Killed by Long-Context LLMs?](https://zilliz.com/blog/will-retrieval-augmented-generation-RAG-be-killed-by-long-context-LLMs)

### 4)

최근 인공지능의 발전 속도가 워낙 빠르기 때문에 미래를 예측할 수 없지만 극단적으로 A가 B를 밀어내지는 않을 듯 하고 서로가 서로를 보완하면서 발전해나가지 않을까 싶다.
