Battleship2018 
===========================================================
An implementation of Battleship, which is a guessing game for two players, using Scala and Akka Actors.
## The Basic Rules
This game is played by 2 players on two grids with 5 ships each.

## The Grid
* The grid is made up of 10 rows and 10 columns.
* One grid has to be hidden from a player's opponent and display's the players ship placement.
* The other grid is for the opponent to aim at and fire at a player's ship

## The Ships
* Each player gets 5 ships broken down into the following
	* One 5 length ship
	* One 4 length ship
	* Two 3 length ship
	* One 2 length ship
* The player can place a ship on 'his/her' grid (the one not visible to the opponents).
* Each ship will occupy the number of squares for it's size so a 5 length ship will occupy 5 squares on the grid.
* A ship can be placed either vertically or horizontally and cannot hang off the grid nor can they be placed diagonally.
* Ships can be placed side by side or any distance apart as long as the previous rule is not broken, but they cannot overlap.

## Gameplay
* Players take turns to fire shots at their opponent ships. On the grid that does not display that opponent's ships.

* They do so by guessing where an opponent's ships might be and their aim is to completely obliterate a ship.

* A player marks a shot on his/her grid and the system must let that player know if it was a hit or a miss. 

* A ship is sunk if all the squares it occupies are hit.

* A distinction must be made between hits and misses.

## Game's modes
* Player - user places own ships and fires on opponent's ships using commandline 
* CPU - commands are carried out using AI. AI places ships randomly and analyses possible ships settings to constructs 
probability density functions and fire at the best field. 

## Usage

```sh
$ sbt run
```

