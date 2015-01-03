---
layout: post
title: "MySQL FullText Plugin에 대한 요약"
categories: MySQL
---

MySQL FullText Plugin에 대한 간단한 요약입니다. 혼자 볼 용도로 정리한 것이라서 막 대충 적었음을 양해 바랍니다.

## 자료구조

### `st_mysql_ftparser`

Full Text Plugin 등록 시 다음과 같은 자료 구조를 채워야 한다.

```
struct st_mysql_ftparser
{
    int interface_version;
    int (*parse)(MYSQL_FTPARSER_PARAM *param);
    int (*init)(MYSQL_FTPARSER_PARAM *param);
    int (*deinit)(MYSQL_FTPARSER_PARAM *param);
};
```

* `parse()` : 파싱 함수
* `init()` : 파싱 시작 시 호출되는 함수
* `deinit()` : 파싱 완료 후 호출되는 함수

### `MYSQL_FTPARSER_PARAM`

위의 3개 함수들이 호출될 때마다 전달되는 파라미터

```
typedef struct st_mysql_ftparser_param
{
    int (*mysql_parse)(struct st_mysql_ftparser_param *,
    char *doc, int doc_len);
    int (*mysql_add_word)(struct st_mysql_ftparser_param *,
    char *word, int word_len,
    MYSQL_FTPARSER_BOOLEAN_INFO *);
    void *ftparser_state;
    void *mysql_ftparam;
    struct charset_info_st *cs;
    char *doc;
    int length;
    int flags;
    enum enum_ftparser_mode mode;
} MYSQL_FTPARSER_PARAM;
```

|멤버|설명|
|----|----|
|mysql_parse()   |MySQL에서 기본 제공하는 파서|
|mysql_add_word()|text로부터 추출된 word들을 MySQL로 전달하는 함수. word마다 호출해줘야 함|
|ftparser_state  |MySQL에서는 사용하지 않는다. plugin에서 원하는 용도로 사용하면 됨|
|mysql_ftparam   |plugin에서는 절대 사용하면 안 된다. MySQL 내부적으로만 사용된다.|
|doc             |파싱할 문자열|
|length          |doc의 길이|
|cs              |doc의 character set|
|flags           |`MYSQL_FTFLAGS_NEED_COPY` 혹은 0만 저장할 수 있다.|
|mode            |파서의 동작 모드. 아래에서 설명됨|

* `MYSQL_FTFLAGS_NEED_COPY`
 * `mysql_add_word()`에 전달된 word를 MySQL이 복사해야 하는지 여부를 저장한다. 다음과 같은 예를 보자. `buf`의 값은 `while ()` 문을 돌 때마다 값이 변경되므로 word들은 copy가 되어야 한다.

```
    char buf[1024];
    FILE *f = fopen(param->doc, "r");

    while (fgets(buf, sizeof(buf), f))
        param->mysql_parse(param, buf, strlen(buf));

    fclose(f);
```

 * copy를 하는 경우 속도가 느려지게 된다. copy가 필요한 경우와 필요하지 않은 경우를 잘 이해하고 활용하도록 하자.

* `mode`
 * 약간 이해가 안 되는 내용임
 * `MYSQL_FTPARSER_SIMPLE_MODE` : index할 word들을 반환하는 mode
 * `MYSQL_FTPARSER_WITH_STOPWORDS` : Phrase match를 위한 Boolean Search에 사용된다.
 * `MYSQL_FTPARSER_FULL_BOOLEAN_INFO` : Boolean Search에 사용된다. `MYSQL_FTPARSE_BOOLEAN_INFO`에 Boolean 연산을 위한 정보가 저장되어 있다.

### `MYSQL_FTPARSER_BOOLEAN_INFO`

`mysql_add_word()`의 마지막 인자로 `MYSQL_FTPARSER_BOOLEAN_INFO`가 있다.

```
typedef struct st_mysql_ftparser_boolean_info
{
    enum enum_ft_token_type type;
    int yesno;
    int weight_adjust;
    char wasign;
    char trunc;
    char prev;
    char *quot;
} MYSQL_FTPARSER_BOOLEAN_INFO;
```

Boolean mode에는 별 관심이 없어서 패스. 간단한 경우에는 그냥 다음과 같이 사용하면 된다고 함.

```
MYSQL_FTPARSER_BOOLEAN_INFO boolean_info =
    { FT_TOKEN_WORD, 0, 0, 0, 0, 0, 0 };

param->mysql_add_word(param, word, len, &boolean_info);
```

그런데, 이해 안 되는 점 중 하나는 Boolean 검색은 사용자에 의해 전달되는 값이므로 MySQL에서 plugin에 전달을 해 주야 하는 자료 구조라 생각되는데, 왜 `mysql_add_word()` 호출 시에 오히려 plugin이 MySQL에 전달을 해줘야 하는지...

## 간단한 FullText plugin 구현

PHP 코드 중에서 변수만 발라내는 plugin을 제작한다. PHP의 변수는 dollor sign ($)으로 시작하며, 영문자/숫자/underscore/127~255의 byte를 포함하는데 PHP 코드 중에서 이런 문자만 추려내서 index할 수 있도록 하는 parser이다. 변수 부분만 발라래는 것은 생략하고 대략 큰 구조만 적으면...

* 파싱 함수

```
static int parse_php(MYSQL_FTPARSER_PARAM *param)
{
    char *end = param->doc + param->length;
    char *s = param->doc;

    ...

    for (;;)
    {
        // 여기에 PHP 변수를 발라래는 코드가 들어감
        param->mysql_add_word(param, word_start, s ? word_start,
                              &bool_info);
    }
    return 0;
}
```

* plugin 정보 등록

```
static struct st_mysql_ftparser ft_php=
{
    MYSQL_FTPARSER_INTERFACE_VERSION,
    parse_php,
    NULL,
    NULL
};
mysql_declare_plugin(fulltext_demo)
{
    MYSQL_FTPARSER_PLUGIN,
    &ft_php,
    "php_code",
    "Sergei Golubchik",
    "Simple Full-Text Parser for PHP scripts",
    PLUGIN_LICENSE_GPL,
    NULL,
    NULL,
    0x0100,
    NULL,
    NULL,
    NULL
}
mysql_declare_plugin_end;
```

* plugin 등록

```
mysql> CREATE TABLE ft_demo (php TEXT, FULLTEXT(php) WITH PARSER php_code) ENGINE=MyISAM;
Query OK, 0 rows affected (0.01 sec)
```

* 사용하기

```
mysql> INSERT ft_demo VALUES ('$a=15; echo $this->var;'), ('echo $classname::CONST_VALUE;'), ('echo "$foo$bar";'), ('echo AnotherClass::$varvar;'), ('echo MyClass::CONST_VALUE;');
Query OK, 5 rows affected (0.00 sec)
Records: 5 Duplicates: 0 Warnings: 0

mysql> SELECT * FROM ft_demo;
+-------------------------------+
| php                           |
+-------------------------------+
| $a=15; echo $this->var;       |
| echo $classname::CONST_VALUE; |
| echo "$foo$bar";              |
| echo AnotherClass::$varvar;   |
| echo MyClass::CONST_VALUE;    |
+-------------------------------+
5 rows in set (0.01 sec)

mysql> select * from ft_demo where MATCH php AGAINST('this');
Empty set (0.00 sec)

mysql> select * from ft_demo where MATCH php AGAINST('$this');
+-------------------------+
| php                     |
+-------------------------+
| $a=15; echo $this->var; |
+-------------------------+
1 row in set (0.01 sec)
```
