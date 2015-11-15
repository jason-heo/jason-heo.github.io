---
layout: post
title: "From Dolphin to ELK"
categories: elasticsearch
---

약 3주전부터 Elasticsearch를 사용 중에 있다. 최근 들어 사람들이 ELK, ELK 할 때마다, "뭐 별 거 있겠어?"하고 넘어갔었는데, 이번에 대량의 Data를 분석할 일이 있어서 자료 조사를 하는 중 "어라, 이거 별거 있는 거 같은데?"라는 생각이 들었고, 실제 사용을 해보고 MySQL과 비교를 해보니 "오호. 이거 물건인데"라는 결론에 이르게 되었다.

Elasticsearch 자료를 찾던 중 [From Elephant to ELK](http://www.digitalgov.gov/2015/01/07/elk/)라는 글을 읽게 되었다. 여기서 *Elephant* 즉, 코끼리는 Hadoop의 Logo이고 *ELK*는 Elasticsearch, Logstash, Kibana의 약자이면서 사슴과 동물의 이름이기도 하다. digitalgov.gov라는 도메인 이름으로보아, 미국의 디지털 정부 관련된 부서에서 운영되는 곳 같다. 이곳에서 Hadoop을 쓰던 중 ELK로 옮겨가게 된 이유와 ELK의 장단점이 잘 정리된 문서이다.

윗 글의 제목을 약각 각색하면 "From Dolphin to ELK"가 된다.

The Dolphin Years
------------------

![Dolphin](https://upload.wikimedia.org/wikipedia/en/6/62/MySQL.svg)
(출처: Wikimedia)

MySQL을 1998년부터 써왔으니 약 17년 동안 MySQL을 잘 써 왔고 이덕분에 많은 일도 있었고, 좋은 사람도 만나고 현 직장에도 취업하게 되었다. 1990년대 말, 2000년 초반 닷컴붐에서는 LAMP Stack이 인기였고 웹 프로그래밍을 하려면 MySQL은 거의 필수적이었다. 나우누리 리눅스 동호회에 MySQL 강좌도 써보고 (아직도 검색되는 글이 있는데 지금 읽어보면 많이 쪽팔린다), 각종 문서 작성/번역, database.sarang.net이라는 커뮤니티 활동도 하면서 나의 20대는 MySQL과 함께한 시대였다.

시간당 10만원 짜리 컨설팅도 해 봤고 (딱 5번;;) MySQL이 많은 사람들에게 사용될 수 있게 많은 역할을 했다고 자부할 수 있는데, 단 하나 어찌 해결하기 어려운 문제가 있었으니 `GROUP BY` 성능이 만족스럽지 못하다는 점이다. INDEX를 아무리 잘 걸고, Table 설계를 잘 하더라도 "GROUP BY" 성능을 높히기 어려웠다.

the ELK era
-----------

![ELK](http://images.nationalgeographic.com/wpf/media-live/photos/000/005/cache/elk_520_600x450.jpg)
(이미지 출처: National Geographic)

그러던 중 2개월 전에 누군가로부터 이런 이야기를 들었다. "MySQL로 1분 넘던 것이 있었는데, Elasticsearch로 Aggregation했더니 1.5초 만에 결과가 나오더라" 이런 이야기를 듣고 "오~ 신기하다" 정도로 넘어갔었다. 

Hadoop은 Bigdata라는 용어가 있기도 전인 2008년부터 2년 정도 써 봤는데, RDBMS로는 죽어도 안 돌아가는 것이 (비록 응답시간은 느렸지만) 결과가 출력되는 것에 엄청 신기했던 적이 있다. 어쨌든 2010년부터는 Batch 작업보다는 OLTP성 작업만 다루다보니 Bigdata와 약 5년 정도 멀어져가고 있었는데 (Bigdata는 점점 인기가 끌고 있었는데 2008년에 2년간 Hadoop을 써 봤다는 자만감 때문에 난 내가 Bigdata에 강하다고 생각하고 있었지만, 이미 시대에 뒤떨어진 사람이 되어 있었다)

그러던 중 드디어 나도 간만에 나름 대용량의 Data를 분석할 일이 생겼다. 분석 Platform을 선정해야 했는데, MySQL로는 답이 안 나올 것을 알고 있었고, Hadoop 왠지 싫고... 그러던 중 2개월 전 들었던 이야기가 생각나서 Elasticsearch를 설치해보고, Logstash를 공부해서 자료도 입력해보고 Kibana로 시각화도 해보려했는데 이건 어려워서 다른 팀원에게 넘기고 (난 역시 CUI에만 강한 듯...)

DSL은 초기엔 메뉴얼만 봐서는 무슨 소린지 알 수가 없어서 남들이 만든 snippet에서 field 명만 바꿔서 실행해 봤다. (DSL은 지금도 영 익숙치가 않다) Aggregation Query를 처음 돌려본 결과는? "이거 무슨 마술이지?"라는 충격과 공포였다. "이거 세상이 이렇게 변하고 발전할 동안 난 뭘한거지?"

Dolphin v.s ELK
---------------

학부 시절 DB를 가르쳐주신 교수님이 Benchmark 쪽으로 유명하신 분이었다(고 들었다). 20년전에 들은 수업이라 잘 기억이 잘 나진 않지만, 대충 요약하면 "Benchmark 결과는 굉장히 민감한 자료이다. 오라클 라이센스에는 '벤치마크 용도로 사용할 수 없음'이라고 쓰여있다. (정말 그런가?)" 대충 이런 이야기인데 벤치마크 결과 공개는 항상 조심스럽다.

MySQL과 Elasticsearch의 성능을 비교해봤는데 Elasticsearch의 완승이다. 논란이 있을 수 있어서 상세한 테스트 환경 및 결과를 공개하긴 어렵고, 대략적인 비교 항목만 말해보자면,

1. 자료 입력 시간
1. Data Size
1. Query 수행 시간 - `GROUP BY` Query
    - 컬럼을 1개에서 5개까지 늘려가면서 비교

MySQL과 Elasticsearch 둘다 내가 아는 선에서 튜닝한 상태였는데, 입력 속도도 Elasticsearch가 빠르고, Data size도 Elasticsearch의 것이 더 적은데도 Query 수행 시간은 수십배~100배 정도 빨랐다.

### 주의 사항

- 여기서 말한 "Elasticsearch의 완승"이라는 표현은 Data 분석 측면이다.
- Test 결과는 각자의 Data 특성 및 시스템 환경에 따라 다르므로 본인의 실험 결과는 신뢰하지 말것.

