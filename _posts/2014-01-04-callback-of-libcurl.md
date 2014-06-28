---
layout: post
title: "C++에서 libcurl의 callback 사용 시 주의사항"
date: 2014-03-21 21:34:00
categories: programming
---

[cURL](http://curl.haxx.se/libcurl/)은 C 언에서 HTTP, HTTPs, FTP를 사용하게 해 주는 대표적인 라이브러리이다. cURL이란 이름에서 알 수 있듯이 C API이며 C++용 API는 따로 제공하지 않는다.

cURL을 이용하여 URL의 HTML 내용을 string으로 저장하기 위해선 callback을 사용해야 하는데 (HTML내용을 그냥 stdout으로 redirect할 때는 callback이 필요없다.) C++의 경우 주의할 것이 있다.

[cURL의 공식 Tutorial](http://curl.haxx.se/libcurl/c/libcurl-tutorial.html)을 보면 다음과 같은 내용이 나와 있다.

> There's basically only one thing to keep in mind when using C++ instead of C when interfacing libcurl:
> The callbacks CANNOT be non-static class member functions

> Example C++ code:

```cpp
class AClass

{

  static size_t callback(void *ptr, size_t size, size_t nmemb,   void *ourpointer);

  ...

};
```

즉, 위의 예와 같이 class method를 callback으로 사용하고자 하는 경우 static으로 선언되어야 한다는 점이다. Callback 등록 시에는 다음과 같이 호출하면 된다.

```cpp
curl_easy_setopt(ctx, CURLOPT_WRITEFUNCTION, AClass::callback);
```
