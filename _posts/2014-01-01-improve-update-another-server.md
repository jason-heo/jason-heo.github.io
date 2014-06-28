---
layout: post
title: "서로 다른 MySQL 서버의 UPDATE 속도를 향상 시키는 방법"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20366311/how-do-i-speed-up-mysql-updates-other-than-looping/20367036

## 질문

A라는 MySQL 서버에 46만개의 레코드가 있다. 46만개의 레코드를 SELECT한 뒤 loop를 돌면서 매 레코드마다 B MySQL 서버에 대해 UPDATE문을 수행해야 한다. 즉, 원격에 있는 MySQL 서버에 총46만 번의 UPDATE 문을 실행하고 있는데 속도가 느리다. 성능을 향상 시킬 방법이 없을까?

{% include adsense-content.md %}

## 답변

원격에 있는 MySQL과 통신하는 횟수를 줄여야 한다. 아무리 컴퓨터나 네트워크가 빠르다고 하더라도 MySQL 질의를 원격으로 보내고 결과를 받는데 고정 비용이 발생하게 된다. 이 비용을 46만번 반복하는 것은 비효율적이다. 총 수행해야 할 UPDATE 횟수는 레코드 수로 정해져 있으므로 통신 횟수를 줄이기 위해서는 1개의 UPDATE마다 질의를 수행하는 것이 아니라 여러 개의 UPDATE문을 묶어서 1번에 수행하는 방법을 택하면 된다. 1,000개의 UPDATE문을 세미콜론으로 묶어서 실행하면 질의 횟수가 46만번에서 460번으로 크게 줄어들게 된다. 물론 많은 UPDATE 문을 묶을수록 질의 문자열의 길이가길어지고 Parsing 시간이 길어지게 되는 일장일단(trade-off)가 발생한다.

여러 개의 질의를 1개의 문자열로 묶어서 실행하는 것을 다중 질의 (Multiple Query)라고 한다. 다중 질의는 MySQL API에서 기본적으로 비활성화되어 있는데, SQL Injection 등 보안에 취약할 수 있기때문에다. 개발자가 다중 질의의 장단점을 잘 알고 있고 SQL Injection을 방어할 수 있다면 API 옵션을 통해서 다중 질의를 사용할 수 있다. 개발 언어에 따라서 다중 질의를 활성화하는 방법은 다음과 같다.

- PHP : http://www.php.net/manual/en/mysqli.quickstart.multiple-statement.php
- C API : http://dev.mysql.com/doc/refman/5.0/en/c-api-multiple-queries.html
- VB : http://www.devart.com/dotconnect/mysql/docs/MultiQuery.html  

다중 질의를 사용하는 의사 코드(Pseudo Code)는 다음과 같다. 1,000개의 UPDATE를 묶어서 1번에 질의를 실행하고 있다.

    $cnt = 1;
    for ($row in $rows)
    {
        $multi_query .= "UPDATE ..;";
     
        if ($cnt % 1000 == 0)
        {
            mysql_query($multi_query);
            $cnt = 0;
            $multi_query = "";
        }
        ++$cnt;
     
    }

### 질문자 피드백

다중 질의를 이용해서 약 40%의 성능 향상이 있었다. (3분 23초에서 2분 1초로 단축됨). 몇 개를 묶는 것이 가장 빠른지 실험해 보았는데, 100개의 UPDATE를 묶어서 실행할 때가 가장 성능이 좋으며 100개 이상을 묶는 순간 성능이 다시 느려지기 시작했다.

