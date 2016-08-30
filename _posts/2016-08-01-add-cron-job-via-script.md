---
layout: post
title: "script를 이용해서 cron job 등록하기
categories: "sys admin"
---

검색하면 다 나오는 내용이라서 블로깅하는 게 의미가 있을지 모르겠으나, 그래도 내가 어떤 일을 하고 있는지 기록 차원에서 정리해 본다.

서버 1대에서 crontab을 이용하여 cron job을 등록할 때는 불편은 없지만, 여러 서버에 동시에 같은 job을 추가할 때는 불편하다. 이 경우 아래처럼 명령을 주면 편리하다

```
$ (crontab -l 2>/dev/null; echo "*/5 * * * * /path/to/job -with args") | crontab -
```
(출처: http://stackoverflow.com/questions/4880290/how-do-i-create-a-crontab-through-a-script/9625233#9625233)

사용 예)
---------

우선 cron에 아무 내용도 없는 상태에서 시작했다.

```
$ crontab -l
crontab: no crontab for user
```

cron job 하나를 등록해보자

```
$ (crontab -l 2>/dev/null; echo "*/5 * * * * /path/to/job -with args") | crontab -

$ crontab -l   # job이 등록된 것을 볼 수 있다.
10 23 * * * myscript.sh -f
```

위 명령을 반복하면 계속 추가할 수도 있다.

```
$ (crontab -l 2>/dev/null; echo "10 23 * * * another_script.sh -g") | crontab -

$ crontab -l
10 23 * * * myscript.sh -f
10 23 * * * another_script.sh -g
```

job 변경
-------

`sed`를 이용하면 간단한 수정 정도는 가능하다. "10 23"을 "50 10"으로 변경해보자

```
$ crontab -l
10 23 * * * myscript.sh -f
10 23 * * * another_script.sh -g

$ (crontab -l 2>/dev/null | sed 's/10 23/50 10/' ) | crontab -

$ crontab -l
50 10 * * * myscript.sh -f
50 10 * * * another_script.sh -g
```

더 복잡한 수정은 당신의 sed, awk 등 스크립트 사용 능력에 달려있다. ;)
