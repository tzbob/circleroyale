package be.tzbob.circleroyale

trait AttackState

object AttackState {
  case object Attacking extends AttackState
  case object Cooling   extends AttackState
  case object Available extends AttackState

  val attacking: AttackState = Attacking
  val cooling: AttackState = Cooling
  val available: AttackState = Available
}
