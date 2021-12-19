---
layout: post
title: "Run Length Encoding 특성을 활용하여 Parquet File Size 줄이기"
categories: "bigdata"
---

흔한 압푹 기법 중에, RLE (Run Length Encoding)이라는 것이 있다. 예를 들어 텍스트 파일에서 "AAAAA"와 같이 A가 다섯 번 등장한 문자열을 그냥 "A5"와 같이 표현하면 크기를 약 40%로 줄일 수 있다.

Parquet 같은 Column DB들도 RLE를 지원하는데, 이를 잘 응용하면 특정 필드로 정렬함으로서 File Size를 크게 줄일 수 있다.

예를 들어 다음과 같은 순서로 저장된 웹 로그가 있다고 가정하자

|사용자 id|지역|방문 페이지|방문 시각|
|---------|----|-----------|---------|
|user1    |서울|page1      |11시 11분|
|user2    |경기|page2      |11시 12분|
|user1    |서울|page3      |11시 13분|
|user2    |경기|page4      |11시 14분|

Parquet는 컬럼 DB이므로 컬럼 단위로 Data가 저장된 순서를 보면 현재 record의 컬럼 값과 다음 record의 동일 컬럼 값이 중복되는 것이 없으므로 RLE의 효과를 볼 수 없다.

이번엔 `사용자 id`로 정렬을 해서 Parquet로 저장했다고 가정하자

|사용자 id|지역|방문 페이지|방문 시각|
|---------|----|-----------|---------|
|user1    |서울|page1      |11시 11분|
|user1    |서울|page3      |11시 13분|
|user2    |경기|page2      |11시 12분|
|user2    |경기|page4      |11시 14분|

`사용자 id` 컬럼과 `지역` 컬럼에서 중복된 값이 존재하므로 Data Size가 줄어들게 된다.

본인이 사용하는 Data는 특정 컬럼으로 정렬하는 것 많으로도 40% 정도의 크기를 줄일 수 있었다.

{% include spark-reco.md %}
