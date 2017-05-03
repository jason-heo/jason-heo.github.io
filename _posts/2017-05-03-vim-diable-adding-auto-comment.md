---
layout: post
title: "vim에서 comment가 자동 추가되는 것 방지하기"
categories: "programming"
---

`vimrc`에 아래 내용 추가

```
set formatoptions-=cro
```

각각의 의미는 [출처](https://superuser.com/questions/271023/vim-can-i-disable-continuation-of-comments-to-the-next-line) 참고
