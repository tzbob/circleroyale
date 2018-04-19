package be.tzbob.examples

import mtfrp.core._
import slogging._

trait ExampleMain extends MyMain {
  LoggerConfig.factory = PrintLoggerFactory()
  PrintLogger.printTimestamp = true
  LoggerConfig.level = LogLevel.TRACE

  val headExtensions = {
    val nameLC = BuildInfo.name.toLowerCase
    import UI.html.all._
    List(
      script(src := s"$nameLC-fastopt-library.js"),
      raw(
        """
           <script language="JavaScript">
var exports = window;
exports.require = window["ScalaJSBundlerLibrary"].require;
</script>
        """
      ),
      script(src := s"$nameLC-fastopt.js")
    )
  }

}

object ClientCounter extends ExampleMain {
  val counter = new Counter(1, -1, _.fold(0)(_ + _).toDBehavior)
  val ui = counter.ui
}

object SessionCounter extends ExampleMain {
  val counter = new Counter(1, -1, ev => {
    val sessionB = ClientEvent.toSession(ev).fold(0)(_ + _)
    SessionIBehavior.toClient(sessionB).toDBehavior
  })

  val ui = counter.ui
}

object AppCounter extends ExampleMain {
  val counter = new Counter(
    1,
    -1,
    ev => {
      val sessionE: SessionEvent[Int] = ClientEvent.toSession(ev)
      val appEv: AppEvent[Map[Client, Int]] = SessionEvent.toApp(sessionE)

      val counter: AppEvent[Int] = AppDBehavior.clients.snapshotWith(appEv) {
        (clients, cf) =>
          clients.map(cf.get(_).getOrElse(0)).sum
      }
      val globalCounter: AppIBehavior[Int, Int] = counter.fold(0) { (acc, n) =>
        println(s"Executing sum: ${acc + n}")
        acc + n
      }
      AppIBehavior.broadcast(globalCounter).toDBehavior
    }
  )

  val ui = counter.ui
}
object Test extends ExampleMain {
  import UI.html.all._

  val ui = ClientDBehavior.constant(div("hello"))
}
