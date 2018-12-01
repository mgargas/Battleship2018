package com.battleship2018.Actors

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.battleship2018.Actors.Board._
import com.battleship2018.Actors.Field.{FieldState, Hit, Miss}
import com.battleship2018.Actors.Ship.FieldAdded
import com.battleship2018.Actors.ShipDirection.ShipDirection
import com.battleship2018.Utils.dto.{BoardSize, FireResult, Position, ShipAmount}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object Board{
  case object GetState
  case class PlaceShip(position: Position, length: Int, shipDirection: ShipDirection)
  case class ShipValidated(shipPositionsList: List[Position], isValid: Boolean)
  case class ShipPlaced(shipActor: ActorRef, isPlaced: Boolean)
  case class Fire(position: Position)
  case class ShipDestroyed(length: Int) extends FireResult
  case class BoardState(statesBoard: Map[Position, FieldState], activeShips: List[Int])

  def verifyPosition(position: Position): Boolean =
    (0 until BoardSize.rows contains position.row) && (0 until BoardSize.columns contains position.column)


  def getShipPositionsList(startPosition: Position, shipLength: Int, shipDirection: ShipDirection): List[Position] = {
    (1 until shipLength).foldLeft(List(startPosition))((list, _) =>  list.head.nextPosition(shipDirection) :: list)
      .reverse
  }
}

class Board(val rows: Int, val columns: Int) extends Actor{
  import context.dispatcher
  implicit val timeout: Timeout = Timeout(5 seconds)
  def receive: Receive = buildBoard

  def buildBoard: Receive = {
    val fields = for(r <- 0 until rows; c <- 0 until columns)
      yield Position(r, c) -> context.actorOf(Props(new Field(Position(r, c))))
    readyForPlacingShips(fields.toMap, List.empty[ActorRef])
  }

  def readyForPlacingShips(fields: Map[Position, ActorRef], ships: List[ActorRef]): Receive = {
    case GetState =>
      getBoardState(fields, ships) pipeTo sender() pipeTo sender()
    case PlaceShip(position, length, shipDirection) =>
      validateShip(position, length, shipDirection, fields)
        .map(validationResult => ShipValidated(validationResult._1, validationResult._2))
        .pipeTo(self)(sender())
      context become waitForShipValidation(fields, ships)
  }

  def waitForShipValidation(fields: Map[Position, ActorRef], ships: List[ActorRef]): Receive = {
    case ShipValidated(shipPositionsList, true) =>
      placeShip(fields, shipPositionsList)
        .map(placingResult => ShipPlaced(placingResult._1, placingResult._2))
        .pipeTo(self)(sender)
      context become waitForShipPlacing(fields, ships)
    case ShipValidated(_, false) =>
      sender() ! false
      context become readyForPlacingShips(fields, ships)

  }

  def waitForShipPlacing(fields: Map[Position, ActorRef], ships: List[ActorRef]): Receive = {
    case ShipPlaced(shipActor, true) =>
      sender ! true
      val newShipsList = shipActor :: ships
      if(newShipsList.length == ShipAmount.amount){
        context become play(fields, newShipsList)
      }else{
        context become readyForPlacingShips(fields, newShipsList)
      }
    case ShipPlaced(_, false) =>
      sender() ! false

  }

  def play(fields: Map[Position, ActorRef], ships: List[ActorRef]): Receive = {
    case GetState =>
      getBoardState(fields, ships) pipeTo sender()
    case Fire(position) =>
      if(fields.contains(position)) {
        (fields(position) ? Field.Fire).pipeTo(self)(sender())
        context become fire(fields, ships)
      }
      else{
        sender() ! Miss
      }
  }

  def fire(fields: Map[Position, ActorRef], ships: List[ActorRef]): Receive = {
    case Miss =>
      sender() ! Miss
      context become play(fields, ships)
    case Hit =>
      sender() ! Hit
      context become play(fields, ships)
    case Ship.ShipDestroyed(length, shipActor) =>
      val newShips = ships.filter(_ != shipActor)
      sender() ! ShipDestroyed(length)
      context become play(fields, newShips)
  }


  def validateShip(position: Position, length: Int,
                   shipDirection: ShipDirection, fields: Map[Position, ActorRef]): Future[(List[Position], Boolean)] = {
    val shipPositionsList = getShipPositionsList(position, length, shipDirection)
    if(!shipPositionsList.forall(p => verifyPosition(p))) return Future.successful((shipPositionsList, false))
    val futureList = Future.traverse(shipPositionsList)(p => ask(fields(p), Field.GetState))
    val validationResult = for{
      isValid <- futureList.map(_.forall(state => state == Field.Active))
    } yield (shipPositionsList, isValid)
    validationResult
  }


  def placeShip(fields: Map[Position, ActorRef], shipPositionsList: List[Position]): Future[(ActorRef, Boolean)] = {
    val shipActor = context.actorOf(Props(new Ship(shipPositionsList.size)))
    val futureList = Future.traverse(shipPositionsList)(p => ask(fields(p), Field.PlaceShip(shipActor)))
    val placingResult = for{
      isPlaced <- futureList.map(_.forall(result => result == FieldAdded))
    } yield (shipActor, isPlaced)
    placingResult
  }


  def getBoardState(fields: Map[Position, ActorRef], ships: List[ActorRef]): Future[BoardState] = {
    val fieldsStates = fields.map { case (k, v) => k -> ask(v, Field.GetState).mapTo[FieldState] }
    val fieldsStatesFutureList = Future.traverse(fieldsStates) { case (k, fv) => fv.map(k -> _) } map(_.toMap)
    val activeShipsFutureList = Future.traverse(ships)(shipActor => ask(shipActor, Ship.GetLength).mapTo[Int])
    val getBoardStateResult = for {
      statesBoard <- fieldsStatesFutureList.mapTo[Map[Position, FieldState]]
      activeShips <- activeShipsFutureList.mapTo[List[Int]]
    } yield BoardState(statesBoard, activeShips)
    getBoardStateResult
  }
}
