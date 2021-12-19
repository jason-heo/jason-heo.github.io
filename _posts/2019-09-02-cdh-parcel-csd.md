---
layout: post
title: "Cloudera CDH에 custom 패키지 올리기 (parcel/csd 제작)"
categories: "bigdata"
---

예전부터 프로그램들을 from scratch로 설치하고 운영하는 것들이 재미이었는데, CDH나 Ambari라는 것을 처음 들었을 때도 이런 것들의 필요성을 잘 체감할 수 없었다.  그러다 본격적으로 Data Engineering 업무를 하게 되면서 CDH를 도입하여 사용 중인데, 사용할 수록 Cloudera에게 감사함을 느끼며 살고 있다.

Druid를 도입하게 되면서 운영 비용을 줄이기 위해 CDH 내에서 Druid를 사용하는 방법을 찾아봤는데 Cloudera에서 공식 지원하지 않는 플랫폼이더라도 직접 parcel/CSD를 구현하는 경우 CDH에서 사용할 수 있다는 걸 알았다.

이때의 장점은 Druid 배포, Start/Stop, 설정 변경, 의존성 관리 등을 쉽게할 수 있다는 점이다. (더불어 우리는 CDH Rest API와 연동하여 Rolling Restart Script도 제작하여 사용 중이다)

[CSD Primer](https://github.com/cloudera/cm_ext/wiki/CSD-Primer)라는 문서에서 echo 서버를 CDH로 배포할 수 있게 하는 간단한 예제가 있다.

CSD는 Custom Service Descriptor의 약자로서 json으로 다음과 같은 내용을 기술한다

- role 정보
- role별 각종 configuration
- role별 start/stop script

CDH의 UI를 통해서 설정을 쉽게 관리할 수 있는데 이런 내용들이 csd로 기술되어 있다.

CSD Primer는 기초적인 내용만 담고 있고 CSD에 대한 전체 내용은 [CSD Language Reference](https://github.com/cloudera/cm_ext/wiki/Service-Descriptor-Language-Reference)에서 볼 수 있는데 필요할 때마다 검색해서 찾아보면 된다.

CSD는 각종 설정을 정의하는 반면 Parcel은 패키지 그 자체이다. 실행 가능한 binary와 운영 스크립트를 tgz로 압축한 뒤 확장자만 `.parcel`로 변경하면 된다.

Parcel/CSD를 제작한 뒤 CDH에 올려두면, 앞으로 신규 노드가 추가될 때 자동으로 Parcel을 배포한다. 역할을 추가할 때는 CSD에 정의된 설정들을 자동으로 해당 서버에 배포하고 Start/Stop script에 의해 역할이 시작/종료된다.

개념 자체는 간단하지만 막상 Parcel/CSD를 만들어보려면 쉽지가 않다. 아래 참고 자료를 보면 많은 도움이 된다.

### 참고 자료

- https://github.com/cloudera/cm_csds
    - CDH에서 제공되는 각종 패키지들의 csd를 볼 수 있다
    - 다만 완전 최신 버전은 아닌 듯 하다
    - 단순 참고용으로는 사용 가능
- https://github.com/knoguchi/cm-druid
    - 외부 개발자가 만든 Druid용 Parcel/CSD이다
    - 위의://github.com/cloudera/cm_csds 에서는 CSD를 참조하기 좋고,
    - 본 링크에서는 Parcel/CSD의 전체적인 패키징 방법, 배포 방법을 확인하기 좋다
    - CDH 5.8과 Druid 0.10으로 테스트를 해 봤는데 작동은 된다
    - 그런데 parcel 제작 과정이 좀 번거롭고, 설정도 부족하여 실 서비스를 위해선 직접 수정해야할 내용이 많다

{% include spark-reco.md %}
