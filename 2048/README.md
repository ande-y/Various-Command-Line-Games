## 2048 (popular mobile game from 2014)

<!> *This program uses the library conio.h, which doesn't seem to be available on MacOS.*  
<!> *This program prints unicode character. Execute `chcp 65001` in the terminal before running the game.*

**CODE CONCEPTS**  
  - Nothing special really. Just a few clever uses of arrays. 

**HOW TO PLAY**
  - press WASD to shift tiles up, left, down, or right respectively. Press [space] to end the game immediately.
  - The game will reject a button press if the action does not move/merge any tiles in the grid.
  - LOSE: run out of space & cannot merge any tiles on the grid.
  - WIN: the game never ends until you lose

**NOTABLE ISSUES**
  - if a tile's number is becomes larger than 4 digits, the display of the grid will print improperly