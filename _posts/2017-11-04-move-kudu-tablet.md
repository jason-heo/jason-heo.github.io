---
layout: post
title: "Kudu Tablet 위치 옮기기"
categories: "bigdata"
---

### 들어가며

Data를 분산하여 저장하는 시스템들마다 저마다 특징이 있는데, 서버 확장 시 Data를 자동으로 분산시켜주느냐, 메뉴얼하게 분산시켜야하느냐도 운영에 중요한 점검 사항이다.

Elasticsearch 같은 경우, 새로운 Data Node가 추가되면 자동으로 Shard들을 분산시켜준다. (복제 시, Network Throttling까지 조절이 가능해서 운영 중인 클러스터에 Data Node 추가가 수월하다)

Kafka의 경우 서버가 추가되어도 topic의 partition들은 자동으로 이동되지 않는다. 사람이 수동으로 신규 서버로 옮겨야 하는데, 쉽게 사용할 수 있는 tool을 제공하므로 메뉴얼한 운영은 큰 문제가 안 된다 (Network Throttling이 안 되서 옮기는 동안 서비스가 지연된다는 단점이 있지만 말이다.)

### kudu Tablet의 재분배 방법

하지만 Kudu는 서버 추가 시, tablet이 추가된 서버로 자동으로 옮겨지지 않는다. 또한 쉽에 옮기는 Tool도 제공되지 않아서 사람이 해야할 것이 좀 많다. (물론 이건 서버 추가 시에 대한 이야기이며, tserver down시에는 down된 tserver에 존재하는 tablet들은 나머지 tserver로 자동으로 분배된다)

다음과 같이 3대의 tserver가 있는 상황에서,

- `tserver_a`
- `tserver_b`
- `tserver_c`

`tserver_d`가 추가되면서 총 4개의 tserver가 존재한다고 가정하자. `tserver_c` 에 존재하던, `tablet_a`를 `tserver_d`로 옮기는 명령은 아래와 같다

```
$ kudu tablet change-config add-replica <masters> <tablet_a> <tserver_d> VOTER
$ kudu tablet change-conf remove-replaca <masters> <tablet_a> <tserver_c>
```

잘 보면 알겠지만, `move` 명령이라기보단 `add -> remove` 명령을 조합하여 `move` 효과를 낸 것이라 볼 수 있다.

tserver 및 tablet은 hash 값을 사용해야 하므로 아래와 같이 명령이 좀 많이 복잡해보이는 것은 사실이다.

```
$ kudu tablet \
    change-config \
    add-replica \
    master1.com,master2.com,master3.com \
    4c86403768ad4247a5a9d25478ba9bc5 \
    4844aa86b8674bc4a31460498f4cc5ac \
    VOTER
$ kudu tablet \
    change-config \
    remove-replica \
    master1.com,master2.com,master3.com \
    4c86403768ad4247a5a9d25478ba9bc5 \
    b3e92a30fbc3470c8211040b81e62272
```

tablet 1개를 옮기는 명령이 위와 같고, 실제로는 table 및 tablet 개수가 많기 때문에 위 명령을 수동으로 실행하기엔 쉬운 일이 아니라 생각된다.

본인의 경우는 서버 추가 시, (kafka의 partition 재분배 스크립트 비슷하게) 어느 정도 자동화된 방식으로 위 명령을 생성해주는 간단한 script를 개발해서 사용 중이다.

{% include spark-reco.md %}
