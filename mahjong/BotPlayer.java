package mahjong;

import java.util.ArrayList;

public class BotPlayer extends Player{
    public static final int KONG = 4;
    public static final int PONG = 3;
    public static final int EYES = 2;
    public static final int CHOW = 1;
    public static final int JOINTPARTIAL = 0;
    public static final int PARTEDPARTIAL = -1;

    public static final int CRAKS = 0;
    public static final int DOTS = 1;
    public static final int STICKS = 2;
    public static final int WIND = 3;
    public static final int DRAGON = 4;

    private static class Meld {
        int type;
        int suit;
        int rank;
        ArrayList<WantedTile> wantedTiles = new ArrayList<>();
        public Meld(int type, int suit, int rank){
            this.type = type;
            this.suit = suit;
            this.rank = rank;
        }
        public Meld clone(){
            return new Meld(type, suit, rank);
        }
    }
    private static class WantedTile {
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

    public BotPlayer(String n){
        super(n);
    }

    public int askToSteal(int playThatCanChow){
        return 1;
    }

    public static void printMeldList(ArrayList<Meld> list){
        System.out.print("[");
        for (int i = 0; i < list.size(); i++){
            Meld group = list.get(i);
            System.out.print("{" + group.type + "," + group.rank + "}");                
        }
        System.out.print("]");
    }

