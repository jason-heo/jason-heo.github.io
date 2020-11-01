---
layout: post
title: "Scala의 Multi Threading"
categories: "programming"
---

Scala의 Multi Threading은 Java와 동일하다. 자주 사용되는 몇 가지만 정리해본다.

## 1. 동시성 자료구조

`java.util.concurrent`에는 multi thread를 위한 유용한 자료구조가 있다.

그 중에서 `LinkedBlockingQueue`는 다음과 같은 특징이 있다.

- queue의 capacity를 지정할 수 있음
- queue의 size를 constant 시간에 계산할 수 있음. 즉, time complexity가 `O(1)`
- 하지만 blocking queue이다
  - 즉, queue가 empty한 상황에서 `poll()`을 하면 thread가 block된다
  - timeout을 지정하면 blocking될 시간을 사용자가 설정할 수 있다
  - timeout 이후에 `poll()`은 `null`을 반환한다


`ConcurrentLinkedQueue`에는 다음과 같은 특징이 있다.

- non-blocking queue이다
  - 즉, queue가 empty한 상황에서 `poll()`을 하더라도 blocking되지 않는다
- 다만 queue의 capacity를 지정할 수 없다
- `size()` 함수의 time complexity는 `O(n)`이다
- 또한 `size()`의 결과는 부정확하다

`LinkedBlockingQueue`에 대한 샘플 코드는 다음과 같다.

```scala
package io.github.jasonheo

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

object ThreadTest {
  val queue: LinkedBlockingQueue[Integer] = new LinkedBlockingQueue[Integer](10)

  def main(args: Array[String]) = {
    println("====== poll() blocked when queue is empty ========")
    println(s"current ts=${System.currentTimeMillis() / 1000} second")

    println(s"queue.poll()=${queue.poll(5, TimeUnit.SECONDS)}")

    println(s"current ts=${System.currentTimeMillis() / 1000} second\n")

    println("====== put() & poll() =======")
    println(s"current ts=${System.currentTimeMillis() / 1000} second")

    queue.put(10)

    println(s"queue.poll()=${queue.poll(5, TimeUnit.SECONDS)} second")

    println(s"current ts=${System.currentTimeMillis() / 1000} second")
  }
}
```

출력 결과

```
====== poll() blocked when queue is empty ========
current ts=1604196357 second
queue.poll()=null
current ts=1604196362 second

====== put() & poll() =======
current ts=1604196362 second
queue.poll()=10 second
current ts=1604196362 second
```

즉, queue가 empty일 때 5초간 blocking한 것을 볼 수 있고, queue에 element가 존재하는 경우 `poll()`이 바로 return한 것을 볼 수 있다.

## 2. Main Thread와 Child Thread 간에 Data 교환

Thread가 class의 member variable인 경우 다른 member variable에 접근할 수 있다. `LinkedBlockingQueue`를 사용하는 경우 Thread간에 thread-safe한 방식으로 Data를 서로 주고 받을 수 있다.

```scala
package io.github.jasonheo

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import scala.util.Random

object ThreadTest {
  val queue: LinkedBlockingQueue[Integer] = new LinkedBlockingQueue[Integer](10)

  val thread = createThread()

  def main(args: Array[String]) = {

    thread.start

    while (true) {
      println(s"current ts=${System.currentTimeMillis()} milli second")
      println(s"queue.poll()=${queue.poll(100, TimeUnit.MILLISECONDS)}")
    }

    thread.stop
  }

  def createThread(): Thread = {
    val t = new Thread("myThread") {
      setDaemon(true)

      val rnd = Random
      override def run(): Unit = {
        while (true) {
          val random = rnd.nextInt(500) + 1
          queue.put(rnd.nextInt(random))

          Thread.sleep(random)
        }
      }
    }

    t
  }
}
```

출력 예)

```
current ts=1604197496974 milli second
queue.poll()=36
current ts=1604197496979 milli second
queue.poll()=null
current ts=1604197497083 milli second
queue.poll()=21
current ts=1604197497162 milli second
queue.poll()=133
current ts=1604197497197 milli second
queue.poll()=null
current ts=1604197497301 milli second
queue.poll()=null
current ts=1604197497404 milli second
queue.poll()=245
current ts=1604197497447 milli second
queue.poll()=null
...
```

## 3. 일정 주기마다 일을하는 Thread

예를 들어 '매 1분마다 queue의 size를 출력'하고 싶다고 하자. 이런 경우 `ScheduledExecutorService`를 이용하면 된다.

아래의 코드는 5초 단위로 queue의 size를 출력하는 예이다.

```scala
package io.github.jasonheo

import java.util.concurrent.{Executors, LinkedBlockingQueue, TimeUnit}

import scala.util.Random

object ThreadTest {
  val queue: LinkedBlockingQueue[Integer] = new LinkedBlockingQueue[Integer](100)

  val thread = createThread()

  val printQueueSize: Runnable = new Runnable {
    override def run(): Unit = {
      println(s"current ts=${System.currentTimeMillis() / 1000} second")
      println(s"queue.size=${queue.size}")
    }
  }

  val queueSizePrinter = Executors.newScheduledThreadPool(2)
  queueSizePrinter.scheduleWithFixedDelay(printQueueSize, 0, 5, TimeUnit.SECONDS)

  def main(args: Array[String]) = {

    thread.start

    while (true) {
      queue.poll()

      Thread.sleep(25)
    }

    thread.stop
  }

  def createThread(): Thread = {
    val t = new Thread("myThread") {
      setDaemon(true)

      val rnd = Random
      override def run(): Unit = {
        while (true) {
          val random = rnd.nextInt(50) + 1
          queue.put(rnd.nextInt(random))

          Thread.sleep(random)
        }
      }
    }

    t
  }
}
```

출력 결과

```
current ts=1604199242 second
queue.size=1
current ts=1604199247 second
queue.size=2
current ts=1604199252 second
queue.size=5
current ts=1604199257 second
queue.size=2
current ts=1604199262 second
queue.size=6
current ts=1604199267 second
queue.size=14
current ts=1604199272 second
queue.size=8
current ts=1604199277 second
queue.size=13
current ts=1604199282 second
queue.size=1
current ts=1604199287 second
queue.size=0
current ts=1604199292 second
queue.size=5
...
```
