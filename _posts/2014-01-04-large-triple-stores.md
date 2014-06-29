---
layout: post
title: "Large Triple Stores"
date: 2014-04-28 
categories: programming
---

Semantic Web은 여러 구성 요소가 조합된 개념인데, 그 중 RDF 저장을 위한 RDF 저장소의 (혹은 Triple 저장소) 비중이 가장 크다고 생각된다. 여러 종류의 Triple Store가 있는데 어떤 Store가 얼마나 많은 Triple을 저장했는지 보여주는 자료가 있어서 공유한다.

출처 : http://www.w3.org/wiki/LargeTripleStores

시간과 공간만 충분하다면 어떤 Store 든지 무조건 많은 양을 저장할 수 있겠으나, 

- 빠른 Data 저장 속도
- 대용량에서도 빠른 질의처리

가 중요하리라.

## AllegroGraph : 1조 개 이상

1조라... 상상이 되지 않는다. 1조개의 Triple을 로딩하기 위해 83만 INSERT/sec의 속도로 338시간 동안 로딩을 하였다고 한다. 초당 1만건 INSERT도 어려운데 83만건 INSERT라니 엄청난 속도다. 1조 개 이상의 Triple을 저장하는 것이 현실에서 쓸모 있을까 생각될 수도 있지만 China Mobile에서는 고객들에 대한 상세한 정보를 Triple로 저장하려면 2조 개 이상의 Triple이 필요하다고 한다.

Intel의 도움을 얻어 Franz라는 회사에서 세운 기록이라고 한다. 이에 대해 더 자세한 내용은 여기를 참고하라.

## Stardog : 500억 개

Stardog은 처음 듣는 제품이다. 1만 달러(1,100만원)짜리 단일 서버에서 10억 개까지는 초당 50만개, 200억 개까지는 초당 30만개의 속도로 로딩되었다고 한다. Stardog은 Java로 만들어진 RDF 데이터베이스라고 한다. Stardog에서 500억 개의 Triple을 로딩한 것에 대한 뉴스는 여기를 참고하도록 하자.

## OpenLink Virtuoso v6.1 : 154억 개

shared-nothing 구조의 8개 node로 구성된 Virtuoso에서 세운 기록이라고 한다. 27만 INSERT/sec의 속도.

다들 빠르다 빨라.

## 기타
기타라고 불러서 미안하게 되었지만 아래 것들도 뛰어난 성능을 보여준다. 다만 위에 것들이 너무 뛰어날 뿐.

- BigOWLIM : 200억
- Garlik 4store : 150억
- BigData (r) : 127억
- Yars2 : 70억
- Jena TDB : 17억
...
...
- Sesame : 7억
