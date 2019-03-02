package be.tzbob.proman

import be.tzbob.examples.ExampleMain
import cats.instances.all._
import cats.syntax.all._
import mtfrp.core.UI.HTML
import mtfrp.core.{ClientDBehavior, ClientEvent}

object ProMan extends PageMain {
  val indexPage = new IndexPage
  val currentProject = indexPage.selectedProject.fold(Option.empty[Project]) {
    (_, p) =>
      Option(p)
  }

  val projectPage = new ProjectPage(currentProject)

  val selectedPage: ClientDBehavior[Page] = {
    val indexPages: ClientEvent[Page] = projectPage.backToIndex.as(indexPage)
    val projectPages: ClientEvent[Page] =
      indexPage.selectedProject.as(projectPage)

    indexPages.unionRight(projectPages).hold(indexPage)
  }

  val pages = Vector(indexPage, projectPage)
}

trait PageMain extends ExampleMain {
  val selectedPage: ClientDBehavior[Page]
  val pages: Vector[Page]

  lazy val sequenced: ClientDBehavior[Map[Page, HTML]] = {
    val interfaces = pages.map(p => p.scene.interface.map(p -> _))
    interfaces.sequence.map(_.toMap)
  }

  def ui: ClientDBehavior[HTML] =
    noWebSockets((selectedPage, sequenced).mapN { (page, interfaces) =>
      interfaces(page)
    })
}
