---
layout: post
title: "(2023.09 기준)  code 생성용 LLM들"
categories: "llm"
---

바야흐로 code 생성용 LLM의 전성 시대다.

### StarCoder 계열

2023.05에 StarCoder release

- StarCoderBase: A code generation model trained on 80+ programming languages, providing broad language coverage for code generation tasks
- StarCoder: A finetuned version of StarCoderBase specifically focused on Python, while also maintaining strong performance on other programming languages
- StarCoderPlus: A finetuned version of StarCoderBase on English web data, making it strong in both English text and code generation
- 위 3개의 playground: https://huggingface.co/spaces/bigcode/bigcode-playground
- StarChat
  - 개발기: https://huggingface.co/blog/starchat-alpha
  - playground: https://huggingface.co/spaces/HuggingFaceH4/starchat-playground
- 학습에 사용된 data set: https://huggingface.co/datasets/bigcode/the-stack
  - 전체 size는 3TB나 된다
  - python만 해도 parquet file로 80GB나 되는 듯

### Salesforce CodeGen 2.5

발표일: 2023.05

https://blog.salesforceairesearch.com/codegen25/

요것도 playground를 못 찾았다.

https://www.salesforceairesearch.com/demos 에서 대기 걸어두었음

### StableCoder

발표일: 2023.08.08

https://stability.ai/blog/stablecode-llm-generative-ai-coding

parameter가 3B 밖에 안된다

playground는 못 찾았다

model은 https://huggingface.co/stabilityai/stablecode-instruct-alpha-3b

### Meta Code Llama

발표일: 2023.08.24

https://about.fb.com/news/2023/08/code-llama-ai-for-coding/

Llama 2를 기반으로 fine-tuned되었다고 한다

기본적으로 Code Llama가 있고, 더불어 Code Llama Python, Code Llama Instruct가 있다고 함.

