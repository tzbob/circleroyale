package be.tzbob.proman

import be.tzbob.examples.ExampleMain
import be.tzbob.proman.db.DB
import cats.instances.all._
import cats.syntax.all._
import mtfrp.core.UI.HTML
import mtfrp.core.{ClientDBehavior, ClientEvent}

object ProMan extends ExampleMain {
  val indexPage = new IndexPage
  val currentProject = indexPage.selectedProject.fold(Option.empty[Project]) {
    (_, p) =>
      Option(p)
  }

  val projectPage = new ProjectPage(currentProject.toDBehavior)

  val selectedPage: ClientDBehavior[Page] = {
    val indexPages: ClientEvent[Page] = projectPage.backToIndex.as(indexPage)
    val projectPages: ClientEvent[Page] =
      indexPage.selectedProject.as(projectPage)

    indexPages.unionRight(projectPages).hold(indexPage).toDBehavior
  }

  val pages: Vector[Page] = Vector(indexPage, projectPage)
  val sequenced: ClientDBehavior[Map[Page, HTML]] = {
    val interfaces = pages.map(p => p.scene.interface.map(p -> _))
    interfaces.sequence.map(_.toMap)
  }

//  def ui: ClientDBehavior[HTML] = (selectedPage, sequenced).mapN {
//    (page, interfaces) =>
//      interfaces(page)
//  }
//
//  def ui = (indexPage.scene.interface, projectPage.scene.interface).mapN {
//    (i, p) =>
//      println(i)
//      i
//  }

  def ui =
    indexPage.scene.interface
      .map { i =>
        i
      }
}
