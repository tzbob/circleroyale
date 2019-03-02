package be.tzbob.circleroyale

trait Shape {
  val position: Vec2D
  def intersectsWithCircle(other: Shape.Circle): Boolean
}

object Shape {
  case class Circle(position: Vec2D, radius: Double) extends Shape {
    def intersectsWithCircle(other: Circle): Boolean =
      (position - other.position).magnitude <= (radius + other.radius)
  }
}
