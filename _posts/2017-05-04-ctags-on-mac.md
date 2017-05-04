---
layout: post
title: "mac에서 ctags recursive 사용하기"
categories: "programming"
---

mac에서 `ctags -R` 옵션이 안 먹히는 경우 다음처럼 하면 된다.

```
#you need to get new ctags, i recommend homebrew but anything will work
$ brew install ctags

#alias ctags if you used homebrew
$ alias ctags="`brew --prefix`/bin/ctags"

#try again!
ctags -R --exclude=.git --exclude=log *
```

출처: [https://gist.github.com/nazgob/1570678](https://gist.github.com/nazgob/1570678)
