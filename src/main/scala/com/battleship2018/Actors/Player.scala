package com.battleship2018.Actors

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.battleship2018.Actors.Player.GetID

import scala.concurrent.duration._
import scala.language.postfixOps

object Player{
  final case object GetID
}

class Player(id: Int, game: ActorRef) extends Actor{

  import context.dispatcher
  implicit val timeout: Timeout = Timeout(5 seconds)
  override def receive: Receive = {
    game ! Game.ConnectPlayer
    play
  }

  def play: Receive = {
    case Game.PlaceShip(self, position, length, shipDirection) =>
      (game ? Game.PlaceShip(self, position, length, shipDirection)) pipeTo sender()
    case Game.GetState(player) =>
      (game ? Game.GetState(self)) pipeTo sender()
    case Game.Fire(player, position) =>
      (game ? Game.Fire(self, position)) pipeTo sender()
    case GetID => sender() ! id
  }
}
