package be.tzbob.circleroyale

class PlayerTest extends org.scalatest.FunSuite {

  test("A player within a camera should calculate its relative position.") {
    val camera = Camera(Vec2D.zero, 200, 150)
    val player = Player(Vec2D(20, 30), 5, null)

    val maybePos = player.positionInCamera(camera)
    assert(maybePos === Some(Vec2D(20, 30)))
  }

  test("A player on an edge should still calculate its relative position.") {
    val camera = Camera(Vec2D.zero, 200, 150)

    val player = Player(Vec2D(0, 0), 1, null)

    assert(player.left(1).positionInCamera(camera) === Some(Vec2D(-1, 0)))
    assert(player.down(1).positionInCamera(camera) === Some(Vec2D(0, -1)))

    assert(player.right(201).positionInCamera(camera) === Some(Vec2D(201, 0)))
    assert(player.up(151).positionInCamera(camera) === Some(Vec2D(0, 151)))
  }

  test("A player over an edge should not calculate its position.") {
    val camera = Camera(Vec2D.zero, 200, 150)

    val player = Player(Vec2D(0, 0), 1, null)

    assert(player.left(2).positionInCamera(camera) === None)
    assert(player.down(2).positionInCamera(camera) === None)

    assert(player.right(202).positionInCamera(camera) === None)
    assert(player.up(152).positionInCamera(camera) === None)
  }
}
