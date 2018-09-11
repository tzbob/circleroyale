package be.tzbob.circleroyale

import io.circe.generic.JsonCodec
import mtfrp.core.{Client, UI}

import scala.util.Random

@JsonCodec case class Player(position: Vec2D,
                             radius: Int,
                             color: String,
                             attacking: Boolean,
                             weapon: Weapon) {
  lazy val attack  = copy(attacking = true)
  lazy val passive = copy(attacking = false)

  val boundingCircle =
    if (attacking) weapon.boundingCircle(position) else Circle(position, radius)

  def move(vec2D: Vec2D): Player = copy(position = position.move(vec2D))

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
    import UI.html.svgAttrs._

    if (attacking)
      g(
        weapon.svg(cx := position.x, cy := position.y),
        circle(cx := position.x,
               cy := position.y,
               r := this.radius,
               fill := this.color)
      )
    else
      circle(cx := position.x,
             cy := position.y,
             r := this.radius,
             fill := this.color)
  }

  def intersects(other: Player): Boolean =
    boundingCircle.intersects(other.boundingCircle)
}

object Player {
  val size = 20
  val color = {
    Random.setSeed(System.currentTimeMillis())
    val r = Random.nextInt(200)
    val g = Random.nextInt(200)
    val b = Random.nextInt(200)
    s"rgb($r,$g,$b)"
  }

  def randomDefault(width: Int, height: Int): Player = {
    val pos = Vec2D.random(width, height)
    Player(pos, size, color, false, Weapon.Wipe(40))
  }
}
