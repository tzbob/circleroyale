package be.tzbob.circleroyale

import io.circe.generic.JsonCodec
import mtfrp.core.{Client, UI}

import scala.util.Random

@JsonCodec
case class Player(position: Vec2D,
                  radius: Int,
                  color: String,
                  attacking: Boolean,
                  weapon: Weapon,
                  rotation: Vec2D) {
  lazy val attack  = copy(attacking = true)
  lazy val passive = copy(attacking = false)

  def rotateTo(rotation: Vec2D): Player = copy(rotation = rotation.normalize)
  def move(vec2D: Vec2D): Player        = copy(position = position.move(vec2D))
  def step(steps: Long): Player         = move(rotation * steps)

  def clamp(min: Vec2D, max: Vec2D): Player = {
    val offset = (Vec2D.right + Vec2D.up) * radius
    copy(position = position.clamp(min + offset, max - offset))
  }

  def left(steps: Int)  = move(Vec2D.left.scale(steps))
  def right(steps: Int) = move(Vec2D.right.scale(steps))
  def up(steps: Int)    = move(Vec2D.up.scale(steps))
  def down(steps: Int)  = move(Vec2D.down.scale(steps))

  lazy val svg: UI.HTML = {
    import UI.html.implicits._
    import UI.html.svgTags._
    import UI.html.{svgAttrs => a}

    g(
      if (attacking)
        Seq(
          weapon.svg(a.cx := position.x,
                     a.cy := position.y))
//                     a.transform := s"rotate(${rotation.angle})"))
      else Seq.empty[UI.HTML],
      circle(a.cx := position.x,
             a.cy := position.y,
             a.r := this.radius,
             a.fill := this.color)
    )
  }

  def hits(other: Player, orientation: Vec2D): Boolean =
    if (attacking) weapon.hits(position, orientation, other)
    else false
}

object Player {
  val size  = 20
  val color = s"rgb(255, 224, 189)"

  def randomDefault(width: Int, height: Int): Player = {
    import scala.concurrent.duration._
    val pos = Vec2D.random(width, height)
    Player(pos,
           size,
           color,
           false,
//           Weapon.Laser(40, 2.seconds, 3.seconds),
           Weapon.Wipe(40, 2.seconds, 3.seconds),
           Vec2D.zero)
  }
}
