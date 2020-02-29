---
layout: post
title: "Python subprocess.Popen()을 이용한 exit code, stdout, stderr 캡처"
categories: "python"
---

{% include python.md %}

첫 번째로는 `subprocess.Popen()`인데, 이걸 사용하면 내가 python에서 호출한 외부 스크립트의 exit code, stdout, stderr 내용을 변수에 쉽게 저장할 수 있다.

또한 다른 장점으로 command과 argument들을 array의 원소로 전달하다보니 따옴표 등을 escape 처리가 쉽고 command의 가독성이 증가한다는 장점이 있다.

`poll()` 함수가 있어서 async mode로 명령을 실행시킬 수도 있다.

python2, python3 모두에서 사용할 수 있다.

간략한 사용예) - 아래는 Python3 예제인데 `print()`와 f-string을 제외하면 Python2에서도 그대로 사용 가능하다 (물론 Python2에서도 `print()`와 f-string 사용하는 방법이 있긴 하다)

```python
>>> cmd_arr = ["echo", "hello, world"]

>>> child = subprocess.Popen(cmd_arr, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

>>> (stdout, stderr) = child.communicate()

>>> print(f"child.returncode='{child.returncode}'")
child.returncode='0'

>>> print(f"stdout='{stdout}'")
stdout='b'hello, world\n''

>>> print(f"stderr='{stderr}'")
stderr='b'''
```

stdout의 출력 결과물을 보면, `b'hello, world\n'` 처럼 binary인 것을 알 수 있다.

한글을 출력해보면 아채처럼 utf8 코드 값이 보인다.

```python
>>> cmd_arr = ["echo", "안녕하세요"]

>>> child = subprocess.Popen(cmd_arr, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

>>> (stdout, stderr) = child.communicate()

>>> print(f"stdout='{stdout}'")
stdout='b'\xec\x95\x88\xeb\x85\x95\xed\x95\x98\xec\x84\xb8\xec\x9a\x94\n''
```

이때는 utf8로 decode를 해서 출력하면 된다.

```python
>>> cmd_arr = ["echo", "안녕하세요"]

>>> child = subprocess.Popen(cmd_arr, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

>>> (stdout, stderr) = child.communicate()

>>> print(f"stdout='{stdout.decode('UTF-8')}'")
stdout='안녕하세요
'
```
