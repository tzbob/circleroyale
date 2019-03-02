package be.tzbob.examples

import be.tzbob.circleroyale.Time.Time
import be.tzbob.circleroyale._
import cats.syntax.all._
import mtfrp.core.UI.HTML
import mtfrp.core._

import scala.concurrent.duration._

object _1Client extends ExampleMain {
  val width  = 3000
  val height = 2000

  val kb: Keyboard                        = new Keyboard()
  val attacking: ClientDBehavior[Boolean] = kb.isKeyDown(" ")

  val svgFRP: SvgFRP = new SvgFRP("playground", width, height)
  val direction: ClientDBehavior[Vec2D] = {
    val previousPosition: ClientBehavior[Vec2D] =
      ClientDBehavior.delayed(position)

    val directionEv = previousPosition.snapshotWith(svgFRP.mousePosition) {
      (prevPos, absoluteMouse) =>
        absoluteMouse - prevPos
    }
    directionEv.hold(Vec2D.zero)
  }

  val in: ClientDBehavior[(Vec2D, Boolean)] =
    direction.map2(attacking) { (_, _) }

  val time: ClientEvent[Time] = new IntervalCycle(1.second / 10).elapsedTime

  val throttledInput: ClientEvent[(Vec2D, Boolean)] = in.sampledBy(time)

  val player: ClientDBehavior[Player] = {
    throttledInput
      .fold(Player.default) { case (p, (dir, att)) => p.update(dir, att) }
  }

  val position: ClientDBehavior[Vec2D] = player.map(_.position)

  val camera: ClientDBehavior[Camera] = position.map { p =>
    val cameraWidth  = 600.0
    val cameraHeight = 400.0
    Camera(p - Vec2D(cameraWidth / 2, cameraHeight / 2),
           cameraWidth,
           cameraHeight)
  }

  val svgContent = player.map(p => Seq(p.svg))

  val ui: ClientDBehavior[HTML] = svgFRP.svg(camera, svgContent).map { svg =>
    import UI.html.tags2._
    section(article(svg))
  }
}
