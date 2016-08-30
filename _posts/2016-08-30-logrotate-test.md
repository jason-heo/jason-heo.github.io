---
layout: post
title: "Linux logrotate의 log 및 작동하지 않을 때 test 방법"
categories: "sys admin"
---

인터넷 좀만 검색하면 나오는 주제이긴 하지만, 정리를 해두는 게 좋을 것 같다. 정리를 하지 않으니, 매번 까먹는다.

logrotate의 수행 로그
----------------

` /var/lib/logrotate.status` 파일을 보면 파일별 최근 rotate 시각을 볼 수 있다.

```
$ cat /var/lib/logrotate.status
"/path/to/file1" 2016-08-27:0:0
"/path/to/file2" 2016-08-27-3:0:0
...
```

CentOS 7.2 기준이지만 하위 버전에도 적용되는 내용

logrotate가 안 될 때 test 방법
---------------------

logrotate를 설정하고, 다음 날 출근해서 확인했더니 rotate가 안 되었나? 뭐가 문제일까? logrotate 자체의 로그는 위에서 설명한 파일이 전부인데 여기는 이에 관련된 내용이 남지 않아서 원인 파악이 힘들다.

이 경우 logrotate를 아래 방법으로 직접 수행해보면 된다.

```
# logrotate -d -f /etc/logrotate.d/mylog
Ignoring mylog because of bad file mode.

Handling 0 logs
```

그렇다. file 권한의 문제로 수행되지 않았다. `ls -l /etc/logrotate.d/mylog`를 보면 664로 설정되어있는데, 이걸 644로 설정 후 잘 돌아갈 수 있었다.

- `-d`: dry-run 옵션으로서 file을 실제 지우지는 않고 작동 과정만 출력하는 옵션이다.
