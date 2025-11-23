## Poker / Texas Hold'em

### `poker.cpp`

<!> *This program contains the `system("cls")` Windows command, replace it with `system("clear")` if you're on MacOS.*  
<!> *This program prints unicode character. Execute `chcp 65001` in the terminal before running the game on Windows.*

**CODE CONCEPTS**
  - typedefs & pointers
  - nested for loop (printing logic)
  - Objects, Classes, & Structs

**HOW TO PLAY**
  - execute the program & read the prompts
  - make your decision & wait for the bots (you can game with just a numpad)
  - TO LOSE: lose all of your chips
  - TO WIN: steal everyone's chip & make them all leave  

**NOTABLE ISSUES**
  - no option to do a "check"
  - small blind & big blind is not properly implemented amongst the bots
  - player whose turn is before the player who raises in a betting cycle will not need to raise during the river
  - some bots will continuously fold when there's few players remaining

 ### `pokerV2.cpp`

<!> *This program contains the `system("cls")` Windows command, replace it with `system("clear")` if you're on MacOS.*  
<!> *This program prints unicode character. Execute `chcp 65001` in the terminal before running the game on Windows.*

**WHAT'S DIFFERENT**  
This is a complete rewrite of `poker.cpp`, though the game plays mostly the same & many portions of this newer program are recognizably similar. This version's code is much cleaner & far better in terms of readability. This version removes unnecessary `for` statements, uses better variable naming, avoids magic numbers via `#define` keywords, includes comments, & eliminates global variables in favor of objects.

**WHAT'S NEW**  
  - changed the UI/graphics a little bit
  - added the option to do a "check"
  - properly implemented realistic betting rounds