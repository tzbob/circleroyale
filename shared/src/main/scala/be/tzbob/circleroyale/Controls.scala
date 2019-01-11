package be.tzbob.circleroyale

import mtfrp.core._

class Controls(svgFRP: SvgFRP,
               width: Int,
               height: Int,
               previousPlayerPosition: ClientDBehavior[Vec2D]) {

//  val camera: ClientBehavior[Camera] =
//    svgFRP.dimensions.map { case (w, h) => Camera(Vec2D(0, 0), w, h) }
//
//  val defaultCamera = Camera(Vec2D(0, 0), width, height)
//
//  val previousRelativePosition = camera
//    .snapshotWith(previousPlayerPosition.changes) { (c, p) =>
//      c.absolutePosition(p)
//    }
//    .hold(Vec2D.zero)
//    .toDBehavior
//
//  val direction = previousRelativePosition.snapshotWith(svgFRP.mousePosition) {
//    case (p, (x, y)) => Vec2D(x, y).move(p * -1).normalize
//  }
}
