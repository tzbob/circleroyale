package be.tzbob.circleroyale

case class Camera(position: Vec2D, width: Int, height: Int) {
  val viewboxText: String = {
    val Vec2D(x, y) = position
    s"$x $y $width $height"
  }
}
