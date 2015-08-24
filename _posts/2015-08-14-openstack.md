---
layout: post
title: "Openstack에 대한 자료"
categories: programming
---

세상이 많이 바뀌어서 온통 BigData니 Machine Learning이니 Cloud니 이런 이야기가 넘친다. 주로 DB 관련된 업무로 10년 넘게 밥벌어 먹고 살았고, 앞으로도 당장은 밥벌이하는데 부족함은 없겠지만, 자꾸 시대에 뒤쳐지는 느낌이다. 이제는 틈틈히 새로운 것에 대한 자료도 찾아봐야겠다.

첫 번째로 OpenStack.

An Introduction to OpenStack
=================================

- 출처: https://puppetlabs.com/puppet/what-is-puppet

What OpenStack is not
---------------------

- It's not a single open source project
- It's not a **hypervisor**
- It's not a storage platform
- It's not competitive to VMware

### 참고: Hypervisor란?

Hypervisor가 뭔지 몰랐는데 찾아보니 PC 기준으로는 VMware나 VirtualBox 같은 것을 이야기한다. VMWare를 첨 써본 게 1999년도니깐 오래전에 Hypervisor를 접해본 것이었네. VirtualBox도 몇 년 전에 테스트로 써 봤었고.
그런데 Main frame에서 1967년 IBM CP-40에서 이미 Full Virtualization이 개발되었다니 이거 엄청 신기하다.

Type 1과 Type 2 유형 2가지가 존재한다.

- Type 2: 우리가 흔히 사용하는 VMware, VirtualBox 등의 방식으로 작동
- Type 1: Hypervisor가 H/W를 직접 실행하며 Guest OS는 2 수준에서 사용됨. Main frame 급에서 1960년대부터 이미 지원되는 유형이고, Linux에서는 KVM이 요런 모델인 것 같음

OpenStack's components
----------------------

- OpenStack compute (code-named "Nova")
 - Amazon EC2와 개념적으로 동일
 - 다양한 hypervisor 사용 가능 (Xen, KVM, vSphere/ESXi, Hyper-V)
- OpenStack Object Store (code-named "Swift")
 - Amazon S3와 비슷하다고 생각하면 된다
 - 분산된 객체 저장소 기능을 제공한다
- OpenStack Image (code-named "Glance")
 - Amazon AMI catalog과 비교된다.
 - image store, image retrieval, and image discovery 서비스 제공
- OpenStack Identity (code-named "keystone")
 - 인증, 정책 서비스
- OpenStack Block Storage (code-named "Cinder")
 - "Network as a Service" 기능 제공
- OpenStack Networking (formerly code-named "Quantum")
- OpenStack Dashboard (code-named "Horizon")

Why does OpenStack matter?
--------------------------

- 포괄적인 Cloud 서비스를 제공하는 몇 개 안 되는 framework 중 하나이다.
- 유의미한 제공자(Provider)들이 그들의 Cloud 서비스를 제공하기 위하여 OpenStack을 적용 중이다.
- Linux가 그랬던 것 처럼 OpenStack은 Data Center 혹은 Cloud 배포에 상당한 영향력을 발휘할 것이다.

How do I get started with OpenStack?
------------------------------------

- DevStack은 OpenStack을 시작하는데 있어 훌륭한 도구이다.
- OpenStack은 Linux에 기반을 두고 있다. Linux를 잘 모른다면 친숙해져라(Ubuntu는 OpenStack에서 매우 일반적이다)
- Instance는 상태가 없다. (Instances are stateless). 따라서 설정 관리가 치명적이다. (Puppet)
- OpenStack 자체는 대부분 Python으로 만들어졌다.
- 주로 사용되는 다른 기술들에는 KVM, network namespace, & OVS가 있다.

### 참고: Puppet이란?

수십~수천 대의 서버 관리를 자동으로 해주는 Tool인 것 같음. 지금 다니는 회사에서는 서버 관리를 인프라 관련 팀에서 관리해주다보니 이런 분야의 새로운 기술들은 너무 모르는 것 같다;;

- https://puppetlabs.com/puppet/what-is-puppet

How to use OpenStack in Your Small Business
===========================================

- 출처: http://www.cio.com/article/2376267/cloud-computing/how-to-use-openstack-in-your-small-business.html
[1]: http://www.slideshare.net/lowescott/an-introduction-to-openstack
