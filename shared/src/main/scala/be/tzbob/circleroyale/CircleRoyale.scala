package be.tzbob.circleroyale

import be.tzbob.examples.ExampleMain
import cats.instances.all._
import cats.syntax.all._
import mtfrp.core.UI.HTML
import mtfrp.core._
import slogging._

object CircleRoyale extends SceneMain with LazyLogging {
  import slogging._

  LoggerConfig.level = LogLevel.OFF
  LoggerConfig.factory = FilterLoggerFactory()
  FilterLogger.filter = {
    // use PrintLogger for all trace statements from sources starting with "foo.bar"
//    case (_, source)
//        if source.contains("GamePlay") ||
//          source.contains("Replication") =>
//          source.startsWith("scalatags.hokko") ||
//        source.contains("GamePlay") ||
//          source.contains("ClientDBehavior") =>
//      PrintLogger
    // log all other levels
//    case _ => NullLogger
    case _ => PrintLogger
  }

  val start    = new Start()
  val gamePlay = new GamePlay(start.started)
  val gameOver = new GameOver(gamePlay.timeSpentAlive)
  val scenes   = Vector(start, gamePlay, gameOver)

  // start.start fires -> gameplay
  // gameplay.dead -> game over
  val selectedScene: ClientDBehavior[GameScene] =
    (start.started, SessionDBehavior.toClient(gamePlay.dead)).mapN {
      (started, dead) =>
        if (!started) start
        else if (!dead) gamePlay
        else gameOver
    }
}

trait SceneMain extends ExampleMain {
  val selectedScene: ClientDBehavior[GameScene]
  val scenes: Vector[GameScene]

  private lazy val sequenced: ClientDBehavior[Map[GameScene, HTML]] = {
    val interfaces = scenes.map(p => p.ui.map(p -> _))
    interfaces.sequence.map(_.toMap)
  }

  def ui: ClientDBehavior[HTML] = (selectedScene, sequenced).mapN {
    (page, interfaces) =>
      import UI.html.all._
      import UI.html.tags2._

      section(article(interfaces(page)))
  }
}
