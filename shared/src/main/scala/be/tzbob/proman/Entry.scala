package be.tzbob.proman

case class Entry(id: Option[Long],
                 projectId: Long,
                 date: Long,
                 message: String,
                 done: Boolean) {
  import mtfrp.core.UI.html.all._
  val interface = div(p(message))
}
