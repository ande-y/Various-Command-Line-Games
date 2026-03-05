package mahjong;

import java.util.ArrayList;

abstract public class Player {
    protected final String name;
    protected ArrayList<Tile> hand = new ArrayList<>();
    protected ArrayList<Tile> bonusTiles = new ArrayList<>();
    protected int meldsPlacedDown = 0;
    protected boolean mahjong = false;

    public Player(String n){
        name = n;
    }

    public String getName(){ return name; }
    public boolean getMahjong(){ return mahjong; }

    public void debug(){
        for (Tile t: hand) System.out.print(t.getSymbol());
        System.out.println();
    }

    public boolean pickTile(Tile t){
        if (t.getSuit() >= 5) return true;
        hand.add(t);
        return false;
    }
    // public Tile dropTile(Tile t){
    //     return;
    // }

    abstract public void evaluate(Table table);
    abstract public Tile makeDecision(Table table);
    abstract public int askToSteal(int playThatCanChow);
}
