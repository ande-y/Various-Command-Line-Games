package mahjong;

import java.util.Scanner;

public class UserPlayer extends Player {
    public UserPlayer(String n){
        super(n);
    }

    public Tile makeDecision(){
        Scanner scan = new Scanner(System.in);
        int index;
        do {
            index = scan.nextInt();
        } while (index < 1 || index > hand.size());
        scan.close();

        return hand.remove(index - 1);
    }

    public int askToSteal(int playThatCanChow){
        return 1;
    }
}
