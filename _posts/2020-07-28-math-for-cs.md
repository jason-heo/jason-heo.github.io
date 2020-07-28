---
layout: post
title: "Mathematics for Computer Science"
categories: "programming"
---

Text book: https://cs121.boazbarak.org/LehmanLeighton.pdf

시청해둔 내용 정리해두면 나중에 시간이 지난 후에 '내가 한때는 이런 것도 봤었구나'하고 시청했다는 사실 정도는 기억하겠지.

### 1강: Introduction and Proofs

- 시청일: 2020.07.20.(월)
- https://www.youtube.com/watch?v=L3LMbpZIKhQ&list=PLB7540DEDD482705B&index=1
- 시청한지 일주일 밖에 안 되었는데 벌써 기억이 안난다;;
- 재미없을 줄 알았는데 흥미 진진했다
- (n^2 + n + 41) is a prime number?
    - n이 1~39일 때까지는 계속 prime number인데
    - 40은 prime number가 아니다
- (a^4 + b^4 + c^4) = d^4를 만족하는 양의 정수 a, b, c, d는 없다?
    - a=95800, b=217519, c=414560, d=422481
    - 대단하다. 이걸 어떻게 증명했지?
- 313\*(x^3 + y^3 = z^3을 만족하는 양의 정수 a, b, c는 없다?
    - 있다, 그런데 1천 자리가 넘는다. 헉
    - 컴퓨터를 이용하여 brute-force 방식으로 찾았다고 한다

### 2강: Induction

- 시청일: 2020.07.25.(토)
- https://www.youtube.com/watch?v=z8HKWUWS-lA&list=PLB7540DEDD482705B&index=2
- Proof by Contradiction
    - root 2가 무리수임을 증명하기
    - 증명 방법: root 2가 유리수인 경우 모순에 빠지는 것을 보여줌으로서 무리수인 것을 증명한다
- 가우스 합이 true임을 증명
    - 1부터 n까지의 합 = n\*(n+1)/2
    - 이게 참임을 Induction 방식으로 증명한다
- 90 > 92
    - 이거 교수님 그림 때문에 헷갈려서 '왜 이게 성립되지?'하고 한참을 헤맸다;;

### 3강: Strong Induction

- 시청일: 2020.07.27.(월)
- https://www.youtube.com/watch?v=NuGDkmwEObM&list=PLB7540DEDD482705B&index=3
- 1, 2강에서 배운 내용을 까먹어서 3강부터는 노트에 필기를 했는데, 옮겨 적는 것도 일이네
- 증명을 왜 배워야하나?
    - 내가 작성한 코드에 버그가 없다는 걸 증명하기 -> 아무래도 이걸 잘 해야 Unit TC도 잘 작성할 것 같긴 하다
- invariant property
    - 불변하는 성질
    - 검색해보니 알고리즘이 잘 작동하는데 증명하는 용도로도 사용되는구만
    - 암튼 이걸 이용해서 주어진 8-puzzle의 상태가 풀 수 없는 문제라는 걸 증명하는데 신기했다
- unstacking game
    - 조교들과 학생들이 나와서 누가 더 높은 점수를 내는지 게임하는데 재미있었다
    - 알고 봤더니 어떤 전략을 택하든 항상 같은 점수가 나오는 문제이다
    - 요걸 증명할 때부터 약간 집중이 떨어졌다
    - 증명: stack에서 k개를 덜어내더라도 total 점수가 k에 independent함을 수식으로 밝힘
