package mahjong;

import java.util.ArrayList;

public class Meld {
    int type;
    int suit;
    int rank;
    ArrayList<Tile> tiles = new ArrayList<>();
    ArrayList<WantedTile> wantedTiles = new ArrayList<>();
    public Meld(int type, int suit, int rank){
        this.type = type;
        this.suit = suit;
        this.rank = rank;
    }
    public Meld clone(){
        Meld m = new Meld(type, suit, rank);
        for (Tile t: tiles) m.tiles.add(t);
        return m;
    }
}
