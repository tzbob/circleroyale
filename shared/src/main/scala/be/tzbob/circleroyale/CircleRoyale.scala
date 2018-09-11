package be.tzbob.circleroyale

import be.tzbob.examples.ExampleMain
import cats.Functor
import io.circe.generic.auto._
import mtfrp.core.UI.HTML
import mtfrp.core._
import scalatags.Text
import cats._
import cats.data._
import cats.implicits._
import slogging.LazyLogging

import scala.util.Random

object CircleRoyale extends ExampleMain with LazyLogging {
  val width  = 3000
  val height = 2000

  def print[A](pre: String)(a: A): A = {
//    println(s"$pre $a")
    a
  }

  def printF[F[_]: Functor, A](pre: String)(f: F[A]): F[A] = {
//    import cats.implicits._
//    f.map(print(pre))
    f
  }

  val svgFRP        = new SvgFRP("playground", width, height)
  val mousePosition = svgFRP.mousePosition.map { case (x, y) => Vec2D(x, y) }

//  val previousPosition = ClientDBehavior.delayed(position, Vec2D.zero)
  val previousPosition = ClientDBehavior.constant(Vec2D.zero)
  val camera = previousPosition.map { p =>
    val cameraWidth  = 3000
    val cameraHeight = 2000
    Camera(p - Vec2D(cameraWidth / 2, cameraHeight / 2),
           cameraWidth,
           cameraHeight)
  }

  val newDirection =
    previousPosition.snapshotWith(mousePosition) { (prevPos, absoluteMouse) =>
      val reversePos = prevPos * -1
      absoluteMouse.move(reversePos).normalize
    }
  val direction = newDirection.hold(Vec2D.zero).toDBehavior

  val pushToServerCycle = new IntervalCycle(10000000)
  val pushToServerTime  = pushToServerCycle.elapsedTime

//  val animationCycle = new AnimationCycle
  val animationCycle = pushToServerCycle
  val animationFrame = animationCycle.elapsedTime

  val kb = new Keyboard
  // fix bug (without identity the spacebar behavior does not hold true)
  val spacebar = kb.isKeyDown(" ").map(identity)
  val actions  = ClientDBehavior.toSession(spacebar)

  // For now just send on every animation frame, normalize for security
  val sessionDirection =
    ClientEvent
      .toSession(direction.sampledBy(pushToServerTime))
      .map(_.normalize)

  val serverTick = new ServerTick(10000000)
  val elapsed    = AppEvent.toSession(serverTick.elapsedTime)

  // perform computations every 'elapsed'
  val directionB = sessionDirection.hold(Vec2D.zero).toDBehavior
  val serverTickPlayerInformation =
    directionB
      .map2(actions) { _ -> _ }
      .snapshotWith(elapsed) { (tup, time) =>
        val (dir, action) = tup
        (dir * (time / 5), action)
      }

  val player =
    serverTickPlayerInformation
      .fold(Player.randomDefault(width, height)) {
        case (player, (dir, action)) =>
          player
            .move(dir)
            .clamp(Vec2D.zero, Vec2D(width, height))
            .copy(attacking = action)
      }
      .toDBehavior

//  val clientPlayer = SessionDBehavior.toClient(player)
//  val position: ClientDBehavior[Vec2D] =
//    clientPlayer.map(_.position)

  val players: AppDBehavior[Seq[Player]] =
    SessionDBehavior
      .toApp(player)
      .map { playersMap =>
        playersMap.values.toList
//
//      players match {
//        case Nil        => List.empty
//        case one :: Nil => List(one)
//        case xs =>
//          val playerPairs = xs.combinations(2)
//          playerPairs
//            .map {
//              case List(x, y) =>
//                if (x intersects y) {
//                  if (x.attacking && y.attacking) List.empty
//                  else if (!x.attacking) List(y)
//                  else if (!y.attacking) List(x)
//                  else List(x, y)
//                } else List.empty
//            }
//            .flatten
//            .toSeq
//      }

      }

  val clientPlayers = SessionDBehavior.toClient(AppDBehavior.toSession(players))

  val drawUpdates = clientPlayers.sampledBy(animationFrame)
  val svgContent = drawUpdates
    .map { _.map(_.svg) }
    .hold(Seq.empty)
    .toDBehavior

  val debugInterval = pushToServerTime
  val debugInfo: ClientDBehavior[HTML] =
    camera
      .sampledBy(debugInterval)
      .map(_.toString)
      .hold("")
      .toDBehavior
      .map { str =>
        import UI.html.all._
        div(s"debug info: $str")
      }

  def ui: ClientDBehavior[HTML] =
    svgFRP.svg(camera, svgContent).map2(debugInfo) { (svg, debug) =>
      import UI.html.all._
      div(svg, debug)
    }

}
