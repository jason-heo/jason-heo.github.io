---
layout: post
title: "HDFS inotify"
categories: "programming"
---

- 참고 글: [HDFS-6634: inotify in HDFS](http://johnjianfang.blogspot.com/2015/03/hdfs-6634-inotify-in-hdfs.html?m=1)
    - 이 문서가 설명이 잘 되어 있다
    - Hadoop 2.6에 추가된 API
    - 무려 2014년에 나온 기능이다
- 지원되는 Event type
    - `CREATE`
    - `CLOSE`
    - `APPEND`
    - `RENAME`
    - `METADATA`
    - `UNLINK`
    - `TRUNCATE`
- (아쉽게도) 본 API는 admin user만 사용할 수 있다
    - Admin API를 사용해야하기 때문에 admin user만 사용할 수 있는 기능이다
    - 혹시나하고 실행시켜 봤는데 normal user인 경우 다음과 같은 에러가 발생한다
        - > Exception in thread "main" org.apache.hadoop.ipc.RemoteException(org.apache.hadoop.security.AccessControlException): Access denied for user XXX. Superuser privilege is require
- event 조회 방식
    - `take()`: event가 발생할 때까지 기다렸다가 return
    - `poll()`: 주기적으로 polling후 return
- 예제 프로그램
    - [`HdfsINotifyExample.java`](https://github.com/onefoursix/hdfs-inotify-example/blob/master/src/main/java/com/onefoursix/HdfsINotifyExample.java)
    - `lastReadTxId`는 optional이다
