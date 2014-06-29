---
layout: post
title: "MySQL용 TPC-H Data 생성하기"
date: 2014-03-05 
categories: mysql
---

영어 Multiplicity는 한국어로 '다양성'이라는 뜻이다. (첫 장만 읽고 진도가 안 나가고 있지만...) 최근 읽고 있는 "Modern C++ Design"에서는 Multiplicity에 대해서 다음과 같이 언급하고 있다.

> Software engineering, maybe more than any other engineering discipline, exhibits a rich multiplicity: You can do the same thing in many correct ways, and there are infinite nuances between right and wrong.
> 중략
> The most important difference between an expert software architect and a beginner is the knowledge of what works and what doesn't . For any given architectural problem, there are many competing ways of solving it. However they scale differently and have distinct sets of advantages and disadvantages.
 
즉, 동일한 문제를 푸는 방법은 여러 가지가 있을 수 있지만, 해법들이 풀 수 있는 규모는 다를 수 있다. 소규모 Data에서는 아무 문제 없이 수행되는 질의이지만 Big Data에서는 시간이 오래 걸릴 수 있다. 대부분의 SQL은 탐색 시간이 오래 걸리는 것이 문제인데 효율적인 SQL의 시간 복잡도 (Time Complexity)는 n*Log(n)인 반면 비효율적인 SQL의 시간 복잡도는 n2보다 크기 때문에 DB 크기가 10배증가하더라도 속도는 10보다 훨씬 느려질 수 있다.

Stackoverflow에는 효율적인 SQL을 물어보는 질문이 많다. 이 경우 질문자가 사용하는 실제 데이터를 확인하기 어렵기 때문에 몇 가지 패턴을 이용하여 답변을 해 주는 편인데, (예를 들어 'NOT IN 대신LEFT JOIN을 써라' 등) 실제 얼마나 빨라졌는지 구체적인 수치를 알 수 없으니 답변 후에 얻는 쾌감(?)이 적을 뿐 더러, '당신의 SQL이 정말 좋은 것이냐'라는 논란이 생길 때 대응하기가 어렵다. 따라서 남들도 동의할 수 있으면서 적당히 크기도 큰 Data가 필요한데 이는 역시 공인된 벤치마크, 그 중에서도 TPC-H가 좋다고 생각되었다.

서론이 길어졌는데, dbgen이라는 프로그램을 이용하여 TPC-H 용 데이터를 MySQL에 입력하는 방법에 대해 설명하고자 한다.

## 1. dbgen 소스 코드 다운로드

소스에 대한 라이선스 안내가 없어서 소스 코드를 필자의 블로그에 올려 놓지는 못했다. 우선 다음과 같이 dbgen 소스 코드를 구하도록 하자.

    1. http://www.tpc.org/tpch/ 방문
    2. zip 파일 클릭 및 다운로드 (우측 상단)
    3. $ unzip tpch_version.zip
    4. $ cd tpch_version/dbgen

2014년 2월 현재 dbgen 소스 코드 URL은 http://www.tpc.org/tpch/spec/tpch_2_16_1.zip 이지만 언제 버전이 올라갈진 모른다.

## 2. dbgen 컴파일
소스 코드를 다운로드하고 압축을 풀었으면 다음과 같이 컴파일 하도록 하자.

    $ cp makefile.suite Makefile
     
    $ vi Makefile
        // Makefile의 내용을 다음과 같이 수정하자
        CC      = gcc
        DATABASE= SQLSERVER // currently dbgen does not support MySQL
        MACHINE = LINUX
        WORKLOAD = TPCH
     
    $ make dbgen

필자의 경우 CentOS 5.3, gcc 4.1.2에서 컴파일하였는데 아무 문제없이 컴파일 되었다.

## 3. TPC-H용 텍스트 파일 생성
dbgen은 TPC-H용 텍스트 파일을 생성해 주는 프로그램이다. 이 텍스트 파일을 MySQL의 LOAD DATA 구문을 이용하여 테이블에 로딩할 것이다. 우선 다음과 같이 텍스트 파일을 생성해 보자.

    $ time ./dbgen
    TPC-H Population Generator (Version 2.16.0)
    Copyright Transaction Processing Performance Council 1994 - 2010
     
    real    0m39.142s
    user    0m38.165s
    sys     0m0.903s
     
    $ wc -l *.tbl
        150000 customer.tbl
       6001215 lineitem.tbl
            25 nation.tbl
       1500000 orders.tbl
        800000 partsupp.tbl
        200000 part.tbl
             5 region.tbl
         10000 supplier.tbl
       8661245 total

아무런 옵션도 없이 ./dbgen을 실행하면 약 860만건의 레코드를 생성한다. TPC-H는 총 8개의 테이블 규정하는데 customer.tbl은 고객 테이블용 데이터이고, orders.tbl는 주문 테이블용 데이터이다. 즉, 15만명의 고객, 150만건의 주문을 생성했다고 보면 된다.

dbgen의 -s 옵션은 scale을 의미하는데 기본 값은 1이다. 이 값을 2로 지정하면 1일 때보다 2배 더 많은 데이터를 생성한다.

