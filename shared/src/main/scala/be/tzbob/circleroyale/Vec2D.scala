package be.tzbob.circleroyale

import io.circe.generic.JsonCodec

import scala.util.Random

@JsonCodec case class Vec2D(x: Double, y: Double) {
  def move(v: Vec2D) = Vec2D(x + v.x, y + v.y)
  def +(v: Vec2D)    = move(v)
  def -(v: Vec2D)    = move(v * -1)

  def scale(i: Double) = Vec2D(x * i, y * i)
  def *(i: Double)     = Vec2D(x * i, y * i)

  def clamp(min: Vec2D, max: Vec2D): Vec2D = {
    def clampValue(f: Vec2D => Double) =
      if (f(this) < f(min)) f(min)
      else if (f(this) > f(max)) f(max)
      else f(this)

    Vec2D(clampValue(_.x), clampValue(_.y))
  }

  val magnitude: Double = Math.sqrt(x * x + y * y)
  lazy val normalize = {
    if (magnitude == 0) Vec2D.zero
    else this * (1 / magnitude)
  }
}

object Vec2D {
  val zero = Vec2D(0, 0)

  val up   = Vec2D(0, 1)
  val down = Vec2D(0, -1)

  val right = Vec2D(1, 0)
  val left  = Vec2D(-1, 0)

  def random(maxW: Int, maxH: Int) =
    Vec2D(Random.nextDouble() * maxW, Random.nextDouble() * maxH)

  val directions = Seq(up, right, down, left)

  def apply[N: Numeric](x: N, y: N): Vec2D = {
    val num = implicitly[Numeric[N]]
    Vec2D(num.toDouble(x), num.toDouble(y))
  }
}
