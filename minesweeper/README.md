### Minesweeper

This program contains Windows commands, not compatible with MacOS.\

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