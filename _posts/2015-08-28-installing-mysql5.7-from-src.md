---
layout: post
title: "MySQL 5.6, 5.7을 소스 코드로 부터 설치하기"
categories: MySQL
---

## CMake로 MySQL 컴파일 하기

MySQL 5.5까지는 autotools를 사용했던 것 같은데, MySQL 5.7 테스트를 하려고 소스코드를 다운받았더니 CMake로 변경되었나보다...

autotools는 `./configure --prefix=path` 한방이면 편했는데 `cmake`는 자주 사용하지 않다보니 Install Path 지정하는 방법이 바로 생각나지 않아서 구글링을 좀 했네..

5.5와 다르게 Boost Library도 필요하고... 많이 바뀌었구나.. MySQL 너...

암튼 CMake로는 다음과 같이 설치하면 된다.

```
$ tar xvfz mysql-version.tar.gz
$ cd mysql-version/
$ cmake ./ -DCMAKE_INSTALL_PREFIX=path_to_install
$ make
$ make install
```

Boost Library가 없으면 cmake 과정에서 에러가 발생한다. 각자 사용하는 OS에 맞게 Boost를 설치할 수도 있지만 다음과 같이 CMake 과정에서 Boost를 다운받을 수도 있다.

```
$ cmake ./ -DWITH_BOOST=./boost/ -DDOWNLOAD_BOOST=1 -DCMAKE_INSTALL_PREFIX=path_to_install
```

현재 경로의 `boost/` 디렉터리에 Boost 소스 코드가 다운로드 된다.

## MySQL 설치의 변경된 점

캬... 근데 MySQL 설치 방법이 많이 바뀌었네.

- 5.7.6부터는 `mysql_install_db`도 없어졌다. `bin/mysqld --initalize` 명령을 이용하여 초기화를 해야 한다.
- root 암호가 랜덤하게 생성된다. 이거 까먹으면 좀 귀찮지...
- my.cnf의 기본 파일이 사라졌다!!!! 이거 일일히 만들어야 하나?

## 컴파일이 귀찮다면 Docker를 사용해 보자.

컴파일이 귀찮다면 MySQL Binary 버전을 구해서 사용해도 된다. 그런데 말입니다. Docker를 사용해 보는 것은 어떨까요? 이것 저것 설정하기가 엄청 귀찮네...

[Deview 2013의 Docker 소개](http://deview.kr/2013/detail.nhn?topicSeq=45)
