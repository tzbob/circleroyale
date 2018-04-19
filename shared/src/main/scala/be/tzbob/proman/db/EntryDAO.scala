package be.tzbob.proman.db

import be.tzbob.proman.Entry
import mtfrp.core.macros.server

object EntryDAO {

  @server val dropTable: doobie.ConnectionIO[Int] = {
    import doobie.implicits._
    sql"DROP TABLE IF EXISTS entry".update.run
  }

  @server val createIfNotExistsTable: doobie.ConnectionIO[Int] = {
    import doobie.implicits._
    sql"""CREATE TABLE IF NOT EXISTS entry (
          id bigint auto_increment PRIMARY KEY NOT NULL,
          project_id bigint,
          date bigint NOT NULL,
          message varchar NOT NULL,
          done boolean NOT NULL,
          foreign key (project_id) references project(id) 
          )
       """.update.run
  }

  @server val findById: Int => doobie.ConnectionIO[Entry] = (id: Int) => {
    import doobie.implicits._

    sql"select id, date, message, done from entry where id = $id"
      .query[Entry]
      .unique
  }

  @server val findByProjectId: Int => doobie.ConnectionIO[Vector[Entry]] =
    (id: Int) => {
      import doobie.implicits._

      sql"select id, date, message, done from entry where project_id = $id"
        .query[Entry]
        .to[Vector]
    }

  @server val add: Vector[Entry] => doobie.ConnectionIO[Vector[Entry]] =
    (entries: Vector[Entry]) => {
      import doobie.implicits._
      import cats.implicits._

      val insert = sql"""insert into entry (project_id, date, message, done)
                      values (?, ?, ?, ?)"""

      val es = entries.map { entry =>
        (entry.projectId, entry.date, entry.message, entry.done)
      }

      val insertions: Vector[doobie.ConnectionIO[Entry]] = es.map { entry =>
        val (pid, date, msg, done) = entry
        for {
          _  <- insert.update.run
          id <- sql"select lastval()".query[Long].unique
        } yield Entry(Option(id), pid, date, msg, done)
      }

      insertions.sequence
    }

}
