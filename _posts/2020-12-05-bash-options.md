---
layout: post
title: "bash에서 command line short/long option 사용법"
categories: "programming"
---

본 포스팅에서는 bash의 command line 옵션 parsing 방법에 대해 설명한다.

## 개요

command line 프로그램의 실행 옵션 parsing이 생각보다 어렵다.

`curl`로 예를 들어보자.

첫 번째 어려움은 argument를 필요로 하느냐 불필요하느냐이다.

- `X`
    - `curl`에서는 `-X`는 request method를 의미하며 `GET`, `POST` 같은 argument를 필요로 하는 옵션이다
    `-X`에 aruegment가 지정되지 않은 경우 에러이다
- `v`
    - `v` 옵션은 verbose한 메시지를 출력하라는 옵션이다
    - flag성 옵션으로서 argument를 필요로하지 않는다

```console
$ curl -X GET -v http://m.daum.net
```

두 번째 어려움은 argument가 필요한 경우 옵션과 argument를 구분하는 방식이 3가지 존재한다는 점이다. 아래 3가지 표현은 모두 동일한 것을 의미한다

```console
$ curl -XGET http://m.daum.net
$ curl -X GET http://m.daum.net
$ curl -X=GET http://m.daum.net
```

세 번째 어려움은 long option, short option을 지원해야하는 경우다. 사내 혹은 개인적으로 사용할 때는 short option만 지원해도 되지만 외부에 공개할 때는 user friendly하게 long option, short option을 모두 지원하는 것이 좋다.

아래는 short option을 long option으로 변경해본 예이다.

```console
$ curl --request=GET --verbose http://m.daum.net
```

네 번째 어려움은 option과 option이 아닌 것(?)의 순서가 섞여 있을 수 있다는 점이다.

아래 3개 명령의 수행 결과는 모두 동일하다.

```console
$ curl -XGET -v http://m.daum.net
$ curl -XGET http://m.daum.net -v
$ curl http://m.daum.net -XGET -v
```

다섯 번째 어려움은 short option의 경우 flag 변수는 서로 붙여서 사용할 수 있다는 점이다.

예를 들어 `ls`의 경우 아래 두 개 명령의 수행 결과는 동일하다.

```console
$ ls -l -h
$ ls -lh
```

여셧 번째 어려움은 옵션의 arugment가 아닌 프로그램의 argument인 옵션이 존재하다는 점이다. 예를 들어 `http://m.daum.net`은 최종적으로 `curl`에서 읽어들일 url argument이다. `curl`을 사용할 때마다 매번 `curl -u http://m.daum.net` 처럼 사용하는 건 번거로울 것이다.

게다가 `ls` 처럼 여러 개의 file을 지정할 수 있는 경우를 위해서는 좀 더 까다로운 처리가 필요하다.

```console
$ ls -lh a.txt b.txt
```

그외의 `--` 처리도 해야하는 어려움이 있다. `--`의 의미는 "`--` 이후에 나오는 문자열은 option이 아닌 프로그램 argument"인 것을 의미한다.

따라서 아래 명령에서 `-l`은 더 이상 option을 의미하지 않고 `-l` 이라는 파일을 의미하게 된다.

```console
$ ls -- -l
ls: -l: No such file or directory
```

찾아보면 더 많은 어려움이 있겠지만 서론이 너무 길어지므로 어려움에 대한 것은 이쯤에서 마무리하자.

C 언에에서는 위의 어려움을 해결할 수 있는 library를 제공하지만 bash에서는 아쉽게도 이런 library가 부족하다.

## 예제 프로그램

본 예제에서는 위의 curl 예처럼 `-X`와 `-v` 옵션을 지원하는 bash script를 만들어보려한다.

기본적인 사용법은 아래와 같다.

```console
$ ./curl.sh
Usage: ./curl.sh [options] <url>
Options:
 -X, --request COMMAND   Specify request command to use
 -v, --verbose           Make the operation more talkative
```

