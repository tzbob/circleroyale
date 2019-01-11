package be.tzbob.circleroyale

import java.util.concurrent.TimeUnit

import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.JsonCodec
import mtfrp.core.UI

import scala.concurrent.duration.FiniteDuration

@JsonCodec
sealed trait Weapon {
  val svg: UI.html.Tag
  def hits(position: Vec2D, orientation: Vec2D, player: Player): Boolean

  val useTime: FiniteDuration
  val downTime: FiniteDuration
}

object Weapon {
  implicit final val finiteDurationDecoder: Decoder[FiniteDuration] =
    new Decoder[FiniteDuration] {
      def apply(c: HCursor): Result[FiniteDuration] =
        for {
          length     <- c.downField("length").as[Long].right
          unitString <- c.downField("unit").as[String].right
          unit <- (try { Right(TimeUnit.valueOf(unitString)) } catch {
            case _: IllegalArgumentException =>
              Left(DecodingFailure("FiniteDuration", c.history))
          }).right
        } yield FiniteDuration(length, unit)
    }
  implicit final val finiteDurationEncoder: Encoder[FiniteDuration] =
    new Encoder[FiniteDuration] {
      final def apply(a: FiniteDuration): Json =
        Json.fromJsonObject(
          JsonObject("length" -> Json.fromLong(a.length),
                     "unit"   -> Json.fromString(a.unit.name)))
    }

  case class Wipe(radius: Double,
                  useTime: FiniteDuration,
                  downTime: FiniteDuration)
      extends Weapon {
    lazy val svg: UI.html.Tag = {
      import UI.html.implicits._
      import UI.html.svgTags._
      import UI.html.svgAttrs._

      circle(r := this.radius,
             fill := "green",
             animate(attributeName := "fill",
                     values := "green;yellow;green;",
                     dur := "2s",
                     repeatCount := "indefinite"))
    }

    def hits(position: Vec2D, orientation: Vec2D, player: Player): Boolean =
      (position - player.position).magnitude <= (radius + player.radius)
  }

  case class Laser(length: Double,
                   useTime: FiniteDuration,
                   downTime: FiniteDuration)
      extends Weapon {
    lazy val svg: UI.html.Tag = {
      import UI.html.implicits._
      import UI.html.svgTags._
      import UI.html.svgAttrs._

      line(pathLength := length,
           fill := "green",
           animate(attributeName := "fill",
                   values := "green;red;green;",
                   dur := "2s",
                   repeatCount := "indefinite"))
    }

    def hits(position: Vec2D, orientation: Vec2D, player: Player): Boolean = {
      // http://mathworld.wolfram.com/Circle-LineIntersection.html
      val laserEnd = position + orientation * length
      val dsq = Math.pow(position.x - laserEnd.x, 2) +
        Math.pow(position.y - laserEnd.y, 2)
      val D = position.x * laserEnd.y - laserEnd.x * position.y

      Math.pow(length, 2) * dsq - Math.pow(D, 2) >= 0
    }
  }
}
