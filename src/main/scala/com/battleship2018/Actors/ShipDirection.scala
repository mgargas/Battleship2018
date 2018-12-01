package com.battleship2018.Actors

import scala.util.Random

object ShipDirection extends Enumeration{
  type ShipDirection = Value
  val UP = Value("U")
  val RIGHT = Value("R")
  val DOWN = Value("D")
  val LEFT = Value("L")

  def getRandomShipDirection: ShipDirection = ShipDirection.values.toList(Random.nextInt(ShipDirection.values.size))
}