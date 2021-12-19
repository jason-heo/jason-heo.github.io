---
layout: post
title: "mysql.sock 파일 위치 찾기"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20451046/mysql-server-socket-tmp-mysql-sock-connection-issue/20452310

## 질문

MySQL 설치는 잘 되었고, 데몬도 정상적으로 실행이 되었다. 그런데 mysql에 접속하려고 하면 mysql.sock 파일을 찾지 못한다고 한다.

    $ mysql -uroot
    ERROR 2002 (HY000): Can't connect to local MySQL server through socket '/tmp/mysql.sock' (2)

mysql.sock 파일의 위치를 알 수 있는 방법은 무엇인가?

{% include adsense-content.md %}

## 답변

Unix 계열 OS에서는 다음과 같은 방법으로 mysql.sock 파일을 찾을 수 있다. 필자는 윈도 계열 OS는 잘 다루지는 못하므로 양해 바란다.

### lsof를 이용하기

유닉스 계열에 존재하는 lsof는 "list open files"의 약자로서 프로그램에 open된 파일을 출력하는 유틸리티 프로그램이다. Unix에서는 모든 것을 파일로 다루므로 mysql.sock 파일의 위치도lsof로 찾을 수 있다.

    $ /usr/sbin/lsof | grep mysql.sock
    mysqld      pid username    ...  /path/to/mysql.sock
    mysqld      pid username    ...  /path2/tmp/mysql.sock

필자의 경우는 2개의 MySQL instance를 실행 중이라서 2개의 mysql.sock 파일이 검색되었다. mysql.sock 파일이 검색되지 않았다면 MySQL이 실행 중이지 않을 가능성이 많다. 물론 파일 이름이mysql.sock 아닐 가능성도 존재한다. (my.cnf를 통해서 mysql.sock 이름을 변경할 수 있다.)

lsof는 OS에 기본으로 포함된 유틸리티가 아닐 수 있는데, 이 경우는 OS에 맞는 패키지 관리 프로그램을 이용하여 lsof를 설치해서 사용하면 된다.

### /proc/net/unix를 이용하기

/proc/net/unix는 현재 OS에 존재하는 Unix Domain Socket 파일 목록을 저장하는 파일이다. 따라서 다음과 같이 mysql.sock 파일을 찾을 수 있다.

    $ cat /proc/net/unix | grep mysql.sock
    ...... /path/to/mysql.sock
    ...... /path2/tmp/mysql.sock

username이나 pid 정보는 알 수 없지만, mysql.sock 파일 위치를 찾는데 부족함은 없다고 생각된다. /proc/net/unix는 Unix 계열 OS에 거의 대부분 존재하는 파일이기 때문에 별도의 프로그램을 설치해야 하는 번거로움도 없다. (단, Mac 계열에서도 /proc/net/unix 파일이 지원되는지는 확실하지 않다.)

{% include mysql-reco.md %}
