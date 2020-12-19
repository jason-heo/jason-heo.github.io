---
layout: post
title: "수행 시간 측정 (Scala, Python, Java)"
categories: "programming"
---

## 목차

- [개요](#개요)
- [Scala](#scala)
- [Python2](#python2)
- [Java](#java)

## 개요

일반적으로 code의 수행 시간을 측정할 때는 아래와 같은 방식을 사용한다.

```
start = time.now()

// some codes

end = time.now()

print("수행 시간:", (end - start))
```

함수 호출 1줄에 대해 3개의 라인을 추가해야해서 좀 번거롭다. 본 포스트에서는 좀 더 우아하게 수행 시간을 측정할 수 있는 방법을 소개한다.

## Scala

(참고 자료: [https://biercoff.com/easily-measuring-code-execution-time-in-scala/](https://biercoff.com/easily-measuring-code-execution-time-in-scala/))

우선 아래와 같은 함수를 만들자.

```scala
def elapsedTime[R](block: => R): R = {
    val s = System.currentTimeMillis
    val result = block    // call-by-name
    val e = System.currentTimeMillis
    println("[elapsedTime]: " + ((e - s) / 1000.0f) + " sec")
    result
}
```

함수의 argument는 좀 복잡하다. 함수 내부에서 하는 일은 start/end time을 저장하고 그 사이에 `val result = block`으로 argument로 전달받은 `block`을 수행하는 방식이다.

사용 방법은 다음과 같다.

우선 테스트용 함수 하나를 만들었다

```scala
def myFunction() = {
    val sleepInMs = (Math.random * 1000).toLong
    println(s"[myFunction] sleeping ${sleepInMs} ms")

    Thread.sleep(sleepInMs)
    println("[myFunction] ended")

    true
}
```

그리고 my_function()을 다음과 같이 실행시켜보자.

```scala
scala> elapsedTime(myFunction)
[myFunction] sleeping 286 ms
[myFunction] ended
[elapsedTime]: 0.287 sec
res18: Boolean = true
```

함수 수행 시간이 정확히 출력된 것을 볼 수 있다.

Scala 언어의 특성 상, `elapsedTime()`의 arugment로는 함수만이 아닌 임의의 코드 블럭이 전달 될 수 있다. (`elapsedTime()` 함수의 argument 이름부터가 `block`이다)

아래 코드처럼 if-else도 전달 가능하다.

```scala
scala> elapsedTime(if (true) Thread.sleep(1000) else Thread.sleep(500))
[elapsedTime]: 1.005 sec
```

line이 긴 경우 `{}`으로 여러 줄을 전달하면 된다.

```scala
scala> elapsedTime({
     |     val sleepInMs = (Math.random * 1000).toLong
     |     Thread.sleep(sleepInMs)
     | })
[elapsedTime]: 0.387 sec
```

`elapsedTime()`이 있어서 Scala에서 간단히 성능 측정할 때 매우 편한다

## Python2

(출처: [https://stackoverflow.com/a/3620972](https://stackoverflow.com/a/3620972))

(Python3는 잘 모르겠고) Python2에서는 Scala 같이 functional한 언어의 특성을 활용한 수행 시간 측정 방법은 없는 듯 하다.

젤 단순한 것은 아래처럼 시작시간/종료시간을 이용하는 것.

```python
>>> import time

>>> start_time = time.time()

>>> time.sleep(1)

>>> elapsed_time_in_ms = time.time() - start_time

>>> print (elapsed_time_in_ms / 1000), " ms took"
0.0207117118835  ms took
```

위의 코드는 너무 일반적이라 설명할 것은 없어보이고, 출처에 연결된 Stackoverflow를 보면 아래와 같은 코드가 있다.

```python
import random

from functools import wraps

PROF_DATA = {}

def profile(fn):
    @wraps(fn)
    def with_profiling(*args, **kwargs):
        start_time = time.time()
        ret = fn(*args, **kwargs)

        elapsed_time = time.time() - start_time

        if fn.__name__ not in PROF_DATA:
            PROF_DATA[fn.__name__] = [0, []]

        PROF_DATA[fn.__name__][0] += 1
        PROF_DATA[fn.__name__][1].append(elapsed_time)

        return ret

    return with_profiling

def print_prof_data():
    for fname, data in PROF_DATA.items():
        max_time = max(data[1])
        avg_time = sum(data[1]) / len(data[1])
        print "Function %s called %d times. " % (fname, data[0]),
        print 'Execution time max: %.3f, average: %.3f' % (max_time, avg_time)

def clear_prof_data():
    global PROF_DATA
    PROF_DATA = {}
```

code만 봐서는 `functools`의 `wraps`를 이용해서 함수 호출 시마다 특정 함수가 자동 호출되도록 한 것 같은데 필자가 Python 전문자가 아니라서 어떤 메커니즘으로 작동하는지는 잘 모르겠다.

사용 방법은 다음가 같다. 우선 테스트용 함수를 두 개 만들어 놓고

```python
@profile
def my_function():
    time.sleep(random.random())
    print "my_function() called"

@profile
def your_function():
    time.sleep(random.random())
    print "your_function() called"
```

함수를 여러 번 호출해보자. `my_function()`을 두 번, `your_function()`을 세번 호출했다.

```
>>> for cnt in xrange(2):
...     my_function()
...
my_function() called
my_function() called

>>> for cnt in xrange(3):
...     your_function()
...
your_function() called
your_function() called
your_function() called
```

함수별 호출 횟수와 수행 시간은 `print_prof_data()`를 실행하면 된다.

```python
>>> print_prof_data()
Function your_function called 3 times.  Execution time max: 0.197, average: 0.172
Function my_function called 2 times.  Execution time max: 0.868, average: 0.495
```

Scala는 임의 코드 블럭의 수행 시간을 볼 수 있지만, 위의 Python 방식으로는 어렵다. 하지만, 함수 단위의 수행 시간을 확인하긴 나쁘지 않아보인다.

사용 패턴에 따라서 함수 호출하고나머 매번 `print_prop_data()`로 출력하고, `clear_prof_data()`로 profile data를 호출하기 번거로울 수 있는데, `profile()` 내부를 수정하여 단순히 수행 시간을 출력만 하도록 고치면 될 것이다.


## Java

(참고 자료: [https://www.baeldung.com/java-measure-elapsed-time](https://www.baeldung.com/java-measure-elapsed-time))

Java도 기본적으로는 Python 처럼 시작시각/종료시각을 이용하는 방법이 있다.

```
long start = System.nanoTime();

// ...
// ...

long finish = System.nanoTime();
long timeElapsed = finish - start;
```

그런데, Apache Commons Lang3의 [StopWatch](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/time/StopWatch.html) 기능을 사용하면 좀 더 편하게 수행 시간을 측정할 수 있다.

우선 `pom.xml`에 아래 의존성을 추가하자.

```
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.7</version>
    </dependency>
```

StopWatch의 사용법은 아래와 같다.

```java
StopWatch watch = new StopWatch();

watch.start();

Thread.sleep((long) (Math.random() * 1000));

watch.stop();

System.out.println(String.format("%.3f sec took", watch.getTime()/1000f));

// 출력 결과
0.368 sec took
```

`StopWatch.split()` 기능을 사용하면 구간별 수행 시간을 측정하기도 편하다.

아래는 test code

```java
// 생성과 start를 동시에 해주는 편의 함수
StopWatch watch = StopWatch.createStarted();

watch.split();

// file을 다운로드한다
// ...
sleepInMs(1000);
System.out.println(String.format("[downloading] %d ms took\n", watch.getTime() - watch.getSplitTime()));

// 다운로드한 file을 parsing한다
// ...
watch.split();
sleepInMs(500);
System.out.println(String.format("[parsing] %d ms took\n", watch.getTime() - watch.getSplitTime()));

watch.split();
// 최종 결과를 사용자에게 출력한다
// ...
sleepInMs(2000);
System.out.println(String.format("[sending] %d ms took\n", watch.getTime() - watch.getSplitTime()));

watch.stop();
// toString()으로 하면 hour:minute:second.ms 형식으로 출력해준다
System.out.println(String.format("total %s took", watch.toString()));

void sleepInMs(long sleepAmount) throws InterruptedException
{
  System.out.println(String.format("sleeping %d ms", sleepAmount));
  Thread.sleep(sleepAmount);
}
```

출력 결과는 다음가 같다.

```
sleeping 1000 ms
[downloading] 1045 ms took

sleeping 500 ms
[parsing] 503 ms took

sleeping 2000 ms
[sending] 2005 ms took
total 00:00:03.554 took
```
