### Texas Hold'em

This program contains Windows commands, therefore it may have issues executing on MacOS.\
This program contains UNICODE, in terminal, use "chcp 65001" to display UNICODE characters properly

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
