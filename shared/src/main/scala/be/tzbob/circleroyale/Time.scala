package be.tzbob.circleroyale

import cats.effect.{IO, IOApp}
import mtfrp.core._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object Time {
  implicit private[this] val timer = IO.timer(ExecutionContext.global)

  type Time = Long

  trait HasTime[T <: Tier] {
    val now: T#Behavior[Time]
    def delay[A](ev: T#Event[A], duration: FiniteDuration): T#Event[A]
  }

  object HasTime {
    implicit lazy val clientTime = mkHasTime[ClientTier]
    implicit lazy val appTime    = mkHasTime[AppTier]
    implicit lazy val sessionTime = new HasTime[SessionTier] {
      val now: SessionBehavior[Time] = AppBehavior.toSession(appTime.now)
      def delay[A](ev: SessionEvent[A],
                   duration: FiniteDuration): SessionEvent[A] = {
        val df = mkDelay[SessionTier, A]
        df(ev, duration)
      }
    }
  }

  private[this] def mkTime[T <: Tier: Tier.Concrete]: T#Behavior[Time] =
    Tier.tier[T].Behavior.fromPoll(() => System.currentTimeMillis())

  private[this] def mkDelay[T <: Tier: Tier.Concrete, A]
    : (T#Event[A], FiniteDuration) => T#Event[A] = (ev, dur) => {
    val tier = Tier.tier[T]

    import tier.Event._
    tier.Async.execute(ev.map { e =>
      IO.sleep(dur).as(e)
    })
  }

  private[this] def mkHasTime[T <: Tier: Tier.Concrete]: HasTime[T] =
    new HasTime[T] {
      val tier = Tier.tier[T]

      val now: T#Behavior[Time] = mkTime[T]
      def delay[A](ev: T#Event[A], duration: FiniteDuration): T#Event[A] = {
        val df = mkDelay[T, A]
        df(ev, duration)
      }
    }

  def time[T <: Tier: HasTime]: HasTime[T] = implicitly[HasTime[T]]
}
