---
layout: post
title: "Dependency Injection Container의 필요성"
categories: "programming"
---

Data Engineering 쪽으로는 나름 자신이 있고, Scala를 이용한 Spark Data Pipeline 개발도 지금 사용되는 업무에서는 크게 부족함 없이 잘 개발 중이다.

그런데, 업무 도메인이 제한적이라서 그런지 Software Engineering, 특히 OOD 및 Design Pattern 쪽으로는 항상 뭔가 많이 부족함을 느끼고 있다.

틈날 때 이쪽 분야 공부를 하고 있는데, 작년 여름에 시작된 신규 프로젝트에서 Scala code에 Dependency Injection Container를 도입하려 시도했다가 잠시 미루게 되었다. (그래도 DI Container를 도입하지는 못했지만 기존 프로젝트 대비 나름 괜찮은 개선이 있었다. hocon include/substitution/class 자동 binding, airlift 도입 등 평소 작성하던 boilerplate code를 제거, Dataset을 도입하여 strong type 적용 등등)

2020년 상반기도 거의 끝나가고 약간 시간이 남아서 DI Container를 다시 공부 중이다.

## DI (Dependency Injection)

class 내에서 new를 사용하여 다른 concrete class의 instance를 생성하는 경우 다양한 문제가 생길 수 있는데, 이와 관련된 자료들이 넘치게 많으니 따로 정리하지 않았다.

### DI vs DI Container

DI와 DI Container도 분리해서 생각해야한다.

[IoC? DIP? IoC Container? DI? DI Framework? 도대체 그게 뭔데?](https://velog.io/@wickedev/IoC-DIP-IoC-Container-DI-DI-Framework-%EB%8F%84%EB%8C%80%EC%B2%B4-%EA%B7%B8%EA%B2%8C-%EB%AD%94%EB%8D%B0)에 잘 설명되어 있다.

## DI Container를 사용해야할까? 어떤 장점이 있을까?

Software 개발에서 DI는 필수적으로 도입을 해야하지만, DI Container는 왜 사용하는지 논의가 있다.

[Why do I need an IoC container as opposed to straightforward DI code?](https://stackoverflow.com/questions/871405/why-do-i-need-an-ioc-container-as-opposed-to-straightforward-di-code)에 Stackoverflow의 창업자 [Joel Spolsky가 작성한 답변](https://stackoverflow.com/a/871420/2930152)도 있는데 Joel은 DI Container는 불필요하다는 의견이다. 이유는 'a lot harder to read'라고한다.

하지만 위 질문에서 [upvote를 가장 많이 받은 답변](https://stackoverflow.com/a/1532254/2930152)은 DI Container를 사용해야한다는 주장.

[What is a Dependency Injection Container and why use one](https://www.sarulabs.com/post/2/2018-06-12/what-is-a-dependency-injection-container-and-why-use-one.html)라는 글도 괜찮았다. DI가 적용 안 된 코드를 차례대로 개선해가면서 DI Container릐 필요성과 구현에 대해서 설명을 하고 있다.


나도 아직까지는 DI Container를 도입했을 때 얼마나 큰 장점이 있을지 잘 모르는 상태이긴 하다. over-engineering이 될까 걱정이다.

어쨌거나 많은 사람들이 사용하고 있으니, 나도 기회가 되면 적용을 해봐야겠다.

## Google Guice

[Learn google guice - absolute beginners](https://www.tutorialspoint.com/guice/index.htm)라는 tutorial 문서가 있는데, Guice code를 작성하는데 도움이 되지만, 몇 가지가 좀 아쉽다 (DI Container의 필요성, 왜 Guice를 사용해야하는지, 어떤 장점이 있는지, 내부 작동 방식은 어떻게 되는지 등)

Injector와 Module의 개념도 좀 헷갈리긴 했는데, 이런 개념들은 Guice 개발자가 직접 프리젠테이션한 [Google I/O 2009 - Big Modular Java with Guice](https://youtu.be/hBVJbzAagfs)가 도움이 된다. (제목에서 볼 수 있듯 2009년 발표 자료이다. 난 이때 뭘하고 있었을까)

<a href="https://ibb.co/DL99FV7"><img src="https://i.ibb.co/M9ccH2R/image.png" alt="image" border="0"></a>

(출처: https://youtu.be/hBVJbzAagfs?t=695)

<a href="https://ibb.co/sH6RTp8"><img src="https://i.ibb.co/74RC3xs/image.png" alt="image" border="0"></a>

(출처: https://youtu.be/hBVJbzAagfs?t=714)

그리고 발표 자료 초반에도 DI Container의 필요성과 장점에 대해서 설명을 하고 있다. 발표 초반 12분 정도는 시청을 해보면 좋겠다. (개인적으로는 두 명이 서로 왔다갔다 하면서 발표하는 방식은 헷갈리고 집중이 안 되서 싫어하지만 이렇게 좋은 동영상은 봐야할 듯)

## Scala에서의 DI Container

Google Guice를 Scala에서 사용해보고 싶었는데, Scala code의 어디에 annotation을 써야하는지 잘 모르겠다. 그래서 검색을 해보니 [scala-guice]https://github.com/codingwell/scala-guice) - 'Scala extentions for Google Guide'라는 프로젝트가 있었다. [김용환 블로그](https://knight76.tistory.com/entry/scalaspark-scalaguice%EC%99%80-finatra%EC%9D%98-TwitterModule-injection-%EA%B2%BD%ED%97%98)를 보면 "실제로 scala-guice를 사용해 구현해보니 경량스럽게 개발이 가능하다"라고 평가하고 있다. 그런데, Star 개수도 적고 프로젝트도 그다지 활발한 것 같지 않다.

[macwire](https://github.com/softwaremill/macwire)라는 프로젝트가 있는데, 이걸 공부 좀 해봐야겠다.
