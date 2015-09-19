---
layout: post
title: "선형대수(Linear Algebra)에 대한 나의 기초적 생각"
categories: programming
---

Caution
=======
아래 글은 본인이 맘대로 끄적여본 내용입니다. 사실이 아닌 내용을 포함하고 있을 가능성이 많습니다.

대수학
======
 
- Algebra = 대수학 != 大수학
- Algebra = 代수학
    - 대수학이라는 말보단, 표현? Representative? 이라는 말이 어울리지 않을까 싶다.
 
네이버 지식백과
===============
 
- http://terms.naver.com/entry.nhn?docId=2098120&cid=44413&categoryId=44413
- 대수학(代數學)은 수 대신에 문자를 사용하여 방정식의 풀이 방법이나 대수적 구조를 연구하는 학문이다. 대수학의 범위는 다양한 분야를 포함하고 있는데, 그중 대표적으로 선형대수학과 추상대수학이 있다.
 
기호로서 숫자와 연산을 표현한다.
================================
 
- http://www.regentsprep.org/regents/math/algebra/AV1/LALgRep.htm
- `4*3`는 4를 3번 더한다는 의미
- `123 % 25`는 123을 25로 나눈 뒤 나머지
 
- 선형대수도 기본적으로 행렬의 사칙연산 및 특성을 배우는 학문이라 생각하면 됨
 
소수점 표현의 예
================
 
- 비교적 최근까지 분수를 소수로 표현하는 방법이 없었음
- 있더라도 소수간 연산이 어려움
    - 스테빈: 5.912 = 5ⓞ9①1②2③
- 현대의 소수표현은 1619년 네이피어가 최초 사용
 
쉬어가는 코너
============
 
네이피어 = 로그 발명자
----------------------
- 로그 발명 이유
- 계산기가 없던 시절, 천문 현상 계산을 위해선 곱하기, 나누기를 사람이 직접 계산해야 겠다.
- 티코라는 당대 수학자가 sin, cos의 성질을 이용하여 곱셈 계산을 빨리했는데
- 자존심이 상해서 티코보다 더 빨리, 더 정확히 계산하기 위해 Log를 개발함
- `sin(x)*cos(y) = (sin(x+y) + sin(x-y))/2`
- `log(n*m) = log(n) + log(m)`
 
티코 = 천문학자/수학자
----------------------
 
- 망원경이 없던 시절 매눈으로 천문현상 관찰
    - 시력이 5.0이라는 썰이 있음
    - 티코가 관찰한 천문현상은 케플러에 의해 지동설을 지지하는 증거가 됨
    - 티코는 파티에서 오줌을 오래 참다가 병 걸려서 죽음 (파티에 맘에 드는 여자가 있었는데, 그 여자 앞에서 오줌 싸러 가는 것이 x팔리다고 참다가...)
 
오일러
-----
 
- "대수학 원론" written by 오일러, 1765
    - 당시까지만해도 혼란스러웠던 수학표기법을 현대적으로 정립함
    - 함수의 기호 f(x) (현대에서는 함수 이름조차 사라지고 body만 남게 되어 람다 함수라 불림)
    - http://www.kyobobook.co.kr/product/detailViewKor.laf?mallGb=KOR&ejkGb=KOR&barcode=9788952215406
 
결론
===
 
- 선형대수도 결국 각 기호들이 어떤 것을 의미하고, 어떤 성질이 있는지 배우는 학문?
- 특히, Matrix multiplication is really useful since you can pack a lot of computation into just one matrix operation.
- 복잡한 곱셈 연산 수식을 "행렬의 곱"으로 간단히 표현 가능하다.
 
참고
===
 
행렬 연산의 특징
---------------
 
- 연산이 독립적이라서 병렬 처리가 쉽다.
- "HAMA: An Efficient Maxtrix Computation with the MapReduce Framework"
    - http://csl.skku.edu/papers/CS-TR-2010-330.pdf
- "Matrix Multiplication with CUDA"
    - http://www.shodor.org/media/content/petascale/materials/UPModules/matrixMultiplication/moduleDocument.pdf
 
가우스 소거법
-------------
 
- 행렬을 이용하여 연립 방정식 풀기
    - http://blog.naver.com/mykepzzang/220146344544


