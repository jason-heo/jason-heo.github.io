---
layout: post
title: "Linux의 pwd와 cd의 옵션"
categories: programming
---

## 새로운 사실을 알게되는 기쁨

새로운 사실을 알게되는 이런 기쁨을 어떤 말로 표현할 수 있을까. (이런 기쁨을 느끼기 위해 내가 공부를 안하는 것인가...)

오늘도 아주 우연하게 pwd와 cd의 옵션을 알게 되었다. 아주 간단한 (것처럼 보이는) pwd에 옵션이 있다고 상상이나 했을까.... 쉘 내장 명령인 cd에도 옵션이 있다니 신기하지 않은가.

## physical path v.s logical path

- logical path : soft link를 포함한 경로
- physical path : soft link가 가리키고 있는 원래의 물리적인 경로

## pwd -P

`pwd -P` 옵션은 현재 경로를 출력하는데 physical path를 출력한다. 다음의 예를 보자

```
$ pwd
/home/foo/bar
$ ls -l s
s -> /home/foo/p/
$ cd s
$ pwd              <= 아무 옵션 없을 때
/home/foo/bar/s
$ pwd -P           <= -P 옵션을 사용하여 physical path를 출력
/home/foo/p
```

## cd -P

`cd -P` 는 pysical path를 기준으로 이동한다.

```
$ pwd
/home/foo/bar/s
$ pwd -P
/home/foo/p
$ cd -P ..
$ pwd
/home/foo  <= cd에서 -P 옵션이 없었다면 /home/foor/bar로 이동했을 것이다.

## cd -

cd에서 `-` 옵션을 사용할 수 있다. 이 옵션도 2년 전쯤 우연히 알게된 옵션인데 현재 directory로 이동하기 전의 경로로 이동하게 된다. 바로 이전 작업 디렉터리가 기억나지 않으면 'cd -'로 이동할 수 있다. 'cd -'를 한다고 해서 계속 뒤로 가는 건 아니고, 'cd -'를 2번하면 원래의 디렉터리로 돌오안다.
