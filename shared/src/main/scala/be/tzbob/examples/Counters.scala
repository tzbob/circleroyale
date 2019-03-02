package be.tzbob.examples
import mtfrp.core._
import mtfrp.core.UI.HTML

class Counters extends ExampleMain {
  import UI.html.all._

  val in = ClientEvent.source[Int]

  val clientSum = in.fold(0)(_ + _)

  val sessionIn: SessionEvent[Int] = ClientEvent.toSession(in)

  val sessionSum = sessionIn.fold(0)(_ + _)

  val appSum =
    SessionEvent.toApp(sessionIn).fold(0)(_ + _.values.sum)

  val ui: ClientDBehavior[HTML] =
    (clientSum, SessionDBehavior.toClient(sessionSum).map { ss =>
      println(ss)
      ss
    }, SessionDBehavior.toClient(AppDBehavior.toSession(appSum))).mapN {
      (cs, ss, as) =>
        div(
          h1("Counters"),
          button("+", UI.listen(onclick, in)(_ => 1)),
          button("-", UI.listen(onclick, in)(_ => -1)),
          div(s"client sum: $cs"),
          div(s"session sum: $ss"),
          div(s"app sum: $as")
        )
    }
}
//    }
