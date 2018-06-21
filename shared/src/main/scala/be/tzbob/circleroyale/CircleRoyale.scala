package be.tzbob.circleroyale

import be.tzbob.examples.ExampleMain
import mtfrp.core._
import mtfrp.core.UI.HTML

object CircleRoyale extends ExampleMain {

//  val animationFrame: ClientEvent[Unit] = ???
//  val players: AppDBehavior[Set[Player]] = ???

  val camera: ClientDBehavior[Camera] =
    ClientDBehavior.constant(Camera(Vec2D(0, 0), 200, 150))

  val clientPlayers: ClientDBehavior[Set[Player]] =
    ClientDBehavior.constant(
      Set(Player(Vec2D(20, 30), 5, null))
    )

  def draw(svgElements: Seq[HTML]) = {
    import UI.html.all._
    import UI.html.svgTags

    div(
      svgTags.svg(
        width := "200",
        height := "150",
        svgElements
      )
    )
  }

  def ui: ClientDBehavior[HTML] =
    (clientPlayers, camera).mapN { (players, camera) =>
      val playerSvgs = players.map(_.svg(camera)).flatten
      draw(playerSvgs.toSeq)
    }
}
