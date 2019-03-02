package be.tzbob.circleroyale

import java.util.concurrent.{Executors, TimeUnit}

import be.tzbob.circleroyale.Time.Time
import mtfrp.core._
import mtfrp.core.macros.server
import scala.concurrent.duration.FiniteDuration

class ServerTick(interval: FiniteDuration) {
  @server private[this] val ex = Executors.newSingleThreadScheduledExecutor()

  private[this] val rawServerTick: AppEventSource[Unit] =
    AppEvent.sourceWithEngineEffect[Unit] { (fire: Unit => Unit) =>
      ex.scheduleAtFixedRate(
        () => {
          fire(())
        },
        0,
        interval.toMillis,
        TimeUnit.MILLISECONDS
      )
    }

  val clock: AppDBehavior[Time] = Time
    .time[AppTier]
    .now
    .sampledBy(rawServerTick: AppEvent[_])
    .hold(0)

  val elapsedTime: AppEvent[Time] = AppDBehavior
    .delayed(clock)
    .snapshotWith(clock) { (prevTime, currentTime) =>
      currentTime - prevTime
    }
    .changes

  val sessionElapsedTime: SessionEvent[Time] = AppEvent.toSession(elapsedTime)
}
