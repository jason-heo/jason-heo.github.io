---
layout: post
title: "Kubernetes Python API 사용법"
categories: "python"
---

- test에 사용된 python 버전: 3.6.5

아래의 내용은 각자가 사용하는 kubernetes cluster 환경에 따라 작동하지 않을 수도 있다.

## 목차

- [1. library 설치](#1-library-설치)
- [2. 연결 정보 설정하기](#2-연결-정보-설정하기)
  - [2-1) config file에서 조회하기](#2-1-config-file에서-조회하기)
  - [2-2) pod 내부에서 파일에 저장된 인증 정보 활용](#2-2-pod-내부에서-파일에-저장된-인증-정보-활용)
  - [2-3) console에서 조회하기](#2-3-console에서-조회하기)
- [3. 예제 프로그램 1 - pod 목록 출력하기](#3-예제-프로그램-1---pod-목록-출력하기)
- [4. 예제 프로그램 2 - 원격 pod에 명령 수행하기](#4-예제-프로그램-2---원격-pod에-명령-수행하기)
  - [4-1) 명령 수행 기초](#4-1-명령-수행-기초)
  - [4-2) interactive mode: stdout과 stderr 구분하기](#4-2-interactive-mode-stdout과-stderr-구분하기)
- [5. 더 많은 예제들](#5-더-많은-예제들)

## 1. library 설치

```console
$ pip3 install kubernetes
```

## 2. 연결 정보 설정하기

Python API를 처음 사용할 때 이 부분이 약간 어려웠다. 특히나 본인의 경우 pod 내부에서 Python API를 사용해야했으므로 더 힘들었던 것 같다. 모든 개발 프로젝트가 그렇듯 처음 셋팅하는 게 제일 어려운 것 같다. 이 부분만 잘 설정되면 나머지는 검색해보면서 코딩하면 된다.

### 2-1) config file 이용하기

[참고 문서](https://stackoverflow.com/a/54051435/2930152)

제일 편한 방법이다.

```python
from kubernetes import client, config
from kubernetes.stream import stream

# create an instance of the API class

config.load_kube_config()
api_client = client.CoreV1Api()
```

### 2-2) pod 내부에서 파일에 저장된 인증 정보 활용

위의 코드가 제일 간단했고 `kubectl`이 정상 작동하는 환경에서 잘 수행되었다. 하지만 본인의 경우 pod 내부에서 Python API를 사용할 때 위의 코드는 작동하지 않았다.

pod 내부에서는 아래와 같은 파일을 이용하여 Kubernetes API에 접근할 수 있다.

- API 서버: `https://kubernetes.default`로 고정되어 있다
- token: `/var/run/secrets/kubernetes.io/serviceaccount/token`에 저장되어 있다
- cert: `/var/run/secrets/kubernetes.io/serviceaccount/ca.crt`에 저장되어 있다

다음과 같은 코드를 작성하면 된다.

```python
from kubernetes import client, config

config = client.Configuration()

config.api_key['authorization'] = open('/var/run/secrets/kubernetes.io/serviceaccount/token').read()
config.api_key_prefix['authorization'] = 'Bearer'
config.host = 'https://kubernetes.default'
config.ssl_ca_cert = '/var/run/secrets/kubernetes.io/serviceaccount/ca.crt'
config.verify_ssl=True
```

### 2-3) console에서 조회하기

[참고 문서](https://kubernetes.io/ko/docs/tasks/administer-cluster/access-cluster-api/)

```console
# .KUBECONFIG에 여러 콘텍스트가 있을 수 있으므로, 가능한 모든 클러스터를 확인한다.
$ kubectl config view -o jsonpath='{"Cluster name\tServer\n"}{range .clusters[*]}{.name}{"\t"}{.cluster.server}{"\n"}{end}'

# 위의 출력에서 상호 작용하려는 클러스터의 이름을 선택한다.
$ CLUSTER_NAME="cluster 이름을 적는다"

# 클러스터 이름을 참조하는 API 서버를 가리킨다.
$ API_SERVER=$(kubectl config view -o jsonpath="{.clusters[?(@.name==\"$CLUSTER_NAME\")].cluster.server}")

# 토큰 값을 얻는다
$ TOKEN=$(kubectl get secrets -o jsonpath="{.items[?(@.metadata.annotations['kubernetes\.io/service-account\.name']=='default')].data.token}"|base64 --decode)
```

`$API_SERVER`와 `$TOEKN`에 API 서버와 인증에 필요한 토큰 값이 저장되어 있다.

```console
$ echo $API_SERVER
$ echo $TOKEN
```

token 값이 정상적으로 저장된 경우 `curl`을 이용하여 Rest API를 조회했을 때 오류가 없어야한다.

```console
$ curl -X GET $API_SERVER/api --header "Authorization: Bearer $TOKEN" --insecure
```

Rest API의 출력 결과는 아래와 비슷하다.

```json
{
  "kind": "APIVersions",
  "versions": [
    "v1"
  ],
  "serverAddressByClientCIDRs": [
    {
      "clientCIDR": "0.0.0.0/0",
      "serverAddress": "10.0.1.149:443"
    }
  ]
}
```

이제 아래와 같은 코드를 작성하면 Kubernetes API 서버와 연결할 준비가 완료된 것이다.

```python
from kubernetes import client, config

import ssl

ssl._create_default_https_context = ssl._create_unverified_context

config = client.Configuration()

config.api_key['authorization'] = 'echo $TOKEN 결과'
config.api_key_prefix['authorization'] = 'Bearer'
config.host = 'echo $API_SERVER 결과'
config.verify_ssl=False
```

## 3. 예제 프로그램 1 - pod 목록 출력하기

[참고 문서](https://kubernetes.io/ko/docs/tasks/administer-cluster/access-cluster-api/#python-client)

namespace에 존재하는 모든 pod의 IP 주소와 이름을 출력해보자.

```python
from kubernetes import client, config

import ssl

config = client.Configuration()

config.api_key['authorization'] = open('/var/run/secrets/kubernetes.io/serviceaccount/token').read()
config.api_key_prefix['authorization'] = 'Bearer'
config.host = 'https://kubernetes.default'
config.ssl_ca_cert = '/var/run/secrets/kubernetes.io/serviceaccount/ca.crt'
config.verify_ssl=True

api_client = client.CoreV1Api(client.ApiClient(config))

# 첫 번째 argument에 당신이 사용하는 namespace를 입력한다
ret = api_client.list_namespaced_pod("namespace 입력", watch=False)

print("Listing pods with their IPs:")

for i in ret.items:
	print(f"{i.status.pod_ip}\t{i.metadata.name}")
```

pod의 IP 주소와 이름이 출력된다.

## 4. 예제 프로그램 2 - 원격 pod에 명령 수행하기

[참고 코드](https://github.com/kubernetes-client/python/blob/master/examples/pod_exec.py)

### 4-1) 명령 수행 기초

이번에는 다음의 pod에 대응되는 코드를 작성해보자.

```console
$ kubectl exec <pod name> -- ls -l /tmp
```

전체 코드는 다음과 같다.

```python
from kubernetes import client, config
from kubernetes.stream import stream

config = client.Configuration()

config.api_key['authorization'] = open('/var/run/secrets/kubernetes.io/serviceaccount/token').read()
config.api_key_prefix['authorization'] = 'Bearer'
config.host = 'https://kubernetes.default'
config.ssl_ca_cert = '/var/run/secrets/kubernetes.io/serviceaccount/ca.crt'
config.verify_ssl=True

api_client = client.CoreV1Api(client.ApiClient(config))

my_command = ['ls', '-l', '/tmp']

response = stream(api_client.connect_get_namespaced_pod_exec,
              'pod name',
              'namespace',
              command=my_command,
              stdin=False,
              stderr=True,
              stdout=True,
              tty=False)

print(response)
```

출력 결과로는 `ls -l /tmp/`와 동일한 결과가 출력된다.

`kubectl`을 이용하더라도 원격 pod에 명령 수행하는데 아무 문제가 없지만 python을 이용한 경우 몇 가지에서 좀 더 편할 수 있다. 물론 shell script를 아주 잘 짠다면 python api를 사용할 필요가 없다. 그런데 경험상 명령 수행 결과를 확인하는 경우엔 shell script보단 python으로 수행하는 것이 편했다. 개인 편차가 있을 수 있지만 stdout/sttderr 분리, argument에 따옴표 escape 처리 등등을 생각하면 python으로 작성하는 게 편하다.

### 4-2) interactive mode: stdout과 stderr 구분하기

앞의 예제인 non-interactive mode에서는 `response` 변수의 type이 String이었고 stdout과 stderr이 모두 저장되는 것 같았다.

이번에는 interactive 모드를 이용하여 stdout과 stderr를 구분해보자. (non-interative mode에서도 가능한 것인지 아닌지는 확실치 않다)

수행할 명령은 아래와 같다.

```console
$ `ls -l /tmp/ /not-existing-dir`
```

stdout에는 `/tmp` 디렉터리의 내용이 저장되고, stderr에는 "ls: cannot access /not-existing-dir: No such file or directory" 같은 메시지가 저장되게 된다.

```python
from kubernetes import client, config
from kubernetes.stream import stream

config = client.Configuration()

config.api_key['authorization'] = open('/var/run/secrets/kubernetes.io/serviceaccount/token').read()
config.api_key_prefix['authorization'] = 'Bearer'
config.host = 'https://kubernetes.default'
config.ssl_ca_cert = '/var/run/secrets/kubernetes.io/serviceaccount/ca.crt'
config.verify_ssl=True

api_client = client.CoreV1Api(client.ApiClient(config))

my_command = ['ls', '-l', '/tmp', '/not-existing-dir']

response = stream(api_client.connect_get_namespaced_pod_exec,
              'pod name',
              'namespace',
              command=my_command,
              stdin=False,
              stderr=True,
              stdout=True,
              tty=False,
              _preload_content=False)

while response.is_open():
    response.update(timeout=1)

    if response.peek_stdout():
        print(f"STDOUT: {response.read_stdout()}")

    if response.peek_stderr():
        print(f"STDERR: {response.read_stderr}")

    if my_command:
        c = my_command.pop(0)
        response.write_stdin(c + "\n")

    else:
        break

print(f"exit code={response.returncode}")

response.close()
```

(종종 Broken Pipe 에러가 나오는데 정확한 해결 방법은 모르겠다)

좀더 interactive해보이는 코드는 아래와 같다.

```python
resp.write_stdin("date\n")
sdate = resp.readline_stdout(timeout=3)
print("Server date command returns: %s" % sdate)
resp.write_stdin("whoami\n")
user = resp.readline_stdout(timeout=3)
print("Server user is: %s" % user)
```

([참고 코드](https://github.com/kubernetes-client/python/blob/818e5ba0ba8cfc6d866195de706f58b7431110c1/examples/pod_exec.py#L109-L115))

interactive mode인 경우 `stream()` 함수는 `WSClient`를 return한다. `WSClient`에 어떤 method가 존재하는지 [code](https://github.com/oz123/python-base/blob/master/stream/ws_client.py)를 직접 확인해보자.

## 5. 더 많은 예제들

https://github.com/kubernetes-client/python/tree/master/examples 참고
