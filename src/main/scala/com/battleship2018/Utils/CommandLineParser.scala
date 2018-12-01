package com.battleship2018.Utils

import akka.actor.ActorRef
import com.battleship2018.Actors.{Game, ShipDirection}
import com.battleship2018.Utils.dto.Position

class CommandLineParser {
  def parsePlaceShipCommand(input: String, player: ActorRef, shipLength: Int): Game.PlaceShip = {
    val arguments = input.split(",")
    val row = arguments(0).toInt
    val column = arguments(1).toInt
    val direction = arguments(2) match {
      case "UP" => ShipDirection.UP
      case "DOWN" => ShipDirection.DOWN
      case "LEFT" => ShipDirection.LEFT
      case "RIGHT" => ShipDirection.RIGHT
    }
    Game.PlaceShip(player, Position(row, column), shipLength, direction)
  }

  def parseFireCommand(input: String, player: ActorRef): Game.Fire = {
    val arguments = input.split(",")
    val row = arguments(0).toInt
    val column = arguments(1).toInt
    Game.Fire(player, Position(row, column))
  }
}
