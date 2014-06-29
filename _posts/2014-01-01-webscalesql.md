---
layout: post
title: "WebScaleSQL"
date: 2014-03-05 
categories: mysql
---

MySQL 5.6 기반의 새로운 MySQL fork가 등장했다. 이름은 WebScaleSQL. 작명에 대한 이유는 [여기](http://webscalesql.org/faq.html)서 찾을 수 있다. 페이스북 그룹도 있으므로 가입해보면 정보를 얻을 수 있을지도...

Google, Twitter, Facebook, LinkedIn 같은 회사들은 MySQL 코드를 직접 수정하는 경우가 있는데 이들의 변경 내용을 모아서 배포하는 MySQL 버전이라고 생각하면 된다. 추가, 수정된 code는 MySQL 공식 소스 코드에 포함되지는 않으므로 새로운 MySQL 배포판 혹은 branch가 되는 듯 하다.

현재는 소스 코드만 제공 중이고 바이너리는 제공하지 않는다. 컴파일을 위해선 gcc 4.7이 필요하다. CentOS 최신 버전이 아닌 이상 gcc 4.7 사용이 어려워서 지금 gcc 컴파일 중인데 시간이 엄청 걸리네... 물론 gcc 4.7을 직접 컴파일 하지 않고 devtool이라는 것을 이용하면 yum으로 쉽게 설치 가능하다고 한다.

시작된지 얼마 안 된 프로젝트라서 WebScaleSQL의 특징, 성능 비교 자료는 찾기가 어렵다. WebScaleSQL에 contribution을 좀 하게 되면 Google, Twitter, Facebook, LinkedIn 회사들에 취업이 좀 쉬워질까...

