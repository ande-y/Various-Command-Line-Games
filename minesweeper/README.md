## Minesweeper

<!> *This program prints unicode character. Execute `chcp 65001` in the terminal before running the game.*

**CODE CONCEPTS**
  - resursion
  - dynamic arrays
  - structs

**HOW TO PLAY**
  - customize field size and number of mines
  - enter number for row, column, & action (c to clear, f to flag)
  - clearing a white (0 bombs adjacent) tile automatically clears surrounding white tiles
  - choosing to clear an already cleared tile attempts to clear all adjacent tiles
  - TO LOSE: clear a mine
  - TO WIN: flag all mines, or clear all safe tiles

**NOTABLE ISSUES**
  - stack overflow if the minefield is set as a very large size
