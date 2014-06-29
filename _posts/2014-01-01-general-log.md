---
layout: post
title: "MySQL General Log"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/19834997/how-to-see-the-querie-logs-of-mysql-in-windows-8-1/19835171#19835171

## 질문

MySQL에서 수행되는 모든 Query들을 로깅하고 싶다. 어떤 방법이 있는가?

{% include adsense-content.md %}

## 답변

MySQL은 General Log라는 기능을 제공하는데, General Log를 이용하여 MySQL에서 수행 중인 모든 Query를 로깅할 수 있다. General Log는 MySQL이 실행 중이더라도 켜거나 끌 수 있어서, 디버깅 할 때 유용하게 사용된다. 단, 질의가 너무 많은 서버에서 General Log를 켜 두면 모든 Query가 로그 파일에 기록될 수 있으므로 디스크가 금방 찰 수 있으므로 주의하자.

다음 질의를 이용하여 현재 General Log 기능이 켜진 상태인지, 로그가 저장되는 파일은 어디인지 조회할 수 있다.

```sql
mysql> SHOW VARIABLES LIKE '%general%';
+------------------+---------------------------------------+
| Variable_name    | Value                                 |
+------------------+---------------------------------------+
| general_log      | OFF                                   |
| general_log_file | /path/to/log                          |
+------------------+---------------------------------------+
```

"general_log = OFF"는 현재 로깅이 꺼진 것을 의미한다. MySQL이 실행 중인 경우 다음과 같이 General Log 기능을 켤 수 있다.

```sql
mysql> SET GLOBAL general_log  = 'ON';
Query OK, 0 rows affected (0.00 sec)
```

로그가 저장되는 위치는 /path/to/log이며 "SET GLOBAL general_log_file = '/tmp/query.log'" 식으로 위치를 변경할 수 있다. 윈도 사용자의 경우 'C:/some_folder/log_filename'처럼 드라이브 명을 지정해야 하며 디렉터리 구분은 역슬래시가 아닌 슬래시(/)이다.
