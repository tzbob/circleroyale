package be.tzbob.circleroyale

import hokko.core.Engine
import mtfrp.core._
import mtfrp.core.macros.client

class AnimationCycle {
  type Time = Double

  @client private[this] var oldTime = {
    import org.scalajs.dom
    dom.window.performance.now()
  }
  private[this] var stopToken = 0
  private[this] val rawAnimationFrame: ClientEventSource[Time] =
    ClientEvent.sourceWithEngineEffect[Time] { (fire: Time => Unit) =>
      println(s"executing effects")
      startAnimation(fire)
    }

  val elapsedTime: ClientEvent[Time] = rawAnimationFrame

  @client private[this] val nextAnimationFrame: (Time => Unit) => Time => Int =
    (fire: Time => Unit) =>
      (time: Time) => {
        import org.scalajs.dom

        val timeDiff = time - oldTime
        oldTime = time

        fire(timeDiff)

        val stopAnimationToken =
          dom.window.requestAnimationFrame(nextAnimationFrame(fire))

        stopAnimationToken
    }

  @client private[this] val startAnimation = (fire: Time => Unit) => {
    stopToken = nextAnimationFrame(fire)(0)
  }

  @client private[this] val stopAnimation = () => {
    import org.scalajs.dom
    dom.window.cancelAnimationFrame(stopToken)
  }

}
