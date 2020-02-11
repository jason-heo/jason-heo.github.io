---
layout: post
title: "'PersistentVolumeClaim를 이용한 Deployment'와 Statefulset의 차이"
categories: "kubernetes"
---

올해 2019년의 목표가 Apache Spark 심화 학습 및 적용과 Kubernetes를 실 서비스에 도입하기였다. Spark은 목표를 90% 이상은 채웠다고 생각되는데 Kubernetes는 아직도 걸음마 수준이다. (할줄 하는 건 `kubectl get pods` 수준의 몇 가지 명령 정도;;)

암튼 뒤 늦게 공부를 좀 하고 있는데 [Run a Single-Instance Stateful Application](https://kubernetes.io/docs/tasks/run-application/run-single-instance-stateful-application/)를 보고 있다.

그런데, 분명 제목은 "Stateful Application"인데 `kind: Deployment`이다. 어라? `kind: Statefulset`이 아니네?

이쯤에서 "`PersistentVolumeClaim`을 이용한 Deployment"와 "Statefulset"의 차이가 궁금해졌다.

문서 제목을 잘 보면 "Run a Single-Instance"라고 나온다. 문서의 마지막 부분에 [Updating](https://kubernetes.io/docs/tasks/run-application/run-single-instance-stateful-application/#updating) 섹션을 보면 "This setup is for single-instance apps only. The underlying PersistentVolume can only be mounted to one Pod"이라고 나온다.

즉, `kind: Deployment`라고 해서 Stateful Application을 서빙하지 못하지 않지만, single instance만 운영할 수 있다 (라고 생각한다)

검색을 해보면 이에 관한 문서로 [Kubernetes Persistent Volumes with Deployment and StatefulSet](https://akomljen.com/kubernetes-persistent-volumes-with-deployment-and-statefulset/)를 찾을 수 있다. 문서 앞 부분에 중요한 내용이 있는데 "일반적으로 stateful한 것은 Statefulset을 사용하고 state-less한 App은 Deployment를 사용하라고 하지만, Deployment라고 해서 stateful한 App을 못 돌리는 것은 아니다"

위 문서보다 쉽게 검색되는 문서로 [K8s: Deployments vs StatefulSets vs DaemonSets](https://medium.com/stakater/k8s-deployments-vs-statefulsets-vs-daemonsets-60582f0c62d4)가 있는데, 여기서는 이런 내용이 잘 안 드러나는 듯 하다.

암튼 위에 두 개 문서를 다 읽고나서 이어서 Kubernetes 공식 문서인 [Run a Replicated Stateful Application](https://kubernetes.io/docs/tasks/run-application/run-replicated-stateful-application/)를 계속 읽어나가야겠다.

