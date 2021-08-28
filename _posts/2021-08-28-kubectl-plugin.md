---
layout: post
title: "kubectl plugin 개발하기"
categories: "programming"
---

`kubectl`에서는 plugin을 아주 쉽게 개발할 수 있다. 직접 사용해보면 이걸 plugin이라고 불러야할지도 애매모호한데, 암튼 `kubectl`에 명령을 쉽게 추가할 수 있다.

관심있는 개발자는 https://kubernetes.io/docs/tasks/extend-kubectl/kubectl-plugins/ 를 읽어보는 것이 좋다.

### 예제: pod name만 출력하기

`kubectl`을 이용하여 pod name만 출력하려면 [본 Stackoverflow 답변](https://stackoverflow.com/q/35797906/2930152)에 있는 것과 같은 것을 해야한다.

- 방법 1
    ```
    kubectl get pods --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}'
    ```
- 방법 2
    ```
    kubectl get pods --no-headers -o custom-columns=":metadata.name"
    ```

물론 그 외에도 `awk`나 `cut` 등을 이용하는 다양한 방법이 있다.

그런데 사용법도 잘 기억이 안나고, 잘 기억하더라도 매번 위 내용을 입력하는 건 번거롭다.

그래서 shell script를 만들어 놓는 경우가 많은데 이미 shell script가 준비되었다면 손 쉽게 plugin을 만들 수 있다.

우선 다음과 같이 `kubectl-ppn`이라는 파일을 만들어두자. (`PATH`에 걸린 디렉터리에 생성해야한다)

ppn은 'print pod name'의 약자라고 해두자.

```console
$ cat <<EOM > kubectl-ppn
#!/bin/sh

kubectl get pods --no-headers -o custom-columns=":metadata.name" $@
EOM

$ chmod 755 kubectl-ppn
```

이후 `kubectl ppn` 명령을 입력하면 pod name이 출력된 것을 볼 수 있다.

그렇다! 우리는 `kubectl`에 `ppn`이라는 command를 추가한 것이다!

### naming 규칙

`ppn`이라는 이름이 너무 짧다보니깐 어떤 의미인지 알기 어렵다는 단점이 있다.

`kubectl` plugin naming 규칙에 따르면 `kubectl-print-podname` 과 같이 `-`로 구분을 하는 경우 아래와 같이 실행할 수 있다.

```console
$ kubectl print podname
```

이렇게 만들면 어떤 파일이 실행되는 것인지 헷갈릴 것 같다. `kubectl-print`라는 file이 존재하고 여기에 `podname`이 argument로 전달되는 것일 수도 있으니 말이다.

### krew를 이용한 plugin 설치

`krew`를 이용하면 외부 개발자들이 만든 plugin을 쉽게 설치할 수 있다.

설치 방법은 [이 문서](https://krew.sigs.k8s.io/docs/user-guide/setup/install/)를 참고한다.

plugin 목록은 [여기](https://krew.sigs.k8s.io/plugins/)에서 볼 수 있다.

원하는 plugin을 찾은 경우 다음과 같은 명령을 이용하여 plugin을 설치할 수 있다.

```console
$ kubectl krew install <pod-name>
```

### 설치된 plugin 조회

`kubectl plugin list` 를 입력하면 설치된 plugin 목록을 볼 수 있다.

```console
$ kubectl plugin list
The following compatible plugins are available:

/Users/user/pkgs/kubectl-plugins/kubectl-ppn
/usr/local/bin/kubectl-krew
/Users/user/.krew/bin/kubectl-graph
```
