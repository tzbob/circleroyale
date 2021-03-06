package be.tzbob.examples

import be.tzbob.circleroyale.Time.Time
import be.tzbob.circleroyale.{Vec2D, _}
import io.circe.generic.JsonCodec
import mtfrp.core.UI.HTML
import mtfrp.core._

import scala.concurrent.duration._

object _4Chat extends ExampleMain {
  import UI.html.all._

  @JsonCodec
  case class Message(name: String, message: String) {
    val string = s"$name says $message"
  }

  val width  = 3000
  val height = 2000

  val kb: Keyboard                        = new Keyboard()
  val attacking: ClientDBehavior[Boolean] = kb.isKeyDown(" ")

  val svgFRP: SvgFRP = new SvgFRP("playground", width, height)
  val direction: ClientDBehavior[Vec2D] = {
    val previousPosition: ClientBehavior[Vec2D] =
      ClientDBehavior.delayed(position)

    val directionEv = previousPosition.snapshotWith(svgFRP.mousePosition) {
      (prevPos, absoluteMouse) =>
        absoluteMouse - prevPos
    }
    directionEv.hold(Vec2D.zero)
  }

  val in: ClientDBehavior[(Vec2D, Boolean)] =
    direction.map2(attacking) { (_, _) }

  val time: ClientEvent[Time] = new IntervalCycle(1.second / 10).elapsedTime

  val throttledInput: ClientEvent[(Vec2D, Boolean)] = in.sampledBy(time)

  val sessionInterval = new ServerTick(1.second / 20).sessionElapsedTime

  val serverInput: SessionEvent[(Vec2D, Boolean)] =
    ClientEvent.toSession(throttledInput)

  val playerInput: SessionEvent[(Vec2D, Boolean)] =
    FrpUtil.throttle(serverInput, sessionInterval)

  val sessionPlayer: SessionDBehavior[Player] =
    playerInput
      .fold(Player.default) { (p, in) =>
        in match {
          case (dir, attacking) =>
            p.update(dir, attacking)
              .clamp(Vec2D.zero, Vec2D(width, height))
        }
      }

  val player: ClientDBehavior[Player] =
    SessionDBehavior.toClient(sessionPlayer)

  val position: ClientDBehavior[Vec2D] = player.map(_.position)

  val camera: ClientDBehavior[Camera] = position.map { p =>
    val cameraWidth  = 600.0
    val cameraHeight = 400.0
    Camera(p - Vec2D(cameraWidth / 2, cameraHeight / 2),
           cameraWidth,
           cameraHeight)
  }

  def deadClients(players: Map[Client, Player]): Set[Client] = {
    val (deadPlayers, alivePlayers) = players.partition {
      case (_, p) => p.dead
    }

    // naive pairwise comparison
    val deadClients = if (alivePlayers.size >= 2) {
      val clientPairs = alivePlayers.keys.toList.combinations(2).toSet

      def hitClient(c1: Client, c2: Client): Option[Client] =
        if (alivePlayers(c1).hits(alivePlayers(c2))) Some(c2)
        else None

      val attackedPlayers = clientPairs.map {
        case List(c1, c2) => Set(hitClient(c1, c2), hitClient(c2, c1))
        case _            => Set.empty[Option[Client]]
      }

      attackedPlayers.flatten.flatten
    } else Set.empty[Client]

    deadClients ++ deadPlayers.keys
  }

  val playerInputAndDead: SessionEvent[(Boolean, (Vec2D, Boolean))] =
    SessionDBehavior.delayed(dead).snapshotWith(playerInput) { _ -> _ }

  val checkedPlayer: SessionDBehavior[Player] =
    playerInputAndDead
      .fold(Player.default) { (p, in) =>
        in match {
          case (dead, (dir, attacking)) =>
            p.update(dir, attacking)
              .clamp(Vec2D.zero, Vec2D(width, height))
              .setDead(dead)
        }
      }

  val checkedPlayers: AppDBehavior[Map[Client, Player]] =
    SessionDBehavior.toApp(checkedPlayer)

  val losers: AppDBehavior[Set[Client]] =
    checkedPlayers.map(deadClients)

  val dead: SessionDBehavior[Boolean] =
    AppDBehavior.toSession(losers).map2(SessionDBehavior.client) {
      _ contains _
    }

  val survivors: AppDBehavior[List[Player]] =
    checkedPlayers.map(_.values.toList.filter(!_.dead))
  val clientSurvivors: ClientDBehavior[List[Player]] =
    SessionDBehavior
      .toClient(AppDBehavior.toSession(survivors))

  val msgSource: ClientEventSource[Message]   = ClientEvent.source[Message]
  val msgs: SessionEvent[Message]             = ClientEvent.toSession(msgSource)
  val appMsgs: AppEvent[Map[Client, Message]] = SessionEvent.toApp(msgs)
  val chatInput: AppEvent[List[String]] =
    appMsgs.map(_.values.toList.map(_.string))

  val chat: AppIBehavior[List[String], List[String]] =
    chatInput.foldI(List.empty[String]) { (acc, n) =>
      n ++ acc
    }

  val optName: SessionDBehavior[Option[String]] =
    msgs.map(msg => Option(msg.name)).hold(None)

  val chatUI: ClientDBehavior[HTML] =
    SessionIBehavior.toClient(AppIBehavior.toSession(chat)).toDBehavior.map {
      c =>
        ul(c.map(msg => li(msg)))
    }

  val labelInformation: SessionDBehavior[(Option[String], Vec2D)] = optName
    .map2(dead) { (n, d) =>
      if (!d) n else None
    }
    .map2(sessionPlayer) { (name, p) =>
      (name, p.position)
    }
  val allLabelInfo: SessionDBehavior[List[(Option[String], Vec2D)]] =
    AppDBehavior
      .toSession(SessionDBehavior.toApp(labelInformation).map(_.values.toList))

  val clientLabels: ClientDBehavior[List[Option[HTML]]] =
    SessionDBehavior.toClient(allLabelInfo).map { ls =>
      ls.map {
        case (name, Vec2D(x, y)) =>
          import UI.html.implicits._
          import UI.html.svgTags._
          import UI.html.{svgAttrs => a}
          name.map { str =>
            text(a.x := x, a.y := y, str)
          }
      }
    }

  val svgContent: ClientDBehavior[Seq[HTML]] =
    clientSurvivors.map2(clientLabels) { (ap, ls) =>
      ap.map(_.svg) ++ ls.flatten
    }

  val gameUI: ClientDBehavior[HTML] =
    svgFRP
      .svg(camera, svgContent)
      .map2(SessionDBehavior.toClient(dead)) { (svg, dead) =>
        import UI.html.all._
        import UI.html.tags2._
        section(article(if (!dead) svg else h1("You died!")))
      }

  val ui = chatUI.map2(gameUI) { (chat, game) =>
    import UI.html.tags2._

    div(
      game,
      p(),
      section(
        article(
          form(
            input(`type` := "text", placeholder := "Name", name := "name"),
            input(`type` := "text", placeholder := "Message", name := "msg"),
            input(`type` := "submit"),
            UI.listen(onsubmit, msgSource) { ev =>
              val formElements = ev.target.elements
              val name         = formElements.name.value.asInstanceOf[String]
              val message      = formElements.msg.value.asInstanceOf[String]
              Message(name, message)
            }
          ),
          chat
        ))
    )
  }
}
