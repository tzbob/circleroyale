package be.tzbob.circleroyale
import mtfrp.core._
import mtfrp.core.UI.HTML

import UI.html.all._

class Start extends GameScene {
  private[this] val buttonSource: ClientEventSource[Unit] =
    ClientEvent.source[Unit]
  private[this] val startPlay: ClientEvent[Unit] = buttonSource

  val started: ClientDBehavior[Boolean] =
    startPlay
      .fold(false) { (_, _) =>
        true
      }
      .toDBehavior

  val ui: ClientDBehavior[HTML] = ClientDBehavior.constant {
    div(
      h1("CircleRoyale"),
      button(UI.listen(onclick, buttonSource) { _ =>
        ()
      }, "Play")
    )
  }
}
