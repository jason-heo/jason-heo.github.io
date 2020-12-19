---
layout: post
title: "Spark 기능 확장하기 (How to Extend Apache Spark with Customized Optimizations)"
categories: "bigdata"
---

## 목차

- [1. 개요](#1-개요)
- [2. Use Cases](#2-use-cases)
  - [2-1) Case 1 - groupBy pushdown](#2-1-case-1---groupby-pushdown)
  - [2-2) Case 2 - DDL 추가](#2-2-case-2---ddl-추가)
  - [2-3) Case 3 - Analyzer에 기능 추가](#2-3-case-3---analyzer에-기능-추가)
  - [2-4) Case 4 - 실행 계획 기능 추가](#2-4-case-4---실행-계획-기능-추가)
- [3. customize 기능을 추가하는 다른 방법](#3-customize-기능을-추가하는-다른-방법)
- [4. SPARK-18127을 이용한 프로젝트들](#4-spark-18127을-이용한-프로젝트들)
  - [4-1) Microsoft Hyperspace](#4-1-microsoft-hyperspace)
  - [4-2) Microsoft SparkCruise](#4-2-microsoft-sparkcruise)
- [5. 마무리](#5-마무리)

## 1. 개요

본 포스팅은 SPARK+AI SUMMIT 2019 세션 중 "How to Extend Apache Spark with Customized Optimizations" 세션을 간단히 정리한 글이다.

- [발표 세션](https://databricks.com/session/how-to-extend-apache-spark-with-customized-optimizations)
- [발표 자료](https://www.slideshare.net/databricks/how-to-extend-apache-spark-with-customized-optimizations)
- [발표 영상](https://www.youtube.com/watch?v=IlovS-Y7KUk)

Spark 2.2에 추가된 [SPARK-18127](https://issues.apache.org/jira/browse/SPARK-18127) 기능을 이용하면 Spark의 원본 소스 코드를 수정하지 않고도 API만을 사용하여 SparkSQL에 내가 원하는 기능을 추가할 수 있다.

어떤 기능이냐하면 예를 들어 SQL에 새로운 문법을 추가한다거나 Optimizer를 수정한다거나 할 수 있다는 이야기이다.

SPARK-18127 이전에는 기능을 추가할 떄 Spark 소스 코드를 수정해야했으므로 upstream 소스 코드와 동기화 문제가 큰 골치거리였다. 내가 직접해봤다는 건 아니지만 당연히 이런 문제가 생길 수 밖에 없다. 수정한 소스 코드가 Spark 소스 코드에 merge되지 않는 이상 말이다.

UDF를 생각해보자. SparkSQL에 내가 원하는 함수를 넣기 위해 Spark 소스 코드를 수정해야한다면 얼마나 힘든 일이겠는가. SparkSQL에서 UDF를 지원하기 때문에 이러한 고민없이 함수를 쉽게 추가할 수 있다.

아래에서 설명되는 내용도 이런 것에 관련된 내용이다. 추가 대상이 SQL 함수가 아닌 SQL 문법 혹은 최적화 rule에 관련된 것이라는 게 UDF와 비슷하면서도 다른 점이다.

customize할 수 있는 부분들은 다음과 같다.

- SQL parser
- 최적화 rules
- 실행 계획

구체적으로는 SQL 수행 단계에서 아래의 부분을 우리 맘대로 확장할 수 있다. (물론 마음대로 막 되는 건 

<a href="https://imgur.com/ZDoXNOm"><img src="https://i.imgur.com/ZDoXNOml.png" title="source: imgur.com" /></a>

(출처: 발표 자료)

## 2. Use Case 별 API

확장 기능 종류에 따라 사용되는 API가 다르다.

<a href="https://imgur.com/oTiS9cJ"><img src="https://i.imgur.com/oTiS9cJl.png" title="source: imgur.com" /></a>

여기서 소개되는 use case는 발표자의 사용 예이다.

use-case별로 어떤 함수를 사용하는지를 중점보자.

- `e.injectOptimizerRule()`
- `e.injectParser()`
- `e.injectResolutionRule()`
- `e.injectPlannerStrategy()`

### 2-1) Case 1 - groupBy pushdown

```scala
case class GroupByPushDown(spark: SparkSession) extends Rule[LogicalPlan] {
    ...
}

type ExtentionsBuilder = SparkSessionExtenions => Unit
val f: ExtentionsBuilder = { e => e.injectOptimizerRule(GroupByPushDown) }

val spark = SparkSession.builder().withExtentions(f).getOrCreate()
```

### 2-2) Case 2 - DDL 추가

```scala
case class MyParser(spark: SparkSession,
                    delegate: ParserInterface) extends ParserInterface {
    ...
}

type ExtentionsBuilder = SparkSessionExtentions => Unit
val f: ExtentionsBuilder = { e => e.injectParser(MyParser) }

val spark = SparkSession.builder().withExtentions(f).getOrCreate()
```

### 2-3) Case 3 - Analyzer에 기능 추가

```scala
case class MyRule(spark: SparkSession) extends Rule[LogicalPlan] {
    def apply(plan: LogicalPlan): LogicalPlan = {
        plan transform { ... }
    }
}

type ExtentionsBuilder = SparkSessionExtentions => Unit
val f = ExtentionsBuilder = { e => e.injectResolutionRule(MyRule) }

val spark = SparkSession.builder().withExtentions(f).getOrCreate()
```

### 2-4) Case 4 - 실행 계획 기능 추가

LogicalPlan을 PhysicalPlan으로 변경 시 실행 계획을 변경한다.

```scala
case class MyStrategy(spark: SparkSession) extends SparkStrategy {
    override def apply(plan: LogicalPlan): Seq[SparkPlan] = {
        ...
    }
}

type ExtentionsBuilder = SparkSessionExtentions => Unit
val f = ExtentionsBuilder = { e => e.injectPlannerStrategy(MyStrategy) }

val spark = SparkSession.builder().withExtentions(f).getOrCreate()
```

## 3. customize 기능을 추가하는 다른 방법

`SparkSession.sessionState.experimentalMethods`를 이용하는 방법이 있다.

이를 이용하여 아래 기능을 추가할 수 있다.

- physical planning strategy
    - 특징: SparkPlanner의 strategy 제일 앞에 추가된다
- optimizer rule
    - 특징: SparkOptimizer batch 제일 뒤에 추가된다

{% include adsense-content.md %}

## 4. SPARK-18127을 이용한 프로젝트들

### 4-1) Microsoft Hyperspace

[본 블로그에서도 한번 소개](http://jason-heo.github.io/bigdata/2020/12/06/spark-hyperspace.html)했던 Hyperspace도 SPARK-18127을 사용 중이었다.

index 생성 여부에 따라  SQL의 실행 계획을 변경하는데 이때 SPARK-18127이 사용된다.

[`enableHyperspace()`](https://github.com/microsoft/hyperspace/blob/cafaa91389549cbf5f3ef29a7733ab709a255e00/src/main/scala/com/microsoft/hyperspace/package.scala#L47)가 호출될 때 확장을 등록하고,

[`disableHyperspace()`](https://github.com/microsoft/hyperspace/blob/cafaa91389549cbf5f3ef29a7733ab709a255e00/src/main/scala/com/microsoft/hyperspace/package.scala#L61)가 호출될 때 등록했던 확장 기능을 제거하고 있다.

### 4-2) Microsoft SparkCruise

공교롭게도 SparkCruise도 Microsoft에서 만든 것이다.

- [Spark+AI SUMMIT 2020 발표 영상](https://databricks.com/session_na20/sparkcruise-automatic-computation-reuse-in-apache-spark)
- [VLDB 논문](http://www.vldb.org/pvldb/vol12/p1850-roy.pdf)

간단하게 훑어본 결과 내용은 대략 이런 것 같다.

- 사용자가 자주 사용하는 질의를 자동으로 materialized view로 만든다 (사용자는 이런 게 생성되었는지 모른다)
- 이후에 materialzied view를 재활용할 수 있는 query가 입력되는 경우 query 처음부터 수행하지 않고 view를 사용한다

이것도 마찬가지로 사용자가 입력하는 SQL에는 아무 변화가 없고 Spark 내부적으로 처리하는 것이기 때문에 SPARK-18127을 이용하여 구현되었다.

## 5. 마무리

Spark에 추가된지 2년이 넘은 기능인데 아직 관련된 자료를 찾기가 쉽지 않다. 그도 그럴 것이 API 사용법 자체보다는 Logical Plan이라던가 Physical Plan 혹은 SQL parsing을 위한 ANTLR까지 다뤄야하는 주제라서 쉽게 접근하기가 어려운 주제이다.

발표 영상을 보고난 뒤에 실습을 한번 해 볼까 했는데 마땅히 추가할 만한 기능도 없고 자료도 없어서 정리 수준으로 마무리한다.

나중에 업무상으로 필요해지면 그때 깊이 있게 조사를 해봐야겠다.
