package be.tzbob.circleroyale
import be.tzbob.circleroyale.Time.Time
import cats.syntax.all._
import io.circe.generic.auto._
import mtfrp.core.UI.HTML
import mtfrp.core._
import slogging.LazyLogging

import scala.concurrent.duration._

class GamePlay(started: ClientDBehavior[Boolean])
    extends GameScene
    with LazyLogging {

  val width  = 3000
  val height = 2000

  val svgFRP = new SvgFRP("playground", width, height)

  val sessionInput: SessionDBehavior[Input] = {
    val mousePosition    = svgFRP.mousePosition
    val previousPosition = ClientDBehavior.delayed(clientPosition)

    val newDirection =
      previousPosition.snapshotWith(mousePosition) { (prevPos, absoluteMouse) =>
        val reversePos = prevPos * -1
        absoluteMouse.move(reversePos).normalize
      }
    val direction = newDirection.hold(Vec2D.zero)

    val kb       = new Keyboard
    val spacebar = kb.isKeyDown(" ")

    val input = direction.map2(spacebar)(Input.apply)
    val inputIfStarted = (input, started).mapN { (in, start) =>
      if (start) Some(in)
      else None
    }

    val pushToServerCycle = new IntervalCycle(1.second / 10)
    val pushToServerTime  = pushToServerCycle.elapsedTime
    val inputToServer = inputIfStarted
      .sampledBy(pushToServerTime)
      .collect(identity)

    ClientEvent
      .toSession(inputToServer)
      .map(_.normalize)
      .hold(Input(Vec2D.zero, false))
  }

  val serverTick = new ServerTick(1.second / 60)
  val elapsed    = AppEvent.toSession(serverTick.elapsedTime)

  def progressToDeadline(start: SessionEvent[_],
                         interval: FiniteDuration): SessionDBehavior[Time] = {
    val sessionElapsed = AppEvent.toSession(serverTick.elapsedTime)
    val addElapsed = sessionElapsed.map { time => (old: Time) =>
      val newTime = old - time
      if (newTime <= 0) 0
      else newTime
    }
    val reStart = start.map(_ => (_: Time) => interval.toMillis)

    addElapsed
      .unionRight(reStart)
      .fold(0L) { (acc, f) =>
        f(acc)
      }
  }

  val attackState: SessionDBehavior[AttackState] = {
    val startAttacking = {
      val delayedAttackState: SessionBehavior[AttackState] =
        SessionDBehavior.delayed(attackState)
      val canAttack = delayedAttackState.map(_ == AttackState.Available)
      val tryAttack = sessionInput.map(_.action).changes.dropIf(!_)
      canAttack.sampledBy(tryAttack).dropIf(!_)
    }

    val start = startAttacking.as(AttackState.attacking)

    val cool =
      Time.time[SessionTier].delay(start, 2.seconds).as(AttackState.cooling)
    val available =
      Time.time[SessionTier].delay(cool, 3.seconds).as(AttackState.available)

    start
      .unionRight(cool)
      .unionRight(available)
      .hold(AttackState.available)
  }

  val sessionPlayer: SessionDBehavior[Player] = {
    val queuedAttackingInput =
      sessionInput.map2(attackState.map(_ == AttackState.attacking)) {
        (in, att) =>
          in.copy(action = att)
      }

    val serverTickPlayerInformation =
      queuedAttackingInput.snapshotWith(elapsed) { (tup, time) =>
        val Input(dir, action) = tup
        (dir, time / 5, action)
      }

    serverTickPlayerInformation
    // FIXME: all players start at default pos
      .fold(Player.randomDefault(width, height)) {
        case (player, (dir, steps, action)) =>
          player
            .rotateTo(dir)
            .step(steps)
            .clamp(Vec2D.zero, Vec2D(width, height))
            .copy(attacking = action)
      }
  }

  val clientPosition: ClientDBehavior[Vec2D] = {
    val clientPlayer = SessionDBehavior.toClient(sessionPlayer)
    clientPlayer.map(_.position)
  }

  val camera: ClientDBehavior[Camera] = clientPosition.map { p =>
    val cameraWidth  = 600.0
    val cameraHeight = 400.0
    Camera(p - Vec2D(cameraWidth / 2, cameraHeight / 2),
           cameraWidth,
           cameraHeight)
  }

  val sessionStarted = ClientDBehavior.toSession(started)

  val activeClients: AppDBehavior[Map[Client, Player]] = {
    val startedPlayer = sessionStarted.map2(sessionPlayer) { (s, p) =>
      if (s) Some(p)
      else None
    }

    val delayedDead = SessionDBehavior.delayed(dead)
    val startedAlivePlayer = delayedDead.snapshotWith(startedPlayer) {
      (bool, optPlayer) =>
        if (bool) None else optPlayer
    }

    SessionDBehavior.toApp(startedAlivePlayer).map { playMap =>
      playMap.collect { case (c, Some(p)) => c -> p }
    }
  }

  val dead: SessionDBehavior[Boolean] = {
    val directions = SessionDBehavior.toApp(sessionInput.map(_.direction))
    val deadClients = activeClients.map2(directions) {
      (playerMap, directionMap) =>
        // naive pairwise comparison
        if (playerMap.size >= 2) {
          val pairs = playerMap.keys.toList.combinations(2)
          def clientIntersect(c1: Client, c2: Client) =
            playerMap(c1).hits(playerMap(c2))

          val attackedPlayers = pairs.map {
            case List(c1, c2) if clientIntersect(c1, c2) =>
              val p1 = playerMap(c1)
              val p2 = playerMap(c2)
              if (p1.attacking && p2.attacking) List(c1, c2)
              else if (p1.attacking) List(c2)
              else if (p2.attacking) List(c1)
              else List.empty
            case _ => List.empty
          }

          attackedPlayers.flatten.toSeq
        } else Seq.empty
    }

    val dcs = AppDBehavior.toSession(deadClients)
    val dead = SessionDBehavior.client
      .map2(dcs) { (c, cs) =>
        cs.toSet contains c
      }
      .changes

    // once dead, always dead
    dead.fold(false)((acc, n) => if (acc) acc else n)
  }

  val _ = sessionStarted
    .map2(dead)(_ && _)
    .snapshotWith(elapsed)(_ -> _)
    .fold(0L) {
      case (count, (dead, time)) =>
        if (dead) count else count + time
    }

  val timeSpentAlive: SessionDBehavior[Time] = {
    val startTime = Time
      .time[SessionTier]
      .now
      .sampledBy(EventUtil.removeDoubles(sessionStarted.changes))
    val stopTime = Time
      .time[SessionTier]
      .now
      .sampledBy(EventUtil.removeDoubles(dead.changes))

    startTime
      .unionRight(stopTime)
      .fold(0L) { (acc, n) =>
        n - acc
      }
  }

  val clientPlayers = SessionDBehavior.toClient(
    AppDBehavior.toSession(activeClients).map(_.values))
  val svgContent = clientPlayers.map { players =>
    logger.debug(s"players $players")
    players.toSeq.map(_.svg)
  }

  val animationCycle = new AnimationCycle
  val animationFrame = animationCycle.elapsedTime

  val attackingProgress: ClientDBehavior[Time] = {
    // TODO: FINISH SIMPLIFYING => Continue with adding Line and angle on Weapons
    //    val timedState =
    //      Time.time[SessionTier].now.snapshotWith(attackState) { _ -> _ }
    //    timedState.changes.fold(0L) { case (currentCount, (timeForState, state))
    //    => }

    val startAttacking =
      attackState.changes.dropIf(_ != AttackState.attacking)
    SessionDBehavior.toClient(progressToDeadline(startAttacking, 2.seconds))
  }

  val ui: ClientDBehavior[HTML] = svgFRP
    .svg(camera, svgContent)
    .sampledBy(animationFrame)
    .hold(UI.html.all.div())
    .map2(attackingProgress) { (game, progress) =>
      import UI.html.all._

      div(
        game,
        p(f"Attack: ${progress / 1000.0}%1.2fs")
      )
    }
}
