---
layout: post
title: "Homebrew must be run under Ruby 2.3! (RuntimeError)"
categories: "programming"
---

### 문제

최근에 Xcode가 update된 것 같은데, brew 실행 시 다음과 같은 에러가 발생한다.

```
/usr/local/Homebrew/Library/Homebrew/brew.rb:12:in `<main>': Homebrew must be run under Ruby 2.3! (RuntimeError)
```

### 해결책

`$ brew update` 한방이면 해결됨. (근데 실행 결과가 `Already up-to-date.`인데, 이게 어떤 과정에 의해 해결된 것인지 이해는 안 됨;;)
