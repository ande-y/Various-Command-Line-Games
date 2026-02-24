package mahjong;

import java.util.ArrayList;

public class Player {
    protected final String name;
    protected ArrayList<Tile> hand = new ArrayList<>();
    
    public Player(String n){
        name = n;
    }

    public String getName(){ return name; }

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
}
