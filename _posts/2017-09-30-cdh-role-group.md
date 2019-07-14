---
layout: post
title: "Cloudera CDH에서 Role Group"
categories: "cdh"
---

CDH를 1년 넘게 사용하면서 그 섬세한 기능에 감탄할 때가 한 두번이 아니다. 최근 느낀 기능으로서는 [Role Groups](2017-09-30-druid-interval-retention.md) 기능이 있다.

서비스를 운영하는 기간이 길어짐에 따라 장비 Spec도 다양하게 되었다. 동일 Spec의 장비를 CDH에 넣어서 사용하는 게 운영상 편할 줄 알았는데, 꼭 그렇지만은 않았다. CDH에서 Role Group 기능을 제공하기 때문이다.

예를 들어 Ram 48GB까리와 128GB짜리 장비를 kudu-tserver에 할당했다고 하자. `block_cache_capacity_mb`를 두 서버에 서로 다르게 설정하고 싶은데, 이때 Role Group을 사용하면 된다.

Role Group별로 각종 설정 값을 다르게 줄 수도 있고, 값을 변경한 후에 적용을 위해서 Restart할 때도 값이 변경된 Role Group만 자동 Restart된다.
