package be.tzbob.proman.db

import cats.effect.IO
import mtfrp.core.macros.server

object DB {
  @server val _ = {
    import doobie.implicits._
    import scala.concurrent.duration._

    val initTables = for {
      _ <- EntryDAO.createIfNotExistsTable
      _ <- ProjectDAO.createIfNotExistsTable
    } yield {}

    initTables.transact(DB.transactor).unsafeRunTimed(5.seconds)
  }

  @server lazy val transactor = {
    doobie.Transactor.fromDriverManager[IO](
      "org.h2.Driver",
      "jdbc:h2:./db"
    )
  }
}
