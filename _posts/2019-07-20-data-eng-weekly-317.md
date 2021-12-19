---
layout: post
title: "Data Engineering weekly news issue #317 (2019.07.13 발행)"
categories: "bigdata"
---

{% include data-eng-weekly.md %}

## 이슈 #317

### Kubernetes에서 Database를 실행할 것인가 말 것인가

https://cloud.google.com/blog/products/databases/to-run-or-not-to-run-a-database-on-kubernetes-what-to-consider

MSA (Microservice Architecture)는 Kubernetes와 궁합이 딱 맞고 많은 곳에서 Kubernetes를 도입하고있다. 하지만 Database를 Kubernetes에서 돌리려면 아직도 고려할 것들이 많다.

GPC에서는 아래와 같은 방식으로 DB를 사용할 수 있는데, 

1. Fully managed databases 사용: Cloud Spanner, Cloud Bigtable 등
1. Do-it-yourself on a VM: 고객이 직접 관리
1. Kubernetes에서 실행

이 중 Kubernetes에서 DB를 실행할 때 고려해야할 것들에 대한 블로그 포스트이다.


### Introducing Dagster

https://medium.com/@schrockn/introducing-dagster-dbd28442b2b7

Dagster라는 worklfow engine 이 새로 나왔나보다. GraphQL의 co-creator가 만들었다고 한다.

아직 Workflow 관리 툴을 사용하지 않고 있다면 뭐가 되었든 도입해보도록 하자. 관리해야하는 작업들이 많아질 수록 큰 도움이 된다.

### OpenTSDB Metric HBase Region Finder

https://engineering.salesforce.com/opentsdb-metric-hbase-region-finder-cf3d55c79565

OpenTSDB의 HBase 구조 internal에 대해서 공부할 수 있는 포스팅 같다. 난 OpenTSDB 및 HBase에 관심이 없어서 패스.

### The Best Open-Source Log Management Tools for Log Analysis, Monitoring & More

https://sematext.com/blog/best-log-management-tools/

로그 수집, 로그 분석, 시각화, 알람 등에 관련된 오픈 소스 툴에 대한 포스팅.

혹시라도 로그 분석 툴을 지금막 도입하려고 조사 중인 분이 계시다면 일단 Elastic Stack부터 시작하는 것이 좋을 듯 하다. ES도 만능은 아니라서  한계가 있지만, 이런 것들은 겪어보면서 경험해보면서 여러 공부도하고 다른 대안거리가 있는지 찾아보면서 많은 도움이 될 듯.

{% include spark-reco.md %}
