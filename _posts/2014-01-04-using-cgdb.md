---
layout: post
title: "괜찮은 텍스트 디버거 - cgdb 사용법"
date: 2014-04-08 
categories: programming
---

gdb를 쓸 때 불편한 점은 소스 코드를 보기 어렵다는 점이었다. cgdb는 이런 점을 말끔히 해결해 준다.

스크린샷을 보자.

![cgdb](/images/posts/programming/cgdb.PNG)

현재 설정된 Breakpoint (빨간색 표시)와 현재 수행 중인 line (녹색)을 볼 수 있어서 직관적이다. 사용법 및 명령어는 gdb와 동일하다. 

### 단 약간 다른 점

- TERM이 xterm이어야 syntax highlight가 된다.
- ESC 를 누르면 vi key로 소스 코드를 navigation할 수 있다.
- 다시 명령 줄로 오려면 'i' 키를 누르면 된다.
- print로 내용이 긴 것을 출력하면 화면이 짤리는데 이땐 Page Up,Down을 입력해야 위로 지나간 화면을 볼 수 있다.
