---
layout: post
title: "NoSQL 위키피디어 번역"
date: 2011-11-06 
categories: programming
---

원문 - http://en.wikipedia.org/wiki/Nosql 

최근 2년전부터 이슈화되고 있는 NoSQL에 관련된 영문 위키피디아 글을 번역해 보았습니다. 기사나 튜토리얼 등을 번역해 본지 5년이 넘었네요... 앞으론 시간이 허용하는데로 관심있는 영문 자료를 한글로 번역해서 올리겠습니다.

---

전산 분야에서 NoSQL(간혹 "Not Only SQL"로 불리기도)은 넓은 분류에서는 Database 관리 시스템이지만, RDBMS의 기존 모델과는 확연한 차이를 보인다. NoSQL 같은 Data 저장소는 고정된 테이블 스키마를 필요로 하지 않으며 Join 연산을 회피하고 있으며, 수평적 확장(Scale Horizontally 혹은 Scale-out)을 지원한다. 학계 연구자들은 이런 데이터베이스를 구조적 저장이라고 부르며, 이 용어는 기존 관계형 데이터베이스를 하위 집합으로 포함하고 있다.

## History

Carlo Strozzi는 그가 1998년에 만든 표준 SQL 인터페이스를 노출하지 않는 가벼우며 오픈 소스인 관계형 데이터베이스에 NoSQL이란 용어를 사용했다. (Strozzi는 현재 NoSQL 움직임에 대해 "관계형 모델에서 벗어났기 때문에, 'NoREL'가 적절하거나 혹은 이 효과를 낼 수 있는 다른 용어로 불려야했다고 제안한다.)

Rackspace에서 근무하는 Eric Evans는 2009년 초기에 NoSQL이란 용어를 다시 소개했다. 당시 last.rm의 Johan Oskarsson은 오픈소스 분산 데이터베이스를 토론하기 위한 행사를 조직하길 원했다. NoSQL은 비관계형, ACID(atomicity, consistency, isolation, durability)를 지원하지 않는 저장소가 크게 증가하는 것에 이름 붙이기 위한 시도였다. ACID 속성은 IBM DB2, MySQL, Microsoft SQL Server 등과 같은 관계형 데이터베이스의 핵심 기능이다.

2011년에는  NoSQL 데이터베이스를 위한 특정 언어를 제공하고자 하는 UnQL(Unstructured Query Language)가 시작되었다. UnQL은 약하게 정의된 필드(RDB의 컬럼)들의 문서(RDB의 레코드)의 묶음(RDB의 테이블)에 질의하기 위한 내장 언어이다. UnQL은 SQL의 상위 집합이며 SQL은 UnQL에서 제약조건이 부여된 형태의 언어이다. SQL에서는 동일 개수, 동일한 이름과 형식을 갖춘 필드를 반환해야만 한다. 하지만, UQL은 CREATE TABLE, CREATE INDEX 같은 데이터 정의 언어(Data Definition Language - DDL) SQL은 정의하지 않고 있다.

## Architecture

현대의 전형적인 관계형 데이터베이스는 문서가 많고 트래픽이 많은 웹사이트, 스트리밍 미디어 같은 특정 데이터 집약적인 응용에서 나쁜 성능을 내고 있다. 일반적으로 RDBMS의 구현은 작은 데이터를 빠르게 읽고 쓰거나 쓰는 작업은 거의 없고 큰 배치 작업을 하는데 적합하도록 되어 있다. 반면 NoSQL은 많은 양의 읽기/쓰기 작업을 소화할 수 있다. 실제 NoSQL은 Digg의 3TB 용량의 '녹색 뱃지(소셜 네트웍에서 다른 사람들에 의해 추천된 이야기임을 표시함)에 사용되었으며 페이스북의 50TB 용량의 인박스 검색에도 활용되어 있다.

NoSQL 구조는 eventual consistency 같은 약한 일관성(Consistency)를 보장하며 트랜잭선은 1개의 항목에 대해서만 지원한다. 하지만 일부 시스템에서는 AppScale이나 CloudTPS같은 미들웨어를 통해서 ACID 기능 전부를 보장한다. BigTable에 기반한 구글 Percolator 시스템과 University of Waterloo에서 개발한 HBase를 위한 트랜잭션 시스템은 컬럼 저장소를 위한 snapshot isolation을 제공한다. 독립적으로 개발된 이들 시스템은 컬럼 저장소를 위한 snapshot isolation 보장을 이용하여 다중행(multi-row) 분산 ACID 트랜잭션을 지원하기 위해 비슷한 개념을 사용하는데, 데이터를 관리하기 위한 부하가 없고, 미들웨어를 필요로 하지 않으며 미들웨에서처럼 관리를 필요로 하지 않는다.

많은 NoSQL 시스템은 분산 구조를 사용하며 데이터는 분산 해시 테이블을 이용하여 여러 대의 서버에 중복되어 저장된다. 이러한 방식으로 시스템은 필요 시 서버를 추가하기만 하면 되는 scale-out 방식으로 확장 가능하며, 1대의 서버가 장애가 나더라도 시스템에 장애가 발생하지 않는다.

일부 NoSQL은 연관 배열 혹은 key-value 쌍 같은 단순한 인터페이스를 선호한다. 반면 XML 데이터베이스 같은 시스템에서는  XQuery 표준을 지원하길 홍보하고 있다. CloudTPS 같은 시스템은 조인 쿼리를 지원하기도 한다.

--- 

윗글 이후의 위키피디아 원문 내용은 NoQL을 종류별로 분류한 내용입니다. 관심있는 분은 원문(http://en.wikipedia.org/wiki/NoSQL)을 읽어 보시기 바랍니다.

개인적인 의견을 몇자 첨언하자면... NoSQL이 이슈화되고 있지만, 전혀 새로운... 기존에 없던 것이 나온 것은 아닙니다. 마치 새로운 개념이 나온 것처럼 흥분하는 지금 분위기가 좀 어색하긴 합니다. NoSQL이란 이름만 보면 SQL만 사용하지 않는 것 같은데, NoSQL은 단지 SQL이 없는 게 중요한 것이 아닌 RDB 형태의 데이터 모델링이 아닌 다른 방식의 모델링을 하면서, RDB와 같은 무결성 보단 성능에 기반한 시스템이라 생각하면 좋겠습니다. NoSQL에서도 SQL 같은 쉽게 코딩할 수 있는 인터페이스가 있으면 있을 수록 좋습니다. 위 글을 보면 CloudTPS는 조인도 지원한다고 하죠... 따라서 NoSQL이란 용어는 다른 용어로 바뀌면 좋겠습니다(만 이미 늦어 버렸죠 ㅎㅎ)
