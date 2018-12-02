package com.battleship2018

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.battleship2018.Actors._
import com.battleship2018.PlayerModes.{AIMode, CommandLineMode, PlayerMode}
import com.battleship2018.Utils.GameStatePrinter

import scala.concurrent.duration._
import scala.language.postfixOps


object Main extends App{

  implicit val timeout: Timeout = Timeout(5 seconds)
  val system = ActorSystem("BattleShip2018")
  val game = system.actorOf(Props(new Game()))


  val player1Actor = system.actorOf(Props(new Player(1, game)))
  val player2Actor = system.actorOf(Props(new Player(2, game)))

  println("BattleShip2018 \n" +
    "Choose game's mode:\n" +
    "1 ----> Player vs Player \n" +
    "2 ----> Player vs CPU \n"+
    "3 ----> CPU vs CPU \n")
  val gameMode = io.StdIn.readLine()
  val player1Mode: PlayerMode = gameMode match {
    case "1" => new CommandLineMode(player1Actor)
    case "2" => new CommandLineMode(player1Actor)
    case "3" => new AIMode(player1Actor)
  }
  val player2Mode: PlayerMode = gameMode match {
    case "1" => new CommandLineMode(player2Actor)
    case "2" => new AIMode(player2Actor)
    case "3" => new AIMode(player2Actor)
  }

  val gameStatePrinter = new GameStatePrinter()

  println("SHIPS PLACING IS STARTING NOW!")
  player1Mode.placeShips()
  player2Mode.placeShips()

  println("FIRING IS STARTING NOW!")
  var gameFinished = false
  var roundsCounter = 0
  while(!gameFinished){
    player1Mode.logPlayerTurn()
    val player1GameState = player1Mode.getGameState
    gameStatePrinter.print(player1GameState)
    val player1FireResult = player1Mode.fire()
    println("Result: " + player1FireResult)
    println("Press enter to change player...")
    scala.io.StdIn.readLine()

    player2Mode.logPlayerTurn()
    val player2GameState = player2Mode.getGameState
    gameStatePrinter.print(player2GameState)
    val player2FireResult = player2Mode.fire()
    println("Result: " + player2FireResult)
    println("Press enter to change player...")
    scala.io.StdIn.readLine()

    roundsCounter = roundsCounter + 1
    val player1Ships = player1Mode.getGameState.myBoardState.activeShips
    val player2Ships = player2Mode.getGameState.myBoardState.activeShips

    (player1Ships.size, player2Ships.size) match {
      case (0, 0) =>
        println("Tie")
        gameFinished = true
      case (0, _) =>
        println("Player2 won")
        gameFinished = true
      case (_, 0) =>
        println("Player1 won")
        gameFinished = true
      case _ => gameFinished = false
    }

  }
  println(s"Game finished after $roundsCounter rounds.")
  system.terminate()
}
