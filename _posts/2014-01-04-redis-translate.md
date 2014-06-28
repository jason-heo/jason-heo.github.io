---
layout: post
title: "Redis 위키피디아 번역"
date: 2011-05-10 21:34:00
categories: programming
---

원문 : http://en.wikipedia.org/wiki/Redis

Redis는 네트워크, 인-메모리, 키-값 데이터 저장, 옵션으로 지속성(Durability)를 지원하는 오픈소스 프로젝트이며 ANSI C로 작성되어 있다. VMware로부터 스폰서 지원을 받아 개발되고 있다.


## 지원 언어

Redis는 다음가 같은 언어를 지원한다.

ActionScript, C, C++, C#, Clofure, Common Lisp, Erlang, Go, Haskell, Haxe, lo, Java, Server-side Javascript(Node.js), Lua, Objective-C, Perl, PHP, Pure Data, Python, Ruby, Scala, Smalltalk, Tcl

## 데이터 모델

Redis에서 외부 계층의 데이터 모델은 키가 값으로 매핑된 딕셔너리이다. Redis와 다른 구조적 저장소 시스템과 제일 다른 점은 값이 문자열만으로 한정되지 않는 점이다. 문자열 이외에 다음과 같은 추상적인 데이터 타입을 저장할 수 있다.


- 문자열의 리스트
- 문자열의 집합 (정렬되지 않고, 반복되지 않는 원소들의 컬렉션)
- 문자열들의 정렬된 집합 (score라는 소수(floating-point number) 값에 의해 원소들이 정렬된 비반복적 원소들의 컬렉션)
- 키와 값이 문자열인 해시

저장되는 값에 따라 허용되는 연산(command라 불림)이 다르다. Redis는 intersection, union, 리스트/집합/정렬된 집합의 차이 등의 상위 레벨의 원자적(atomic) 서버 측 연산을 지원한다.

## 지속성 (Persistence)

(앞에서 나온 지속성은 Durability 임. 역자주.)

Redis는 일반적으로 모든 데이터를 RAM에 저장한다. 2.4 이전 버전에서는 가상 메모리를 사용할 수 있도록 설정할 수 있으나 현재 지원하지 않는다. 지속성은 다음과 같은 2가지 방법으로 유지된다. 첫 번째는 스냅샷(snapshot)이라는 방법으로 반지속성을 지원하는데 메모리 내용을 주기적으로 디스크로 전송하는 방법이다. 1.1 버전부터는 append-only 파일(저널) 방식을 도입하여 좀 더 안전한 방법을 지원하는데, 메모리 상의 데이터에 변경을 가하는 연산을 쓰는 방법이다. Redis는 저널이 무기한 커지는 것을 방지하기 위해 append-only 파일을 백그라운드 상태에서 재작성할 수 있다.

## 복제 (Replication)

Redis는 마스터-슬레이스 구조의 복제를 지원한다. Redis 서버에 있는 모든 자료는 여러 대의 슬레이브 서버로 복제될 수 있다. 또한 슬레이브는 다른 슬레이브의 마스트가 될 수도 있다. Redis는 루트가 1개인 복제 트리를 구현할 수 있다. Redis 슬레이브들은 인스턴스들 사이의 의도적 혹은 비의도적인 비일관성을 허용한다. Publish/Subscribe 기능은 완벽하게 구현되어 있다. 따라서 복제 트리에서 아무데서나 슬레이브의 클라이언트는 채널에 SUBSCRIBE한 뒤, 마스터에게 PUBLISH를 전송할 수 있다. 복제는 읽기에 대해 확장성을 높이거나 데이터 중복을 하는 유용하다

## 성능

Redis는 데이터의 지속성 (durability)이 필요하지 않다면 트랜잭션의 커밋 시점에 모든 변화를 디스크에 기록하는 데이터베이스 시스템보다 훨씬 빠른 성능을 제공한다. 읽기와 쓰기 성능에 커다란 차이점은 없다.

---

## 역자의 생각.

Redis와 비교될 수 있는 가장 근접한 시스템은 현재도 많이 사용되는 Memcached일 것입니다. Redis 프로젝트가 시작한지 꽤나 지났지만 아직 국내에서는 Redis가 잘 알려지지 않았고 Memcached에 비해 덜 사용되고 있습니다.

앞에 번역문에도 나왔다 시피 Redis는 단순히 key:value를 저장할 때 value가 string이 아닌 string의 list가 저장될 수 있고 이 value간의 list 연산을 제공하는데 장점이 있다고 생각됩니다. 물론 string 만을 저장하는 것이 나쁘다는 것은 아닙니다.

최근에는 Redis에서 직접 Lua 스크립트를 실행시킬 수도 있고(Lua 언어가 Redis를 지원한다는 것과 다른 의미입니다.) SQL 언어를 Redis에서 사용할 수 있고 user에겐 Redis안에 마치 TABLE이 있는 것처럼 보이는 프로젝트도 진행 중입니다.

데이터 구조가 단순하고 웹 사이트에서 cache 같은 기능을 구현하는데 Redis는 좋은 선택이 될 것입니다.
