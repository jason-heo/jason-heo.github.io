---
layout: post
title: "Adaptive Execution in Apache Spark 3.0"
categories: "bigdata"
---

어제 [Apache Spark 3.0의 기능](http://jason-heo.github.io/bigdata/2019/11/30/spark-30-preview.html)에 대해 조사하면서 "Adaptive Execution" 기능이 들어간다는 걸 알게 되었다.

이게 왜 대단한 건지를 알려면 SQL이 수행되는 방식에 대해서 고민을 해봐야한다.

아래와 같은 SQL이 있다고 하자.

```sql
SELECT
  COUNT(*)
FROM tab1
  INNER JOIN tab2 ON tab1.key1 = tab2.key1
WHERE tab2.key2 = 'AAA'
```

RDBMS나 Spark의 user인 우리는 SQL을 작성만 하면 RDBMS나 Spark이 SQL을 알아서 실행하고 결과를 떡하니 출력해준다.

그런데, 이번엔 우리가 SQL을 작성하는 user가 아닌 RDBMS의 개발자라고 생각해보자. 누군가 위의 SQL을 던졌다. 우리는 어떻게 해야 위의 SQL을 실행할 것인가?

제일 쉬운 Nested Loop Join만 하더라도 `tab1`을 outer loop에 둬야할지, `tab2`를 outer에 둬야할지 고민이 생긴다. Oracle이나 MySQL 같은 RDBMS에서는 실행 계계획을 위한 통계를 테이블의 필드마다 저장하고 있지만, BigData 환경에서 제공되는 Storage에서는 통계를 얻기 힘든 경우가 많다.

Selectivity 문제(field 1개의 filtering을 만족하는 레코드 개수 예상하기)는 통계를 이용하면 그나마 쉬운데, JOIN된 결과 레코드 개수를 예상하는 문제는 많은 가정과 상상을 통해서 계산을 해야하므로 올바르지 않은 경우가 많다.

이에 관한 설명은 [Row Estimation Examples](https://www.postgresql.org/docs/8.3/row-estimation-examples.html)를 읽어보는 것이 좋다.

더 나은 개발자가 되기 위해서 주어진 SQL을 RDBMS나 Spark 같은 SQL 엔진이 어떻게 실행하는지에 대해 고민을 꼭 해봤으면 한다.

이야기가 잠시 샛는데, 암튼 제대로된 실행 계획을 만드는 건 어려운 일이다. *"adaptive"*는 "적응하는", "적응형"이라는 의미의 단어인데 주위 환경에 맞춰서 능동적으로 변화하는 것이라 생각하면 된다.

즉, 제한된 조건만 가지고 일단 실행 계획을 만든 뒤 실행을 해본다. 실행을 하면서 실제 데이터를 읽어보기 때문에 더 많은 정보가 쌓이고 앞서 만든 실행 계획이 잘못된 경우 올바른 실행 계획으로 변경을 한다.

이렇게 멋진 기능이 Spark 3.0에 포함될 예정이라고 한다. 이미 preview 버전도 다운로드할 수 있기 때문에 관심있는 분은 직접 돌려볼 수도 있다.

Adaptive Execution Engine에 대한 논의 자체는 꽤 오래전부터 있었던 듯 하다

Intel 개발자들에 의해 [SPARK-9850](https://issues.apache.org/jira/browse/SPARK-9850) 이슈가 2015년 8월에 생성되어 있었고, 2018년 1월에 [SPARK-23128](https://issues.apache.org/jira/browse/SPARK-23128) 이슈에서 PR이 생성되었다.

개발자들이 [2017년 10월에 Spark Summit](https://www.youtube.com/watch?v=FZgojLWdjaw)에서 발표한 영상도 있으니 어떤 문제를 어떻게 해결했는지 참고할 수 있다.

SPARK-23128 이슈를 보면 Baidu에서 성능 측정한 것이 있는데 50~200% 정도의 성능 향상이 있다고 한다.
