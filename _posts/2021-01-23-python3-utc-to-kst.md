---
layout: post
title: "Python 3.6에서 UTC를 KST로 변경하기"
categories: "python"
---

test에 사용된 python 버전: 3.6.5

어느 언어가 되었든 날짜 연산, Timezone 변환은 어려운 것 같다. 컨퍼런스 참석을 위해 영국을 갔을 때 그리니치 천문대 근처에 간적이 있었다. 소위 GMT의 G가 Greenwich의 약자이다. 영국에 모인 세계 개발자들과 대화 중에 Timezone 변환이 싫다는 의미로 우스개 소리로 "나는 그리니치 천문대가 싫어요"라고 이야기했는데 영어가 짧아서 그 이유는 설명하지 못한 슬픈 일화가 있다.

암튼 UTC로 표현된 datetime을 KST로 변환하려면 아래와 같은 `astimezone()`을 호출하면 된다.

우선 UTC로 datetime 변수 하나를 만들어보자.

현재 시각은 15시 45분이다.

```python
>>> birthdate = datetime.datetime.now(datetime.timezone.utc)

>>> birthdate
datetime.datetime(2021, 1, 23, 6, 45, 52, 851781, tzinfo=datetime.timezone.utc)

>>> birthdate.strftime('%Y-%m-%d %H:%M:%S')
'2021-01-23 06:45:52'
```

`strftime()`의 출력 결과를 보면 알겠지만, `06:45:52` 처럼 KST에서 9시간을 뺀 값이 출력되었다.

이를 KST로 변환하려면 아래처럼 `astimezone()`을 호출하면 된다.

```python
>>> birthdate.astimezone().strftime('%Y-%m-%d %H:%M:%S')
'2021-01-23 15:45:52'
```

더 좋은 점은 시간대를 명시하는 것이다. format에서 `%Z`를 출력하면 timezone을 출력한다.

```python
>>> birthdate.astimezone().strftime('%Y-%m-%d %H:%M:%S %Z')
'2021-01-23 15:45:52 KST'
```

`KST` 대신 timezone offset을 출력하고 싶은 경우 `%z`를 입력한다.

```python
>>> birthdate.astimezone().strftime('%Y-%m-%d %H:%M:%S %z')
'2021-01-23 15:45:52 +0900'
```

`+09:00` 처럼 colon이 출력되어야 표준인데 Python 3.6의 버그인지 의도한 것인지 모르겠다. Python 3.7에서는 colon이 잘 추가되는 듯 하다 (python [issue 31800](https://bugs.python.org/issue31800) 참고).

참고로 Java는 `XX`는 colon이 없는 것이고 `XXX`는 colon을 추가하는 포맷으로 알고 있다.

(참고 자료: https://stackoverflow.com/a/46339491/2930152)

