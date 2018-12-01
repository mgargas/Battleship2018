package com.battleship2018.Utils

import com.battleship2018.Actors.ShipDirection
import com.battleship2018.Actors.ShipDirection.ShipDirection

object dto {
  trait FireResult
  final case class Position(row: Int, column: Int){
    def nextPosition(shipDirection: ShipDirection): Position = {
      shipDirection match {
        case ShipDirection.UP => Position(row + 1, column)
        case ShipDirection.RIGHT => Position(row, column + 1)
        case ShipDirection.DOWN => Position(row - 1, column)
        case ShipDirection.LEFT => Position(row, column - 1)
      }
    }
  }
  final case object ShipAmount{
    val amount: Int = ShipLengths.shipLengths.size
  }
  final case object ShipLengths{
    val shipLengths = List(2, 3, 3, 4, 5)
  }
  final case object BoardSize{
    val rows = 10
    val columns = 10
  }
  final case object PlayersAmount{
    val amount = 2
  }
}