## short option 사용하기 - bash의 `getopts` 사용

다행인 점은 bash에서도 `getopts`를 이용하면 short option을 사용할 수 있다는 점이다. 앞서 설명했던 `curl` 만큼의 자유도는 없지만 꽤나 편하게 사용할 수 있다.

```sh
#!/bin/bash

function usage()
{
    cat <<EOM
Usage: $0 [options] <url>
Options:
 -X, --request COMMAND   Specify request command to use
 -v, --verbose           Make the operation more talkative
EOM

    exit 1
}

function set_options()
{
    while getopts "X:v" optname
    do
        case $optname in
        X)
            request_method=$OPTARG
            ;;
        v)
            verbose_mode="true"
            ;;
        *)
            usage
            ;;
        esac
    done

    # 남아있는 인자 얻기, 남아있는 인자는 url이다
    shift $((OPTIND-1))
    url=$@
}

set_options "$@"

echo "request_method='${request_method}'"
echo "verbose_mode='${verbose_mode}'"
echo "url='${url}'"
```

정상적인 수행 예)

```console
$ ./curl.sh -X GET -v m.daum.net
request_method='GET'
verbose_mode='true'
url='m.daum.net'
```

지정된 옵션이 아닌 경우 에러를 출력한다.

```console
$ ./curl.sh -X GET -v -h m.daum.net
./curl.sh: illegal option -- h
Usage: ./curl.sh [options] <url>
Options:
 -X, --request COMMAND   Specify request command to use
 -v, --verbose           Make the operation more talkative
```

하지만 아쉽게도 복잡한 표현을 인식을 못한다.

예를 들어 `-X=GET` 과 같은 사용하면 `=GET`을 `-X`의 value로 인식한다.

```console
./curl.sh -X=GET -v m.daum.net
request_method='=GET'
verbose_mode='true'
url='m.daum.net'
```

(하지만 `-XGET`은 제대로 parsing한다)

또한 인자의 순서가 바뀐 것을 인식하지 못한다.

```console
$ ./curl.sh -XGET m.daum.net -v
request_method='GET'
verbose_mode=''
url='m.daum.net -v'
```

{% include adsense-content.md %}

## long short option 섞어 사용하기 1 - bash의 `${1:-}` 사용

이번엔 long option과 short option을 동시에 지원해보자.

