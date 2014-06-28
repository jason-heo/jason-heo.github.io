---
layout: post
title: "Liquid Exception: No such file or directory - C:\\Windows\\system32\\cmd.exe"
date: 2014-06-04 21:34:00
categories: programming
---

블로그를 github로 전환하기 위해 이것 저것 테스트 중이다. 우선 cygwin에 Jekyll 설치는 완료했고, markdown으로 이것 저것 테스트 중이다. Pygments로 Syntax Highlight가 되는지 다음 처럼 만들어봤는데...

    {% highlight java %}
    System.out.println("fff");
    {% endhighlight %}

## 오류

앗. 그런데 다음과 같은 오류가 발생

    MS-DOS style path detected: /usr/local/bin/C:\Windows\system32\cmd.exe
    Preferred POSIX equivalent is: /usr/local/bin/C:/Windows/system32/cmd.exe

    CYGWIN environment variable option "nodosfilewarning" turns off this warning.
    Consult the user's guide for more details about POSIX paths:

        http://cygwin.com/cygwin-ug-net/using.html#using-pathnames

    Liquid Exception: No such file or directory - C:\Windows\system32\cmd.exe in test.md
    ...done.
          Regenerating: 1 files at 2014-06-04 20:54:55   Liquid Exception: No such file or directory - C:\Windows\system32\cmd.exe in test.md

## 해결 방법

블로그 검색을 통해서 해결했다. 고마워요 구글님.

    export COMSPEC=/cygdrive/c/Windows/System32/cmd.exe 

출처 : https://github.com/jekyll/jekyll/issues/1383#issuecomment-29472956
