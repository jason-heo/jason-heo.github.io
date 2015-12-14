---
layout: post
title: "Vim에서 python code edit를 편하게 하자"
categories: programming
---

C/C++/Java 등의 프로그래밍 언어들은 code block은 `{ ... }` 처럼 curly bracket으로 구성된다. vim에서 `%` 키를 이용하면 block의 시작과 끝으로 편하게 이동할 수 있다. Python은 이들 언어와 다르게 block을 indent로 표현을 한다. 이로 인해 넣는 장점도 많지만, block의 시작과 끝으로 이동하는 것이 불편했다.

비슷한 고민을 한 사람이 역시 있었고 다음과 같은 명령을 지원하는 plugin이 있더라.

- `]t` -- Jump to beginning of block
- `]e` -- Jump to end of block
- `]v` -- Select (Visual Line Mode) block
- `]<` -- Shift block to left
- `]>` -- Shift block to right
- `]#` -- Comment selection
- `]u` -- Uncomment selection
- `]c` -- Select current/previous class
- `]d` -- Select current/previous function
- `]<up>` -- Jump to previous line with the same/lower indentation
- `]<down>` -- Jump to next line with the same/lower indentation

설치 방법
-----

본인처럼 Bundle을 사용하는 사람은 `.vimrc`에 `Plugin 'python.vim'` 입력 후 `:PluginInstall` 한방이면 설치 완료.


Reference
---------

Plugin URL: http://www.vim.org/scripts/script.php?script_id=30
Plugin을 알게된 출처: http://stackoverflow.com/questions/896145/more-efficient-movements-editing-python-files-in-vim
