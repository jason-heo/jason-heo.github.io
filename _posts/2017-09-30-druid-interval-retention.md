---
layout: post
title: "Druid에서 interval 보관 기간 설정하기"
categories: "druid"
---

경험해본 BigData Platform 중에 가장 성능이 괜찮았던 Druid. (참고: 각자의 use-case마다 성능은 다를 수 있음) 하지만, 하지만 사용자 비친화적이라서 사용하기가 너무 불편하다.

Interval 보관 시간이 최근 30일이며 과거 31일 이후는 필요없다고 가정해보자.

Coordinator에서 다음과 같이 설정을 해 주면 최근 30일만 저장할 수 있다

```
LoadByInterval: P30D
DropByForever
```

처음엔 `DropByInterval: P30D`를 지정했었는데, 이렇게 하면 최근 30일의 Interval이 disable된다 ㄷㄷㄷ
