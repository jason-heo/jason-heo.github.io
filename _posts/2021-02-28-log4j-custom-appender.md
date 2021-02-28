---
layout: post
title: "scala에서 log4j custom appender 구현하기"
categories: "programming"
---

벌써 2021년 2월의 마지막날이다. 2월에는 다른 공부를 하느라 블로그에 글을 올리지 못했다.

업무적으로 custom appender를 사용할 일이 있을 듯 하여 공부한 내용을 기록한다.

log4j와 log4j2의 custom appender를 어떻게 구현할 수 있는지 테스트해봤다.

- log4j
    - 참고 자료: https://gist.github.com/kengelke/4664612
    - 잘 돌아간다
- log4j2
    - 참고 자료: https://www.baeldung.com/log4j2-custom-appender
    - 뭐가 문제인지 제대로 돌아가지를 않는다

log4j용 custom appender 소스 코드는 다음과 같다.

실제 작동되는 소스 코드는 [log4j-custom-appender](https://github.com/jason-heo/log4j-custom-appender)에 올려두었다.

- Appender class
    ```scala
    class MyAppender extends AppenderSkeleton {
      override def append(event: LoggingEvent): Unit = {
        println(s"[${event.getLevel}][${event.timeStamp}] ${event.getMessage.toString}")

        if (event.getThrowableInformation != null) {
          event.getThrowableInformation.getThrowable.printStackTrace()
        }
      }

      override def close(): Unit = {

      }

      override def requiresLayout(): Boolean = {
        false
      }
    }
    ```
- `log4j.properties` 내용
    ```
    log4j.rootLogger=INFO, myappender
    log4j.appender.myappender=io.github.jasonheo.log4j.MyAppender
    ```
- test program
    ```scala
    object Main {
      def main(args: Array[String]): Unit = {
        val logger: Logger = Logger.getLogger(getClass.getName)

        logger.info("hello info")
        logger.debug("hello debug") // 출력되지 않는다
        logger.warn("hello warn")

        try {
          throw new IllegalStateException("exception thrown")
        }
        catch {
          case e: Throwable => {
            logger.error("hello exception", e)
          }
        }
      }
    }
    ```
- 출력 결과
    ```
    [INFO][1614495330582] hello info
    [WARN][1614495330999] hello warn
    [ERROR][1614495330999] hello exception
    java.lang.IllegalStateException: exception thrown
        at io.github.jasonheo.log4j.Main$.main(Main.scala:34)
        at io.github.jasonheo.log4j.Main.main(Main.scala)
    ```
