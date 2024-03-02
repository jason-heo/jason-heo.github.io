---
layout: post
title: "TIL - 2024.03.02 - Obsidian Android git sync"
categories: "til"
---

### 들어가며

동기화 측면에서 노션이 훨씬 편하지만, 맥에서 구름입력기를 사용하는 나로서는 노션 에디터가 너무 불편하였다.

관련 이슈는 다음과 같다.

- https://github.com/gureum/gureum/issues/880
- https://www.clien.net/service/board/cm_mac/15864494

요 부분이 너무 불편해서 Obsidian으로 넘어가볼까 하고 Android에서 git sync를 해봤다.

마치 20여년 전에 한참 리눅스 공부하며 이런 저런 프로그램 설치하는 생각이 들어 재미있었다 (라고 적었지만 사실 귀찮기도 하였다)

### 요약

- Android에 Termux 설치
- git sync script 작성
- sync 방식에 따른 추가 설정
  - widget 방식: Termux-Widget 사용
  - cron 방식: Termux-Tasker 사용

### Android에 설치해야할 apk 파일들

- Termux
  - Android 용 terminal
  - https://f-droid.org/en/packages/com.termux/
  - 주의 사항: f-droid로 설치하니깐 부가적인 것이 많이 설치된다
    - 화면 하단에서 apk file만 설치 가능하다
- Termux-Widget
  - git sync용 shell script를 Android 홈 화면 위젯으로 실행시킬 수 있게 한다
    - 즉, 사람이 위젯을 터치할 때만 sync가 된다
  - https://f-droid.org/en/packages/com.termux.widget/
  - 주의 사항: github에서 download받은 apk 파일은 설치 시 에러가 발생했다
- Termux-Tasker
  - cron와 유사해보인다
    - 즉, 자동으로 주기적으로 git sync용 shell script를 sync할 수 있다
  - https://f-droid.org/en/packages/com.termux.tasker/

동기화 trigger 방식은 Termux-Widget과 Termux-Tasker 두개 중이 하나를 선택하면 된다. 본인은 Widget 방식을 선택하였다.

Termux-Widget을 사용하는 경우 Termux-api도 필요하다. 이건 apk로 설치하지 않고 Termux 내에서 `pkg install termux-api`를 실행하면 된다.

### 그외 설정

사실 위의 apk를 설치하는 게 제일 어려웠고 나머지는 reddit 글을 따라하면 된다.

### 참고 자료

- https://www.reddit.com/r/ObsidianMD/comments/110om69/guide_obsidian_git_sync_on_app_opening_android/
- https://www.clien.net/service/board/lecture/18149332
- https://velog.io/@qkqkwl147/Obsidian-git-sync-동기화
- https://meeco.kr/Review/37397353
- https://forum.obsidian.md/t/guide-using-git-to-sync-your-obsidian-vault-on-android-devices/41887