아래 소스코드는 [Stackoverflow의 답변](https://stackoverflow.com/a/9271406/2930152)을 참고하여 작성했다.

그런데 아쉽게도 이 방법에서는 `-u http://m.daum.net` 처럼 `-u` 옵션을 항상 지정해야했다. bash 내장 `getopts`의 경우 remaining argument를 처리하는 방법이 있지만 현재 설명하는 구현에서는 remain argument 처리가 불가능하였다. (`case`의 default case 안에서 예외 처리를 하면 가능할 것으로 보인다)

```sh
#!/bin/bash

function usage()
{
    cat <<EOM
Usage: $0 [options] <url>
Options:
 -X, --request COMMAND   Specify request command to use
 -v, --verbose           Make the operation more talkative
EOM

    exit 1
}

function set_options()
{
    while [ "${1:-}" != "" ]; do
        case "$1" in
            -X | --request)
                shift
                request_method=$1
                ;;
            -v | --verbose)
                verbose_mode="true"
                ;;
            -u | --url)
                shift
                url=$1
                ;;
            *)
                usage
                ;;
        esac
        shift
    done
}

set_options "$@"

echo "request_method='${request_method}'"
echo "verbose_mode='${verbose_mode}'"
echo "url='${url}'"
```

short option 사용 예는 다음과 같다.

```console
$ ./curl2.sh -X GET -v -u http://m.daum.net
request_method='GET'
verbose_mode='true'
url='http://m.daum.net'
```

long option과 short option을 섞어서 사용할 수 있다.

```console
$ ./curl2.sh --request GET -v --url http://m.daum.net
request_method='GET'
verbose_mode='true'
url='http://m.daum.net'
```

하지만 아쉽게도 `-X=GET` 과 같은 표현은 인식을 못한다. `case "$1"` 부분의 `$1`에 `-X=GET` 전체가 전달되기 때문이다.

또한 `-X` 처럼 arugment가 필요한 option에 argument가 누락된 것을 검사하지 못한다.

아래 예는 `-X`의 argument가 생략되었지만 `-v`를 `-X`의 argument로 인식한 경우이다. (이 문제는 오류 검사를 좀 더 하면 해결할 수 있는 문제이긴 하다)

```console
$ ./curl2.sh -X -v -u http://m.daum.net
request_method='-v'
verbose_mode=''
url='http://m.daum.net'
```

{% include adsense-content.md %}

## long short option 섞어 사용하기 2 - gnu `getopt` 사용

마지막으로 gnu `getopt`를 사용하는 예를 보자. 앞에서 본 예는 모두 bash에 내장된 기능을 활용한 예였다. 하지만 gnu `getopt`는 별도 프로그램을 이용한다.

console에서 `getopt -h` 명령을 수행하면 아래와 같은 도움말이 나와야한다.

```console
$ getopt -h

Usage:
 getopt <optstring> <parameters>
 getopt [options] [--] <optstring> <parameters>
 getopt [options] -o|--options <optstring> [options] [--] <parameters>

Parse command options.

Options:
 -a, --alternative             allow long options starting with single -
 -l, --longoptions <longopts>  the long options to be recognized
 -n, --name <progname>         the name under which errors are reported
 -o, --options <optstring>     the short options to be recognized
 -q, --quiet                   disable error reporting by getopt(3)
 -Q, --quiet-output            no normal output
 -s, --shell <shell>           set quoting conventions to those of <shell>
 -T, --test                    test for getopt(1) version
 -u, --unquoted                do not quote the output

 -h, --help                    display this help
 -V, --version                 display version

For more details see getopt(1).
```

Mac에는 BSD `getopt`가 설치되어 있으므로 아래 방법을 이용하여 gnu `getopt`를 설치하도록 하자.

```bash
$ brew install gnu-getopt
$ echo 'export PATH="/usr/local/opt/gnu-getopt/bin:$PATH"' >> /Users/user/.bash_profile
```

아래 코드도 [Stackoverflow의 답변](https://stackoverflow.com/a/9274633/2930152)을 참고했다.

```sh
#!/bin/bash

function usage()
{
    cat <<EOM
Usage: $0 [options] <url>
Options:
 -X, --request COMMAND   Specify request command to use
 -v, --verbose           Make the operation more talkative
EOM

    exit 1
}

function set_options()
{
    # --options에는 short option을 지정한다
    # --longoptions에는 말그대로 long option을 지정한다
    # --name은 도움말에 출력할 프로그램 이름이다
    # -- 이후에는 사용자가 입력한 문자열이 입력된다
    arguments=$(getopt --options X:v \
                       --longoptions request:,verbose \
                       --name $(basename $0) \
                       -- "$@")

    eval set -- "$arguments"

    while true
    do
        case "$1" in
            -X | --request)
                request_method=$2
				shift 2
                ;;
            -v | --verbose)
                verbose_mode="true"
				shift
                ;;
            --)
				shift
				break
                ;;
            *)
                usage
                ;;
        esac
    done

    # 남아있는 인자 얻기, 남아있는 인자는 url이다
    shift $((OPTIND-1))
    url=$@
}

set_options "$@"

echo "request_method='${request_method}'"
echo "verbose_mode='${verbose_mode}'"
echo "url='${url}'"
```

이 방법을 이용하면 개요에서 이야기했던 많은 어려움들을 다 해결할 수 있다.

수행 예)

```console
$ ./curl3.sh --request=GET -v m.daum.net
request_method='GET'
verbose_mode='true'
url='m.daum.net'
```
