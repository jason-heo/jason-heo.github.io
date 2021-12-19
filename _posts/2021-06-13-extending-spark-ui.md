---
layout: post
title: "Spark UI 확장하기"
categories: "bigdata"
---

### 들어가며

어느 날 갑자기 '수행 중인 Spark App의 input/output에 대한 metadata를 spark UI에서 볼 수 있으면 좋겠다'는 생각이 들었다. 코드 주석도 그렇고 시스템 문서화도 그렇고, 시간을 내서 아무리 잘 작성을 한다하더라도 나중에 참조를 잘 하지 않는다. 또한  시간이 지나면 금방 out-of-date가 되서 최신성을 유지하기도 힘들고 투입한 시간 대비 얻는 것도 적다.

이는 Data Pipeline 문서화도 마찬가지인데, 그동안 어떻게 문서화를 하는 것이 시간도 절약되고 참조하는데도 쉬운지 고민도 하고 시도를 해봤는데도 좋은 방법을 찾지 못하였다. 그나마 [Apache Atlas](https://atlas.apache.org/)를 이용해서 pipline의 lineage를 표현하는 게 좋긴 했는데 이건 lineage를 생성/수정하는데 비용이 너무 컸다.

그러던 중 Spark App에 대한 정보를 Spark UI에 남겨볼 수 있다면 별도의 문서화를 많이 줄일 수 있고, lineage를 수동으로 그리는 수고도 줄일 수 있을 듯 하였다 (여러 개의 Spark app간의 pipeline 전체를 lineage를 그리는 것은 Spark UI 확장만으로는 어렵다. 어떻게 구현할지는 머리 속에 있기 한데, 이건 우선 1개 Spark App에 집중한 뒤 향후 구현 시도하려한다)

### Spark UI 확장하기

Spark UI에 내가 원하는 정보를 출력하기 위한 방법을 구글링해봤다. 그랬더니 역시 다른 개발자가 작성한 [How to extend Spark UI](http://blog.kprajapati.com/spark-ui-extension/)라는 글이 검색되었다. 2017년에 작성했던 글인데 이런 걸 보면 나의 부족함을 많이 느낀다.

블로그 작성자도 나와 같은 고민이 있었던 것인지, Spark UI에 Dataframe의 생성 위치와 schema를 출력하는 예를 만들었다.

[작성자가 github에 올린 코드](https://github.com/skp33/spark-ui-extension)를 실행 후 Spark UI에 접속하면 아래와 같은 UI를 볼 수 있다.

<img src="https://i.imgur.com/ol5nIF1.png">

Spark UI 확장은 Spark 외부 API가 아닌 private class, method를 사용하는데 이런 경우 Spark Version에 의존적인 경우가 많다. 예를 들어 Spark 2.2 기준으로 만든 Spark 확장이 Saprk 3.0에서는 작동되지 않을 경우도 있다. 하지만 다행이도 UI 관련 함수는 Spark 2.2부터 Spark 3.1까지 테스트해봤을 때 동일 코드가 잘 작동되었다. (특정 버전에 의존적인 경우 새로운 Spark 버전이 나와도 upgrade를 하기 어려울 수 있다)

원글 작성자의 소스코드는 maven & Spark 2.2 기반이라서, sbt & Spark 3.0 기반으로 코드를 작성해서 github에 올려두었다. [repository 바로 가기](https://github.com/jason-heo/spark-ui-extention)

{% include spark-reco.md %}
