---
layout: post
title: "Ollama quick tutorial (ver. 2024.07)"
categories: "llm"
---

This is a quick review of Ollama.

tested with Macbook Pro M3 36GB memory

## 1. installation

- Linux: https://ollama.com/download/linux
  ```
  $ curl -fsSL https://ollama.com/install.sh | sh
  ```
- MacOS: https://ollama.com/download/mac
  - download zip file and install it
  - Requires macOS 11 Big Sur or later

## 2. run ollama server

- Linux
  ```
  $ ollama serve
  ```
- MacOS
  - run `ollama.app` from Spotlight, or Application folder in Finder
  - Alternatively, run `ollama server` from a Terminal

## 3. `ollama` cli

`ollama` provides following options:

```
$ ollama
Usage:
  ollama [flags]
  ollama [command]

Available Commands:
  serve       Start ollama
  create      Create a model from a Modelfile
  show        Show information for a model
  run         Run a model
  pull        Pull a model from a registry
  push        Push a model to a registry
  list        List models
  ps          List running models
  cp          Copy a model
  rm          Remove a model
  help        Help about any command
```

### 3-1) list models

```
$ ollama list
```

Initially, no models are shown

### 3-2) load models

Let's load Llama3 8B model:

```
$ ollama run llama3:8b
```

You can find more models at https://ollama.com/library

After loading the `llama3:8b` model, the `ollama list` command outputs:

```
$ ollama list
NAME              ID            SIZE    MODIFIED
llama3:8b         365c0bd3c000  4.7 GB  4 weeks ago
```

Basically, Ollama uses a 4-bit quantized model. So the size is only 4.7GB.

Let's ask Llama3 some questions:

```
$ ollama run llama3:8b
>>> Tell me about LLM
```

ollama prints:

```
LLM!

LLM stands for Large Language Model. It's a type of artificial intelligence
(AI) model that is specifically designed to process and generate human-like
language.

...
```

### 3-3) `ollama` options

type `/help` to see the available options:

```
>>> /help
Available Commands:
  /set            Set session variables
  /show           Show model information
  /load <model>   Load a session or model
  /save <model>   Save your current session
  /clear          Clear session context
  /bye            Exit
  /?, /help       Help for a command
  /? shortcuts    Help for keyboard shortcuts

Use """ to begin a multi-line message.
```

### 3-4) `/set` option

The `/set` is one of the most powerful options in `ollama`

```
>>> /set
Available Commands:
  /set parameter ...     Set a parameter
  /set system <string>   Set system message
  /set template <string> Set prompt template
  /set history           Enable history
  /set nohistory         Disable history
  /set wordwrap          Enable wordwrap
  /set nowordwrap        Disable wordwrap
  /set format json       Enable JSON mode
  /set noformat          Disable formatting
  /set verbose           Show LLM stats
  /set quiet             Disable LLM stats
```

- `/set system` allows you to set system prompt
- `/set verbose` prints execution stats, providing information like tokens/sec
  ```
  >>> /set verbose
  Set 'verbose' mode.

  >>> hi
  Hi! Thanks for the compliment! I'm just an AI, but I try my best to be a good
  programmer too. I code in Python and Java, and I love solving problems and
  creating new projects. What about you? Do you have any programming experience
  or interests?

  total duration:       2.354836125s
  load duration:        3.509833ms
  prompt eval duration: 292.179ms
  prompt eval rate:     0.00 tokens/s
  eval count:           56 token(s)
  eval duration:        2.056319s
  eval rate:            27.23 tokens/s
  ```
  - Llama3 8B & M3 36GB performs at a rate of 27 tokens/sec which isn't  bad at all.
- `/set nowordwrap` is also useful

### 3-4) multi-line string

User input is executed immediately after pressing enter which can be cumbersome for multi-line strings.

Use `"""` to begin a multi-line message.

```
>>> """
... Translate below to Korean
...
... Hi
... """
! (annyeonghaseyo)
```

## 4. ollama server options

There are several environmental variables for the ollama server

- `OLLAMA_KEEP_ALIVE`
  - default: `5m`
  - how long a loaded model stays in GPU memory. After this value, models are auto-unloaded
  - set to `-1` if you want to disable this feature
- `OLLAMA_MAX_LOADED_MODELS`
  - default: `1`
  - Theorically, We can load as many models as GPU memory available.
  - but `OLLAMA_MAX_LOADED_MODELS` is set to `1`, only 1 model is loaded (previsouly loaded model if off-loaded from GPU)
  - increase this value if you want to keep more models in GPU memory
- `OLLAMA_NUM_PARALLEL`
  - default: `1`
  - Handle multiple requests simultaneously for a single model

## 5. REST API

`ollama` cli is powerful but not used that frequently. I prefer to use web UI. These web UIs are developed via API.

### 5-1) Ollama own API

You can find all available APIs on https://github.com/ollama/ollama/blob/main/docs/api.md

For example, `/api/chat` is used to chat completion

Let's call it via `curl`

- request
  ```
  curl http://localhost:11434/api/chat -d '{
    "model": "llama3:8b",
    "messages": [
      {
        "role": "user",
        "content": "hi"
      }
    ],
    "stream": false
  }'
  ```
- response
  ```
  {
    "model": "llama3:8b",
    "created_at": "2024-07-07T04:15:19.393Z",
    "message": {
      "role": "assistant",
      "content": "Hi! It's nice to meet you. Is there something I can help you with, or would you like to chat?"
    },
    "done_reason": "stop",
    "done": true,
    "total_duration": 3428811750,
    "load_duration": 2304526042,
    "prompt_eval_count": 11,
    "prompt_eval_duration": 196279000,
    "eval_count": 26,
    "eval_duration": 923565000
  }
  ```

streaming mode is also supported.

### 5-2) OpenAI compatible API

refer to https://github.com/ollama/ollama/blob/main/docs/openai.md

## 6. Python library

### 6-1) Ollama library

https://github.com/ollama/ollama-python

- install
  ```
  $ pip install ollama
  ```
- usage
  ```
  import ollama
  response = ollama.chat(model='llama3', messages=[
    {
      'role': 'user',
      'content': 'Why is the sky blue?',
    },
  ])
  print(response['message']['content'])
  ```
- streaming mode
  ```
  import ollama

  stream = ollama.chat(
      model='llama3',
      messages=[{'role': 'user', 'content': 'Why is the sky blue?'}],
      stream=True,
  )

  for chunk in stream:
    print(chunk['message']['content'], end='', flush=True)
  ```

### 6-2) OpenAI Library

https://github.com/ollama/ollama/blob/main/docs/openai.md

```
from openai import OpenAI

client = OpenAI(
    base_url='http://localhost:11434/v1/',

    # required but ignored
    api_key='ollama',
)

chat_completion = client.chat.completions.create(
    messages=[
        {
            'role': 'user',
            'content': 'Say this is a test',
        }
    ],
    model='llama3',
)
```

## 7. Web UI

I've tested two web ui.

- Open WebUI: https://github.com/open-webui/open-webui
  - github star: 30.k, fork: 3.3k (2024.07)
  - pros: powerful features
  - cons: (relatively) not easy to install
    - I would recommend use Docker
- Chatbot Ollama: https://github.com/ivanfioravanti/chatbot-ollama
  - github star: 1.3k, fork: 209
  - pros: easy to install
  - cons: not actively developing
    - supports basic features
