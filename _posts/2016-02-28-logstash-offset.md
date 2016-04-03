---
layout: post
title: "Logstash의 offset 관리 기법"
categories: elasticsearch
---

Logstash는 Logcollector로서 다양한 Input/Output Plugin을 지원하기 때문에 손쉽게 로그 취합 시 편하게 작업할 수 있다.

예를 들어, Logstash 2.3에서는 20개 이상의 [Output Plugin](https://www.elastic.co/guide/en/logstash/current/output-plugins.html)이 지원되고 있는데, Log를 Elasticsearch에 싶으면 Elasticsearch Plugin을 사용하면 되고, Configuration만 바꾸면 손 쉽게 Kafka에도 저장할 수 있다. 즉, 직접 Kafka Producer를 개발하지 않아도 되는 것이 큰 장점 중의 하나이다.

편리함 때문에 Logstash를 도입하거나 도입을 테스트하는 곳이 늘어나고 있는데, Logstash의 Offset 기법을 제대로 파악하지 못하는 경우, 일부 로그가 유실될 수 있다.

따라서 본 글에서는 Logstash의 Offset 관리 기법에 대해서 적어볼까 한다.

start_position
--------------

Logstash를 처음 사용할 때 제일 헷갈리는 것이 [`start_position`](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-file.html#plugins-inputs-file-start_position)이다. 이름에서 알 수 있다시피, Lostash를 시작할 때 file의 어디서부터 읽을지를 결정하는 설정이며, 다음과 같은 2가지를 지원한다.

- beginning
- end (default)

주의할 점은 이 설정은 file을 **최초** 읽을 때만 적용된다는 점이다. 실수하기 쉬운 것이, `start_position => "beginning"`인 경우 Logstash를 여러 번 restart하더라도 항상 동일한 내용이 출력될 것이라 생각하는게 그렇지 않다.

since_db
-------

Logstash가 최초 시작될 때는 file을 읽을 위치가 `start_position`에 의해 결정되지만, 이후 실행시에는 sincedb에 저장된 offset부터 읽기 시작한다. 아래의 예를 보자.

```
$ cat ~/.sincedb_a8ae6517118b64cf101eba5a72c366d4 
22253332 1 3 14323
```

각각의 항목은 다음과 같은 것을 의미한다.

1. 첫 번째 필드: file의 inode
1. 두 번째 필드: file system의 major device number
1. 세 번째 필드: file system의 minor device number
1. 네 번째 필드: current offset

즉, 위의 예에서 22253332번 파일은 현재 14323번 offset까지 처리가 되었다는 것을 의미한다. 이 상태에서 Logstash를 재실행하는 경우 `start_position` 값과 상관없이 14323 offset부터 file을 읽기 시작한다.

Logstash를 테스트하는 동안에는 이 sincdedb 때문에 테스트가 좀 번거롭다. 이 경우 (sincedb_path)[https://www.elastic.co/guide/en/logstash/current/plugins-inputs-file.html#plugins-inputs-file-sincedb_path)를 /dev/null로 설정하면 된다.

```
input {
    file {
        path => "/path/to/file"
        start_position => "beginning"
        sincedb_path => "/dev/null"
    }   
}
```

Log Rotate 사용 시, Log 유실이 없으려면?
------------------------------

Logstash는 Input file이 Rotate되더라도 로그를 잘 수집한다. 단, 유실이 없으려면 Logstash의 특성을 잘 이해해야 한다. (Log rotate에는 move 방식과 copy & truncate 방식 2가지가 있는데 Logstash는 두 가지 모두 잘 작동한다)

[Logstash Manaual](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-file.html#_file_rotation)을 읽어보면 File rotation에 대해서 다음과 같이 설명되어 있다.

> File rotation is detected and handled by this input, regardless of whether the file is rotated via a rename or a copy operation. To support programs that write to the rotated file for some time after the rotation has taken place, include both the original filename and the rotated filename (e.g. /var/log/syslog and /var/log/syslog.1) in the filename patterns to watch (the path option). Note that the rotated filename will be treated as a new file so if start_position is set to beginning the rotated file will be reprocessed.
> With the default value of start_position (end) any messages written to the end of the file between the last read operation prior to the rotation and its reopening under the new name (an interval determined by the stat_interval and discover_interval options) will not get picked up.

위 말을 쉽게 이해하는 사람이면 본 문서를 더 이상 읽을 필요가 없다.

우선 Logstash 설정 2개에 대해서 알고 있어야한다. 성능 향상을 위하여 Logstash는 다음과 같은 설정 2개를 사용한다.

- [stat_interval](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-file.html#plugins-inputs-file-stat_interval)
    - 파일이 갱신되었는지 확인하는 주기이다. 기본 값은 1초이다. 
- [discover_interval](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-file.html#plugins-inputs-file-discover_interval)
    - file pattern에 맞는 file이 생성되었는지 확인하는 주기이다.

위 두 가지 변수와 `start_position`의 의미만 알고 있다면 영문 메뉴얼의 설명을 쉽게 이해할 수 있다.

### Log rotate 시나리오에서 Logstash 작동 방식

Log rotate를 위해 다음과 같이 file path에 pattern을 주었다고 가정한다.

```
path => "/var/log/syslog*"
```

|시간|이벤트|행동|
|----|----|
|t초|`stat_interval` 경과|`/var/log/syslog`에서 sincedb에 기록된 offset 이후의 내용을 읽는다.|
|t+1초|N/A|`/var/log/syslog`에 신규 로그가 쌓인다|
|t+2초|Log Rotate|`/var/log/syslog`가 `/var/log/syslog.1`로 rename됨 (inode가 변하지 않았으므로 Logstash 입장에서는 신규 파일이 아니다.|
|t+3초|Log Rotate|`/var/log/syslog` 재생성 (신규 파일이 생성된 것이다.)|
|t+4초|N/A|`/var/log/syslog`에 신규 로그가 쌓이기 시작한다.|
|t+5초|`stat_interval` 경과|`/var/log/syslog.1`의 since에 기록된 offset 이부터 읽기 시작함. 즉, t+1초부터 쌓인 로그는 유실없이 처리됨|
|t+6초|`discover_interval` 경과|`/var/log/syslog`가 새로 생긴 것을 인지한 뒤 `start_position`부터 읽기 시작함|

### `start_position => "end"`인 경우 유실 가능성 존재

Logstash가 새로 생긴 `/var/log/syslog`를 최초 읽는 시점은 t+6이다. 이때 `start_position => "end"` 이므로 (t+4)초부터 (t+6)초 사이에 쌓인 로그는 유실된다.

따라서 `start_position => "beginning"`으로 설정해야만 유실없이 사용할 수 있다.

기타 중요한 설정
---------

### sincedb_write_interval

비교적 쉽게 이해할 수 있는 설정인데, Logstash가 처리 중인 offset을 얼마의 주기로 sincedb에 기록할지 지정하는 설정이며 기본 값은 15초이다. start_position, stat_interval, discover_interval은 '유실 없는 처리' 때문에 중요하다면 sincedb_write_interval은 중복 유입에 관련된 설정이기 때문에 중요하다.

예를 들어 t초 sincedb에 offset 1000을 기록 후, (t+14)초에 offset 2000까지 읽은 뒤, Logstash가 죽었다고 하자. sincedb에는 1000이 적혀 있으므로 Logstash를 restart하는 경우 1001~2000까지는 중복 처리된다.

match되는 파일이 복수 개일 때, 처리 방식
--------------------------

file이 1개일 때 log는 sequential하게 시간 순으로 처리되지만, file이 여러 개인 경우 처리되는 순서는 시간 순인 것을 보장하지 않는다.

예를 들어 다음과 같은 파일 A, B가 있다고 가정하자.

```
+-------------+  +-------------+
|file A의 내용|  |file B의 내용|
+-------------+  +-------------+
| t1-A        |  | t1-B        |
| t2-A        |  | t2-B        |
| t3-A        |  | t3-B        |
| t4-A        |  | t4-B        |
| t5-A        |  | t5-B        |
+-------------+  +-------------+
```

t1, t2, ..., t5을 의미한다(즉, t1이 t5보다 빠르다). 이때 Logstash를 실행시켜서 stdout으로 출력하면 어떤 순으로 출력될까?

1. `t1-A` => `t1-B` => `t2-A` => `t2-B` ... => `t5-A` => `t5-B`
1. `t1-A` => `t2-A` => `t3-A` ... => `t3-B` => `t4-B` => `t5-B`

정답은 2번이다. 즉, Logstash output이 Cassandra인 경우 Cassandra 입장에서는 로그가 시간 순으로 입력되지 않는다. Time series log를 column에 저장하는 경우 순서가 뒤죽박죽 될 수 있다.
