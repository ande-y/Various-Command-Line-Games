package mahjong;

import java.util.ArrayList;
import java.util.Random;

public class Table {
    private ArrayList<Tile> wall = new ArrayList<>();
    private String[][] tileIndex = 
        {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
         {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
         {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
         {"🀀", "🀁", "🀂", "🀃"},
         {"🀆", "🀅", "🀄"},
         {"🀢", "🀣", "🀤", "🀥"},
         {"🀦", "🀧", "🀨", "🀩"}};
    private ArrayList<Tile> discardedTiles = new ArrayList<>();
    private int[][] discardedTilesCounter = 
        {{0, 0, 0, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0},
         {0, 0, 0}};

    public Table(){
        // create all the tile
        ArrayList<Tile> unshuffled = new ArrayList<>();
        for (int i = 0; i < 7; i++){
            for (int j = 0; j < 9; j++){
                if ((i > 2 && j > 3) || (i == 4 && j == 3)) continue;
                Tile newTile = new Tile(i, j, tileIndex[i][j]);
                if (i <= 4){
                    for (int k = 0; k < 4; k++) unshuffled.add(newTile);
                }
                else unshuffled.add(newTile);
            }
        }

        // shuffle those tiles & place them into the wall
        Random random = new Random();
        while (!unshuffled.isEmpty()){
            int randPick = random.nextInt(unshuffled.size());
            wall.add(unshuffled.get(randPick));
            unshuffled.remove(randPick);
        }

        // for (Tile t: wall) System.out.print(t.symbol);
        // System.out.println(wall.size());
    }

    public ArrayList<Tile> getdiscardedTiles(){ return discardedTiles; }
    public int[][] getDiscardedTilesCounter(){ return discardedTilesCounter; }

    public Tile giveTile(){
        return wall.remove(wall.size() - 1);
    }
    public void takeDiscard(Tile t){
        discardedTiles.add(t);
        discardedTilesCounter[t.getSuit()][t.getRank()]++;
    }
}
