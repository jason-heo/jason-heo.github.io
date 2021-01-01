---
layout: post
title: "github에 multiple ssh key 사용하기"
categories: "programming"
---

## 들어가며

github에 ssh rsa 공개키를 등록해두면 암호를 입력할 필요가 없어서 편하다. 그런데 github에는 동일 공개키를 1개의 Repository에만 저장할 수 있다. 따라서 두 개 이상의 Repository를 사용하는 경우 공개 키도 두 개 이상을 등록해야하고 비밀 키도 두 개 이상이 된다.

문제는 `git` 명령을 사용할 때 기본으로 읽는 비밀키가 `$HOME/.ssh/id_rsa`라는 점이다. 따라서 기본 공개 키가 아닌 다른 공개 키를 입력한 경우 인증이 불가능하는 문제가 있다.

이를 해결하는 방법에는 두 가지 방법이 있다.

## 1) config file을 이용하는 방법

`$HOME/.ssh/config` 파일에 아래와 같은 내용을 등록한다.

```
Host github.com
HostName github.com
User git
IdentityFile ~/.ssh/id_rsa

Host github-project-B.com
HostName github.com
User git
IdentityFile ~/.ssh/id_rsa-project-B
```

`Host`는 실제 host 이름의 Alias를 의미한다. `HostName`에 등록된 값이 실제 host 이름이다.

이제 아래의 명령을 이용하면 `~/.ssh/id_rsa-project-B` 비밀키를 사용할 수 있다.

```console
$ git clone git@github-project-B.com:username/project-B.git project-B
```

(출처: https://stackoverflow.com/a/56067132/2930152) 

## 2) GIT_SSH_COMMAND를 이용하는 방법

Git 2.3.0부터 사용할 수 있는 방법이다.

우선 `GIT_SSH_COMMAND`라는 쉘 변수를 등록한다.

```console
$ GIT_SSH_COMMAND='ssh -i ~/.ssh/id_rsa-project-B'
```

이제부터는 일반적인 `git` 명령을 사용하면 된다.

```console
$ git clone git@github.com:username/project-B.git project-B
```

프로젝트가 여러 개인 경우 `GIT_SSH_COMMAND` 변수 값을 변경해야하므로 약간 번거로울 수 있다. 하지만, config file을 만들지 않아도 되므로 키만 잘 관리하고 있으면 개발이나 운영 환경을 만들기 쉽다.

(출처: https://stackoverflow.com/a/29754018/2930152)
