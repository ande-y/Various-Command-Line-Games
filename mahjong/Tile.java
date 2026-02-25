package mahjong;

public class Tile {
    private final int suit;
    private final int rank;
    private final String symbol;

    public Tile(int s, int r, String sym){
        suit = s;
        rank = r;
        symbol = sym;
    }

    public int getSuit(){ return suit; }
    public int getRank(){ return rank; }
    public String getSymbol(){ return symbol; }
}
