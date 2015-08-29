---
layout: post
title: "코세라 머신러닝 강의 내용 정리"
categories: programming
---

Coursera의 명강의, 앤드류 응(Andrew Ng) 교수님의 [Machine Learning 강의](https://www.coursera.org/learn/machine-learning/home/welcome)를 듣기 시작했다. 중요한 내용을 키워드 위주로 기록해보려고 한다. 아마 강의 내용 전체를 기억하긴 어려울텐데 중요 개념이라도 기억하고 있으면 키워드 정도는 남겨 놓은 게 좋을 듯... 알고리즘 강의들었을 때는 강의 듣는 도중은 재미있었고, 기억도 좀 남아있었는데 6개월 지나고 나니 기억이 안난다 ㅎㅎ;

- 참고
 - 머신러닝 강의 홈페이지가 2015년 여름에 변경되었는데 traditional한 coursera 강의 포맷이 아니다보니 좀 헷갈린다.
 - [여기](https://class.coursera.org/ml-005/lecture)가 옛날 형식의 강의 목록 페이지인데, ppt 파일도 다운받을 수 있다.

Week 1
======

Introduction
------------

- Supervised Learning
 - Training Set으로 학습을 시킨 뒤에 Actual Data가 입력되었을 때 학습 내용을 기반으로 Predict를 하는 방법이다.
 - Classification: 분류
  - 예: 스팸 메일 분류도 Supervised Learning의 예이다. 스팸 메일을 학습시킨 뒤에, email이 도착했을 때 학습에 기반하여 스팸 메일인지 분류를 하기 떄문이다. 이런 것을 Classifaction이라고 한다.
  - Classification은 Value가 Discrete하다. 스팸메일인지/아닌지, 악성 종양인지/아닌지
  - Regression: 회귀
   - 예: 날씨 예측. 과거 날씨 Data를 학습시킨 뒤, 미래 날씨를 예측할 수 있다.
   - Regression은 Value가 Continuous하다.

- Unsupervised Learning
 - 학습을 시키지 않더라도, 프로그램이 알아서 뭔가를 해준다.
 - 예) 뉴스 기사를 보면 비슷한 뉴스를 모아서 보여주는데, 이런 것은 누가 학습을 시켜주지 않더라도 프로그램이 알아서 분류해주는 것이다.
 - Segmentation: 분류
 - Clustering: 군집화
 - Social Network Analysis
 - 강의 내용 중 인상 깊었던 것은 "Cocktail Party Problem"인데, 소란스러운 칵테일 파티에서 녹음된 다양한 소리 중에서 한사람의 목소리만 추출해 주는 것이었다. 음성 클립을 실제로 들어볼 수 있었는데, 출처 논문에 "Te-won Lee"라는 사람이보였고 아마 한국 사람같다. (이것 말고도 한국 사람이름이 한번 더 나오는데 헬리콥터가 스스로 Airshow를 펼치는 것도 한국 학생이 만들었나보다)

Model and Cost Function
-----------------------

여기서부터 수학 공식이 등장하는데, 미분 공식도 나온다. 그런데 크게 어렵게 생각하지 않아도 된다. '미분' = '기울기'라는 개념 정도만 있으면 된다.

Linear Regression에서는 Training Set과 가장 근접한 1차 그래프를 찾는 문제와 동일하다.

집 가격을 결정하는데는 많은 요인이 있겠지만, 간단하게 다음과 같이 생각해보자. 동일 지역이라면 대부분 집 면적이 넓을 수록 가격도 비쌀 것이다. 집 가격 데이터를 입력하면 다음과 같이 그려질텐데, 이걸 이용해서 "x 면적의 집은 대략 얼마일까?"를 답하는 것이라고 생각하면 된다.


```
    ^
  집|               *
    |         ***
  가|      **  *   *
  격|   *  *
    |    *
    | ***   
    |*
    ------------------------>
                        (집의 면적)
```

cost function이 라는 수식이 등장하는데, (수식 편집기 사용 방법을 몰라서 블로그에 입력은 못한다;) 첨에 이걸 보면서 "왜 제곱을 넣었지?"라고 생각했는데 이게 2차 방정식의 그래프로 표현이 되더라.

### Contour Plots

Contour Plots가 뭔지는 몰라도 수업 듣는데 불편은 없다.

Feature가 1개인 경우 평면상에 x,y 그래프로 Cost Function을 그릴 수 있다. 하지만, Feature가 2개가 되는 순간 3차원으로 표현해야 한다. 하지만 3차원을 표현하기는 어려우므로 3차원을 평면상에 표현을 하는데 이것이 Contour Plots라고 한다더라.

본 글에 그림이 전혀없어서 표현하긴 어려운데, 산을 보면 등고선이 표시되어 있는데 이걸 Contour Plots라고 볼 수 있고 우리가 눈으로 보는 진짜 산이 3D 그래프이다.

### Gradient Descent

Training Data를 가장 잘 표현하는 Graph를 찾는 알고리즘이다. Cost Function에 제곱을 넣은 이유를 여기서 알 수 있게 된다.  Cost function의 minimum을 결정하는 것이 Linear Regression의 핵심. 2차원 그래프이기 때문에 Local Minimum이 Global Miminum이 된다.... 그래서 Cost function에 일부러 제곱을 넣었나보다.... (Hill Climbing 알고리즘이 있는데, 이것은 Local Minimum이 Global Minimum은 아니다.)

이상 Week 1의 내용을 정리해 봤는데, 남는 건 역시 Supervised Learning, Unsupersived Learing 이거 밖에 없을 것 같다... 이게 머신 러닝에서 젤 쉬운 개념이지;;
