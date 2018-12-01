package com.battleship2018.PlayerModes

import akka.actor.ActorRef
import akka.pattern.ask
import com.battleship2018.Actors.Board.BoardState
import com.battleship2018.Actors.Game.PlaceShip
import com.battleship2018.Actors.{Board, Field, Game, ShipDirection}
import com.battleship2018.Main.timeout
import com.battleship2018.Utils.dto.{BoardSize, FireResult, Position, ShipLengths}

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.language.postfixOps
import scala.util.Random

object AIMode {
  def getBestFirePosition(gameState: Game.GameState): Position = {
    val opponentBoardState: BoardState = gameState.opponentBoardState
    var probabilityMap = (for(row <- 0 until BoardSize.rows; column <- 0 until BoardSize.columns)
      yield Position(row, column) -> 0) toMap

    for(shipLength <- opponentBoardState.activeShips){
      for(row <- 0 until BoardSize.rows){
        for(column <- 0 until BoardSize.columns){
          val startPosition = Position(row, column)
          val shipPositionsListHorizontal = Board.getShipPositionsList(startPosition, shipLength, ShipDirection.RIGHT)
          if(shipPositionsListHorizontal.forall(Board.verifyPosition)){
            val shipPositionsStatesList = shipPositionsListHorizontal.map(p => opponentBoardState.statesBoard(p))
            if(shipPositionsStatesList.forall(f => f == Field.Active)){
              for(position <- shipPositionsListHorizontal){
                val currentProbability = probabilityMap(position)
                probabilityMap = probabilityMap.updated(position, currentProbability + 1)
              }
            }
          }
          val shipPositionsListVertical = Board.getShipPositionsList(startPosition, shipLength, ShipDirection.UP)
          if(shipPositionsListVertical.forall(Board.verifyPosition)){
            val shipPositionsStatesList = shipPositionsListVertical.map(p => opponentBoardState.statesBoard(p))
            if(shipPositionsStatesList.forall(f => f == Field.Active)){
              for(position <- shipPositionsListVertical){
                val currentProbability = probabilityMap(position)
                probabilityMap = probabilityMap.updated(position, currentProbability + 1)
              }
            }
          }
        }
      }
    }
    probabilityMap.maxBy(_._2)._1
  }
}

class AIMode(val player: ActorRef) extends PlayerMode {
  def placeShips(): Boolean = {
    ShipLengths.shipLengths.map(shipLength => placeShip(shipLength)).forall(identity)
  }


  def fire(): FireResult = {
    val gameState = Await.result(ask(player, Game.GetState(player)).mapTo[Game.GameState], timeout.duration)
    import AIMode._
    val bestFirePosition = getBestFirePosition(gameState)
    println("Player" + playerID + s" is firing on (${bestFirePosition.row},${bestFirePosition.column})")
    val fireResult = Await.result((player ? Game.Fire(player, bestFirePosition)).mapTo[FireResult], timeout.duration)
    fireResult
  }


  @tailrec
  final def placeShip(length: Int): Boolean = {
    val row = Random.nextInt(BoardSize.rows)
    val column = Random.nextInt(BoardSize.columns)
    val shipDirection = ShipDirection.getRandomShipDirection
    val placingResult = Await.result(player ? PlaceShip(player, Position(row, column), length, shipDirection),
      timeout.duration).asInstanceOf[Boolean]
    if (placingResult) {
      true
    } else {
      placeShip(length)
    }
  }


}
