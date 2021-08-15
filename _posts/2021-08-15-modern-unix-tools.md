---
layout: post
title: "Modern Unix Tools (bat, mcfly, delta)"
categories: "programming"
---

오랜만의 포스팅이다. 3월까지는 비교적 포스팅을 자주했었는데 어쩌다보니 뜸해졌다. 날도 많이 시원해졌고 나의 작업 환경도 다시 자리를 잡아가고 있다. 평소 수준의 포스팅을 작성해볼까 한다.

### 목차

- [Modern Unix Tools](#modern-unix-tools)
- [`bat`](#bat)
- [`mcfly`](#mcfly)
- [`delta`](#delta)

### Modern Unix Tools

몇년 전부터 페이스북은 자주 사용을 안하고 있다. 그냥 2~3일에 한두번 정도 방문해서 페친들 소식을 보는 수준인데, 오늘은 페친이 공유한 [어썸 블로그](https://www.facebook.com/awesomeblogs/posts/1204819803371049)의 글을 보았다.

내용은 다음과 같다.

> dust, mcfly, rg, duf, procs, dog
> 위 커맨드 중 뭔지 아는게 하나라도 있으신가요?
> du, history, grep, df, ps, dig
> 그럼 이런 것들은 잘 알고 있고 여전히 많이 쓰시죠?
> 새로운 시대에는 새로운 커맨드 씁시다. 정말 좋아요.

그리고 link는 github의 [Modern Unix](https://github.com/ibraheemdev/modern-unix)를 걸어주었다. (글의 제목은 "Modern Unix"이지만  "Modern Unix Tools"가 맞을 것 같다)

간만에 20여전 전으로 되돌아간 느김으로 글을 읽고 설치해봤다.

[Unix Power Tools](https://www.amazon.com/Power-Tools-Third-Shelley-Powers/dp/0596003307) 3rd edition이 2002년에 발간되었는데, 만약 4th edition이 출간된다면 위의 명령어들이 포함될까?

집에 있는 Unix Power Tools 2nd edition을 광화문 교보문고에서 2001년 경에 구매한 것 같다. 리눅스를 처음 접했던 1997년부터 2000년 초까지는 여러 프로그램 직접 설치 및 실행하는 재미가 있었지만 어느새 사라진 것 같다.

간만에 그때 그 느낌을 느낄 수 있었고, 그 중에 재미있는 명령 3개를 정리해본다.

설치 방법은 모두 Mac 기준이다.

### `bat`

- 대체 프로그램: `cat`
- 홈페이지: https://github.com/sharkdp/bat
- 설치 방법
    ```console
    $ brew install bat
    ```
- 기능
    - syntax highlight
    - git repository의 파일이 modified된 경우 diff를 보여줌

스샷은 다음과 같다. (출처: bat 홈페이지)

<img src="https://camo.githubusercontent.com/7b7c397acc5b91b4c4cf7756015185fe3c5f700f70d256a212de51294a0cf673/68747470733a2f2f696d6775722e636f6d2f724773646e44652e706e67" />
<BR>
<img src="https://camo.githubusercontent.com/c436c206f2c86605ab2f9fb632dd485afc05fccbf14af472770b0c59d876c9cc/68747470733a2f2f692e696d6775722e636f6d2f326c53573452452e706e67" />

### `mcfly`

- 홈페이지: https://github.com/cantino/mcfly
- 설치 방법
    ```console
    $ brew tap cantino/mcfly
    $ brew install mcfly
    ```
    - 이후 `~/.bashrc`에 아래 내용을 추가한다
        ```
        eval "$(mcfly init bash)"
        ```
- 사용 방법
    - 쉘에서 <kbd>Ctrl</kbd>+<kbd>r</kbd>를 입력하면 과거 입력했던 명령을 쉽게 선택할 수 있다

스샷은 다음과 같다. (출처: mcfly 홈페이지)

<img src="https://raw.githubusercontent.com/cantino/mcfly/master/docs/screenshot.png">

### `delta`

"Modern Unix"에 나와 있는 툴 중에서 내가 생각했을 때 가장 유용한 것이 `delta` 같다.

- 홈페이지: https://github.com/dandavison/delta
- 설치 방법
    ```console
    $ brew install git-delta
    ```

brew 패키지 이름이 `git-delta`인 것에서 볼 수 있듯이 git과 연동할 때 좋다. (참고로 brew에 이미 delta라는 다른 이름이 존재해서 `git-delta`로 naming한 것 같다)

우선 `delta` 적용 전 `git diff` output을 보자.

<img style="width: 100%; max-width: 549px;" src="https://i.imgur.com/2r07d2V.png" />

이제 delta를 git에 연동해보자.

```console
$ git config --local pager.diff delta
```

이후 `git diff`를 보면 다음과 같이 보인다.

<img style="width: 100%; max-width: 549px;" src="https://i.imgur.com/vCaW4gF.png" />

뭔가 visual해진 것 같은데 default theme가 나랑 잘 안 맞는 것 같다. 이제 thema를 변경해보자.

우선 [`themes.gitconfig`](https://github.com/dandavison/delta/blob/master/themes.gitconfig) 파일을 다운로드하자.

내용은 다음과 같이 생겼다. 내용에서 보면 알겠지만 대략 색상 같은 걸 기술하는 것 같다.

```
[delta "collared-trogon"]
    ...
    hunk-header-decoration-style = "#022b45" box ul
    hunk-header-file-style = "#999999"
    hunk-header-line-number-style = bold "#003300"
	...

[delta "coracias-caudatus"]
	...
    hunk-header-decoration-style = "#cfd6ff" ul
    hunk-header-file-style = "#858dff"
    hunk-header-line-number-style = "#7536ff"
    ...
```

그리고 `themes.gitconfig`를 `/usr/local/Cellar/git-delta/0.8.3/themes.gitconfig`에 저장한다. (path 및 version은 각자 환경마다 다름)

이후 다음의 명령을 실행한다. git 명령 실행 시 `themes.gitconfig`를 include하라는 설정이다.

```console
$ git config --local include.path /usr/local/Cellar/git-delta/0.8.3/themes.gitconfig
```

마지막으로 원하는 theme를 지정하자. `calochortus-lyallii`는 여러 theme 중에서 내 관점에서 제일 괜찮았던 theme이다.

```console
$ git config --local delta.features calochortus-lyallii
```

`git diff`를 다시 실행하면 결과가 다음과 같이 변경된 걸 볼 수 있다.

<img style="width: 100%; max-width: 549px;" src="https://i.imgur.com/v7plwSL.png" />

아래 내용은 동일한 변경을 github에서 본 것이다. 현재 선택된 delta의 output이 github과 유사해서 괜찮은 듯 하다.

<img style="width: 100%; max-width: 549px;" src="https://i.imgur.com/Mbs1iwo.png" />
