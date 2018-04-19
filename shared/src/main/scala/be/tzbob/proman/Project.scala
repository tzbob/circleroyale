package be.tzbob.proman

import mtfrp.core.{ClientEventSource, UI}

case class Project(id: Option[Long], name: String, entries: Vector[Entry]) {
  import mtfrp.core.UI.html.all._

  def interface(src: ClientEventSource[Project]) =
    div(h2(name, UI.listen(onclick, src)(_ => this)))
}
