---
layout: post
title: "Kubernetes 1.20 docker deprecation"
categories: "programming"
---

Kubernetes 1.20이 release되면서 Docker가 deprecate된 것 때문이 혼란이 많은 것 같다.

향후 버전에서는 Docker가 deprecate된 것을 넘어서 아예 Kubernetes에서 사라지게 된다. 사라지는 버전은 1.23으로서 2021년 하반기를 예상한다고 한다. 

Docker가 사라진다는 것은 무엇을 의미할까? 내가 만든 docker image를 더 이상 사용하지 못한다는 것일까?

정답은 '아니오'이다.

Docker를 두가지 관점에서 봐야한다.

1. Docker image
1. Docker runtime

이 중에서 사라지는 것은 Docker runtime이다.

즉, 나처럼 managed kubernetes cluster를 사용하는 사람 입장에서는 크게 고민할 것이 없다. (`kubectl` 사용법 정도는 바뀔 것 수도 있다)

`docker build`로 생성된 image는 계속하여 Kubernetes에서 사용할 수 있다. docker image는 OCI (Open Container Initiative)에서 정의된 파일 포맷이다.

그리고 이 docker image 실행하는 runtime이 필요한데 Kubernetes 사용 중인 Dockershim이라는 runtime 이 사라지게 되고 containerd를 사용하게 된다.

이것이 Docker가 Kubernetes 1.20에서 deprecate된 것을 의미한다.

관련 문서

- [Don't Panic: Kubernetes and Docker](https://kubernetes.io/blog/2020/12/02/dont-panic-kubernetes-and-docker/)
- [Dockershim Deprecation FAQ](https://kubernetes.io/blog/2020/12/02/dockershim-faq/)
