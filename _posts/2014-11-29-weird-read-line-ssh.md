---
layout: post
title: "bash에서 while read의 본문에 ssh가 있을 때 오류"
categories: programming
---

bash에서 파일을 읽으면서 loop를 돌 때는 다음과 같이 하는 것이 정석이다

```
while read line
do
    echo $line
    ssh hostname cmd
done < url.txt
```

그런데, while 문 안에서 `ssh` 명령을 실행시키면 loop가 1번만 실행되고 종료된다. 내가 bash 스크립트를 잘못 짠줄 알고 한참을 들여다 봐도 모르겠어서 구글링을 해 봤더니 이거 남들도 다 겪었던 문제였다.

오류 원인은 ssh가 실행되면서 /dev/stdin의 내용을 건드리기 때문이라고 한다. ssh 명령 중에 `-n` 옵션을 사용하면 문제를 해결할 수 있다.

```
while read line
do
    echo $line
    ssh -n hostname cmd
done < url.txt
```

manual page에는 ssh의 `-n` 옵션에 대해 다음과 같이 설명되어 있다.

```
     -n      Redirects stdin from /dev/null (actually, prevents reading from
             stdin).  This must be used when ssh is run in the background.  A
             common trick is to use this to run X11 programs on a remote
             machine.  For example, ssh -n shadows.cs.hut.fi emacs & will
             start an emacs on shadows.cs.hut.fi, and the X11 connection will
             be automatically forwarded over an encrypted channel.  The ssh
             program will be put in the background.  (This does not work if
             ssh needs to ask for a password or passphrase; see also the -f
             option.)
```
