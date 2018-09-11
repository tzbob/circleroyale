package be.tzbob.circleroyale

import io.circe.generic.JsonCodec
import mtfrp.core.UI

@JsonCodec
sealed trait Weapon {
  val svg: UI.html.Tag
  def boundingCircle(p: Vec2D): Circle
}

object Weapon {
  case class Wipe(radius: Double) extends Weapon {
    lazy val svg: UI.html.Tag = {
      import UI.html.implicits._
      import UI.html.svgTags._
      import UI.html.svgAttrs._

      circle(r := this.radius,
             fill := "red",
             animate(attributeName := "fill",
                     values := "red;blue;red",
                     dur := "1s",
                     repeatCount := "indefinite"))
    }

    def boundingCircle(p: Vec2D): Circle = Circle(p, radius)
  }

}
