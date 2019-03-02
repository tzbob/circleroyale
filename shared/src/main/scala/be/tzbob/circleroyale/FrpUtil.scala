package be.tzbob.circleroyale

import mtfrp.core._

object FrpUtil {
  def throttle[T <: Tier: Tier.Concrete, A](src: Event[T, A],
                                            target: Event[T, _]): T#Event[A] = {
    throttleWith(src, target)
      .collect {
        case (a, _) => Some(a)
      }
      .asInstanceOf[T#Event[A]]
  }

  def throttleWith[T <: Tier: Tier.Concrete, A, B](
      src: Event[T, A],
      target: Event[T, B]): T#Event[(A, B)] = {

    val tier = Tier.tier[T]
    import tier.Event._
    import tier.IBehavior._
    import cats.syntax.all._

    val s = src.asInstanceOf[T#Event[A]]
    val t = target.asInstanceOf[T#Event[B]]

    tier.DBehavior.mtfrpDBehaviorInstances
      .snapshotWith(tier.Event.mtfrpEventInstances
                      .map(s)(Option.apply)
                      .hold(None: Option[A]),
                    t) {
        case (Some(a), b) => Some(a -> b)
        case (None, _)    => None
      }
      .collect(identity)
      .asInstanceOf[T#Event[(A, B)]]
  }
}
