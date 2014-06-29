---
layout: post
title: "InnoDB filesize를 줄이는 방법. MySQL Barracuda format"
date: 2014-03-05 
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20126169/use-of-primary-key-lowers-innodb-file-size/20134634

질문자의 원래 질문에 대한 대답은 아니지만, 질문에 도움이 될만한 것은 팁으로서 소개하고자 한다. 첫 번째 팁은 InnoDB의 file size를 줄이는 방법이다. 이 옵션은 아직 현업에서 많이 사용되는 것 같지는 않는데, InnoDB의 파일 크기를 손 쉽게 줄일 수 있는 좋은 방법이다.

{% include adsense-content.md %}

DB 설계도 잘되어 있고, 필요한 INDEX도 잘 생성되었으며, Optimizer 또한 실행 계획을 잘 생성하는 경우 남은 성능 최적화 작업 중 하나는 DISK I/O를 줄이는 방법이다.

MySQL에서는 InnoDB를 사용하는 경우 Barrcuda Format을 사용하여 DB의 크기를 50% 정도는 손 쉽게 줄일 수 있다. MySQL 5.1의 특정 버전 이하에서는 InnoDB Plugin을 설치해야 하는 것으로 기억하는데 MySQL 5.5에서는 기본으로 Barracuda Format을 지원한다. 우선 현재 사용 중인 InnoDB format을 확인해 보자. innodb_file_format 변수를 확인해 보면 된다.

    mysql> show variables like 'innodb_file_format';
    +--------------------+-----------+
    | Variable_name      | Value     |
    +--------------------+-----------+
    | innodb_file_format | Barracuda |
    +--------------------+-----------+

기본 값은 Antelope이므로 거의 대부분의 독자는 Antelope 값일 것이라 생각된다. my.cnf에 다음과 같은 내용을 추가한 뒤 MySQL을 restart하면 Barracuda로 변경되어 있을 것이며 Barracuda로 변경되지 않을 경우 MySQL을 업그레이드하기 바란다.

    [mysqld]
    ...
    innodb_file_format = barracuda
    ...

이제 테이블을 생성할 때 다음과 같이 KEY_BLOCK_SIZE을 지정할 수 있는데, 기본 값은 16이며 이를 8로 지정하는 경우 약 50% 압축이 된다. InnoDB의 파일 크기를 약 50% 정도 줄일 수 있다.

    CREATE TABLE test(
    .....
    ) ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8;

KEY_BLOCK_SIZE를 4로 줄이면 압축률이 약 75%이기 때문에 파일 크기를 더 줄일 수 있지만 이 경우 INSERT, UPDATE, DELETE의 속도가 크게 줄어든다. 필자가 테스트한 결과 KEY_BLOCK_SIZE를8로 설정하는 것이 용량도 50% 줄일 수 있으면서 WRITE 성능도 16일 때와 거의 비슷하게 나왔었다.

단, 이는 Disk에 저장되는 InnoDB의 Data와 Index 파일만 줄어들게 된다. Data와 Index가 InnoDB Buffer Pool로 로딩될 때는 압축이 풀려서 저장된다. 압축하고 푸는 비용이 있지만 `KEY_BLOCK_SIZE`를 8로 설정(즉, 50% 압축)시에는 압축 전과 큰 성능 차이가 발생하지 않았다.
