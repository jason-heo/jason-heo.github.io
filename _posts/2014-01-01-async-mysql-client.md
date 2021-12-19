---
layout: post
title: "Asynchronous MySQL Client (비동기 MySQL 클라이언트)"
date: 2014-04-18 
categories: mysql
---

WebScaleSQL에 대한 설명을 읽으면서 재미있는 내용을 발견했다.

> What we’re working on now
> After these initial accomplishments, we’ve started work on a number of other improvements to upstream MySQL. A few activities that Facebook’s WebScaleSQL team is currently working on:
> Contributing an asynchronous MySQL client (links here and here) which means that while querying MySQL, we don’t have to wait to connect, send, or retrieve. This non-blocking client is currently being code-reviewed by the other WebScaleSQL teams, after being used in production at Facebook for many months.

현재 peer review 중인 code 중 non-blocking mode로 작동하는 MySQL client 라이브러리에 대한 내용인데 이미 페이스북에서는 해당 코드를 production에서 사용 중이라고 한다.

percona에서 발표했던 자료를 [여기](http://www.percona.com/live/mysql-conference-2014/sites/default/files/slides/Percona%20Live%202014.pdf)서 받을 수 있다.

{% include mysql-reco.md %}
