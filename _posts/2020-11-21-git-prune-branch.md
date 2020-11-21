---
layout: post
title: "[git] stale local branch 삭제하기"
categories: "programming"
---

git을 사용하다보면 remote에서도 삭제되었고 local에서 `git branch`로 보이지 않는데, `git branch -a`로 하면 보이는 branch가 있다.

이런 것들을 stable local branch라고 부르는 것 같다.

즉, `git branch -a`로 출력했을 때 `remotes/origin/{branch}`처럼 보여지는 branch 중 이미 remote에서 삭제된 branch를 의미한다.

stale local branch가 쌓이면 `git` 명령의 자동 완성을 사용할 때 번거롭다.

이를 삭제하기 위해선 아래의 명령을 수행하면 된다.

```
git remote prune origin
```

참고: https://stackoverflow.com/a/26478292/2930152
