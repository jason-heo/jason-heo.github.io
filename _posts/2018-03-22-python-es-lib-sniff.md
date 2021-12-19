---
layout: post
title: "Python ES Library의 Sniff 옵션에 대해"
categories: "bigdata"
---

요거 옵션 정말 헷갈리더라고요.

다양한 테스트 및 es python code 분석 결과 아래와 같은 결론을 얻었었습니다.

node1~node10까지 10대가 있다고 가정할 때

Case 1. Elasticsearch(["node1", "node2"])

위 방식으로 호출하면 node1, node2에만 round robin 방식으로 요청이 분산됩니다. (즉, node3~10은 Python으로부터 요청을 직접 받진 않음)

Case 2. Elasticsearch(["node1", "node2"], sniff_on_start=True)

sniff_on_start를 켜면, `/_cat/nodes` API를 활용하여 ES에 물려있는 node들 목록을 조회해서 모든 node들에게 round robin으로 요청을 보냅니다. (단, master only node는 제외함)

(sniff라는 용어 대신 다른 용어를 썼으면 헷갈림이 좀 덜했을 듯합니다.)

"you can specify to sniff on startup to inspect the cluster and load balance across all nodes" 설명에서, "inspect the cluster"가 `/_cat/nodes`로 node들을 조회한다는 말이고, "load balance across all nodes"는 조회된 모든 node들에 rr 방식으로 load balance한다는 의미입니다.

Case 3. sniff_timeout이란 옵션도 있는데, `/_cat/nodes`로 sniff하는 간격을 의미합니다. (sniff_interval이 좀 더 직관적이진 않을지...)

Case 4. sniff_on_connection_fail 옵션도 있는데, 중간에 장애 등으로 연결 오류 발생 시, 새로 sniff를 하겠다는 의미입니다.

sniff_on_connection_fail은 sniff_on_start=False인 상황에서도 사용 가능합니다.

Case 5. "When using an http load balancer you cannot use the Sniffing functionality"는 L4 등의 로드 발란서를 의미하는 것으로 보입니다.

확실하진 않지만, 이유는 뒷 문장에 "the cluster would supply the client with IP addresses to directly connect to the cluster"라고 나오는 것처럼, L4 하위에 붙은 장비들의 private IP를 얻기 때문인 듯 한데, `/_cat/nodes`로 sniff를 하기 때문에 L4 하위에 붙어도 잘 작동할 것이라 생각되는데, 테스트를 못해봐서 뭐라 말씀드리긴 어렵네요.


{% include spark-reco.md %}
