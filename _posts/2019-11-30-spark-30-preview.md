---
layout: post
title: "Apache Spark 3.0 Preview"
categories: "bigdata"
---

근 3개월 정도 회사 일이 바빠진 사이, 자연스레 블로그 포스팅도 못하고 Spark 공부도 제대로 못하였다.

그러던 중 드디어 2019년 11월 06일에 [Spark 3.0 preview가 공개](https://spark.apache.org/news/spark-3.0.0-preview.html)되었고, 다운로드가 가능해졌다.

Spark 3.0에 대한 논의는 [2018년 4월](http://apache-spark-developers-list.1001551.n3.nabble.com/time-for-Apache-Spark-3-0-td23755.html)부터 논의되었던 듯 하다.

위 논의는 Spark 1.0이 2014년에 release되었고, Spark 2.0이 2016년에 release되었으니 2018년에 Spark 3.0이 release되면 좋겠다는 논의같다.

이후 논의가 잠잠해지는 듯 싶더니 2019년초부터 Spark 3.0에 대한 논의가 활발해졌고 [2019년 9월](http://apache-spark-developers-list.1001551.n3.nabble.com/Spark-3-0-preview-release-on-going-features-discussion-td27957.html)에는 드디어 Spark 3.0의 구체적인 기능에 대한 이야기도 나오기 시작했다.

아직은 Spark 3.0에 포함되는 구체적인 기능을 알기가 어려운 데 바로 위 mailing list에서 그 기능을 대략 짐작해볼 수 있다. (그런데 아쉽게도 major 버전을 업그레이드할만한 기능 추가가 안 보인다. 2018년에 Spark 3.0에 대한 논의가 더이상 진전되지 못한 것도 3.0 기능에 대한 것인 듯도 싶다.)

부저런한 개발자가 ["Apache Spark 3.0 Review — What the Spark is all about"](https://medium.com/cloudzone/apache-spark-3-0-review-what-the-spark-is-all-about-998844e12b3c)라는 글을 올렸는데, Spark 3.0에 포함되는 글을 여기를 참고해보자.

쑥 훑어봤는데 크게 와닿는 기능은 없고, "Adaptive execution of Spark SQL" 이건 좀 괜찮아 보인다. BigData에서는 모든 게 Big하기 때문에 Cost Based Optimizer에서 사용할 cost가 없는 경우가 많은데, 부족한 정보로 생성한 execution plan을  adaptive하게 변경하는 기능 같다.


