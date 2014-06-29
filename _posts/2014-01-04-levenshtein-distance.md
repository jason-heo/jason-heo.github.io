---
layout: post
title: "Levenshtein distance"
date: 2011-05-11 
categories: programming
---

"Levenshtein distance" 한국 말로하면 "리벤슈타인 거리"가 되겠습니다. 이런 것이 존재하는지 지난 주에 업무 상, 처음 알게 되었습니다. 오늘 해당 글에 대한 wikipedia의 내용을 번역하고 있었는데, 반쯤 번역하다보니 점점 어려운 내용이 나오고 한국어로 번역하기도 어렵군요.... ㅎㅎ; 아.. 알고리즘 분야는 번역하기가 너무 어려워요.

암튼 새로운 것을 알았으니 정리할 겸 몇자 적습니다.

Levenshtein distance는 두개의 문자열을 이용하여, 한 문자열에의 몇번 연산 (insert/delete/substitution)을 해야 다른 문자열과 동일해 지나 하는 편집 거리(Edit distance)입니다.

예를 들어, "kitten"과 "sitting"의 경우 Levenshtein distance는 3인데 그 이유는 다음과 같습니다.

kitten → sitten (substitution of 's' for 'k')
sitten → sittin (substitution of 'i' for 'e')
sittin → sitting (insertion of 'g' at the end).

유닉스의 diff도 대략 비슷한 계열 같습니다. 알고리즘은 다이내믹 프로그래밍을 사용하는데 아마 프로그래밍 경진대회에도 옛날에 자주 나왔던 문제 같습니다. 저는 알고리즘할 때 휴학을 2번하느라 잘 못 배웠고 나중에 혼자 공부해보려는데 잘 안 되네요 ㅎㅎ;

psuedo code는 인터넷에 많이 나와 있습니다. (제가 적는 글 또한 인터넷의 정보만 늘리는 셈인가;;) PHP에서는 기본으로 제공되는 문자열 함수입니다.  성능이 이슈인듯 하네요... 단순히 2개의 문자열 사이의 거리를 계산하는 것은 별 문제 없겠지만, 예를 들어 사전 어플에서 사용자가 입력한 문자열에 오타가 있다.. 이 경우 제일 가까운 단어를 표시해 주고자 할 때 Levenshtein distance를 이용한다면 (실제 이렇게 사용하는지는 모르겠습니다.) 2개 문자열만 거리를 계산하는 것이 아니라 사용자가 입력한 1개의 단어와 사전에 있는 많은 단어와 거리 계산을 해야 할 것입니다.

영문 위키를 보면 다음과 같은 성능 개선에 대한 이야기가 나오고 있습니다.

## Possible improvements

Possible improvements to this algorithm include:

- We can adapt the algorithm to use less space, O(min(n,m)) instead of O(mn), since it only requires that the previous row and current row be stored at any one time.
- We can store the number of insertions, deletions, and substitutions separately, or even the positions at which they occur, which is always j.
- We can normalize the distance to the interval [0,1].
- If we are only interested in the distance if it is smaller than a threshold k, then it suffices to compute a diagonal stripe of width2k+1 in the matrix. In this way, the algorithm can be run in O(kl) time, where l is the length of the shortest string.[2]
- We can give different penalty costs to insertion, deletion and substitution. We can also give penalty costs that depend on which characters are inserted, deleted or substituted.
- By initializing the first row of the matrix with 0, the algorithm can be used for fuzzy string search of a string in a text.[3] This modification gives the end-position of matching substrings of the text. To determine the start-position of the matching substrings, the number of insertions and deletions can be stored separately and used to compute the start-position from the end-position.[4]
- This algorithm parallelizes poorly, due to a large number of data dependencies. However, all the cost values can be computed in parallel, and the algorithm can be adapted to perform the minimum function in phases to eliminate dependencies.
- By examining diagonals instead of rows, and by using lazy evaluation, we can find the Levenshtein distance in O(m (1 + d)) time (where d is the Levenshtein distance), which is much faster than the regular dynamic programming algorithm if the distance is small.[5]

## Levenshtein distance와 비슷한 것들

Levenshtein distance와 비슷한 matric으로 다음과 같은 것들도 있다고 합니다.

- length of longest common subsequence : 최장 공통 부분수열의 길이를 이용하는 방법. insert/delete만 있고 substitution은 없다고 하는 군요.
- Damerau-Levenshtein distance : Levenshtein distance에서 인접한 두 문자를 서로 순서를 바꾸는 연산까지 포함합니다.
- Hamming distance : insert와 delete가 없고, 대치만 가능합니다. 따라서 길이가 동일한 두 문자열에 대한 척도입니다.
