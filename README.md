<style>
p:has(+ ul) {
  margin-bottom: 0;
}
p + ul {
  margin-top: 0;
}
</style>

# Random Games in Terminal

**What is this?**  
This repository is a collection of the programs with replicate basic tabletop games or simple mobile games you may have played.  

Each game will be contained in a singular C++ script. They're implemented in as rudimentary of a fashion as possible. There is no use of any GUI system, so all graphics are printed directly in the terminal & interfacing with the game utilizes the keyboard only. 

**Note of Compatibility**  
All programs are written from the perspective of a windows user. For Mac device users, there may be code within a program that can prevent the game from running properly. Preferebly, compile the code with GCC rather than Clang.

**Purpose**  
All programs in this repository solely written by me for the purpose of coding practice. Feel free to download & play. Playing the games simply requires compiling & running the executable in a terminal. 

Enjoy :)

## 2048 (popular mobile game from 2014)

<!> *This program uses the library conio.h, which doesn't seem to be available on MacOS.*  
<!> *This program prints unicode character. Type execute `chcp 65001` in the terminal before running the game.*

**Coding Concepts Used:**  
Nothing special really. Just a clever way of using arrays & functions.  

**How to Play:**  
  - press WASD to shift tiles up, left, down, or right respectively. Press [space] to end the game immediately.
  - The game will reject a button press if the action does not move/merge any tiles in the grid.
  - LOSE: run out of space & cannot merge any tiles on the grid.
  - WIN: the game never ends until you lose

**Notable issues:**  
  - if a tile's number is becomes larger than 4 digits, the display of the grid will print improperly

## Mahjong

<!> *This program prints unicode character. Type execute `chcp 65001` in the terminal before running the game.*

**Coding Concepts Used:**  
  - objects & classes

**How to Play:**  
  - this program plays the standard Hong Kong style (look up a video tutorial)
  - execute program & answer the input prompts in terminal

**Notable Issue:**  


## Minesweeper

<!> *This program prints unicode character. Type execute `chcp 65001` in the terminal before running the game.*

Basic coding concepts used:
  - resursion
  - dynamic arrays
  - structs

What's included:
  - customization for minefield size & amount of mines
  - printing logic in terminal/console for a numbered grid minefield
  - clearing a white (0 bombs adjacent) tile automatically clears surrounding white tiles
  - choosing to clear an already cleared tile attempts to clear all adjacent tiles

How to play:
  - customize field size and number of mines
  - enter number for row, column, & action (c to clear, f to flag)
  - to lose: clear a mine
  - to win: flag all mines, or clear all safe tiles

Notable issues:
  - stack overflow if the minefield is set as a very large size

## Texas Hold'em

This program contains Windows commands, therefore it may have issues executing on MacOS.  
<!> *This program prints unicode character. Type execute `chcp 65001` in the terminal before running the game.*

Basic coding concepts used:
  - typedefs & pointers
  - nested for loop (printing logic)
  - Objects, Classes, & Structs

What's included:
  - poker hand/combination detection logic
  - system to determine the best hand
  - bots with rudimentary decision making (call, raise, fold, & go all in)
  - chips distribution logic after each round
  - printing logic in terminal/console for card graphics utilizing UNICODE characters 

How to play:
  - execute the program & read the prompts
  - make your decision & wait for the bots (you can game with just a numpad)
  - to lose: lose all of your chips
  - to win: steal everyone's chip & make them all leave  

Notable issues:
  - no option to do a "check"
  - small blind & big blind is not properly implemented amongst the bots
  - player whose turn is before the player who raises in a betting cycle will not need to raise during the river
  - some bots will continuously fold when there's few players remaining
