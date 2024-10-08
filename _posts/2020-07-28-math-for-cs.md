---
layout: post
title: "Mathematics for Computer Science"
categories: "programming"
---

Text book: https://cs121.boazbarak.org/LehmanLeighton.pdf

시청해둔 내용 정리해두면 나중에 시간이 지난 후에 '내가 한때는 이런 것도 봤었구나'하고 시청했다는 사실 정도는 기억하겠지.

### 1강: Introduction and Proofs

- https://www.youtube.com/watch?v=L3LMbpZIKhQ&list=PLB7540DEDD482705B&index=1
- 시청일: 2020.07.20.(월)
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

- https://www.youtube.com/watch?v=z8HKWUWS-lA&list=PLB7540DEDD482705B&index=2
- 시청일: 2020.07.25.(토)
- Proof by Contradiction
    - root 2가 무리수임을 증명하기
    - 증명 방법: root 2가 유리수인 경우 모순에 빠지는 것을 보여줌으로서 무리수인 것을 증명한다
- 가우스 합이 true임을 증명
    - 1부터 n까지의 합 = n\*(n+1)/2
    - 이게 참임을 Induction 방식으로 증명한다
- 90 > 92
    - 이거 교수님 그림 때문에 헷갈려서 '왜 이게 성립되지?'하고 한참을 헤맸다;;

### 3강: Strong Induction

- https://www.youtube.com/watch?v=NuGDkmwEObM&list=PLB7540DEDD482705B&index=3
- 시청일: 2020.07.27.(월)
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

- https://www.youtube.com/watch?v=NuY7szYSXSw&list=PLB7540DEDD482705B&index=4
- 시청일: 2020.08.02.(일)
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

### 5강: Number Theory II

- https://www.youtube.com/watch?v=XX7ePR21Ook&list=PLB7540DEDD482705B&index=5
- 시청일: 2020.08.04, 2020.08.06, 2020.08.08
    - 총 3일에 걸쳐서 시청했다
    - 강의 보면서 필기하랴, 모르는 용어는 검색하랴, 검색하다보면 잠시 다른 웹 서핑하랴 진도가 안나간다
    - 사실 이해가 잘 안 되는 용어 및 수식이 있어서 이번엔 비디오보다는 text book을 주로 확인했다
- A4 종이에 노트는 많이 했는데 옮겨적기가 어려워서 대략적인 흐름만 적는다
- textbook에는 이미테이션 게임이라는 영화로도 소개된 앨런 튜링 이야기가 나온다
- 수업의 주요 내용은 정수론을 이용하여 암호를 만들고 해독하는 것에 대한 이야기
- Turing's code v1
    - encryption: `m' = m*k`
    - decryption: `m =m'/k=(m*k)/k`
    - 문제점: 암호화된 메시지 두 개를 얻는 경우 k를 쉽게 알아낼 수 있다. (k=gcd(m1', m2'))
- Turing's code v2
    - encryption: `m'=mk rem p`
- congruent
    - Turing's code v2의 decryption을 위해선 많은 정수론 개념이 필요하다
    - 그 중 하나가 congruent
    - gauss가 최초로 정의한 개념
    - 31≡16(mod 5), 즉, 31과 16은 '5로 나눈 나머지' 관점에서 congruent하다. 왜냐? (31 mod 5) = (16 mod 5)이기 때문
    - 시계로 이야기를 해보자. 11시와 35시는 congruent하다. 35시도 24로 나머지 연산을 하면 11시가 된다
    - 참고 자료: https://science.jrank.org/pages/4772/Number-Theory-Gauss-congruence.html
- multiplicative inverse (곱의 역원)
    - real number(실수)에서는 integer에 없는 재미있는 성질이 있다
    - r에 대해서 곱셈에 대한 inverse가 있다는 것인데
    - 즉, r 곱하기 (1/r)은 1이다
    - 하지만, integer에는 이런 성질이 없다
    - 그런데 congruent에 대해선 multiplicative inverse가 존재한다
    - 2의 '5의 나머지'에 대한 multiplicative inverse는 3이다
    - `2*3≡1(mod 5)`가 된다
- 페르마의 소정리
    - k^(p-1) congruent to 1(mod p), p는 소수, k는 p의 배수가 아님
    - 이걸 이용해서 multiplicative inverse를 빠르게 구할 수 있다는 이야기인듯하다
    - 그리고 이걸 이용해서 Turing's code V2의 k를 구할 수 있으므로, 암호를 해석할 수 있다는 이야기인 듯
    - 잘 이해가 안 되서 [경희대 이상준 교수님의 강의](https://youtu.be/sWJtKpYNzk0?t=848)를 참고했다
        - 이 분도 강의를 쉽고 재미있게 하시네 
- Turing's code v2
    - decryption: multiplicative inverse를 이용한다(라고 강의를 이해했다)
        - `m = m'*k^-1 rem p`
        - 강의 들을 때는 이해한 것 같았는데 정리하려니 무슨 말인지 또 모르겠다
    - 문제는 `k^-1`을 빠르게 알아오는 방법이 있어야 한다
        - 이건 페르마의 소정이를 이용한다
    - 비밀키 k를 알아오는 방법: 이것도 페르마의 소정리를 이용한다
        ```
        m*(p^−2) ≡ m^(p-2)*mk (mod p)
                 ≡ m^(p-1)*k (mod p)
                 ≡ k (mod p)
        ```
### 6강: Graph Theory 1

최초 목표는 일주일에 두 개 강의 시청이었다가, 5강부터 일주일에 1개로 줄었다.

그런데 6강부터는 일주일에 1개도 못보고 있다. 아무래도 업무 때문에 밤 늦게까지 일하고 주말에도 업무 관련 공부를 했던 영향이 컸다. 이번주에는 공부에도 좀 더 투자를 해야겠다.

- https://www.youtube.com/watch?v=h9wxtqoa1jY&list=PLB7540DEDD482705B&index=6
- 시청일: 2020.08.10(월), 2020.08.11(화), 2020.08.18(화)
- 그래프에 대한 용어부터 다시 배웠다
    - adjcent
    - incident
- graph coloring 문제에 대해서 공부를 했다
    - 일단 greedy한 방법
- graph coloring을 응용한 문제도 알아봤다
    - mit 출신이 만든 기업이 akamai라는 회사에서 서버 7.5만대를 서비스에 영향없이 restart하는 방법
    - 통신 회사에서 주파수 간섭을 최소화하기 위해 송신기에 주파수를 배정하는 방법

