package be.tzbob.circleroyale
import hokko.core.tc.{Event, Snapshottable}

object EventUtil {

  def removeDoublesBy[Ev[_], IBeh[_, _], A](eq: (A, A) => Boolean,
                                            event: Ev[A])(
      implicit
      ev: Event[Ev, IBeh],
      snapshottable: Snapshottable[IBeh[?, A], Ev]): Ev[A] = {

    val folded = ev.fold(event, Option.empty[A] -> Option.empty[A]) {
      (acc, n) =>
        val right = acc match {
          case (Some(acc), _) if eq(acc, n) => None
          case _                            => Some(n)
        }
        Some(n) -> right
    }

    val result: Ev[(Option[A], Option[A])] =
      snapshottable.sampledBy(folded, event)

    ev.collect(ev.map(result)(_._2))(identity)
  }

  def removeDoubles[Ev[_], IBeh[_, _], A](event: Ev[A])(
      implicit
      ev: Event[Ev, IBeh],
      snapshottable: Snapshottable[IBeh[?, A], Ev]): Ev[A] =
    removeDoublesBy(_ == _, event)
}
