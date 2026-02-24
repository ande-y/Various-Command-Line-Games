package mahjong;

import java.util.Scanner;

public class UserPlayer extends Player {
    public UserPlayer(){
        super();
    }

    public Tile makeDecision(){
        Scanner scan = new Scanner();
        int index = scan.nextInt();

        return hand.remove(index + 1);
    }

    public int askToSteal(){
        return 1;
    }
}
