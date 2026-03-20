package mahjong;

import java.util.ArrayList;

public class Meld {
    private MeldType type;
    private int suit;
    private int rank;
    public ArrayList<Tile> tiles = new ArrayList<>();
    public ArrayList<WantedTile> wantedTiles = new ArrayList<>();

    public Meld(MeldType type, int suit, int rank){
        this.type = type;
        this.suit = suit;
        this.rank = rank;
    }

    public MeldType getType(){ return type; }
    public int getSuit(){ return suit; }
    public int getRank(){ return rank; }
    // public ArrayList<Tile> getTiles(){ return tiles; }
    // public ArrayList<WantedTile> getWantedTiles(){ return wantedTiles; }

    public Meld clone(){
        Meld m = new Meld(type, suit, rank);
        for (Tile t: tiles) m.tiles.add(t);
        return m;
    }
}
