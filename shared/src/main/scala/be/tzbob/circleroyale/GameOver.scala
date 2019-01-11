package be.tzbob.circleroyale
import mtfrp.core.{ClientDBehavior, SessionDBehavior, UI}
import mtfrp.core.UI.HTML
import UI.html.all._

class GameOver(timeSpentAlive: SessionDBehavior[Long]) extends GameScene {

  val ui: ClientDBehavior[HTML] =
    SessionDBehavior.toClient(timeSpentAlive).map { score =>
      div(
        h2("Game Lost"),
        p(s"Score $score")
      )
    }
}
