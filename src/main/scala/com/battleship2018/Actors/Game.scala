package com.battleship2018.Actors

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.battleship2018.Actors.Game._
import com.battleship2018.Actors.ShipDirection.ShipDirection
import com.battleship2018.Utils.dto.{BoardSize, PlayersAmount, Position}

import scala.concurrent.duration._
import scala.language.postfixOps



object Game {
  sealed trait Message
  case class PlaceShip(player: ActorRef, position: Position, length: Int, shipDirection: ShipDirection) extends Message
  case class Fire(player: ActorRef, position: Position) extends Message
  case class GetState(player: ActorRef) extends Message
  case object ConnectPlayer extends Message
  case class GameState(myBoardState: Board.BoardState, opponentBoardState: Board.BoardState) extends Message
}

class Game() extends Actor{
  import context.dispatcher
  implicit val timeout: Timeout = Timeout(5 seconds)

  override def receive: Receive = connectPlayers(Map.empty[ActorRef, ActorRef])

  def connectPlayers(playersWithBoards: Map[ActorRef, ActorRef]): Receive = {
    case ConnectPlayer =>
      if(!playersWithBoards.contains(sender())){
        val newPlayerWithBoard =  sender() -> context.actorOf(Props(new Board(BoardSize.rows, BoardSize.columns)))
        val newPlayersWithBoards = playersWithBoards + newPlayerWithBoard
        if(newPlayersWithBoards.size == PlayersAmount.amount) context become play(newPlayersWithBoards)
        else context become connectPlayers(newPlayersWithBoards)
      }
    case a => println(a)
  }


  def play(playersWithBoards: Map[ActorRef, ActorRef]): Receive = {
    case PlaceShip(player, position, length, shipDirection) =>
      (playersWithBoards(player) ? Board.PlaceShip(position, length, shipDirection)) pipeTo sender()
    case GetState(player) =>
      val opponent: ActorRef = playersWithBoards.keys.filter(_ != player).head
      val result = for{
        myBoardState <- (playersWithBoards(player) ? Board.GetState).mapTo[Board.BoardState]
        opponentBoardState <- (playersWithBoards(opponent) ? Board.GetState).mapTo[Board.BoardState]
      } yield GameState(myBoardState, opponentBoardState
        .copy(statesBoard = opponentBoardState.statesBoard
          .mapValues(state => if (state == Field.ActiveWithShip) Field.Active else state)))
      result pipeTo sender()
    case Fire(player, position) =>
      val opponent: ActorRef = playersWithBoards.keys.filter(_ != player).head
      (playersWithBoards(opponent) ? Board.Fire(position)) pipeTo sender()
    case _ => println("...")
  }
}
