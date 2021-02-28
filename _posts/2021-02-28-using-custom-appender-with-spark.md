---
layout: post
title: "Spark에서 log4j custom appender 사용하기"
categories: "programming"
---

Spark에서 log4j custom appender를 사용하려고 했는데 계속 class를 찾지 못하는 문제가 발생했다.

최초에 방향을 잘못 잡아서 꽤나 고생했던 문제인데, 다행히 2월 마지막에 해결하였다.

처음에는 Java에서 흔하게 발생하는 jar 충돌 문제로만 생각하고 제대로 구글링도 하지 않았다.

그런데 왠걸, 알고보니 사용자가 지정한 jar file이 읽히기 전에 log4j가 initialize되는 게 문제였다.

[SPARK-10881](https://issues.apache.org/jira/browse/SPARK-10881) 참고

SPARK-10881은 2015년에 Spark 1.3 시절에 open된 이슈인데 아직도 해결이 안 되고 있다.

이를 해결하기 위해선 다음과 같이 두 가지를 설정하면 된다.

1. custom appender jar file을 `--files` 옵션으로 upload를 한다
1. `spark.driver.extraClassPath`, `spark.executor.extraClassPath` 옵션을 이용하여 custom appender jar를 지정한다
    - 이렇게 하면 log4j가 초기화되기 전에 custom appender용 class를 읽히게 할 수 있나보다

Stackoverflow에도 비슷한 질문과 [답변](https://stackoverflow.com/a/39444623/2930152)이 있다.
