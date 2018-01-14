---
layout: post
title: "Spark Data Source API V2 소개"
categories: "bigdata"
---

앞서 작성했던 [Spark에 Custom Data source 붙이기](Spark에 Custom Data source 붙이기)는 Spark 1.3부터 지원하던 Data Source API를 사용한 것이다. Spark 2.3부터는 새로운 Data Source API를 지원하는데, 이를 V2라고 한다.

[SPIP: Data Source API V2](https://docs.google.com/document/d/1n_vUVbF4KD3gxTmkNEon5qdQ-Z8qU5Frf6WMQZ6jJVM/edit#heading=h.mi1fbff5f8f9)라는 문서를 보면, V2가 나오게 된 배경과 새로운 API의 사용법, 관련 PR을 볼 수 있다.

배경을 보면 다음과 같은 내용이 있다.

The current Data Source API was released with Spark 1.3. Based on the community feedbacks, it has the following limitations:

1. Since its input arguments include DataFrame/SQLContext, the data source API compatibility depends on the upper level API.
1. The physical storage information (e.g., partitioning and sorting) is not propagated from the data sources, and thus, not used in the Spark optimizer. 
1. Extensibility is not good and operator push-down capabilities are limited.
1. Lacking columnar read interface for high performance.
1. The write interface is so general without transaction supports.

몇 가지 눈에 띄는 기능들을 간추려보자면,

### 통계 관련 interface 제공

Cost Based Optimizer를 사용하려면 통계를 활용해야한다. 그런데, BigData에서 통계를 구축하려면 Full Scan을 해야한다는 단점이 있다. BigData Platform 중에서는 metadata를 이용하여 아래의 값들을 빠르게 조회하는 기능을 제공하는 것들도 있는데, metadata와 아래 API를 이용하여 통계를 빠르게 구축할 수 있을 것이다


```
public interface Statistics {
  OptionalLong getSize();
  OptionalLong getRows();
  OptionalLong getDistinctValues(String columnName);
}
```

### 임의 Expression에 대한 pushdown

아래 함수의 인자를 보면 `Expression[] filters`를 입력받는 것을 볼 수 있다. 이제 좀 더 복잡한 기능들도 외부 Data Source로도 pushdown할 수 있을 것 같다.

```
@Experimental
@InterfaceStability.Unstable
public interface CatalystFilterPushDownSupport {
  /**
   * Push down filters, returns unsupported filters.
   */
  Expression[] pushDownCatalystFilters(Expression[] filters);
}
```

### Columnar Batch Read

Column DB의 read 효율을 최대화하기 위하여 Columnar Batch Read도 지원한다. (V1 API 같은 경우, 이런 기능이 없기 때문에 Parquet reader는 public API가 아닌 internal API를 사용하여 개발되었다)

[TC에서 발췌](https://github.com/cloud-fan/spark/blob/a29031db10b4c0a2dd87d7f033068feb0f64d14a/sql/core/src/test/scala/org/apache/spark/sql/sources/v2/DataSourceV2Suite.scala#L239)

```
  override def get(): ColumnarBatch = {
    batch.reset()
    if (currentBatch == 1) {
      batch.column(0).putInts(0, 5, 0.until(5).toArray, 0)
      batch.column(1).putInts(0, 5, 0.until(5).map(i => -i).toArray, 0)
    } else {
      batch.column(0).putInts(0, 5, 5.until(10).toArray, 0)
      batch.column(1).putInts(0, 5, 5.until(10).map(i => -i).toArray, 0)
    }
    batch.setNumRows(5)
    batch
  }
```
