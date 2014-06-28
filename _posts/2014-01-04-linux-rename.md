---
layout: post
title: "리눅스에서 여러 파일 이름 변경하기 (rename)"
date: 2014-03-05 21:34:00
categories: computer
---

리눅스에서 여러 개의 파일을 동시에 이름 변경하려면 쉘 스크립트를 작성해야 한다. 변경 규칙이 단순하다면 쉘 스크립트도 간단하지만, 변경 규칙이 복잡한 경우 쉘 스크립트도 복잡해진다. 리눅스에는rename이라는 유틸리티가 존재하는데 파일 이름을 변경할 때 유용하게 사용할 수 있다.

{% include adsense-content.md %}
 
우선 man pages에서 rename에 대한 설명을 보자.

    NAME
       rename - Rename files
     
    SYNOPSIS
       rename from to file...
     
    DESCRIPTION
       rename  will  rename  the specified files by replacing the first occur-
       rence of from in their name by to.
     
       For example, given the files foo1, ..., foo9, foo10, ...,  foo278,  the
       commands
     
              rename foo foo0 foo?
              rename foo foo0 foo??
     
       will turn them into foo001, ..., foo009, foo010, ..., foo278.
     
       And
              rename .htm .html *.htm
     
       will fix the extension of your html files.

## 사용 예1

다음은 *.html 파일을 찾아서 확장자를 .html로 변경하는 예이다.

    $ ls *.htm
    a.htm  b.htm  c.htm
     
    $ rename .htm .html *.htm
     
    $ ls *.html
    a.html  b.html  c.html

쉘 스크립트로도 할 수 있긴 하지만 rename을 이용하면 쉽게 파일 이름을 변경할 수 있다.

## 사용 예2

이번엔 조금 복잡한 예이다. 우선 다음과 같은 파일들이 있다고 하자.

    $ ls *.jpg
    img1.jpg  img111.jpg  img2.jpg  img99.jpg

위의 ls 결과에서 보다시피 파일 정렬 순서가 문자열 기준이기 때문에 img111.jpg가 img99.jpg보다 먼저 출력되었다. 이를 방지하기 위해서는 숫자를 img099.jpg 처럼 3자리로 통일시켜주는 것이 좋다. rename을 2번 실행시키면 된다.

    $ rename img img00 img?.jpg
    $ rename img img0 img??.jpg
    $ ls *.jpg
    img001.jpg  img002.jpg  img099.jpg  img111.jpg

첫 번째 rename은 img1.jpg처럼 1자리 숫자를 img001.jpg로 변경하는 명령이며, 두 번째 rename은 img99.jpg 같은 2자리 숫자를 img099.jpg로 변경한 명령이다. 이것도 쉘 스크립트로 가능하겠지만 앞의 예보다는 쉘 스크립트 작성하기가 복잡하다.

참고로 CentOS의 경우 rename은 util-linux rpm에 의해서 설치된다.

## Unix Power Tools에 소개된 방법

Unix Power Tools라는 오렐리에서 출간된.. 지금은 절판되어 구하기 어려운 책이 있다. "$ mv *.new *.old" 같은 명령을 다음과 같이 sed를 이용할 수 있다고 소개하고 있다.

    $ ls -d *.new | sed "s/\(.*\)\.new$/mv '&' '\1.old'/" | sh
    % ls -d *.new | sed 's/\(.*\)\.new$/mv "&" "\1.old"/' | sh

\[출처] - http://docstore.mik.ua/orelly/unix3/upt/ch10_09.htm
