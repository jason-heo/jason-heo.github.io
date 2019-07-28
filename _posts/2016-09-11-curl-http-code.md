---
layout: post
title: "curl에서 http 응답 code 저장하기"
categories: "sys admin"
---

curl을 사용하면 Rest API도 쉽게 호출할 수 있고, 웹 페이지 결과를 저장하기 쉽다. curl은 용도가 다양한데, 본인의 경우 웹 사이트가 정상 작동 중인지를 모니터링하는 용도로도 사용 중이다.

curl의 exit code를 이용하면 웹 사이트가 비정상적인지 알 수 있다.

```
$ curl -XGET localhost:8080
curl: (7) Failed to connect to localhost port 8080: Connection refused

$ echo $?
7
```

그런데 http 응답 코드를 같이 조회하려면 어떻게 해야 할까? curl에서 제공하는 `-w` 옵션을 이용하면 된다.

```
       -w, --write-out <format>
              Make curl display information on stdout after a completed trans-
              fer. The format is a string that may contain  plain  text  mixed
              with  any  number of variables. The format can be specified as a
              literal "string", or you can have curl read the  format  from  a
              file  with  "@filename" and to tell curl to read the format from
              stdin you write "@-".
```

`-w` 옵션은 다양한 포맷을 지원하는데, 그중 하나가 `%{http_code}`로서 응답 코드를 출력하는 포맷이다.

아래와 같이 수행을 하면 결과의 제일 하단에 `200`이 출력된 것을 볼 수 있다.

```
$ curl -XGET  -w %{http_code} localhost:9200
{
  "name" : "inos",
  "cluster_name" : "elasticsearch",
  "version" : {
    "number" : "2.3.5",
    "build_hash" : "90f439ff60a3c0f497f91663701e64ccd01edbb4",
    "build_timestamp" : "2016-07-27T10:36:52Z",
    "build_snapshot" : false,
    "lucene_version" : "5.5.0"
  },
  "tagline" : "You Know, for Search"
}
200
```

이게 몇 가지 옵션을 조합하면 응답 코드만 추출할 수 있다. 우선 정상 상황에서의 수행 결과를 보자.

```
$ curl -# -o /dev/null -I -w %{http_code} -s -XGET localhost:9200 > stdout 2> stderr
$ cat stdout
200
```

stdout에 200이 출력된 것을 볼 수 있다. 이번엔 웹 사이트가 비정상적인 경우의 예를 보자.

```
$ curl -# -o /dev/null -I -w %{http_code}  -XGET localhost:9200 > stdout 2> stderr

$ cat stdout
000

$ cat stderr

curl: (7) Failed to connect to localhost port 9200: Connection refused
```

stdout에는 000이 찍혔고, stderr에는 오류 메시지가 잘 capture되었다.

- `-I`: Header만 조회하는 옵션
- `-o /dev/null`: output이 필요없으므로 `/dev/null`로 보내는 옵션
- `-#`: 진행 상태를 ####... 으로 출력하는 옵션

`-#` 옵션을 왜 사용하는지 이해가 안 될 수 있는데, 오류 메시지를 깔끔하게 capture하기 위한 용도이다. `-#` 옵션이 없는 상황에서 오류 메시지를 확인해보자.

```
$ curl -o /dev/null -I -w %{http_code}  -XGET localhost:9200 > stdout 2> stderr
$ cat stderr
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0curl: (7) Failed to connect to localhost port 9200: Connection refused
```

출처: [http://superuser.com/questions/272265/getting-curl-to-output-http-status-code](http://superuser.com/questions/272265/getting-curl-to-output-http-status-code)
