package be.tzbob.proman.db

import be.tzbob.proman.{Entry, Project}
import cats.implicits._
import mtfrp.core.macros.server

object ProjectDAO {

  @server val dropTable: doobie.ConnectionIO[Int] = {
    import doobie.implicits._
    sql"DROP TABLE IF EXISTS project".update.run
  }

  @server val createIfNotExistsTable: doobie.ConnectionIO[Int] = {
    import doobie.implicits._
    sql"""CREATE TABLE IF NOT EXISTS project (
          id bigint auto_increment PRIMARY KEY NOT NULL,
          name varchar NOT NULL
          )
       """.update.run
  }

  @server val findAll: doobie.ConnectionIO[Vector[Project]] = {
    import doobie.implicits._
    val getProjectIds = sql"select id from project".query[Int].to[Vector]

    getProjectIds.flatMap { ids =>
      ids.traverse(findById)
    }
  }

  @server val findById: Int => doobie.ConnectionIO[Project] = (id: Int) => {
    import doobie.implicits._
    val getEntries =
      sql"select date, message, done from entry where project_id = $id"
        .query[Entry]
        .to[Vector]

    val getProjectDetails =
      sql"select id, name from project where id = $id"
        .query[(Long, String)]
        .unique

    for {
      entries <- getEntries
      details <- getProjectDetails
    } yield {
      val (id, name) = details
      Project(Some(id), name, entries)
    }
  }

  @server val add: Project => doobie.ConnectionIO[Long] = (p: Project) => {
    import doobie.implicits._
    for {
      _ <- sql"insert into project (name) values (${p.name})".update.run
      id <- sql"select lastval()".query[Long].unique
      entries <- EntryDAO.add(p.entries)
    } yield id
  }

}
