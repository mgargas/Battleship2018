package com.battleship2018.Utils

import com.battleship2018.Actors.{Field, Game}
import com.battleship2018.Utils.dto.{BoardSize, Position}

class GameStatePrinter {
  def print(gameState: Game.GameState): Unit = {
    val playerBoard: Map[Position, Field.FieldState] = gameState.myBoardState.statesBoard
    val playerShips: List[Int] = gameState.myBoardState.activeShips
    val opponentBoard: Map[Position, Field.FieldState] = gameState.opponentBoardState.statesBoard
    val opponentShips: List[Int] = gameState.opponentBoardState.activeShips
    println("MY STATES BOARD")
    println(buildBoardRepresentation(playerBoard))
    println("MY SHIPS: " + playerShips)
    println("OPPONENT'S STATES BOARD")
    println(buildBoardRepresentation(opponentBoard))
    println("OPPONENT'S SHIPS: " + opponentShips)

  }

  def buildBoardRepresentation(statesBoard: Map[Position, Field.FieldState]): String = {
    val builder = StringBuilder.newBuilder
    for(r <- (0 until BoardSize.rows).reverse){
      builder.append(r)
      for(c <- 0 until BoardSize.columns){
        builder.append(statesBoard(Position(r, c)).toString)
      }
      builder.append("\n")
    }
    builder.append("X")
    for(c <- 0 until BoardSize.columns) builder.append(c)
    builder.toString()
  }
}
