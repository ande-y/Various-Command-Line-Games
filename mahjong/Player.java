package mahjong;

import java.util.ArrayList;

abstract public class Player {
    protected final String name;
    protected ArrayList<Tile> hand = new ArrayList<>();
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

    public void pickTile(Tile t){
        hand.add(t);
    }
    // public Tile dropTile(Tile t){
    //     return;
    // }
    abstract public Tile makeDecision();
    abstract public int askToSteal(int playThatCanChow);
}
