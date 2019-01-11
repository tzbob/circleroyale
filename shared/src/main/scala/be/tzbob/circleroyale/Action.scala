package be.tzbob.circleroyale

import mtfrp.core.UI

sealed trait Action

case class Attack(item: Weapon) extends Action

object Action {

}

