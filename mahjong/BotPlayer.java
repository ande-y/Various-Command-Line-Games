package mahjong;

import java.util.ArrayList;

public class BotPlayer extends Player{
    public static final int CHOW = 0;
    public static final int EYES = 1;
    public static final int PONG = 2;
    public static final int KONG = 3;

    public static class meldGroup {
        public meldGroup(int m, int s){
            meldType = m;
            startingTile = s;
        }
        int meldType;
        int startingTile;
    }

    public BotPlayer(String n){
        super(n);
    }

    public int askToSteal(int playThatCanChow){
        return 1;
    }

    public Tile makeDecision(){
        int[][] tileCounter = 
            {{0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0},
             {0, 0, 0}};

        for (Tile t: hand){
            if (t.getSuit() >= 5) continue;
            tileCounter[t.getSuit()][t.getRank()]++;
        }
        for (int i = 0; i < 5; i++){
            for (int j = 0; j < tileCounter[i].length; j++){
                System.out.print(tileCounter[i][j] + " ");
            }
            System.out.println();
        }

        // run evaluations for each suit disjointly
        // for (int i = 0; i < 3; i++){
        //     int[] tileCounterSubset = tileCounter[i];

        //     // in order, scan the subset for melds
        //     // a1. eyes, 2 identicals, implies if no eyes = no pongs
        //     // a2. pongs, 3 identicals, implies if no pongs = no kongs
        //     // a3. kong, 4 identicals
        //     // b1. partial neighbor chows [n, n+1], implies if no this = no complete chows
        //     // b2. complete chows, 3 of same suit in sequence 
        //     // c. partial separated chow, tile missing in middle [n, n+2]
        //     //      OR terminal chows, neighboring chows with terminals [1, 2] or [8, 9]
            
        //     scanDupes(tileCounterSubset, 0, 4);
            // ArrayList<ArrayList<meldGroup>> possibleRoutes; 
        // }

        /// test craks only 
        int[] tileCounterSubset = tileCounter[0];
        ArrayList<meldGroup> possibleMelds = new ArrayList<>(); 
        scanDupes(possibleMelds, tileCounterSubset, 0, 4);

        return hand.remove(0);
    }

    public void scanDupes(ArrayList<meldGroup> possibleMelds, int[] tileCounterSubset, int startIndex, int copies){
        System.out.print(copies + " ");

        int chosenIndex = -1;
        for (int i = startIndex; i < 9; i++){
            if (tileCounterSubset[i] >= copies){
                chosenIndex = i;
                tileCounterSubset[i] -= copies;
                possibleMelds.add(new meldGroup(copies, i));
                System.out.print("melded ");

                scanDupes(possibleMelds, tileCounterSubset, i, copies);
            }
            if (chosenIndex != -1){
                // "release" the tiles, undo the set afterwards
                // this allows backtracking for all possible permutations sets of melds 
                tileCounterSubset[chosenIndex] += copies;
                possibleMelds.remove(possibleMelds.size() - 1);
                chosenIndex = -1;
            }
        }

        // if theres no sets where made, recursion doesnt happen
        // if no kongs, start looking for pongs, then eyes... so & so on
        if (copies > 2) scanDupes(possibleMelds, tileCounterSubset, 0, copies - 1);
        // else scanChow(possibleMelds, tileCounterSubset);
    }

// ------------------

    public void scanChow(int[] tileCounterSubset, int start){
        for (int i = start; i < 8; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;
            tileCounterSubset[i + 2] -= 1;
            if (i < 7) scanChow(tileCounterSubset, i + 1);
        }
        scanJointPartialChow(tileCounterSubset, 1);
    }

    public void scanJointPartialChow(int[] tileCounterSubset, int start){
        for (int i = start; i < 7; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;
            if (i < 7) scanJointPartialChow(tileCounterSubset, i + 1);
        }
        scanDisjointPartialChow(tileCounterSubset, 0);
    }

    public void scanDisjointPartialChow(int[] tileCounterSubset, int start){
        for (int i = start; i < 8; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 2] -= 1;
            if (i < 7) scanDisjointPartialChow(tileCounterSubset, i + 1);
        }
    }
}