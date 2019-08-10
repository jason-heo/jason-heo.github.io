---
layout: post
title: "Scala용 Airlift/Airline 예제 코드"
categories: "programming"
---

Druid의 소스 코드를 보다가 Airline을 알게되었다.

[Airline의 github](https://github.com/airlift/airline)을 보면, Airline에 대해 "Airline is a Java annotation-based framework for parsing Git like command line structures"라고 설명되어있다.


git의 사용법을 보면 명령이 다양하고 명령마다 파라미터가 서로 다른 것을 볼 수 있는데, 이런 것을 일반적인 argument parsing library를 사용하여 구현하기엔 매우 복잡하다.

참고로 Druid에서 Airline을 어떻게 사용하는지 알고자 한다면 [Main.java](https://github.com/apache/incubator-druid/blob/0.15.1-incubating/services/src/main/java/org/apache/druid/cli/Main.java)를 보면 된다.


Druid에서 Airline을 잘 사용하고 있고, 성숙도도 높아보여서 다음 프로젝트에서는 나도 도입해보고자 했다. 그런데 Airline은 Java로 만들어져있고 나는 Scala를 사용하는 구나. Scala용 Airline은 찾을 수 없고, Scala가 어차피 JVM에서 도는 거라 Airline 예제 코드를 Scala 코드로 바꿔보려해도 능력이 안되서;

암튼 Scala 코드로 짠 건 없는지 검색을 해봤는데 찾기 어려웠고, 누군가 github gist에 올린 걸 겨우 찾아냈다. https://gist.github.com/jw3/14e8cc45f7f5fe9161ba

4년 전 코드라서 그런지 `@Arguments` 부분이 잘 돌아가질 않았는데, Data Type을 `Seq[String]`에서 `java.util.List[String]`로 바꾸니 잘 돌아갔다.

아래는 gist에서 찾은 코드를 약간 손 본 코드이다. Scala 2.11.8에서 잘 도는 것 확인 완료.

```scala
import io.airlift.airline._

// 출처: https://gist.github.com/jw3/14e8cc45f7f5fe9161ba
// 위에거에서 Seq[String]만 java.util.List[String]으로 변경했음

/**
  * airline example in scala
  * https://github.com/airlift/airline
  * @author wassj
  */
object AirlineTest {

  def main(args: Array[String]) {

    val builder = Cli.builder[Runnable]("git")
      .withDescription("the stupid content tracker")
      .withDefaultCommand(classOf[Help])
      .withCommands(classOf[Help], classOf[Add])

    builder.withGroup("remote")
      .withDescription("Manage set of tracked repositories")
      .withDefaultCommand(classOf[RemoteShow])
      .withCommands(classOf[RemoteShow], classOf[RemoteAdd])

    val gitParser: Cli[Runnable] = builder.build()

    gitParser.parse(args: _*).run();
  }

  trait GitCommand extends Runnable {
    @Option(`type` = OptionType.GLOBAL, name = Array("-v"), description = "GitCommand mode")
    var GitCommand: Boolean = false

    def run(): Unit = {
      println(s"class=${getClass.getSimpleName}")
      println(s"GitCommand=${GitCommand}")
    }
  }

  @Command(name = "add", description = "Add file contents to the index")
  class Add extends GitCommand {
    @Arguments(description = "Patterns of files to be added")
    var patterns: java.util.List[String] = _

    @Option(name = Array("-i"), description = "Add modified contents interactively.")
    var interactive: Boolean = _

    override def run(): Unit = {
      println(s"class=${getClass.getSimpleName}")
      println(s"patterns = ${patterns}")
      println(s"interactive = ${interactive}")
    }
  }

  @Command(name = "show", description = "Gives some information about the remote <name>")
  class RemoteShow extends GitCommand {
    @Option(name = Array("-n"), description = "Do not query remote heads")
    var noQuery: Boolean = false

    @Arguments(description = "Remote to show")
    var remote: String = _

    override def run(): Unit = {
      println(s"class=${getClass.getSimpleName}")
      println(s"noQuery = ${noQuery}")
      println(s"remote = ${remote}")
    }
  }

  @Command(name = "track", description = "Adds a remote")
  class RemoteAdd extends GitCommand {
    @Option(name = Array("-u"), description = "Track only a specific branch")
    var branch: String = _

    @Arguments(description = "Remote repository to add")
    var remote: java.util.List[String] = _

    override def run(): Unit = {
      println(s"class=${getClass.getSimpleName}")
      println(s"branch = ${branch}")
      println(s"remote = ${remote}")
    }
  }
}
```

### 사용 예 및 출력 결과

본인도 처음에 위의 코드를 볼 때 어떻게 argument를 넘겨야할는지, 어떤 변수에 어떤 값이 할당되는지 살짝 헷갈렸기에 사용 예를 적어 놓는다.

```
$ java -cp ... AirlineTest add
class=Add
patterns = null
interactive = false

$ java -cp ... AirlineTest add -i file1 file2 file3
class=Add
patterns = [file1, file2, file3]
interactive = true

$ java -cp ... AirlineTest remote track
class=RemoteAdd
branch = null
remote = null

$ java -cp ... AirlineTest remote track -u mybranch remote1 remote2
class=RemoteAdd
branch = mybranch
remote = [remote1, remote2]
```
