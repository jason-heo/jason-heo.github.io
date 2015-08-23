---
layout: post
title: "file의 특정 범위만 출력하기"
categories: programming
---

이렇게 쉬운 방법을 몰랐었네. I didn't know such a easy way to print specific range of file content.

- 상황: 파일의 123번 라인부터 578라인까지 출력하기
- 기존 방식: `$ head -n 701 | tail -n 455` <= 숫자 계산하기 귀찮다.. 
- easy way: `$ awk 'NR >= 123 && NR <= 578'` <= trivial하다

