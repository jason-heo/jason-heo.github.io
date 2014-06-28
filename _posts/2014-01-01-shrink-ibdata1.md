---
layout: post
title: "ibdata1 내용 확인하기 - innodb_file_per_table을 사용함에도 ibdata1의 파일 크기가 증가하는 문제"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20350713/find-innodb-undo-size/20351277

## 질문

`innodb_file_per_table` 옵션이 활성화되어 있다. 모든 InnoDB 테이블은 자기 자신의 ibd 파일에 저장되는데 이상하게도 InnoDB Global Tablespace인 ibdata1 파일이 크게 증가되어 있다. 예상하기로는 트랜잭션을 위한 UNDO 로그가 ibdata1에 쌓인 듯 한데 정확히 아는 사람 있는가?

{% include adsense-content.md %}

## 답변

`innodb_file_per_table`을 사용하면 InnoDB 테이블의 데이터와 인덱스는 table_name.ibd 파일에 저장된다. 따라서 InnoDB Global Tablespace인 ibdata1의 파일 크기는 일반적으로 증가하지 않는다. 그런데 MySQL을 운영하다 보면 어느 순간 ibdata1의 파일 크기가 커지는 경우가 있다. 필자 및 필자의 지인들도 위 현상을 겪은 후 ibdata1 파일에 무엇이 저장된 것인지, 그 크기를 줄일 수는 없는지 확인해 보았으나 본 글을 작성 중인 2014년 2월 현재, ibdata1 파일의 파일 크기를 줄일 수는 없는 것으로 확인되었다.

질문자는 ibdata1에 UNDO 로그가 저장된 것으로 의심하고 있다. 필자가 검색해본 결과, MySQL Performance Blog에서 우리가 궁금해 하는 내용을 찾을 수 있었다. 영문 자료이지만 관심 있는 독자는 꼭 읽어 보기 바란다. ibdata1에 저장되는 내용은 무엇인지, 현재 ibdata1에 저장된 내용와 각각의 크기는 얼마인지, ibdata1 파일의 크기를 줄이는 방법은 없는지에 대해서 설명하고 있다.

본 글에서는 ibdata1에 저장된 내용을 확인하는 방법만 요약하여 설명하겠다.

### 수정된 innochecksum 사용하기

innochecksum은 MySQL에 기본으로 포함된 프로그램이다. 이를 외부 개발자가 수정하여 ibdata1의 내용을 출력하도록 한 뒤 공개하였다. 소스 코드는 http://bugs.mysql.com/file.php?id=16076&bug_id=57611 에서 구할 수 있으며 컴파일도 쉽게 할 수 있다.

    $ wget 'http://bugs.mysql.com/file.php?id=16076&bug_id=57611'
    $ mv innochecksum.c.orig innochecksum.c
    $ gcc -o innochecksum  innochecksum.c

앞의 방법처럼 wget으로 소스 코드를 저장하든 브라우저에서 URL을 연 뒤에 소스 코드를 직접 저장하든 상관없다. 파일도 1개만 있으면 되고 CentOS 5.4의 경우 아무런 오류 없이 컴파일도 잘 되었다.

사용 방법은 다음과 같이 ibdata1의 경로만 지정해 주면 된다.

    $ ./innochecksum  ~/db/mysql/data/ibdata1
    0       bad checksum
    657     FIL_PAGE_INDEX
    114382  FIL_PAGE_UNDO_LOG
    7       FIL_PAGE_INODE
    213     FIL_PAGE_IBUF_FREE_LIST
    416     FIL_PAGE_TYPE_ALLOCATED
    8       FIL_PAGE_IBUF_BITMAP
    146     FIL_PAGE_TYPE_SYS
    2       FIL_PAGE_TYPE_TRX_SYS
    2       FIL_PAGE_TYPE_FSP_HDR
    7       FIL_PAGE_TYPE_XDES
    0       FIL_PAGE_TYPE_BLOB
    0       FIL_PAGE_TYPE_ZBLOB
    0       other
    411     max index_id

왼쪽에 있는 숫자를 모두 더하면 ibdata1의 총 크기이다. 앞 예의 경우 전체 116,246 중 UNDO 로그의 크기가 114,382이므로 전체 ibdata1 중 UNDO 로그가 약 98%를 차지하고 있다. 단위는 16KB로예상되나 정확한지는 확인하지 못하였다.

### innodb_ruby를 이용하기

앞에 설명된 MySQL Performance Blog를 읽어보면 innodb_ruby를 이용할 수도 있다고 한다. 소스 코드는 https://github.com/jeremycole/innodb_ruby에서 구할 수 있다. 관심 있는 독자는 직접설치해서 사용해보도록 하자.
