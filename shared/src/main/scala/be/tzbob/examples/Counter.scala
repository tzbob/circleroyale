package be.tzbob.examples

import mtfrp.core.{ClientDBehavior, ClientEvent, ClientEventSource, UI}

class Counter(inc: Int,
              dec: Int,
              fold: ClientEvent[Int] => ClientDBehavior[Int]) {
  import UI.html.all._

  private[this] val incInput = ClientEvent.source[Int]
  private[this] val decInput = ClientEvent.source[Int]

  private def mkButton(src: ClientEventSource[Int], txt: String, v: Int) =
    button(UI.listen(onclick, src)(_ => v), txt)

  val incButton = mkButton(incInput, "Increment", inc)
  val decButton = mkButton(decInput, "Decrement", dec)

  val state = fold(incInput.unionWith(decInput)(_ + _))

  val ui: ClientDBehavior[UI.HTML] = state.map(
    v =>
      div(
        div("Current count: ", span(v)),
        div(incButton, decButton)
    ))
}
