package be.tzbob.proman

import be.tzbob.proman.db.{DB, ProjectDAO}
import cats.effect.IO
import io.circe.generic.auto._
import mtfrp.core.UI.HTML
import mtfrp.core._
import mtfrp.core.macros.server
import slogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global

class IndexPage extends Page with LazyLogging {
  import mtfrp.core.UI.html.all._

  private[this] val projectName      = ClientBehavior.sink("")
  private[this] val submitPress      = ClientEvent.source[Unit]
  private[this] val selectedProject0 = ClientEvent.source[Project]

  val selectedProject: ClientEvent[Project] = selectedProject0

  val firstConnection =
    AppEvent.clientChanges
      .fold(0) { (old, _) =>
        old + 1
      }
      .changes
      .dropIf(_ > 1)

  val queryInitialProjects: AppEvent[IO[Vector[Project]]] = firstConnection.as {
    @server val io = {
      import doobie.implicits._
      for {
        ps <- ProjectDAO.findAll.transact(DB.transactor)
      } yield {
        logger.info(s"Found initial projects: $ps")
        ps
      }
    }
    io
  }

  val initialProjects = AppAsync.execute(queryInitialProjects)

  val projectSubmit: ClientEvent[Project] =
    projectName.snapshotWith(submitPress) { (name, _) =>
      Project(None, name, Vector.empty)
    }

  def isValid(p: Project) = p.name != ""

  val validProjectSubmission: AppEvent[Project] =
    ClientEvent
      .toApp(projectSubmit)
      .dropIf(!isValid(_))

  val persistProject: AppEvent[IO[Project]] = validProjectSubmission.map { p =>
    @server val io: IO[Long] = {
      import doobie.implicits._
      ProjectDAO.add(p).transact(DB.transactor)
    }

    io.map { id: Long =>
      p.copy(id = Option(id))
    }
  }

  val persistedProject: AppEvent[Project] = AppAsync.execute(persistProject)

  val persistedProjects =
    initialProjects
      .map { p =>
        logger.info(s"found $p")

        p
      }
      .unionLeft(persistedProject.map(Vector(_)))

  val projects = persistedProjects.fold(Seq.empty[Project]) { _ ++ _ }

  val interface: ClientDBehavior[HTML] =
    AppIBehavior.broadcast(projects).toDBehavior.map { ps: Seq[Project] =>
      val rawInput = input(id := "projectName", `type` := "text", value := "")
      val boundInput = UI.read(rawInput)(projectName, el => {
        el.value.asInstanceOf[String]
      })
      val submitButton = input(`type` := "submit", value := "Add Project")

      val inputForm = form(
        action := "",
        UI.listen(onsubmit, submitPress)(_ => ()),
        boundInput,
        submitButton
      )

      val sortedProjects = ps.sortBy(_.name)

      div(sortedProjects.map(_.interface(selectedProject0)), hr(), inputForm)
    }

  val warnings: ClientDBehavior[String] = projectSubmit
    .map { p =>
      if (!isValid(p)) "Not a valid project, can't have an empty name!"
      else ""
    }
    .hold("")
    .toDBehavior

  val scene =
    new Scene(ClientDBehavior.constant(h1("Projects")), interface, warnings)
}
