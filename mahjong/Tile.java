package mahjong;

public class Tile {
    private int suit;
    private int rank;
    private String symbol;

    public Tile(int s, int r, String sym){
        suit = s;
        rank = r;
        symbol = sym;
    }

    public int getSuit(){ return suit; }
    public int getRank(){ return rank; }
    public String getSymbol(){ return symbol; }
}
