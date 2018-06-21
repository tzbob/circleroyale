package be.tzbob.circleroyale

import mtfrp.core.Client
import mtfrp.core.UI.HTML

case class Player(position: Vec2D, radius: Int, client: Client) {

  def move(vec2D: Vec2D): Player =
    copy(position = position.move(vec2D))

  def left(steps: Int)  = move(Vec2D.left.scale(steps))
  def right(steps: Int) = move(Vec2D.right.scale(steps))
  def up(steps: Int)    = move(Vec2D.up.scale(steps))
  def down(steps: Int)  = move(Vec2D.down.scale(steps))

  def positionInCamera(camera: Camera): Option[Vec2D] = {
    println(position.x + radius)
    if (position.x + radius < camera.position.x) return None
    if (position.x - radius > camera.position.x + camera.width) return None

    if (position.y + radius < camera.position.y) return None
    if (position.y - radius > camera.position.y + camera.height) return None

    Some(position - camera.position)
  }

  def svg(camera: Camera): Option[HTML] = {
    import mtfrp.core.UI.html.all._
    import mtfrp.core.UI.html.svgTags._
    import mtfrp.core.UI.html.svgAttrs._

    positionInCamera(camera).map { position =>
      circle(cx := position.x,
             cy := position.y,
             r := this.radius,
             fill := "red")
    }

  }
}
