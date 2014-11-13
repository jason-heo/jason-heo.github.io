---
layout: post
title: "cron에서 메일 보낼 때 본문이 file attach되는 현상 수정하기"
categories: programming
---

monitoring 내용을 email로 전송하는 쉘 스크립트가 있고, 이를 cron에 등록하여 실행 중이다. 문제는 쉘 스크립트를 shell에서 실행하면 email의 본문이 메일에 잘 출력되지만 cron을 통해서 실행하면 메일 본문이 file attach가 된다는 것이었다!

역시나 열심히 검색을 해 봐야지. 첨에 검색된 것은 헛소리여서 시간만 빼앗겼고, 역시 [Stackoverflow에서 검색된 문서][1]를 보고 해결하였다.

shell에서 잘 수행되던 것이 cron에서 실행될 때 잘 안 돌아간다면 첫 번째 원인은 쉘 환경 변수를 의심해 봐야 한다. 이번 문제도 `LANG`설정이 `en_US`로 설정되었기 때문이며 쉘 스크립트 안에서 `export LANG=ko_KR.utf8`을 설정하여 해결하였다.

메일보내는 프로그램으로 `mail`을 사용하는데 email 원문 보기해 보면 이 녀석이 `Heirloom mailx`라고 하는 프로그램인데 메일 본문 내용 중 해석할 수 없는 문자열이 있으면 binary 파일로 인식하여 attach를 시킨다고 한다.

[1]: http://stackoverflow.com/questions/18999690/use-crontab-job-send-mail-the-email-text-turns-to-an-attached-file-which-named