    public static void printFancyMeldList(ArrayList<Meld> list){
        final String[][] tileIndex = 
            {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
             {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
             {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
             {"🀀", "🀁", "🀂", "🀃"},
             {"🀆", "🀅", "🀄"},
             {"🀢", "🀣", "🀤", "🀥"},
             {"🀦", "🀧", "🀨", "🀩"}};

        System.out.print("[");
        for (int i = 0; i < list.size(); i++){
            Meld group = list.get(i);
            int s = group.suit;
            int r = group.rank;
            int t = group.type;

            System.out.print("{");
            if (group.type >= 2){
                for (int a = 0; a < t; a++) System.out.print(tileIndex[s][r]);
            }
            else if (group.type == CHOW){
                System.out.print(tileIndex[s][r]);
                System.out.print(tileIndex[s][r + 1]);
                System.out.print(tileIndex[s][r + 2]);
            }
            else if (group.type == JOINTPARTIAL){
                System.out.print(tileIndex[s][r]);
                System.out.print(tileIndex[s][r + 1]);
            }
            else if (group.type == PARTEDPARTIAL){
                System.out.print(tileIndex[s][r]);
                System.out.print(tileIndex[s][r + 2]);
            }
            System.out.print("}");
        }
        System.out.print("]");
    }

    public Tile makeDecision(Table table){
        // // choose the hand with the highest value
        // // assign values to each tile within the chosen hand
        // // determine if hidden kong should be declared, draw another tile and revaluate the whole hand if necessary
        // // discard the tile with the lowest value
        
        return hand.get(0);
    }

    public void evaluate(Table table){
        int[][] tileCounter = 
            {{0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0},
             {0, 0, 0}};

        ArrayList<ArrayList<Meld>> craks = new ArrayList<>();
        ArrayList<ArrayList<Meld>> dots = new ArrayList<>();
        ArrayList<ArrayList<Meld>> sticks = new ArrayList<>();
        ArrayList<ArrayList<Meld>> honors = new ArrayList<>();

        ArrayList<ArrayList<ArrayList<Meld>>> allPermutations = new ArrayList<>();
        allPermutations.add(craks);
        allPermutations.add(dots);
        allPermutations.add(sticks);
        allPermutations.add(honors);

        // populate the tileCounter
        for (Tile t: hand){
            if (t.getSuit() >= 5) continue;
            tileCounter[t.getSuit()][t.getRank()]++;
        }

        ArrayList<ArrayList<Meld>> allPossiblePaths = findAllPossiblePaths(allPermutations, tileCounter);
        
        findWantedTilesForAllMelds(allPermutations, table, tileCounter);

        // give each possible play an advantage value based on sets made & chances of a steal
        ArrayList<Double> valuePerPossiblePlay = new ArrayList<>();
        for (int i = 0; i < allPossiblePaths.size(); i++){
            double temp = findValueOfPath(allPossiblePaths.get(i));
            valuePerPossiblePlay.add(temp);
        }

        { /// DEBUG START print all
        final String[][] tileIndex = 
            {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
             {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
             {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
             {"🀀", "🀁", "🀂", "🀃"},
             {"🀆", "🀅", "🀄"},
             {"🀢", "🀣", "🀤", "🀥"},
             {"🀦", "🀧", "🀨", "🀩"}};
        for (int i = 0; i < allPossiblePaths.size(); i++){
            System.out.printf("%.4f\t", valuePerPossiblePlay.get(i));            
            ArrayList<Meld> possiblePlay = allPossiblePaths.get(i);
            printFancyMeldList(possiblePlay);
            System.out.print(" - [");
            ArrayList<WantedTile> record = new ArrayList<>();
            for (Meld meld: possiblePlay){
                for (WantedTile w: meld.wantedTiles){
                    boolean quit = false;
                    for (WantedTile r: record) if (r == w) quit = true;
                    if (quit) break;
                    record.add(w);
                    System.out.print("<" + w.chance + tileIndex[w.suit][w.rank]);
                    System.out.print(w.priority + ">");
                }
            }
            record.clear();
            System.out.println("]");
        }
        System.out.println();
        for (int i = 0; i < hand.size(); i++) System.out.print(hand.get(i).getSymbol());
        System.out.println("\nPossible plays from this hand: " + allPossiblePaths.size());
        for (int i = 0; i < tileCounter.length; i++){
            for (int j = 0; j < tileCounter[i].length; j++){
                System.out.print(tileCounter[i][j] + " ");
            }
            System.out.println();
        }
        } /// DEBUG END

        return;
    }

    private double findValueOfPath(ArrayList<Meld> possiblePlay){
        double value = 0;

        int meldsComplete = meldsPlacedDown;
        boolean eyesComplete = false;

        // MAGIC NUMBERS FEILD
        final double valKong =      2;
        final double valPong =      1.6;
        final double valEyes =      1.2;
        final double valChow =      1;
        final double valJoint =     .6;
        final double valParted =    .3;

        final double MultPong =      .1;
        final double MultEyes =      .1;
        final double MultChow =      .1;
        final double MultJoint =     .1;
        final double MultParted =    .1;

        for (Meld meld: possiblePlay){
            int t = meld.type;
            if (t <= KONG && t >= CHOW) meldsComplete++;
            
            double temp = 0;
            if (t == KONG) temp = valKong;
            else if (t == PONG){
                temp = valPong;
                for (WantedTile w: meld.wantedTiles) temp -= (w.priority - 1) * MultPong;
            }
            else if (t == EYES){
                temp = valEyes;
                for (WantedTile w: meld.wantedTiles) temp -= (w.priority - 1) * MultChow;
            }
            else if (t == CHOW){
                temp = valChow;
                for (WantedTile w: meld.wantedTiles) temp -= (w.priority - 1) * MultEyes;
            }
            else if (t == JOINTPARTIAL){
                temp = valJoint;
                for (WantedTile w: meld.wantedTiles) temp -= (w.priority - 1) * MultJoint;
            }
            else if (t == PARTEDPARTIAL){
                temp = valParted;
                for (WantedTile w: meld.wantedTiles) temp -= (w.priority - 1) * MultParted;
            }

            value += temp;
        }

        if (meldsComplete == 4 && eyesComplete) mahjong = true;

        return value;
    }

    // subroutine of findWantedTilesForAllMelds
    private void checkDupeWantedTiles(ArrayList<WantedTile> usefulTiles, Meld meld, int suit, int rank, int remaining){
        for (WantedTile w: usefulTiles){
            if (w.suit == suit && w.rank == rank){
                w.priority++;
                meld.wantedTiles.add(w);
                return;
            }
        }
        WantedTile w = new WantedTile(remaining, suit, rank);
        usefulTiles.add(w);
        meld.wantedTiles.add(w);
        return;
    }

    private void findWantedTilesForAllMelds(ArrayList<ArrayList<ArrayList<Meld>>> allPermutations, Table table, int[][] tileCounter){
        // get data on what tiles & how many of them can possibly appear in the future
        int[][] remainingTileCounter = table.getDiscardedTilesCounter();
        for (int i = 0; i < remainingTileCounter.length; i++){
            for (int j = 0; j < remainingTileCounter[i].length; j++){
                remainingTileCounter[i][j] = 4 - (remainingTileCounter[i][j] + tileCounter[i][j]);
            }
        }
        // give each meld a set of wanted tiles
        for (ArrayList<ArrayList<Meld>> suitPermutations: allPermutations){
            for (ArrayList<Meld> permutation: suitPermutations){
                ArrayList<WantedTile> usefulTiles = new ArrayList<>();
                for (Meld meld: permutation){
                    int type = meld.type;
                    int s = meld.suit;
                    int r = meld.rank;

                    if (type == KONG); // cant find more of 4 duplicates, so do nothing
                    else if (type == PONG || type == EYES){
                        if (remainingTileCounter[s][r] != 0){
                            checkDupeWantedTiles(usefulTiles, meld, s, r, remainingTileCounter[s][r]);
                        }
                    }
                    else if (type == CHOW){
                        if (r != 0 && remainingTileCounter[s][r - 1] != 0){
                            checkDupeWantedTiles(usefulTiles, meld, s, r - 1, remainingTileCounter[s][r - 1]);
                        }
                        if (r != 6 && remainingTileCounter[s][r + 3] != 0){
                            checkDupeWantedTiles(usefulTiles, meld, s, r + 3, remainingTileCounter[s][r + 3]);
                        }
                    }
                    else if (type == JOINTPARTIAL){
                        if (r != 0 && remainingTileCounter[s][r - 1] != 0){
                            checkDupeWantedTiles(usefulTiles, meld, s, r - 1, remainingTileCounter[s][r - 1]);
                        }
                        if (r != 7 && remainingTileCounter[s][r + 2] != 0){
                            checkDupeWantedTiles(usefulTiles, meld, s, r + 2, remainingTileCounter[s][r + 2]);
                        }
                    }
                    else if (type == PARTEDPARTIAL){
                        if (remainingTileCounter[s][r + 1] != 0){
                            checkDupeWantedTiles(usefulTiles, meld, s, r + 1, remainingTileCounter[s][r + 1]);
                        }
                    }
                    else System.err.println("unknown meld found");
                }
                usefulTiles.clear();
            }
        }

    }

    // subroutine of findAllPossiblePaths()
    private static ArrayList<Meld> combineArrayLists(ArrayList<Meld> craks, ArrayList<Meld> dots, ArrayList<Meld> sticks, ArrayList<Meld> honors){
        ArrayList<Meld> result = new ArrayList<>(); 
        for (Meld m: craks) result.add(m);
        for (Meld m: dots) result.add(m);
        for (Meld m: sticks) result.add(m);
        for (Meld m: honors) result.add(m);
        return result;
    }
    
    private ArrayList<ArrayList<Meld>> findAllPossiblePaths(ArrayList<ArrayList<ArrayList<Meld>>> allPermutations, int[][] tileCounter){
        ArrayList<ArrayList<Meld>> craks = allPermutations.get(0);
        ArrayList<ArrayList<Meld>> dots = allPermutations.get(1);
        ArrayList<ArrayList<Meld>> sticks = allPermutations.get(2);
        ArrayList<ArrayList<Meld>> honors = allPermutations.get(3);

        // find permutations possible for each suit disjointly
        for (int i = 0; i < 5; i++){
            int[] tileCounterSubset = tileCounter[i];
            ArrayList<Meld> melds = new ArrayList<>(); 
            
            if (i <= 2) scanDupes(i , allPermutations.get(i), melds, tileCounterSubset, 4, true);
            else scanHonors(i, honors, melds, tileCounterSubset, 4, true);

            memoEyes.clear();
            memoChows.clear();
            memoJoint.clear();
            memoDisjoint.clear();
        }

        // if a suit has no possible melds, insert an empty set
        for (ArrayList<ArrayList<Meld>> suitPermutations: allPermutations){
            if (suitPermutations.size() == 0){
                ArrayList<Meld> empty = new ArrayList<>(); 
                suitPermutations.add(empty);
            }
        }
        
        ArrayList<ArrayList<Meld>> allPossiblePaths = new ArrayList<>();

        for (int i = 0; i < craks.size(); i++){
            for (int j = 0; j < dots.size(); j++){
                for (int k = 0; k < sticks.size(); k++){
                    for (int l = 0; l < honors.size(); l++){
                        ArrayList<Meld> temp = combineArrayLists(craks.get(i), dots.get(j), sticks.get(k), honors.get(l));
                        allPossiblePaths.add(temp);
                    }
                }
            }
        }

        { /// DEBUG START print info
        System.out.println("printing all Permutations of all suits: ");
        for (int i = 0; i < allPermutations.size(); i++){
            ArrayList<ArrayList<Meld>> suitPermutations = allPermutations.get(i);
            System.out.printf("%d %-2d ", i, suitPermutations.size());
            for (int j = 0; j < suitPermutations.size(); j++){
                printMeldList(suitPermutations.get(j));
            }
            System.out.println();
        }
        System.out.println("Total permutations: " + (craks.size() + dots.size() + sticks.size() + honors.size()));
        System.out.println("Total tileCounterSubset[]s memoized: " + gCount2);
        System.out.println("Total stack frames invoked: " + gCount1);
        System.out.println();
        } /// DEBUG END

        return allPossiblePaths;
    }

    /* memoization is enforced only when searching for eyes, chows, joint partials, & parted partials, as repeating results can only occur 
       for these combinations. Each type of combination will have it own a memo list. Whenever looking for these combinations, an instance 
       of tileCounterSubset is memoized, as it can track what "paths" of backtracking have already occured */ 

    private ArrayList<int[]> memoEyes = new ArrayList<>();
    private ArrayList<int[]> memoChows = new ArrayList<>();
    private ArrayList<int[]> memoJoint = new ArrayList<>();
    private ArrayList<int[]> memoDisjoint = new ArrayList<>();

    /// DEBUG
    static int gCount1 = 0;
    static int gCount2 = 0;

    // subroutine of all scan melds functions
    private static boolean checkIfNewPath(ArrayList<int[]> memo, int[] tileCounterSubset){
        for (int[] memoElt: memo){
            for (int j = 0; j < tileCounterSubset.length; j++){
                for (int k = 0; k < tileCounterSubset.length; k++){
                if (memoElt[k] != tileCounterSubset[k]) break;
                if (k == tileCounterSubset.length - 1) return false;
                }
            }
        }
        memo.add(tileCounterSubset.clone());
        gCount2++;
        return true;
    }

    // decending the list, recurse/chain call functions
    // scanHonors(4) | scanDupes(4):            kong            {n, n, n, n}
    // scanHonors(3) | scanDupes(3):            pongs           {n, n, n}
    // scanHonors(2) | scanDupes(2):            eyes            {n, n}
    //                 scanChow():              complete chows  {n, n+1, n+2}
    //                 scanJointPartialChow():  partial chows   {n, n+1}
    //                 scanPartialPartedChow(): partial chow    {n, n+2}

    private void scanHonors(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset, int copies, boolean isRootStackFrame){
        gCount1++;
        if (copies == 2 && !checkIfNewPath(memoEyes, tileCounterSubset)) return;

        boolean everCreatedMeld = false;
        for (int i = 0; i < tileCounterSubset.length; i++){
            if (tileCounterSubset[i] >= copies){
                everCreatedMeld = true;
                tileCounterSubset[i] -= copies;

                melds.add(new Meld(copies, suit, i));
                scanHonors(suit, suitPermutations, melds, tileCounterSubset, copies, false);

                tileCounterSubset[i] += copies;
                melds.remove(melds.size() - 1);
            }
        }

        if (copies > 2){
            if (isRootStackFrame) memoEyes.clear();
            scanHonors(suit, suitPermutations, melds, tileCounterSubset, copies - 1, true);
        }

        if (!everCreatedMeld && !melds.isEmpty()){
            ArrayList<Meld> deepCopy = new ArrayList<>();
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i).clone());
            suitPermutations.add(deepCopy);
        }
    }

    private void scanDupes(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset, int copies, boolean isRootStackFrame){
        gCount1++;
        if (copies == 2 && !checkIfNewPath(memoEyes, tileCounterSubset)) return;

        for (int i = 0; i < 9; i++){
            if (tileCounterSubset[i] >= copies){
                tileCounterSubset[i] -= copies;

                // copies: 4 = kong, 3 = pong, 2 = eyes
                melds.add(new Meld(copies, suit, i));
                scanDupes(suit, suitPermutations, melds, tileCounterSubset, copies, false);

                // "release" the tiles, undo the set afterwards
                // this allows backtracking for all possible permutations sets of melds 
                tileCounterSubset[i] += copies;
                melds.remove(melds.size() - 1);
            }
        }

        // if no kongs, start looking for pongs, then eyes
        if (copies > 2){
            /* the memo list when the 1st stack frame is searching for kongs/pongs turns out to be the same when it's 
               searching for eyes, therefore the list must be cleared before searching for eyes, since all path 
               identifiers (tileSubsetCounter variations) have already been memoized when searching kongs/pongs */
            if (isRootStackFrame) memoEyes.clear();
            scanDupes(suit, suitPermutations, melds, tileCounterSubset, copies - 1, true);
        }
        // after eyes, start searching for chows
        else scanChow(suit, suitPermutations, melds, tileCounterSubset, true);
    }

    private void scanChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset, boolean isRootStackFrame){
        gCount1++;
        if (!checkIfNewPath(memoChows, tileCounterSubset)) return;

        for (int i = 0; i < 7; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;
            tileCounterSubset[i + 2] -= 1;

            melds.add(new Meld(CHOW, suit, i));

            scanChow(suit, suitPermutations, melds, tileCounterSubset, false);
            
            // "release" the tiles, undo the set afterwards
            // this allows backtracking for all possible permutations sets of melds 
            tileCounterSubset[i] += 1;
            tileCounterSubset[i + 1] += 1;
            tileCounterSubset[i + 2] += 1;
            melds.remove(melds.size() - 1);
        }
        if (isRootStackFrame) memoChows.clear();
        scanJointPartialChow(suit, suitPermutations, melds, tileCounterSubset, true);
    }

    private void scanJointPartialChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset, boolean isRootStackFrame){
        gCount1++;
        if (!checkIfNewPath(memoJoint, tileCounterSubset)) return;

        for (int i = 0; i < 8; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;

            melds.add(new Meld(JOINTPARTIAL, suit, i));

            scanJointPartialChow(suit, suitPermutations, melds, tileCounterSubset, false);
            
            // "release" the tiles, undo the set afterwards
            // this allows backtracking for all possible permutations sets of melds 
            tileCounterSubset[i] += 1;
            tileCounterSubset[i + 1] += 1;
            melds.remove(melds.size() - 1);
        }
        if (isRootStackFrame) memoJoint.clear();
        scanPartedPartialChow(suit, suitPermutations, melds, tileCounterSubset, true);
    }

    private void scanPartedPartialChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset, boolean isRootStackFrame){
        gCount1++;
        if (!checkIfNewPath(memoDisjoint, tileCounterSubset)) return;

        boolean everCreatedMeld = false;
        for (int i = 0; i < 7; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            everCreatedMeld = true;
            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 2] -= 1;

            melds.add(new Meld(PARTEDPARTIAL, suit, i));

            scanPartedPartialChow(suit, suitPermutations, melds, tileCounterSubset, false);
            
            // "release" the tiles, undo the set afterwards
            // this allows backtracking for all possible permutations sets of melds 
            tileCounterSubset[i] += 1;
            tileCounterSubset[i + 2] += 1;
            melds.remove(melds.size() - 1);
        }
        if (isRootStackFrame) memoDisjoint.clear();

        // record the possible set of melds when no more sets can be formed
        if (!everCreatedMeld && !melds.isEmpty()){
            ArrayList<Meld> deepCopy = new ArrayList<>();
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i).clone());
            suitPermutations.add(deepCopy);
        }
    }
}