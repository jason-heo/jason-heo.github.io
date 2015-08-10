---
layout: post
title: "vim에서 Markdown Header 자동 설정"
categories: programming
---

code 및 사용 법
=======

vim code
--------

아래의 code를 본인의 `.vimrc`에 복사

```
function! UnderlineHeading(level)
  if a:level == 1
    :t.|s/./=/g
  elseif a:level == 2
    :t.|s/./-/g
  else
    normal! I###<space>
  endif
endfunction

command! MH1 call UnderlineHeading(1)
command! MH2 call UnderlineHeading(2)
command! MH3 call UnderlineHeading(3)
```

사용 법
-------

- Heading 전 Line

```
    Chapter 1
```

- `:MH1` 명령 수행 후

```
    Chapter 1
    =========
```

- `:MH2` 명령 수행하면 다음과 같이 변함

```
    Chapter 1
    ---------
```

참고 자료
=========

- 참고 URL 1: https://github.com/christoomey/your-first-vim-plugin/tree/master/markdown-underline
- 참고 URL 2: http://travisjeffery.com/b/2011/11/markdown-headers-in-vim/

