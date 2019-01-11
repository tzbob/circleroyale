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

  case class Rectangle(corner: Vec2D,
                       width: Double,
                       height: Double,
                       orientation: Vec2D)
      extends Shape {
    val position: Vec2D                              = corner
    def intersectsWithCircle(other: Circle): Boolean = ???
  }
}
