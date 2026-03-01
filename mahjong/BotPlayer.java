package mahjong;

import java.util.ArrayList;

public class BotPlayer extends Player{
    // 4 = kong, 3 = pong, 2 = eyes
    public static final int FULLCHOW = 1;
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
        public Meld(int type, int suit, int rank){
            this.type = type;
            this.suit = suit;
            this.rank = rank;
        }
    }
    private static class Chance {
        int chance;
        int suit;
        int rank;
        public Chance(int chance, int suit, int rank){
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
            else if (group.type == FULLCHOW){
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
        int[][] tileCounter = 
            {{0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0},
             {0, 0, 0}};

        // populate the tileCounter
        for (Tile t: hand){
            if (t.getSuit() >= 5) continue;
            tileCounter[t.getSuit()][t.getRank()]++;
        }

        ArrayList<ArrayList<Meld>> allPossiblePlays = findAllPossiblePlays(tileCounter);
        
        // for each possible play, find what tiles are stealable
        int[][] remainingTileCounter = table.getDiscardedTilesCounter();
        for (int i = 0; i < remainingTileCounter.length; i++){
            for (int j = 0; j < remainingTileCounter[i].length; j++){
                remainingTileCounter[i][j] = 4 - (remainingTileCounter[i][j] + tileCounter[i][j]);
            }
        }
        ArrayList<ArrayList<Chance>> usefulTilesOfAllPlays = new ArrayList<>();
        for (int i = 0; i < allPossiblePlays.size(); i++){
            ArrayList<Chance> usefulTiles = findAllStealableTiles(allPossiblePlays.get(i), remainingTileCounter);
            usefulTilesOfAllPlays.add(usefulTiles);
        }
        if (usefulTilesOfAllPlays.size() != allPossiblePlays.size()) System.err.println("<!> usefulTilesOfAllPlays must be same size as allPossiblePlays");

        { /// DEBUG START print all
        final String[][] tileIndex = 
            {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
             {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
             {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
             {"🀀", "🀁", "🀂", "🀃"},
             {"🀆", "🀅", "🀄"},
             {"🀢", "🀣", "🀤", "🀥"},
             {"🀦", "🀧", "🀨", "🀩"}};
        for (int i = 0; i < usefulTilesOfAllPlays.size(); i++){
            printFancyMeldList(allPossiblePlays.get(i));
            ArrayList<Chance> one = usefulTilesOfAllPlays.get(i);
            System.out.print(" - [");
            for (int j = 0; j < one.size(); j++){
                Chance c = one.get(j);
                System.out.print("<" + c.chance + tileIndex[c.suit][c.rank]);
                System.out.print(">");
            }
            System.out.println("]");
        }
        System.out.println();
        for (int i = 0; i < hand.size(); i++) System.out.print(hand.get(i).getSymbol());
        System.out.println("\nPossible plays from this hand: " + allPossiblePlays.size());
        for (int i = 0; i < tileCounter.length; i++){
            for (int j = 0; j < tileCounter[i].length; j++){
                System.out.print(tileCounter[i][j] + " ");
            }
            System.out.println();
        }
        } /// DEBUG END

        // // give each possible play an advantage value based on sets made & chances of a steal
        // // choose the hand with the highest value
        // // assign values to each tile within the chosen hand
        // // discard the tile with the lowest value
        
        return hand.get(0);
    }

    // subroutine of findAllStealableTiles
    private boolean checkDupeChances(ArrayList<Chance> usefulTiles, int suit, int rank){
        for (int i = 0; i < usefulTiles.size(); i++){
            Chance c = usefulTiles.get(i);
            if (c.suit == suit && c.rank == rank) return false;
        }
        return true;
    }

    private ArrayList<Chance> findAllStealableTiles(ArrayList<Meld> possiblePlay, int[][] remainingTileCounter){
        ArrayList<Chance> usefulTiles = new ArrayList<>();
        for (int i = 0; i < possiblePlay.size(); i++){
            Meld meld = possiblePlay.get(i);
            int s = meld.suit;
            int r = meld.rank;

            if (meld.type == 4); // idk what to evaluate a hidden kong
            else if (meld.type == 3 || meld.type == 2){
                if (remainingTileCounter[s][r] != 0 && checkDupeChances(usefulTiles, s, r)){
                    usefulTiles.add(new Chance(remainingTileCounter[s][r], s, r));
                }
            }
            else if (meld.type == FULLCHOW){
                if (r != 0 && remainingTileCounter[s][r - 1] != 0 && checkDupeChances(usefulTiles, s, r - 1)){
                    usefulTiles.add(new Chance(remainingTileCounter[s][r - 1], s, r - 1));
                }
                if (r + 2 != 8 && remainingTileCounter[s][r + 3] != 0 && checkDupeChances(usefulTiles, s, r - 3)){
                    usefulTiles.add(new Chance(remainingTileCounter[s][r + 3], s, r + 3));
                }
            }
            else if (meld.type == JOINTPARTIAL){
                if (r != 0 && remainingTileCounter[s][r - 1] != 0 && checkDupeChances(usefulTiles, s, r - 1)){
                    usefulTiles.add(new Chance(remainingTileCounter[s][r - 1], s, r - 1));
                }
                if (r + 1 != 8 && remainingTileCounter[s][r + 2] != 0 && checkDupeChances(usefulTiles, s, r + 2)){
                    usefulTiles.add(new Chance(remainingTileCounter[s][r + 2], s, r + 2));
                }
            }
            else if (meld.type == PARTEDPARTIAL){
                if (r != 0 && remainingTileCounter[s][r + 1] != 0 && checkDupeChances(usefulTiles, s, r - 1)){
                    usefulTiles.add(new Chance(remainingTileCounter[s][r + 1], s, r + 1));
                }
            }
            else System.err.println("unknown meld found");
        }
        return usefulTiles;
    }

    // subroutine of findAllPossiblePlays()
    private static ArrayList<Meld> combineArrayLists(ArrayList<Meld> craks, ArrayList<Meld> dots, ArrayList<Meld> sticks, ArrayList<Meld> honors){
        ArrayList<Meld> result = new ArrayList<>(); 
        for (int i = 0; i < craks.size(); i++) result.add(craks.get(i));
        for (int i = 0; i < dots.size(); i++) result.add(dots.get(i));
        for (int i = 0; i < sticks.size(); i++) result.add(sticks.get(i));
        for (int i = 0; i < honors.size(); i++) result.add(honors.get(i));
        return result;
    }
    
    private ArrayList<ArrayList<Meld>> findAllPossiblePlays(int[][] tileCounter){
        // find permutations possible for each suit disjointly
        ArrayList<ArrayList<Meld>> craks = new ArrayList<>();
        ArrayList<ArrayList<Meld>> dots = new ArrayList<>();
        ArrayList<ArrayList<Meld>> sticks = new ArrayList<>();
        ArrayList<ArrayList<Meld>> honors = new ArrayList<>();        
        ArrayList[] allPermutations = {craks, dots, sticks, honors};

        for (int i = 0; i < 5; i++){
            int[] tileCounterSubset = tileCounter[i];
            ArrayList<Meld> melds = new ArrayList<>(); 
            
            if (i <= 2) scanDupes(i , allPermutations[i], melds, tileCounterSubset, 4, true);
            else scanHonors(i , honors, melds, tileCounterSubset, 4, true);

            memoEyes.clear();
            memoChows.clear();
            memoJoint.clear();
            memoDisjoint.clear();
        }

        // if a suit has no possible melds, insert an empty set
        for (int i = 0; i < allPermutations.length; i++){
            if (allPermutations[i].size() == 0){
                ArrayList<Meld> empty = new ArrayList<>(); 
                allPermutations[i].add(empty);
            }
        }

        {/// DEBUG START print allSuitsAllPermutations
        System.out.println("printing all Permutations of all suits: ");
        for (int i = 0; i < allPermutations.length; i++){
            ArrayList<ArrayList<Meld>> suitPermutations = allPermutations[i];
            System.out.printf("%d %-2d ", i, suitPermutations.size());
            for (int j = 0; j < suitPermutations.size(); j++){
                printMeldList(suitPermutations.get(j));
            }
            System.out.println();
        }
        } /// DEBUG END
        
        ArrayList<ArrayList<Meld>> allPossiblePlays = new ArrayList<>();
        for (int i = 0; i < craks.size(); i++){
            for (int j = 0; j < dots.size(); j++){
                for (int k = 0; k < sticks.size(); k++){
                    for (int l = 0; l < honors.size(); l++){
                        ArrayList<Meld> temp = combineArrayLists(craks.get(i), dots.get(j), sticks.get(k), honors.get(l));
                        allPossiblePlays.add(temp);
                    }
                }
            }
        }

        { /// DEBUG START print info
        System.out.println("Total permutations: " + (craks.size() + dots.size() + sticks.size() + honors.size()));
        System.out.println("Total tileCounterSubset[]s memoized: " + gCount2);
        System.out.println("Total stack frames invoked: " + gCount1);
        System.out.println();
        } /// DEBUG END

        return allPossiblePlays;
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
        for (int i = 0; i < memo.size(); i++){
            int[] memoElt = memo.get(i);
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
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i));
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

            melds.add(new Meld(FULLCHOW, suit, i));

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
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i));
            suitPermutations.add(deepCopy);
        }
    }
}