## 4. tbl 내용 확인

dbgen은 '|'를 구분자로 사용한 텍스트 파일을 생성한다. 예를 들어 orders.tbl의 내용은 다음과 같다.

    1|73801|O|181503.69|1996-01-02|5-LOW|Clerk#000001902|0|nstructions sleep furiously among |
    2|156004|O|49967.96|1996-12-01|1-URGENT|Clerk#000001759|0| foxes. pending accounts at the pending, silent asymptot|
    3|246628|F|227024.64|1993-10-14|5-LOW|Clerk#000001909|0|sly final accounts boost. carefully regular ideas cajole carefully. depos|
    4|273553|O|36018.68|1995-10-11|5-LOW|Clerk#000000247|0|sits. slyly regular warthogs cajole. regular, regular theodolites acro|
    5|88970|F|112288.43|1994-07-30|5-LOW|Clerk#000001850|0|quickly. bold deposits sleep slyly. packages use slyly|
    6|111245|F|43835.61|1992-02-21|4-NOT SPECIFIED|Clerk#000000116|0|ggle. special, final requests are against the furiously specia|
    7|78269|O|256188.35|1996-01-10|2-HIGH|Clerk#000000940|0|ly special requests |
    32|260114|O|192549.07|1995-07-16|2-HIGH|Clerk#000001232|0|ise blithely bold, regular requests. quickly unusual dep|
    33|133916|F|161183.83|1993-10-27|3-MEDIUM|Clerk#000000818|0|uriously. furiously final request|
    34|122002|O|72940.87|1998-07-21|3-MEDIUM|Clerk#000000446|0|ly final packages. fluffily final deposits wake blithely ideas. spe|
     
## 5.  테이블 생성

dss.ddl에 다음과 같은 CREATE TABLE 구문이 저장되어 있으니 실행해 주자.

    CREATE TABLE nation  ( n_nationkey  INTEGER NOT NULL,
                                n_name       CHAR(25) NOT NULL,
                                n_regionkey  INTEGER NOT NULL,
                                n_comment    VARCHAR(152));
     
    CREATE TABLE region  ( r_regionkey  INTEGER NOT NULL,
                                r_name       CHAR(25) NOT NULL,
                                r_comment    VARCHAR(152));
     
    CREATE TABLE part  ( p_partkey     INTEGER NOT NULL,
                              p_name        VARCHAR(55) NOT NULL,
                              p_mfgr        CHAR(25) NOT NULL,
                              p_brand       CHAR(10) NOT NULL,
                              p_type        VARCHAR(25) NOT NULL,
                              p_size        INTEGER NOT NULL,
                              p_container   CHAR(10) NOT NULL,
                              p_retailprice DECIMAL(15,2) NOT NULL,
                              p_comment     VARCHAR(23) NOT NULL );
     
    CREATE TABLE supplier ( s_suppkey     INTEGER NOT NULL,
                                 s_name        CHAR(25) NOT NULL,
                                 s_address     VARCHAR(40) NOT NULL,
                                 s_nationkey   INTEGER NOT NULL,
                                 s_phone       CHAR(15) NOT NULL,
                                 s_acctbal     DECIMAL(15,2) NOT NULL,
                                 s_comment     VARCHAR(101) NOT NULL);
     
    CREATE TABLE partsupp ( ps_partkey     INTEGER NOT NULL,
                                 ps_suppkey     INTEGER NOT NULL,
                                 ps_availqty    INTEGER NOT NULL,
                                 ps_supplycost  DECIMAL(15,2)  NOT NULL,
                                 ps_comment     VARCHAR(199) NOT NULL );
     
    CREATE TABLE customer ( c_custkey     INTEGER NOT NULL,
                                 c_name        VARCHAR(25) NOT NULL,
                                 c_address     VARCHAR(40) NOT NULL,
                                 c_nationkey   INTEGER NOT NULL,
                                 c_phone       CHAR(15) NOT NULL,
                                 c_acctbal     DECIMAL(15,2)   NOT NULL,
                                 c_mktsegment  CHAR(10) NOT NULL,
                                 c_comment     VARCHAR(117) NOT NULL);
     
    CREATE TABLE orders  ( o_orderkey       INTEGER NOT NULL,
                               o_custkey        INTEGER NOT NULL,
                               o_orderstatus    CHAR(1) NOT NULL,
                               o_totalprice     DECIMAL(15,2) NOT NULL,
                               o_orderDATE      DATE NOT NULL,
                               o_orderpriority  CHAR(15) NOT NULL,
                               o_clerk          CHAR(15) NOT NULL,
                               o_shippriority   INTEGER NOT NULL,
                               o_comment        VARCHAR(79) NOT NULL);
     
    CREATE TABLE lineitem ( l_orderkey    INTEGER NOT NULL,
                                 l_partkey     INTEGER NOT NULL,
                                 l_suppkey     INTEGER NOT NULL,
                                 l_linenumber  INTEGER NOT NULL,
                                 l_quantity    DECIMAL(15,2) NOT NULL,
                                 l_extendedprice  DECIMAL(15,2) NOT NULL,
                                 l_discount    DECIMAL(15,2) NOT NULL,
                                 l_tax         DECIMAL(15,2) NOT NULL,
                                 l_returnflag  CHAR(1) NOT NULL,
                                 l_linestatus  CHAR(1) NOT NULL,
                                 l_shipDATE    DATE NOT NULL,
                                 l_commitDATE  DATE NOT NULL,
                                 l_receiptDATE DATE NOT NULL,
                                 l_shipinstruct CHAR(25) NOT NULL,
                                 l_shipmode     CHAR(10) NOT NULL,
                                 l_comment      VARCHAR(44) NOT NULL);
