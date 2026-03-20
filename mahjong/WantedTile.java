package mahjong;

public class WantedTile {
    int chance;
    int priority = 1;
    int suit;
    int rank;
    
    public WantedTile(int chance, int suit, int rank){
        this.chance = chance;
        this.suit = suit;
        this.rank = rank;
    }
}
