---
layout: post
title: "python3에서 웹 서버 띄우기 (서버에 있는 파일 다운받는 용도)"
categories: "python"
---

{% include python.md %}

환경에 따라서 서버에 있는 파일을 Local로 다운받기 불편한 경우가 있다.

이런 경우 서버에 웹 서버를 띄워놓고 directory listing을 해서 파일을 볼 수 있다면 쉽게 파일을 다운로드할 수 있다.

이런 경우 python3를 이용하면 딱 한 줄로 웹 서버를 띄울 수 있다.

```console
$ python3 -m http.server 8081
```

이제 Local 브라우저에서 해당 서버의 8081 port로 접속하면 디렉터리와 파일이 쫙 나온다. 필요한 파일을 다운받고 서버를 내리면 끝.

당연하겠지만 사용자 인증 및 ssl 기능은 없으니 각자 잘 판단해서 사용할 것.
