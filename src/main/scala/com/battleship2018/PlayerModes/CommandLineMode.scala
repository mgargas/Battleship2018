package com.battleship2018.PlayerModes

import akka.actor.ActorRef
import akka.pattern.ask
import com.battleship2018.Actors.Player
import com.battleship2018.Main.timeout
import com.battleship2018.Utils.CommandLineParser
import com.battleship2018.Utils.dto.{FireResult, ShipLengths}

import scala.concurrent.Await

class CommandLineMode(val player: ActorRef) extends PlayerMode {
  val commandLineParser = new CommandLineParser()

  def placeShips(): Boolean = {
    println("PLAYER" + playerID + " IS PLACING SHIPS")
    for(shipLength <- ShipLengths.shipLengths){
      var shipPlacingResult: Boolean = false
      do{
        println("Pass arguments to create a ship of length " + shipLength
          + ". Example: <0..9>,<0..9><UP/DOWN/RIGHT/LEFT>  0,1,UP ")
        val input = scala.io.StdIn.readLine()
        val shipPlaceMessage = commandLineParser.parsePlaceShipCommand(input, player, shipLength)
        shipPlacingResult = Await.result(player ? shipPlaceMessage, timeout.duration).asInstanceOf[Boolean]
        if(shipPlacingResult) println("Ship of length " + shipLength + " has been placed successfully")
        else println("Ship of length " + shipLength + " has not been placed. Try again.")
      }while(!shipPlacingResult)
    }
    true
  }

  def fire(): FireResult = {
    val playerID = Await.result(player ? Player.GetID, timeout.duration).asInstanceOf[Int]
    println("Player" + playerID + ", pass coordinates to fire. Example: 0,0 ")
    val input = scala.io.StdIn.readLine()
    val fireMessage = commandLineParser.parseFireCommand(input, player)
    println("Player" + playerID + s", is firing on (${fireMessage.position.row},${fireMessage.position.column}).")
    val fireResult = Await.result((player ? fireMessage).mapTo[FireResult], timeout.duration)
    fireResult
  }
}