## 6.INDEX 생성

INDEX는 dss.ri 파일에 저장되어 있다. dss.ri의 INDEX가 MySQL용이 아니다 보니 MySQL에서 실행시키면 FOREIGN KEY 관련 오류가 발생한다. CREATE TABLE 구문은 dss.ddl의 내용을 그대로실행해도 되지만 INDEX는 다음 표에 있는 내용을 실행시키도록 하자.

    ALTER TABLE region ADD PRIMARY KEY (r_regionkey);
     
    -- for table nation
    ALTER TABLE nation ADD PRIMARY KEY (n_nationkey);
     
    ALTER TABLE nation ADD INDEX (n_regionkey),
        ADD FOREIGN KEY nation_fk1 (n_regionkey) REFERENCES region(r_regionkey);
     
    -- for table part
    ALTER TABLE part ADD PRIMARY KEY (p_partkey);
     
    -- for table supplier
    ALTER TABLE supplier ADD PRIMARY KEY (s_suppkey);
     
    ALTER TABLE supplier ADD INDEX (s_nationkey),
        ADD FOREIGN KEY supplier_fk1 (s_nationkey) REFERENCES nation(n_nationkey);
     
    -- for table partsupp
    ALTER TABLE partsupp ADD PRIMARY KEY (ps_partkey, ps_suppkey);
     
    -- for table customer
    ALTER TABLE customer ADD PRIMARY KEY (c_custkey);
     
    ALTER TABLE customer ADD INDEX (c_nationkey),
        ADD FOREIGN KEY customer_fk1 (c_nationkey) REFERENCES nation(n_nationkey);
     
    -- for table lineitem
    ALTER TABLE lineitem ADD PRIMARY KEY (l_orderkey, l_linenumber);
     
    -- for table orders
    ALTER TABLE orders ADD PRIMARY KEY (o_orderkey);
     
    -- for table partsupp
    ALTER TABLE partsupp ADD INDEX (ps_suppkey),
        ADD FOREIGN KEY partsupp_fk1 (ps_suppkey) REFERENCES supplier(s_suppkey);
     
    ALTER TABLE partsupp ADD INDEX (ps_partkey),
        ADD FOREIGN KEY partsupp_fk2 (ps_partkey) REFERENCES part(p_partkey);
     
    -- for table orders
    ALTER TABLE orders ADD INDEX(o_custkey),
        ADD FOREIGN KEY orders_fk1 (o_custkey) REFERENCES customer(c_custkey);
     
    -- for table lineitem
    ALTER TABLE lineitem ADD INDEX(l_orderkey),
        ADD FOREIGN KEY lineitem_fk1 (l_orderkey)  REFERENCES orders(o_orderkey);
     
    ALTER TABLE lineitem ADD INDEX(l_partkey, l_suppkey),
        ADD FOREIGN KEY lineitem_fk2 (l_partkey,l_suppkey) REFERENCES partsupp (ps_partkey, ps_suppkey);

입수 속도를 빠르게 하려면 1) CREATE TABLE, 2) LOAD DATA, 3) CREATE INDEX 순으로 작업하는 것이 좋다.

## 7.  LOAD DATA

마지막 작업이다. LOAD DATA 구문을 이용하여 앞에서 생성된 테이블에 dbgen이 만든 tbl 파일을 로딩하면 된다. 다음과 같은 LOAD DATA 구문을 사용하면 된다.

    SET FOREIGN_KEY_CHECKS = 0;
     
    LOAD DATA LOCAL INFILE './nation.tbl' INTO TABLE nation FIELDS TERMINATED BY '|';
    LOAD DATA LOCAL INFILE './region.tbl' INTO TABLE region FIELDS TERMINATED BY '|';
    LOAD DATA LOCAL INFILE './part.tbl' INTO TABLE part FIELDS TERMINATED BY '|';
    LOAD DATA LOCAL INFILE './supplier.tbl' INTO TABLE supplier FIELDS TERMINATED BY '|';
    LOAD DATA LOCAL INFILE './partsupp.tbl' INTO TABLE partsupp FIELDS TERMINATED BY '|';
    LOAD DATA LOCAL INFILE './customer.tbl' INTO TABLE customer FIELDS TERMINATED BY '|';
    LOAD DATA LOCAL INFILE './orders.tbl' INTO TABLE orders FIELDS TERMINATED BY '|';
    LOAD DATA LOCAL INFILE './lineitem.tbl' INTO TABLE lineitem FIELDS TERMINATED BY '|';
     
    SET FOREIGN_KEY_CHECKS = 1;
 
