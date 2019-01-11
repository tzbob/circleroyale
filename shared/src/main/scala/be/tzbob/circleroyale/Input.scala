package be.tzbob.circleroyale

case class Input(direction: Vec2D, action: Boolean) {
  lazy val normalize = copy(direction = direction.normalize)
}
