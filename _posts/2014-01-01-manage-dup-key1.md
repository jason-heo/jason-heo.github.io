---
layout: post
title: "MySQL 중복 키 관리 방법 1"
date: 2014-03-05 21:34:00
categories: mysql
---

{% include pmysql.md %}

## Stackoverflow URL

http://stackoverflow.com/questions/20184528/how-make-insert-if-select-rows-0-in-one-query/20184689

## 질문

다음과 같은 tbl 테이블이 있다.

    CREATE TABLE tbl(
        id INT NOT NULL PRIMARY KEY,
      name VARCHAR(100),
      UNIQUE(name)
    );

나는 이름이 중복되는 것을 원하지 않는다. 그래서 2개의 SELECT 문을 수행하는데

    $res = mysql(SELECT count(*) as count FROM Table WHERE name='test');
     
    $i = $res -> fetch_assoc();
     
    if ($i['count'] < 1 )
    {
        $res = mysql(INSERT INTO Table (name) VALUES ('test');
    }

위와 같이 SELECT를 먼저 수행해서 COUNT 값이 0이면 INSERT를 하고 있다. 더 중복 키 방지를 위해 더 좋은 방법은 없는가?

{% include adsense-content.md %}

## 답변

우선 질문자는 name 컬럼에 대해서 UNIQUE 속성을 부여했으므로 SELECT COUNT(*)를 하지 않아도 중복된 값이 INSERT되지는 않는다. 프로그램에서 에러가 발생하는 것을 방지하기 위해 SELECT를한번 더 한 것 같은데 좋은 방법은 아니다. SELECT를 했을 때는 COUNT가 0였지만, INSERT를 하기 바로 전에 다른 연결에서 동일한 값을 INSERT할 수도 있으므로 이에 대한 처리도 필요하다. 이를 회피하는 방법으로 INSERT를 수행한 뒤에 다음과 같이 에러 코드를 확인해 보는 것이 좋다.

    mysql_query('INSERT INTO ...');
     
    if (mysql_errno() == 1062)
    {
        echo "duplicated";
    }
    else
    {
        echo "inserted";
    }

MySQL의 에러 중 1062는 UNIQUE 혹은 PRIMARY KEY에 중복된 값이 발생했다는 것을 나타내는 오류 번호이다. 오류 번호가 1062인 경우 프로그램이 정상 흐름을 타도록 하면 쉽게 해결할 수 있다.

참고로 질문자가 선택한 답변은 다음과 같이 INSERT문에 IGNORE 옵션을 사용하는 것이다.

    INSERT IGNORE INTO Table (name) VALUES ('test')

프로그램 내에서 if-else를 사용할 필요가 없어서 편한 방법이다. 이 방법도 단점이 있긴 한데 중복 에러가 발생했는지 정상이었는지 사용자는 알 수 없다는 점이 단점이다. INSERT 후에 중복 여부를판단해야 하는지, 중복 여부는 중요하지 않는지 필요에 따라서 필자의 방법이나 INSERT IGNORE를 선택하기 바란다.

중복 키 관리과 관련하여 `INSERT IGNORE INTO` 이외에  `INSERT INTO ON DUPLICATE UPDATE`와 `REPLACE INTO`를 사용할 수도 있다. 3가지 방법 마다 약간의 차이가 있는데 다음에다시 알아보도록 한다.
