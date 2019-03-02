package be.tzbob.circleroyale

import io.circe.{Decoder, Encoder}
import mtfrp.core.{Client, UI}

case class Player(position: Vec2D,
                  radius: Int,
                  color: String,
                  attacking: Boolean,
                  weapon: Weapon,
                  rotation: Vec2D,
                  dead: Boolean = false) {
  def setDead(d: Boolean): Player = copy(dead = d)
//  def setName(nm: Option[String]): Player = copy(name = nm)

  lazy val attack  = copy(attacking = true)
  lazy val passive = copy(attacking = false)

  def attacking(attacking: Boolean): Player = copy(attacking = attacking)

  def rotateTo(rotation: Vec2D): Player = copy(rotation = rotation)
  def step(steps: Long): Player =
    copy(position = position + rotation.normalize * steps)

  def clamp(min: Vec2D, max: Vec2D): Player = {
    val offset = (Vec2D.right + Vec2D.up) * radius
    copy(position = position.clamp(min + offset, max - offset))
  }

  def update(dir: Vec2D, att: Boolean): Player =
    this.step(5).rotateTo(dir).attacking(att)

  lazy val svg: UI.HTML = {
    import UI.html.implicits._
    import UI.html.svgTags._
    import UI.html.{svgAttrs => a}

    g(
      if (attacking)
        Seq(weapon.svg(a.cx := position.x, a.cy := position.y))
//                     a.transform := s"rotate(${rotation.angle})"))
      else Seq.empty[UI.HTML],
      circle(a.cx := position.x,
             a.cy := position.y,
             a.r := this.radius,
             a.fill := this.color),
//      name.fold(Seq.empty[UI.HTML]) { str =>
//        Seq(text(a.x := position.x, a.y := position.y, str))
//      }
    )
  }

  def hits(other: Player): Boolean =
    if (attacking) weapon.hits(position, rotation, other)
    else false
}

object Player {
  import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  implicit val decodeClient: Decoder[Client] = deriveDecoder[Client]
  implicit val encodeClient: Encoder[Client] = deriveEncoder[Client]
  implicit val decodePlayer: Decoder[Player] = deriveDecoder[Player]
  implicit val encodePlayer: Encoder[Player] = deriveEncoder[Player]

  val size  = 20
  val color = s"rgb(255, 224, 189)"
  import scala.concurrent.duration._

  val default = Player(Vec2D.zero,
                       size,
                       color,
                       false,
                       Weapon.Wipe(40, 2.seconds, 3.seconds),
                       Vec2D.zero)

  def randomDefault(width: Int, height: Int): Player = {
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
