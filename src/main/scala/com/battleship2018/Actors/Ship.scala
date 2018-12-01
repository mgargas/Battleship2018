package com.battleship2018.Actors

import akka.actor.{Actor, ActorRef}
import com.battleship2018.Actors.Field.Hit
import com.battleship2018.Actors.Ship._

object Ship{
  case object GetLength
  case class AddField(fieldActor: ActorRef)
  case object FieldAdded
  case class FireOnShip(fieldActor: ActorRef)
  case class ShipDestroyed(length: Int, shipActor: ActorRef)

}

class Ship(val length: Int) extends Actor{

  def receive: Receive = init(List.empty[ActorRef])

  def init(fieldsList: List[ActorRef]): Receive = {
    case AddField(field: ActorRef) => {
      val newFieldsList = field :: fieldsList
      sender() ! FieldAdded
      if(newFieldsList.size == length) context become placed(newFieldsList)
      else context become init(newFieldsList)
    }
  }

  def placed(fields: List[ActorRef]): Receive = {
    case FireOnShip(fieldActor) =>
      val newFields = fields.filter(_ != fieldActor)
      if(newFields.isEmpty){
        sender() ! ShipDestroyed(length, self)
        context become destroyed
      }else{
        sender() ! Hit
        context become placed(newFields)
      }
    case GetLength => sender() ! length
  }

  def destroyed: Receive = {
    case _ => println("Ship is destroyed")
  }

}
