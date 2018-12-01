package com.battleship2018.Actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.battleship2018.Actors.Field._
import com.battleship2018.Actors.Ship.FireOnShip
import com.battleship2018.Utils.dto.{FireResult, Position}

import scala.concurrent.duration._

object Field{

  case object GetState
  case class PlaceShip(shipActor: ActorRef)
  case object ShipPlaced
  case object Fire
  case object Miss extends FireResult
  case object Hit extends FireResult

  sealed trait FieldState
  case object Active extends FieldState {
    override def toString: String = "A"
  }

  case object Inactive extends FieldState {
    override def toString: String = "I"
  }

  case object ActiveWithShip extends FieldState {
    override def toString: String = "S"
  }

  case object InactiveWithDestroyedShip extends FieldState {
    override def toString: String = "D"
  }
}

class Field(position: Position) extends Actor{
  import context.dispatcher
  implicit val timeout: Timeout = Timeout(5 seconds)
  def receive: Receive = active

  def active: Receive = {
    case GetState => sender() ! Active
    case PlaceShip(ship) =>
      (ship ? Ship.AddField(self)) pipeTo sender()
      context become activeWithShip(ship)
    case Fire =>
      sender() ! Miss
      context become inactive
    case _ => println("Field_active unhandled message")
  }

  def activeWithShip(shipActor: ActorRef): Receive = {
    case GetState => sender() ! ActiveWithShip
    case Fire =>
      (shipActor ? FireOnShip(self)) pipeTo sender()
      context become inactiveWithDestroyedShip
  }

  def inactive: Receive = {
    case GetState => sender() ! Inactive
    case Fire => sender() ! Miss
  }

  def inactiveWithDestroyedShip: Receive = {
    case GetState => sender() ! InactiveWithDestroyedShip
    case Fire => sender() ! Miss
  }
}
