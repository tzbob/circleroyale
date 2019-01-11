package be.tzbob.circleroyale

import be.tzbob.circleroyale.Time.Time
import hokko.core.Engine
import mtfrp.core._
import mtfrp.core.macros.client

class IntervalCycle(interval: Int) {

  private[this] var stopToken = 0

  private[this] val rawAnimationFrame: ClientEventSource[Unit] =
    ClientEvent.sourceWithEngineEffect[Unit] { (fire: Unit => Unit) =>
      println(s"executing effects")
      startInterval(fire)
    }

  val clock: ClientDBehavior[Time] = Time
    .time[ClientTier]
    .now
    .sampledBy(rawAnimationFrame: ClientEvent[_])
    .hold(0)
    .toDBehavior

  val elapsedTime: ClientEvent[Time] =
    ClientDBehavior
      .delayed(clock)
      .snapshotWith(clock) { (prevTime, currentTime) =>
        currentTime - prevTime
      }
      .changes

  @client private[this] val startInterval = (fire: Unit => Unit) => {
    import org.scalajs.dom
    println(s"Starting interval for $interval ms")
    stopToken = org.scalajs.dom.window.setInterval(() => {
      fire(())
    }, interval)
  }

  @client private[this] val stopInterval = () => {
    import org.scalajs.dom
    dom.window.clearInterval(stopToken)
  }
}
