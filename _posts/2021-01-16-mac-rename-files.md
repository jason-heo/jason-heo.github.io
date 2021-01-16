---
layout: post
title: "Mac에서 여러 파일 이름 변경하기 (rename)"
categories: programming
---

내 블로그의 조회수 상위 10% 글 중에 [리눅스에서 여러 파일 이름 변경하기 (rename)](/2014/03/05/linux-rename.html)라는 글이 있다.

Mac에도 `rename`이라는 명령이 있는데 사용방법이 다르다.

Mac의 `rename`에서는 `-s` 옵션을 이용하여 파일 이름을 변경할 수 있다. `-s`는 substitute의 약자로서 `-s from to` 처럼 옵션을 지정하면 된다.

아래는 확장자를 `.htm`에서 `.html`로 변경하는 예이다.

- 이름 변경 전 파일들
    ```console
    $ ls
    a.htm   b.htm   c.htm
    ```
- 이름 변경하기
    ```console
    $ rename -s .htm .html *.htm
    ```
- 이름 변경 결과
    ```console
    $ ls
    a.html  b.html  c.html
    ```

당연하겠지만, 아래처럼 사용하면 `test01.sh`를 `test-01.sh`로 변경할 수 있다.

- 이름 변경 전 파일들
    ```console
    $ ls
    test01.sh   test02.sh   test03.sh
    ```
- 이름 변경하기
    ```console
    $ rename -s test test- *.sh
    ```
- 이름 변경 결과
    ```console
    $ ls
    test-01.sh  test-02.sh  test-03.sh
    ```

이 외에도 옵션이 많은데 `-x` 옵션을 이용하면 파일의 확장자를 제거할 수도 있다.
