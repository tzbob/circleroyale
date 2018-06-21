package be.tzbob.circleroyale

case class Vec2D(x: Int, y: Int) {
  def move(v: Vec2D) = Vec2D(x + v.x, y + v.y)
  def +(v: Vec2D)    = move(v)
  def -(v: Vec2D)    = move(v * -1)

  def scale(i: Int) = Vec2D(x * i, y * i)
  def *(i: Int)     = Vec2D(x * i, y * i)
}

object Vec2D {
  val zero = Vec2D(0, 0)

  val up   = Vec2D(0, 1)
  val down = Vec2D(0, -1)

  val right = Vec2D(1, 0)
  val left  = Vec2D(-1, 0)

  val directions = Seq(up, right, down, left)
}
