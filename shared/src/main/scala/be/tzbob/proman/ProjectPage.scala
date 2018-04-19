package be.tzbob.proman

import cats.Applicative
import cats.instances.all._
import mtfrp.core._

class ProjectPage(project: ClientDBehavior[Option[Project]]) extends Page {
  import mtfrp.core.UI.html.all._

  private[this] val backToIndex0     = ClientEvent.source[Unit]
  val backToIndex: ClientEvent[Unit] = backToIndex0

  val Ap = Applicative[ClientDBehavior].compose(Applicative[Option])

  val body = Ap.map(project) { p: Project =>
    div(p.entries.map(_.interface),
        button(UI.listen(onclick, backToIndex0)(_ => ())))
  }

  val warnings: ClientDBehavior[String] = ClientDBehavior.constant("wip")

  val title = Ap.map(project) { p =>
    h1(s"Project: ${p.name}")
  }

  def stub[A](behavior: ClientDBehavior[Option[A]])(
      default: A): ClientDBehavior[A] = {
    behavior.map {
      case Some(p) => p
      case None    => default
    }
  }

  val scene = new Scene(stub(title)(h1()), stub(body)(div()), warnings)
}
