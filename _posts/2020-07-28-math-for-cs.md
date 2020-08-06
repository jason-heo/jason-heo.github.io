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

### 4강: Number Theory I

일주일에 강의 두 개 시청하는 것이 목표인데 1개 밖에 못 봤다. 총 25강이니깐 이 속도라면 6개월이나 걸리겠네.

- 시청일: 2020.08.02.(일)
- https://www.youtube.com/watch?v=NuY7szYSXSw&list=PLB7540DEDD482705B&index=4
- 강의는 뜬금없이 영화 "다이하드 3"로부터 시작한다
- 다이하드3는 범인이 경찰에게 퀴즈를 내고, 경찰과 FBI가 퀴즈를 푸는 사이에 은행을 터는 그런 영화라고 네이버 영화에 줄거리가 나와있다
- 여러 문제 중에서 강의에서 푸는 문제 정의는 [여기](https://www.youtube.com/watch?v=NuY7szYSXSw&feature=youtu.be&t=205)에서 볼 수 있다. 자막을 켜고 듣는 것이 좋다.
- 이 문제는 무려 [Water and Jub Problem](https://leetcode.com/problems/water-and-jug-problem/)이라는 이름으로 leetcode에 medium 난이도로 올라와 있다
- 물론 강의는 단순히 "3갤런, 5갤런 물통으로 4갤런을 만들라"를 푸는 게 목적이 아니라 일반적인 문제를 풀 수 있는 알고리즘과 증명에 대해서 설명을 하고 있다- 본 강의부터 내용이 어려워져서 사실 잘 이해가 안 된다 ㅠㅠ
- 역시 공부는 때가 있다보다
- `m|a`, `m|b` then `m|any results`
    - 부분이 이해가 잘 안 된다
    - 'any results'라는 게 무엇인가?
- if `a|b` and `a|c` then, `a|sb + tc` for all s and t
    - 대략 "4 갤런은 `4 = 3*3 - 1*5`로 구할 수 있다" 이걸 이야기하고 싶은 것 같다
    - 즉, 3갤런 물통을 3번 채우고, 5갤런 물통을 1번 비우면 4갤런이 만들어진다
    - (내용 추가) 이 부분이 이해가 잘 안 되었었는데, `gcd(a, b) = s*a + t*b)`를 만족하는 s, t가 항상 존재한다는 그런 뜻이었구나. `a|b and a|c`에서 a가 말 그대로 common divisor였었군
- 강의가 이해가 안 되서 text book을 보려고 했는데 2004년 책이라 그런지 2010년 강의 내용과 순서가 다르다
    - text book에서는 Number Theory II에서 이 문제를 다루고 있었네;;
- 잠을 자야 내일 출근할 수 있어서 Number Theory II를 읽기는 어려울 것 같아서 인터넷에서 검색을 배 봤더니 아래와 같은 문서가 나왔다
- https://www.math.tamu.edu/~dallen/hollywood/diehard/diehard.htm
    - p, q가 서로 소 (영어로는 relatively prime이라고 하는구만)인 경우 (단, p < q라고 가정)
    - `m*p + n*p = k`를 만족하는 m과 n이 존재한다고 한다 (단, k < q)
    - 즉, 다이하드3 문제에서도 5보다 작은 k(즉, 4)를 만들 수 있었다
    - k는 5보다 작으면 되므로, 3갤런 물통과 5갤런 물통으로 1, 2, 3, 4갤런을 모두 만들 수 있다
    - 그렇다면 leet code 문제는 몰통의 숫자가 소수인지 판단하고, 만약 그렇다면 만들려는 대상 물통이 큰 물통보다 작은지 확인을 해 보면 되겠구나
        - (내용 추가) leet code 문제를 보니 이건 relative prime이 아니구나
    - p, q가 서로 소가 아닌 경우에는 `m*p + n*q`가 "divisor given by the greatest common divisor"라고 하는데 이 부분은 잘 이해가 안 된다
    - 자면서 생각을 더 해봐야겠다
        - (내용추가) 자면서 읽어봤는데, 최대공약수의 배수는 만들 수 있다는 내용이군
