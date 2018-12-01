package com.battleship2018.PlayerModes

import akka.actor.ActorRef
import akka.pattern.ask
import com.battleship2018.Actors.{Game, Player}
import com.battleship2018.Main.timeout
import com.battleship2018.Utils.dto.FireResult

import scala.concurrent.Await
trait PlayerMode {
  val player: ActorRef
  val playerID: Int = Await.result(player ? Player.GetID, timeout.duration).asInstanceOf[Int]
  def logPlayerTurn(): Unit = println("PLAYER" + playerID + "'S TURN------------------------")
  def placeShips(): Boolean
  def fire(): FireResult
  def getGameState: Game.GameState = {
    Await.result(ask(player, Game.GetState(player)).mapTo[Game.GameState], timeout.duration)
  }
}
