---
layout: post
title: "virtualbox 설치 시 에러. (Win7 64bit host). (Installation failed! Error: 파일 이름, 디렉터리 이름 또는 볼륨 레이블 구문이 잘못되었습니다.)"
date: 2014-03-06 21:34:00
categories: computer
---

virtualbox 좀 설치해서 간만에 (근 10년만) 리눅스를 데스크탑 용도로 좀 써 볼까 했는데...

위 제목과 같은 에러가 계속 발생했다.

Host OS은 윈도7 64bit.....

업무도 바쁘고 해서 3일만에 해결책을 찾았는데...

http://cats-eye.tistory.com/213

여기를 참고했음.

    C:\>VirtualBox-4.0.4-70112-Win.exe -x -p c:\vbox

참고로 본인은 위에서 -p c:\vbox를 주면 안 되었고, 그냥 -p vbox 하면, 현재 위치에 vbox가 잘 생성되었음.

그 뒤에는 생성된 폴더에서 msi 파일을 실행시키면 됨.
