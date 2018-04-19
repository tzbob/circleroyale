package be.tzbob.proman

import mtfrp.core.ClientDBehavior
import mtfrp.core.UI.HTML

import cats.syntax.all._

class Scene(title: ClientDBehavior[_ <: HTML],
            body: ClientDBehavior[_ <: HTML],
            warnings: ClientDBehavior[String]) {
  import mtfrp.core.UI.html.all._

  val interface: ClientDBehavior[HTML] =
    (title, body, warnings).mapN { (t, b, w) =>
      div(
        t,
        b,
        p(w)
      )
    }
}
