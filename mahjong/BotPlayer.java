package mahjong;

import java.util.ArrayList;

public class BotPlayer extends Player{
    // 4 = kong, 3 = pong, 2 = eyes
    public static final int FULLCHOW = 1;
    public static final int JOINTPARTIAL = 0;
    public static final int DISJOINTPARTIAL = -1;

    public static class meld {
        public meld(int t, int s){
            type = t;
            startingTile = s;
        }
        int type;
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
        //     // scanDupes(4): eyes, 2 identicals, implies if no eyes = no pongs
        //     // scanDupes(3): pongs, 3 identicals, implies if no pongs = no kongs
        //     // scanDupes(2): kong, 4 identicals
        //     // scanChows(): complete chows, 3 of same suit in sequence 
        //     // scanPartialNeighborChow(): partial chows [n, n+1], implies if no this = no complete chows
        //     // scanPartialPartedChow() partial chow, tile missing in middle [n, n+2]
        //     //      OR terminal chows, neighboring chows with terminals [1, 2] or [8, 9]
        
        //     scanDupes(tileCounterSubset, 0, 4);
            // ArrayList<ArrayList<meldGroup>> possibleRoutes; 
        // }

        /// test craks only 
        int[] tileCounterSubset = tileCounter[0];
        ArrayList<ArrayList<meld>> allPermutations = new ArrayList<>();
        ArrayList<meld> melds = new ArrayList<>(); 
        scanDupes(allPermutations, melds, tileCounterSubset, 4, true);
        
        System.out.println("\nprinting all permutations");
        for (int i = 0; i < allPermutations.size(); i++){
            ArrayList<meld> onePermutation = allPermutations.get(i);
            System.out.print("[");
            for (int j = 0; j < onePermutation.size(); j++){
                System.out.print("{" + onePermutation.get(j).type + "," + onePermutation.get(j).startingTile + "}");                
            }
            System.out.println("]");
        }

        // for (int i = 0; i < memoPairs.size(); i++){
        //     int[] temp = memoPairs.get(i);
        //     for (int j = 0; j < temp.length; j++){
        //         System.out.print(temp[j]);
        //     }
        //     System.out.println();
        // }
        System.out.println("\nTotal array instances memoized: " + memoPairs.size());
        System.out.println("Total stack frames invoked: " + globalCount);


        return hand.remove(0);
    }

    // memoization, repeatable the same sets may only occur with eyes, chows, & the partials
    // when we enter a stack looking for those, memoize the tileCounterSubset (1 memo for each eyes, chows, & the partials)
    // next time, if a stack is looking 

    ArrayList<int[]> memoPairs = new ArrayList<>();
    int globalCount = 0;

    public void scanDupes(ArrayList<ArrayList<meld>> allPermutations, ArrayList<meld> melds, int[] tileCounterSubset, int copies, boolean isRootStackFrame){
        globalCount++;
        /* memoization is enforced only when searching for pairs, as repeating results can only occur 
           for pairs, whenever looking for a pair, an instance of tileCounterSubset is memoized, as it 
           can track what "paths" of backtracking have already occured */ 
        if (copies == 2){
            for (int j = 0; j < memoPairs.size(); j++){
                int[] checkMemo = memoPairs.get(j);
                for (int k = 0; k < tileCounterSubset.length; k++){
                    if (checkMemo[k] != tileCounterSubset[k]) break;
                    if (k  == tileCounterSubset.length - 1) return;
                }
            }
            // add to memo if this is new path when searching eyes
            memoPairs.add(tileCounterSubset.clone());
        }

        boolean everCreatedMeld = false;
        for (int i = 0; i < 9; i++){
            if (tileCounterSubset[i] >= copies){
                everCreatedMeld = true;  
                tileCounterSubset[i] -= copies;

                // copies: 4 = kong, 3 = pong, 2 = eyes
                melds.add(new meld(copies, i));
                // System.out.print("melded ");

                scanDupes(allPermutations, melds, tileCounterSubset, copies, false);

                // "release" the tiles, undo the set afterwards
                // this allows backtracking for all possible permutations sets of melds 
                tileCounterSubset[i] += copies;
                melds.remove(melds.size() - 1);
            }
        }

        // if no kongs, start looking for pongs, then eyes... so & so on
        if (copies > 2){
            /* the memo list when the 1st stack frame is searching for kongs/pongs turns out to be the same when it's 
               searching for eyes, therefore the list must be cleared before searching for eyes, since all path 
               identifiers (tileSubsetCounter variations) have already been memoized when searching kongs/pongs */
            if (isRootStackFrame) memoPairs.clear();
            scanDupes(allPermutations, melds, tileCounterSubset, copies - 1, true);
        }
        // else scanChow(possibleMelds, tileCounterSubset);

        /// DEBUG 
        // record the possible set of melds when no more sets can be formed
        if (!everCreatedMeld && copies == 2){
            ArrayList<meld> deepCopy = new ArrayList<>();
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i));
            allPermutations.add(deepCopy);
        }
    }

// ------------------

    public void scanChow(ArrayList<ArrayList<meld>> allPermutations, ArrayList<meld> melds, int[] tileCounterSubset){
        boolean everCreatedMeld = false;
        for (int i = 0; i < 7; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            everCreatedMeld = true;
            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;
            tileCounterSubset[i + 2] -= 1;

            melds.add(new meld(FULLCHOW, i));

            scanChow(allPermutations, melds, tileCounterSubset);
            
            // "release" the tiles, undo the set afterwards
            // this allows backtracking for all possible permutations sets of melds 
            tileCounterSubset[i] += 1;
            tileCounterSubset[i + 1] += 1;
            tileCounterSubset[i + 2] += 1;
            melds.remove(melds.size() - 1);
        }
        // scanJointPartialChow(allPermutations, melds, tileCounterSubset);

        if (!everCreatedMeld){
            ArrayList<meld> deepCopy = new ArrayList<>();
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i));
            allPermutations.add(deepCopy);
        }

    }

}