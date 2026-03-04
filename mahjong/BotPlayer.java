package mahjong;

import java.util.ArrayList;

public class BotPlayer extends Player{
    private static final int KONG = 4;
    private static final int PONG = 3;
    private static final int EYES = 2;
    private static final int CHOW = 1;
    private static final int JOINTPARTIAL = 0;
    private static final int PARTEDPARTIAL = -1;
    private static final int LONETILE = -2;

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
            
            String type = "";
            switch (group.type) {
                case KONG:
                    type = "K"; break;
                case PONG:
                    type = "P"; break;
                case EYES:
                    type = "E"; break;                    
                case CHOW:
                    type = "C"; break;
                case JOINTPARTIAL:
                    type = "j"; break;
                case PARTEDPARTIAL:
                    type = "p"; break;
                case LONETILE:
                    type = "l"; break;
            }
            System.out.print(type + ":" + (group.rank + 1));
            if (i != list.size() - 1) System.out.print(" ");
        }
        System.out.print("] ");
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

            if (t >= 2){
                for (int a = 0; a < t; a++) System.out.print(tileIndex[s][r]);
            }
            else if (t == CHOW){
                System.out.print(tileIndex[s][r]);
                System.out.print(tileIndex[s][r + 1]);
                System.out.print(tileIndex[s][r + 2]);
            }
            else if (t == JOINTPARTIAL){
                System.out.print(tileIndex[s][r]);
                System.out.print(tileIndex[s][r + 1]);
            }
            else if (t == PARTEDPARTIAL){
                System.out.print(tileIndex[s][r]);
                System.out.print(tileIndex[s][r + 2]);
            }
            if (t == LONETILE) System.out.print(tileIndex[s][r]);

            if (i != list.size() - 1) System.out.print(":");
        }
        System.out.print("]");
    }

    ArrayList<ArrayList<Meld>> allPossiblePaths;
    ArrayList<Double> valueOfPossiblePaths;

    public Tile makeDecision(Table table){
        // // choose the hand with the highest value
        int bestPath = 0;
        double highestValue = 0;
        for (int i = 0; i < valueOfPossiblePaths.size(); i++){
            if (valueOfPossiblePaths.get(i) > highestValue){
                bestPath = i;
                highestValue = valueOfPossiblePaths.get(i);
            }
        }
        
        // // assign values to each tile within the chosen hand
        ArrayList<Meld> chosenPath = allPossiblePaths.get(bestPath);
        ArrayList<Double> valuePerTile = findPerTileValues(chosenPath);
        
        // // determine if hidden kong should be declared, draw another tile and revaluate the whole hand if necessary

        // // discard the tile with the lowest value
        int toDrop = 0;
        double leastValue = 0;
        for (int i = 0; i < valuePerTile.size(); i++){
            if (valuePerTile.get(i) < leastValue){
                toDrop = i;
                leastValue = valuePerTile.get(i);
            }
        }
        
        return hand.get(toDrop);
    }

    private ArrayList<Double> findPerTileValues(ArrayList<Meld> chosenPath){
        ArrayList<Double> valuePerTile = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) valuePerTile.add(0.0);

        // MAGIC NUMBERS tile values
            // value for being in a set
            final double InKong =   1.3;
            final double InPong =   1;
            final double InEyes =   .7;
            final double InChow =   .7;
            final double InJoint =  .4;
            final double InParted = .2;

        // for each meld, increase the value of tiles within those melds
        for (Meld m: chosenPath){
            double value = 0;

            switch (m.type) {
                case KONG:
                    value += InKong;
                    break;
                case PONG:
                    value += InPong;
                    break;
                case EYES:
                    value += InEyes;
                    break;
                case CHOW:
                    value += InChow;
                    break;
                case JOINTPARTIAL:
                    value += InJoint;
                    break;
                case PARTEDPARTIAL:
                    value += InParted;
                    break;
            }
            
        }

        return valuePerTile;
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

        allPossiblePaths = findAllPossiblePaths(allPermutations, tileCounter);

        // get counter on what tiles & how many of them can possibly appear in the future
        int[][] remainingTileCounter = table.getDiscardedTilesCounter();
        for (int i = 0; i < remainingTileCounter.length; i++){
            for (int j = 0; j < remainingTileCounter[i].length; j++){
                remainingTileCounter[i][j] = 4 - (remainingTileCounter[i][j] + tileCounter[i][j]);
            }
        }
        // for each meld in a permutation, find what tiles are desired/can be stolen
        for (int i = 0; i < allPermutations.size(); i++){
            ArrayList<ArrayList<Meld>> suitPermutations = allPermutations.get(i);
            findWantedTiles(suitPermutations, remainingTileCounter[i]);
        }

        // give each possible play an advantage value based on sets made & chances of a steal
        valueOfPossiblePaths = new ArrayList<>();
        for (int i = 0; i < allPossiblePaths.size(); i++){
            double temp = findValueOfPath(allPossiblePaths.get(i));
            valueOfPossiblePaths.add(temp);
        }

        { /// DEBUG START
        final String[][] tileIndex = 
            {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
             {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
             {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
             {"🀀", "🀁", "🀂", "🀃"},
             {"🀆", "🀅", "🀄"},
             {"🀢", "🀣", "🀤", "🀥"},
             {"🀦", "🀧", "🀨", "🀩"}};
        // print permutations of individual suits
        System.out.println("printing all Permutations of all suits: ");
        for (int i = 0; i < allPermutations.size(); i++){
            ArrayList<ArrayList<Meld>> suitPermutations = allPermutations.get(i);
            System.out.printf("%d %-2d ", i, suitPermutations.size());
            for (int j = 0; j < suitPermutations.size(); j++){
                printMeldList(suitPermutations.get(j));
            }
            System.out.println();
        }
        System.out.println();
        // print tiles unicode grapahics
        for (int i = 0; i < allPossiblePaths.size(); i++){
            System.out.printf("%.1f ", valueOfPossiblePaths.get(i));            
            ArrayList<Meld> possiblePlay = allPossiblePaths.get(i);
            printFancyMeldList(possiblePlay);
            System.out.print(" - [");
            ArrayList<WantedTile> record = new ArrayList<>();
            for (Meld meld: possiblePlay){
                for (int j = 0; j < meld.wantedTiles.size(); j++){
                    WantedTile w = meld.wantedTiles.get(j);
                    boolean quit = false;
                    for (WantedTile r: record) if (r == w) quit = true;
                    if (quit) break;
                    record.add(w);
                    System.out.print(w.chance + tileIndex[w.suit][w.rank]);
                    System.out.print(w.priority + " ");
                }
            }
            record.clear();
            System.out.println("]");
        }
        System.out.println();
        for (int i = 0; i < hand.size(); i++) System.out.print(hand.get(i).getSymbol());
        System.out.println("\nPossible plays from this hand: " + allPossiblePaths.size());
        // print tileCounter
        for (int i = 0; i < tileCounter.length; i++){
            for (int j = 0; j < tileCounter[i].length; j++){
                System.out.print(tileCounter[i][j] + " ");
            }
            System.out.println();
        }
        // print info
        System.out.println("Total permutations: " + (craks.size() + dots.size() + sticks.size() + honors.size()));
        System.out.println("Total tileCounterSubset[]s memoized: " + memoOccurence);
        System.out.println("Total tileCounterSubset[]s checked: " + tileCounterChecks);
        System.out.println("Total subsetChecks occured: " + subsetCheckOccurence);
        System.out.println("Total stack frames invoked: " + stackFrameCounter);
        System.out.println();
        } /// DEBUG END

        return;
    }

    private double findValueOfPath(ArrayList<Meld> possiblePlay){
        double value = 0;

        int meldsComplete = meldsPlacedDown;
        boolean eyesComplete = false;

        // MAGIC NUMBERS: bot decision making parameters
            // base value of all melds/submelds
            final double ValKong =      2.5;
            final double ValPong =      1.6;
            final double ValEyes =      1.2;
            final double ValChow =      1;
            final double ValJoint =     .6;
            final double ValParted =    .3;
            // additional value if the needed tile is undrawn yet
            final double PossibleKong = .1;
            final double PossiblePong = .1;
            final double PossibleChow = .1;
            final double PossibleEyes = .1;
            // subtractive value if multiple melds needs the same tile
            final double SplitNeed =    .1;

        for (Meld meld: possiblePlay){
            int t = meld.type;

            if (t <= KONG && t >= CHOW && t != EYES) meldsComplete++;
            if (t == EYES) eyesComplete = true;
            if (meldsComplete == 4 && eyesComplete) mahjong = true;

            double temp = 0;
            switch (t) {
                case KONG:
                    value += ValKong;
                    break;
                case PONG:
                    value += ValPong;
                    break;
                case EYES:
                    value += ValEyes;
                    break;
                case CHOW:
                    value += ValChow;
                    break;
                case JOINTPARTIAL:
                    value += ValJoint;
                    break;
                case PARTEDPARTIAL:
                    value += ValParted;
                    break;
            }

            value += temp;
        }

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

    // populate each meld's wantedTiles<>
    private void findWantedTiles(ArrayList<ArrayList<Meld>> suitPermutations, int[] remainingTileCounterSubset){
        for (ArrayList<Meld> onePermutation: suitPermutations){
            ArrayList<WantedTile> usefulTiles = new ArrayList<>();
            for (Meld meld: onePermutation){
                int type = meld.type;
                int s = meld.suit;
                int r = meld.rank;

                if (type == KONG); // cant find more of 4 duplicates, so do nothing
                else if (type == PONG || type == EYES || type == LONETILE){
                    if (remainingTileCounterSubset[r] != 0){
                        checkDupeWantedTiles(usefulTiles, meld, s, r, remainingTileCounterSubset[r]);
                    }
                }
                else if (type == CHOW){
                    if (r != 0 && remainingTileCounterSubset[r - 1] != 0){
                        checkDupeWantedTiles(usefulTiles, meld, s, r - 1, remainingTileCounterSubset[r - 1]);
                    }
                    if (r != 6 && remainingTileCounterSubset[r + 3] != 0){
                        checkDupeWantedTiles(usefulTiles, meld, s, r + 3, remainingTileCounterSubset[r + 3]);
                    }
                }
                else if (type == JOINTPARTIAL){
                    if (r != 0 && remainingTileCounterSubset[r - 1] != 0){
                        checkDupeWantedTiles(usefulTiles, meld, s, r - 1, remainingTileCounterSubset[r - 1]);
                    }
                    if (r != 7 && remainingTileCounterSubset[r + 2] != 0){
                        checkDupeWantedTiles(usefulTiles, meld, s, r + 2, remainingTileCounterSubset[r + 2]);
                    }
                }
                else if (type == PARTEDPARTIAL){
                    if (remainingTileCounterSubset[r + 1] != 0){
                        checkDupeWantedTiles(usefulTiles, meld, s, r + 1, remainingTileCounterSubset[r + 1]);
                    }
                }
                else System.err.println("unknown meld found");
            }
            usefulTiles.clear();
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
            
         /* This program will use 2 form of memoization to optimize the outputs for finding permutations.
            Memoizing the int[][] tileCounter. 
                This method checks if the tree is going in a route that has already been backtracked.
                Since repeating results only occur on the route looking for eyes, chows, joint partials, 
                & parted partials, memoization of can only occur in those function instances. Here, 
                memoiziation will only be utilized when searching for eyes, as including a memoizations 
                system for other searches makes the code less readible & bare provides any runtime boosts.
            Brute force comparison to prior permutations
                Compares candidate permutations to all permutations that's already been logged. Determines 
                & removes candidate perumation is the same (but in different order), or a subset to another 
                permutation. The more permutations there are, the longer this algorithm takes. */
            ArrayList<int[]> memoEyes = new ArrayList<>();

            if (i <= 2) scanDupes(i , allPermutations.get(i), melds, tileCounterSubset, memoEyes, 4, true);
            else scanHonors(i, honors, melds, tileCounterSubset, memoEyes, 4, true);
        }

        // if a suit has no possible melds, insert an empty set
        for (ArrayList<ArrayList<Meld>> suitPermutations: allPermutations){
            if (suitPermutations.size() == 0){
                ArrayList<Meld> empty = new ArrayList<>(); 
                suitPermutations.add(empty);
            }
        }

        // append remaining lone tile if any to repective permutations
        for (int suit = 0; suit < allPermutations.size(); suit++){
            ArrayList<ArrayList<Meld>> suitPermutations = allPermutations.get(suit);
            for (ArrayList<Meld> onePermutation: suitPermutations){
                int[] tileCounterSubsetCopy = tileCounter[suit].clone();
                for (Meld m: onePermutation){
                    if (m.type >= 2){
                        tileCounterSubsetCopy[m.rank] -=  m.type;
                    }
                    else if (m.type == CHOW){
                        tileCounterSubsetCopy[m.rank] -=  1;
                        tileCounterSubsetCopy[m.rank + 1] -=  1;
                        tileCounterSubsetCopy[m.rank + 2] -=  1;
                    }
                    else if (m.type == JOINTPARTIAL){
                        tileCounterSubsetCopy[m.rank] -=  1;
                        tileCounterSubsetCopy[m.rank + 1] -=  1;
                    }
                    else if (m.type == PARTEDPARTIAL){
                        tileCounterSubsetCopy[m.rank] -=  1;
                        tileCounterSubsetCopy[m.rank + 2] -=  1;
                    }
                }
                for (int i = 0; i < tileCounterSubsetCopy.length; i++){
                    if (tileCounterSubsetCopy[i] != 0 && tileCounterSubsetCopy[i] == 1) onePermutation.add(new Meld(LONETILE, suit, i));
                }
            }
        }
        
        // cartesian product between all suit to create a full hand/path
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

        return allPossiblePaths;
    }

    /// DEBUG
    static int stackFrameCounter = 0;
    static int memoOccurence = 0;
    static int subsetCheckOccurence = 0;
    static int tileCounterChecks = 0;

    // subroutine of all scan melds functions
    private static boolean checkIfNewPath(ArrayList<int[]> memo, int[] tileCounterSubset){
        for (int[] memoElt: memo){
            for (int j = 0; j < tileCounterSubset.length; j++){
                tileCounterChecks++; /// DEBUG COUNTER
                for (int k = 0; k < tileCounterSubset.length; k++){
                if (memoElt[k] != tileCounterSubset[k]) break;
                if (k == tileCounterSubset.length - 1) return false;
                }
            }
        }
        memo.add(tileCounterSubset.clone());
        memoOccurence++; /// DEBUG COUNTER
        return true;
    }

    // decending the list, recurse/chain call functions
    // scanHonors(4) | scanDupes(4):            kong            {n, n, n, n}
    // scanHonors(3) | scanDupes(3):            pongs           {n, n, n}
    // scanHonors(2) | scanDupes(2):            eyes            {n, n}
    //                 scanChow():              complete chows  {n, n+1, n+2}
    //                 scanJointPartialChow():  partial chows   {n, n+1}
    //                 scanPartialPartedChow(): partial chow    {n, n+2}

    private void scanHonors(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset, ArrayList<int[]> memoEyes, int copies, boolean isFunc1stInstance){
        stackFrameCounter++;
        if (copies == 2 && !checkIfNewPath(memoEyes, tileCounterSubset)) return;

        boolean everCreatedMeld = false;
        for (int i = 0; i < tileCounterSubset.length; i++){
            if (tileCounterSubset[i] >= copies){
                everCreatedMeld = true;
                tileCounterSubset[i] -= copies;

                melds.add(new Meld(copies, suit, i));
                scanHonors(suit, suitPermutations, melds, tileCounterSubset, memoEyes, copies, false);

                tileCounterSubset[i] += copies;
                melds.remove(melds.size() - 1);
            }
        }

        if (copies > 2){
            if (isFunc1stInstance) memoEyes.clear();
            scanHonors(suit, suitPermutations, melds, tileCounterSubset, memoEyes, copies - 1, true);
        }

        if (!everCreatedMeld && !melds.isEmpty() && checkNewSequence(suitPermutations, melds)){
            ArrayList<Meld> deepCopy = new ArrayList<>();
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i).clone());
            suitPermutations.add(deepCopy);
        }
    }

    private void scanDupes(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset, ArrayList<int[]> memoEyes, int copies, boolean isFunc1stInstance){
        stackFrameCounter++;
        if (copies == 2 && !checkIfNewPath(memoEyes, tileCounterSubset)) return;

        for (int i = 0; i < 9; i++){
            if (tileCounterSubset[i] >= copies){
                tileCounterSubset[i] -= copies;

                // copies: 4 = kong, 3 = pong, 2 = eyes
                melds.add(new Meld(copies, suit, i));
                scanDupes(suit, suitPermutations, melds, tileCounterSubset, memoEyes, copies, false);

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
            if (isFunc1stInstance) memoEyes.clear();
            scanDupes(suit, suitPermutations, melds, tileCounterSubset, memoEyes, copies - 1, true);
        }
        // after eyes, start searching for chows
        else scanChow(suit, suitPermutations, melds, tileCounterSubset);
    }

    private void scanChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset){
        stackFrameCounter++;

        for (int i = 0; i < 7; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;
            tileCounterSubset[i + 2] -= 1;

            melds.add(new Meld(CHOW, suit, i));

            scanChow(suit, suitPermutations, melds, tileCounterSubset);
            
            // "release" the tiles, undo the set afterwards
            // this allows backtracking for all possible permutations sets of melds 
            tileCounterSubset[i] += 1;
            tileCounterSubset[i + 1] += 1;
            tileCounterSubset[i + 2] += 1;
            melds.remove(melds.size() - 1);
        }
        scanJointPartialChow(suit, suitPermutations, melds, tileCounterSubset);
    }

    private void scanJointPartialChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset){
        stackFrameCounter++;

        for (int i = 0; i < 8; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;

            melds.add(new Meld(JOINTPARTIAL, suit, i));

            scanJointPartialChow(suit, suitPermutations, melds, tileCounterSubset);
            
            // "release" the tiles, undo the set afterwards
            // this allows backtracking for all possible permutations sets of melds 
            tileCounterSubset[i] += 1;
            tileCounterSubset[i + 1] += 1;
            melds.remove(melds.size() - 1);
        }
        scanPartedPartialChow(suit, suitPermutations, melds, tileCounterSubset);
    }

    private void scanPartedPartialChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, int[] tileCounterSubset){
        stackFrameCounter++;

        boolean everCreatedMeld = false;
        for (int i = 0; i < 7; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            everCreatedMeld = true;
            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 2] -= 1;

            melds.add(new Meld(PARTEDPARTIAL, suit, i));

            scanPartedPartialChow(suit, suitPermutations, melds, tileCounterSubset);
            
            // "release" the tiles, undo the set afterwards
            // this allows backtracking for all possible permutations sets of melds 
            tileCounterSubset[i] += 1;
            tileCounterSubset[i + 2] += 1;
            melds.remove(melds.size() - 1);
        }

        // record the possible set of melds when no more sets can be formed
        if (!everCreatedMeld && !melds.isEmpty() && checkNewSequence(suitPermutations, melds)){
            ArrayList<Meld> deepCopy = new ArrayList<>();
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i).clone());
            suitPermutations.add(deepCopy);
        }
    }

    // ensures no subsets are added to the suitPermutations
    private boolean checkNewSequence(ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds){
        if (suitPermutations.isEmpty()) return true;

        int uniqueToAllPermutations = 0;

        for (ArrayList<Meld> onePermutation: suitPermutations){
            if (melds.size() > onePermutation.size()){
                uniqueToAllPermutations++;
                continue;
            }
    
            subsetCheckOccurence++; /// DEBUG GLOBAL COUNTER

            int sameMeldFound = 0;
            for (Meld m: melds){
                if (m.type == LONETILE) continue;
                for (Meld n: onePermutation){
                    if(n.type == LONETILE) continue;
                    if (m.type == n.type && m.suit == n.suit && m.rank == n.rank){
                        sameMeldFound++;
                        break;
                    }
                }
            }
            if (sameMeldFound != melds.size()) uniqueToAllPermutations++;
        }
        if (uniqueToAllPermutations == suitPermutations.size()) return true;

        return false;
    }
   
}