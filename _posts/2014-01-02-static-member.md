---
layout: post
title: "static member 변수의 definition이 없을 때 optimization level에 따라서 컴파일이 되었다가 안 되었다가..."
date: 2014-06-22 
categories: cpp
---

O2 Level에서 잘 컴파일 되던 것이 O0에서 컴파일이 안 되었다.

다음의 URL을 보고 해결...

http://stackoverflow.com/questions/4891067/weird-undefined-symbols-of-static-constants-inside-a-struct-class

C++에서 static member는 여전히 어렵다.
