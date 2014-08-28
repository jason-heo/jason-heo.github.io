---
layout: post
title: "log4cxx에서 pid 출력하기"
categories: programming
---

[Log4cxx][1]는 태생이 [Log4J][2] 기반이기 때문에 [pid를 출력할 수 없다][3]. (Java는 이식성을 강조하기 때문에 *nix에서만 제공하는 process id 조회 기능이 없다.)

## `MDC`를 사용하는 방법

Log4cxx의 MDC(Mapped Diagnostic Context) 기능을 활용하면 원하는 값을 특정 이름으로 설정한 뒤에 log4cxx의 `ConversionPattern`에서 참조할 수 있다.

```cpp
log4cxx::MDC::put("PID", boost::lexical_cast<string>(getpid()));
```

`%X{}`를 활용하여 MDC에서 설정한 이름에 접급할 수 있다.

```
log4j.appender.C.layout.ConversionPattern=%X{PID} %m%n
```

## `#define`을 사용하는 방법

`MDC`를 사용하면 실행시킬 executable마다 모두 설정해야 하고 `fork()`되는 코드에 모두 설정을 해야 해서 불편할 수 있다. 본인의 경우 처음부터 `LOG4CXX_FATAL` 등을 `FATAL`로 define해서 사용 중이었기 때문에 다음과 같이 `define`을 변경해서 쉽게 해결했다.

```cpp
#define FATAL(message) LOG4CXX_FATAL(Logger::getLogger(""), getpid() << " " <<  message)
```

물론 pid가 코드에 하드 코딩된다는 단점이 있지만 본인의 경우 `ConversionPattern`을 자주 변경하지 않기 때문에 별 문제가 안 된다.

[1]: http://logging.apache.org/log4cxx/
[2]: http://logging.apache.org/log4j/2.x/
[3]: http://t210146.apache-logging-log4cxx-user.apachetalks.us/conversionpattern-to-print-process-id-and-name-in-log4cxx-t210146.html
