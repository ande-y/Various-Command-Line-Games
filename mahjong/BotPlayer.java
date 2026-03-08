package mahjong;

import java.util.ArrayList;

public class BotPlayer extends Player{
    ArrayList<ArrayList<Meld>> allPossiblePaths;
    ArrayList<Double> valueOfPossiblePaths;

    /// DEBUG
    static int stackFrameCounter = 0;
    static int memoOccurence = 0;
    static int subsetCheckOccurence = 0;
    static int tileCounterChecks = 0;

    private static final int KONG = 4;
    private static final int PONG = 3;
    private static final int EYES = 2;
    private static final int LONETILE = 1;
    private static final int CHOW = 0;
    private static final int JOINTPARTIAL = -1;
    private static final int PARTEDPARTIAL = -2;

    private static class Meld {
        int type;
        int suit;
        int rank;
        ArrayList<Tile> tiles = new ArrayList<>();
        ArrayList<WantedTile> wantedTiles = new ArrayList<>();
        public Meld(int type, int suit, int rank){
            this.type = type;
            this.suit = suit;
            this.rank = rank;
        }
        public Meld clone(){
            Meld m = new Meld(type, suit, rank);
            for (Tile t: tiles) m.tiles.add(t);
            return m;
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

    public static void printCodeMeldList(ArrayList<Meld> list){
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
        System.out.print("[");
        for (Meld m: list){
            for (Tile t: m.tiles){
                System.out.print(t.getSymbol());
            }
            System.out.print(":");
        }
        System.out.print("]");
    }

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
        double[] valuePerTile = findTileValues(chosenPath);
        
        // // determine if hidden kong should be declared, draw another tile and revaluate the whole hand if necessary

        // // discard the tile with the lowest value
        int toDrop = 0;
        double leastValue = 0;
        for (int i = 0; i < valuePerTile.length; i++){
            if (valuePerTile[i] < leastValue){
                toDrop = i;
                leastValue = valuePerTile[i];
            }
        }
        
        Tile t = hand.remove(toDrop);
        return t;
    }

    // subroutine of makeDecision() & evaluate()
    private double[] findTileValues(ArrayList<Meld> path){
        // MAGIC NUMBERS: bot decision making parameters
        final double isHonor =          0.1;
        // base value of all melds/submelds
        final double ValKong =          1.8;
        final double ValPong =          1.5;
        final double ValEyes =          0.9;
        final double ValChow =          1;
        // additional value if the needed tile is undrawn yet
        final double ToKong =           0.1;
        final double ToPong =           0.15;
        final double SplitChow =        0.06;
        final double ToChow =           0.11;
        final double ToEyes =           0.14;
        // additional value if a tile in a meld is needed by another meld (tile can be used to switch plans)
        final double MultiMeldBonus =   0.12;
        // additional value if multiple melds needs the same tile
        final double PriorityPong =     .1;
        final double PriorityEyes =     .1;
        final double PriorityChow =     .1;
        final double PriorityJoint =    .1;
        final double PriorityParted =   .1;

        ArrayList<ArrayList<Integer>> meldMap = mapMeldsToHand(path);

        double[] valuePerTile = new double[hand.size()];

        int meldsComplete = meldsPlacedDown;
        boolean eyesComplete = false;
        
        for (int i = 0; i < path.size(); i++){
            Meld meld = path.get(i);
            int t = meld.type;
            int s = meld.suit;
            int r = meld.rank;
            
            // get list of wanted tile by other melds
            ArrayList<WantedTile> wantedByOthers = new ArrayList<>();
            for (Meld m: path){
                if (m == meld) continue;
                for (WantedTile w: m.wantedTiles) wantedByOthers.add(w);
            }

            if (t <= KONG && t >= CHOW && t != EYES) meldsComplete++;
            if (t == EYES) eyesComplete = true;

            // 1. set all tiles within there respective melds a value
            // 2. add any value for potential steals
            // 3. add value if tiles are needed other other melds
            // 4. apply bonus if tile is honor
            if (t == KONG) calculateDupeValues(valuePerTile, meld, i, wantedByOthers, meldMap, ValKong, 0, MultiMeldBonus);
            else if (t == PONG) calculateDupeValues(valuePerTile, meld, i, wantedByOthers, meldMap, ValPong, ToKong, MultiMeldBonus);
            else if (t == EYES) calculateDupeValues(valuePerTile, meld, i, wantedByOthers, meldMap, ValEyes, ToPong, MultiMeldBonus);
            else if (t == CHOW){
                double[] temps = {ValChow, ValChow, ValChow};
                
                ArrayList<WantedTile> wanted = meld.wantedTiles;
                if (wanted.size() != 1 && wanted.size() != 2) System.err.println("<!> findTileValues: Chow only has 1 or 2 wanted tiles, found " + meld.wantedTiles.size());
                if (r == 0){
                    temps[1] += wanted.get(0).chance * SplitChow; 
                    temps[2] += wanted.get(0).chance * SplitChow; 
                }
                else if (r == 6){
                    temps[0] += wanted.get(0).chance * SplitChow; 
                    temps[1] += wanted.get(0).chance * SplitChow; 
                }
                else {
                    temps[0] += wanted.get(0).chance * SplitChow; 
                    temps[1] += wanted.get(0).chance * SplitChow; 
                    temps[1] += wanted.get(1).chance * SplitChow; 
                    temps[2] += wanted.get(1).chance * SplitChow; 
                }

                for (int a = 0; a < 3; a++){
                    for (WantedTile w: wantedByOthers){
                        if (s == w.suit && r + a == w.rank) temps[a] += MultiMeldBonus * w.priority * (4 - w.chance);
                    }
                }  
                for (int a = 0; a < 3; a++){
                    int index = meldMap.get(i).get(a);
                    valuePerTile[index] = temps[a];
                }
            }
            else if (t == JOINTPARTIAL){
                double[] temps = {0, 0};
                
                ArrayList<WantedTile> wanted = meld.wantedTiles;
                if (wanted.size() != 1 && wanted.size() != 2) System.err.println("<!> findTileValues: Joint partial only has 1 or 2 wanted tiles, found " + meld.wantedTiles.size());
                for (WantedTile w: wanted){
                    temps[0] += w.chance * ToChow; 
                    temps[1] += w.chance * ToChow; 
                }

                for (int a = 0; a < 2; a++){
                    for (WantedTile w: wantedByOthers){
                        if (s == w.suit && r + a == w.rank) temps[a] += MultiMeldBonus * w.priority * (4 - w.chance);
                    }
                }
                for (int a = 0; a < 2; a++){
                    int index = meldMap.get(i).get(a);
                    valuePerTile[index] = temps[a];
                }
            }
            else if (t == PARTEDPARTIAL){
                double[] temps = {0, 0};
                
                ArrayList<WantedTile> wanted = meld.wantedTiles;
                if (wanted.size() != 1) System.err.println("<!> findTileValues: Parted partial only has 1 wanted tiles, found " + meld.wantedTiles.size());
                temps[0] += wanted.get(0).chance * ToChow; 
                temps[1] += wanted.get(0).chance * ToChow; 

                for (int a = 0; a < 2; a++){
                    for (WantedTile w: wantedByOthers){
                        if (s == w.suit && r + a == w.rank) temps[a] += MultiMeldBonus * w.priority * (4 - w.chance);
                    }
                }
                for (int a = 0; a < 2; a++){
                    int index = meldMap.get(i).get(a);
                    valuePerTile[index] = temps[a];
                }
            }
            else if (t == LONETILE){
                double temp = 0;

                ArrayList<WantedTile> wanted = meld.wantedTiles;
                if (meld.wantedTiles.size() != 1) System.err.println("<!> findTileValues: Parted partial only has 1 wanted tiles, found " + meld.wantedTiles.size());
                temp += wanted.get(0).chance * ToEyes; 

                int index = meldMap.get(i).get(0);
                valuePerTile[index] = temp;
            }

            // suit 3 are Winds, 4 are Dragons
            if (meld.suit >= 3){
                for (int index: meldMap.get(i)) valuePerTile[index] += isHonor;
            }
        }

        if (meldsComplete == 4 && eyesComplete) mahjong = true;

        return valuePerTile;
    }

    // subroutine of findTileValues()
    private void calculateDupeValues(double[] valuePerTile, Meld meld, int meldIndex, ArrayList<WantedTile> wantedByOthers, ArrayList<ArrayList<Integer>> meldIndexer, double baseVal, double promotionVal, double MultiMeldBonus){
        double val = baseVal;

        // for duplicates, there can only be 1 wanted tile
        if (meld.wantedTiles.size() != 1) debug();

        if (meld.wantedTiles.size() != 1) System.err.println("<!> calculateDupeValues: Dupe meld supposed to have 1 wanted tile, instead has " + meld.wantedTiles.size());
        WantedTile wanted = meld.wantedTiles.get(0);
        val += wanted.chance * promotionVal; 

        for (WantedTile w: wantedByOthers){
            if (meld.suit == w.suit && meld.rank == w.rank) val += MultiMeldBonus * w.priority * (4 - w.chance);
        }
        for (int a = 0; a < meld.type; a++){
            int index = meldIndexer.get(meldIndex).get(a);
            valuePerTile[index] = val;
        }
    }
    
    // map melds' tiles to its respective tile in the hand
    // subroutine of findTileValues()
    private ArrayList<ArrayList<Integer>> mapMeldsToHand(ArrayList<Meld> path){
        ArrayList<ArrayList<Integer>> meldMap = new ArrayList<>();

        for (int mIndex = 0; mIndex < path.size(); mIndex++){
            Meld m = path.get(mIndex);
            meldMap.add(new ArrayList<>());
            for (Tile t: m.tiles){
                for (int i = 0; i < hand.size(); i++){
                    Tile h = hand.get(i);
                    if (t == h) meldMap.get(mIndex).add(i);
                }
            }

        }
        return meldMap;
    }

    public void evaluate(Table table){
        // what's being replicated by below: ArrayList<Tile>[][] tileTracker
        // this will also the tileCounter
        ArrayList<ArrayList<ArrayList<Tile>>> tileTracker = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            tileTracker.add(new ArrayList<>());
            int suitSize = 9;
            if (i == 3) suitSize = 4;
            else if (i == 4) suitSize = 3;
            for (int j = 0; j < suitSize; j++) tileTracker.get(i).add(new ArrayList<>());
        }

        // populate the tileTracker
        for (Tile t: hand){
            if (t.getSuit() >= 5) System.err.println("<!> evaluate: Bonus Tile Found in Hand");
            int s = t.getSuit();
            int r = t.getRank();
            tileTracker.get(s).get(r).add(t);
        }

        ArrayList<ArrayList<Meld>> craks = new ArrayList<>();
        ArrayList<ArrayList<Meld>> dots = new ArrayList<>();
        ArrayList<ArrayList<Meld>> sticks = new ArrayList<>();
        ArrayList<ArrayList<Meld>> winds = new ArrayList<>();
        ArrayList<ArrayList<Meld>> dragons = new ArrayList<>();

        ArrayList<ArrayList<ArrayList<Meld>>> allPermutations = new ArrayList<>();
        allPermutations.add(craks);
        allPermutations.add(dots);
        allPermutations.add(sticks);
        allPermutations.add(winds);
        allPermutations.add(dragons);

        allPossiblePaths = findAllPossiblePaths(allPermutations, tileTracker);

        // get counter on what tiles & how many of them can possibly appear in the future
        int[][] remainingTileCounter = table.getDiscardedTilesCounter();
        for (int i = 0; i < remainingTileCounter.length; i++){
            for (int j = 0; j < remainingTileCounter[i].length; j++){
                remainingTileCounter[i][j] = 4 - (remainingTileCounter[i][j] + tileTracker.get(i).get(j).size());
            }
        }
        // for each meld in a permutation, find what tiles are desired/can be stolen
        for (int i = 0; i < allPermutations.size(); i++){
            ArrayList<ArrayList<Meld>> suitPermutations = allPermutations.get(i);
            findWantedTiles(suitPermutations, remainingTileCounter[i]);
        }

        // give each possible play the average value between all its tiles
        valueOfPossiblePaths = new ArrayList<>();
        for (ArrayList<Meld> possiblePath: allPossiblePaths){
            double[] valuesOfTiles = findTileValues(possiblePath);
            double sum = 0;
            for (double d: valuesOfTiles) sum += d;
            valueOfPossiblePaths.add(sum / valuesOfTiles.length);
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
            System.out.printf("%d %-3d ", i, suitPermutations.size());
            for (int j = 0; j < suitPermutations.size(); j++){
                printCodeMeldList(suitPermutations.get(j));
            }
            System.out.println();
        }
        System.out.println();
        // print tiles unicode grapahics
        for (int i = 0; i < allPossiblePaths.size(); i++){
            System.out.printf("%.2f ", valueOfPossiblePaths.get(i));            
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
        for (int i = 0; i < tileTracker.size(); i++){
            for (int j = 0; j < tileTracker.get(i).size(); j++){
                System.out.print(tileTracker.get(i).get(j).size() + " ");
            }
            System.out.println();
        }
        // print info
        System.out.println("Total permutations: " + (craks.size() + dots.size() + sticks.size() + winds.size() + dragons.size()));
        System.out.println("Total tileCounterSubset[]s memoized: " + memoOccurence);
        System.out.println("Total tileCounterSubset[]s checked: " + tileCounterChecks);
        System.out.println("Total subsetChecks occured: " + subsetCheckOccurence);
        System.out.println("Total stack frames invoked: " + stackFrameCounter);
        System.out.println();
        } /// DEBUG END

        return;
    }

    // subroutine of evaluate()
    // populate each meld's wantedTiles<>
    private void findWantedTiles(ArrayList<ArrayList<Meld>> suitPermutations, int[] remainingTileCounterSubset){
        for (ArrayList<Meld> onePermutation: suitPermutations){
            ArrayList<WantedTile> usefulTiles = new ArrayList<>();
            for (Meld meld: onePermutation){
                int type = meld.type;
                int s = meld.suit;
                int r = meld.rank;

                if (type == KONG || type == PONG || type == EYES || type == LONETILE){
                    checkDupeWantedTiles(usefulTiles, meld, s, r, remainingTileCounterSubset[r]);
                }
                else if (type == CHOW){
                    if (r != 0) checkDupeWantedTiles(usefulTiles, meld, s, r - 1, remainingTileCounterSubset[r - 1]);
                    if (r != 6) checkDupeWantedTiles(usefulTiles, meld, s, r + 3, remainingTileCounterSubset[r + 3]);
                }
                else if (type == JOINTPARTIAL){
                    if (r != 0) checkDupeWantedTiles(usefulTiles, meld, s, r - 1, remainingTileCounterSubset[r - 1]);
                    if (r != 7) checkDupeWantedTiles(usefulTiles, meld, s, r + 2, remainingTileCounterSubset[r + 2]);
                }
                else if (type == PARTEDPARTIAL){
                    checkDupeWantedTiles(usefulTiles, meld, s, r + 1, remainingTileCounterSubset[r + 1]);
                }
                else System.err.println("<!> findWantedTile: unknown meld found");
            }
            usefulTiles.clear();
        }
    }

    // subroutine of findWantedTilesForAllMelds()
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

    private ArrayList<ArrayList<Meld>> findAllPossiblePaths(ArrayList<ArrayList<ArrayList<Meld>>> allPermutations, ArrayList<ArrayList<ArrayList<Tile>>> tileTracker){
        ArrayList<ArrayList<Meld>> craks = allPermutations.get(0);
        ArrayList<ArrayList<Meld>> dots = allPermutations.get(1);
        ArrayList<ArrayList<Meld>> sticks = allPermutations.get(2);
        ArrayList<ArrayList<Meld>> winds = allPermutations.get(3);
        ArrayList<ArrayList<Meld>> dragons = allPermutations.get(4);

        // find permutations possible for each suit disjointly
        for (int i = 0; i < allPermutations.size(); i++){
            ArrayList<ArrayList<Tile>> tileTrackerSubset = tileTracker.get(i);
            ArrayList<Meld> melds = new ArrayList<>(); 
            
         /* This program will use 2 form of memoization to optimize the outputs for finding permutations.
            Memoizing the int[][] tileCounter -> checkIfNewPath()
                This method checks if the tree is going in a route that has already been backtracked.
                Since repeating results only occur on routes looking for eyes, chows, joint partials, 
                & parted partials, memoization of can only occur in those function instances. Here, 
                memoiziation will only be utilized when searching for eyes, as including a memoizations 
                system for other searches makes the code less readible & bare provides any runtime boosts.
            Brute force comparison to prior permutations -> checkNewSequence()
                Compares candidate permutations to all permutations that's already been logged. Determines 
                & removes candidate permutation if the same (but in different order), or a subset to another 
                permutation. But the more permutations there are, the longer this algorithm takes. */
            ArrayList<int[]> memoEyes = new ArrayList<>();

            if (i <= 2) scanDupes(i , allPermutations.get(i), melds, tileTrackerSubset, memoEyes, 4, true);
            else scanHonors(i, allPermutations.get(i), melds, tileTrackerSubset, memoEyes, 4, true);
        }

        // if a suit has no possible melds, insert set with only lone tiles
        // if no lone tiles, insert an empty set
        for (int s = 0; s < allPermutations.size(); s++){
            ArrayList<ArrayList<Meld>> suitPermutations = allPermutations.get(s);
            ArrayList<ArrayList<Tile>> tileTrackerSubset = tileTracker.get(s);
            if (suitPermutations.size() == 0){
                ArrayList<Meld> temp = new ArrayList<>();
                for (int r = 0; r < tileTrackerSubset.size(); r++){
                    ArrayList<Tile> rankTracker = tileTrackerSubset.get(r);
                    if (rankTracker.size() == 1){
                        Meld loneTileMeld = new Meld(LONETILE, s, r);
                        loneTileMeld.tiles.add(rankTracker.get(0));
                        temp.add(loneTileMeld);
                    }
                }
                suitPermutations.add(temp);
            }
        }
        
        // cartesian product between all suit to create a full hand/path
        ArrayList<ArrayList<Meld>> allPossiblePaths = new ArrayList<>();
        for (int a = 0; a < craks.size(); a++){
            for (int b = 0; b < dots.size(); b++){
                for (int c = 0; c < sticks.size(); c++){
                    for (int d = 0; d < winds.size(); d++){
                        for (int e = 0; e < dragons.size(); e++){
                            ArrayList<Meld> result = new ArrayList<>(); 
                            for (Meld m: craks.get(a)) result.add(m);
                            for (Meld m: dots.get(b)) result.add(m);
                            for (Meld m: sticks.get(c)) result.add(m);
                            for (Meld m: winds.get(d)) result.add(m);
                            for (Meld m: dragons.get(e)) result.add(m);
                            allPossiblePaths.add(result);
                        }
                    }
                }
            }
        }

        return allPossiblePaths;
    }

    // decending the list, recurse/chain call functions
    // scanHonors(4) | scanDupes(4):            kong            {n, n, n, n}
    // scanHonors(3) | scanDupes(3):            pongs           {n, n, n}
    // scanHonors(2) | scanDupes(2):            eyes            {n, n}
    //                 scanChow():              complete chows  {n, n+1, n+2}
    //                 scanJointPartialChow():  partial chows   {n, n+1}
    //                 scanPartialPartedChow(): partial chow    {n, n+2}

    private void scanHonors(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset, ArrayList<int[]> memoEyes, int copies, boolean isFunc1stInstance){
        stackFrameCounter++; // DEBUG COUNTER 
        if (copies == 2 && !checkIfNewPath(memoEyes, tileTrackerSubset)) return;

        boolean everCreatedMeld = false;
        for (int r = 0; r < tileTrackerSubset.size(); r++){
            if (tileTrackerSubset.get(r).size() >= copies){
                everCreatedMeld = true;
                captureMeld(melds, tileTrackerSubset, copies, suit, r);
                
                scanHonors(suit, suitPermutations, melds, tileTrackerSubset, memoEyes, copies, false);
                
                releaseMeld(melds, tileTrackerSubset);
            }
        }

        if (copies > 2){
            if (isFunc1stInstance) memoEyes.clear();
            scanHonors(suit, suitPermutations, melds, tileTrackerSubset, memoEyes, copies - 1, true);
        }

        if (!everCreatedMeld && !melds.isEmpty() && checkNewSequence(suitPermutations, melds)){
            addToPermutations(suitPermutations, melds, tileTrackerSubset, suit);
        }
    }

    private void scanDupes(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset, ArrayList<int[]> memoEyes, int copies, boolean isFunc1stInstance){
        stackFrameCounter++; // DEBUG COUNTER
        if (copies == 2 && !checkIfNewPath(memoEyes, tileTrackerSubset)) return;

        for (int r = 0; r < 9; r++){
            if (tileTrackerSubset.get(r).size() >= copies){
                // copies: 4 = kong, 3 = pong, 2 = eyes
                captureMeld(melds, tileTrackerSubset, copies, suit, r);

                scanDupes(suit, suitPermutations, melds, tileTrackerSubset, memoEyes, copies, false);

                // "release" the tiles, undo the set afterwards
                // this allows backtracking for all possible permutations sets of melds 
                releaseMeld(melds, tileTrackerSubset);
            }
        }

        // if no kongs, start looking for pongs, then eyes
        if (copies > 2){
            /* the memo list when the 1st stack frame is searching for kongs/pongs turns out to be the same when it's 
               searching for eyes, therefore the list must be cleared before searching for eyes, since all path 
               identifiers (tileSubsetCounter variations) have already been memoized when searching kongs/pongs */
            if (isFunc1stInstance) memoEyes.clear();
            scanDupes(suit, suitPermutations, melds, tileTrackerSubset, memoEyes, copies - 1, true);
        }
        // after eyes, start searching for chows
        else scanChow(suit, suitPermutations, melds, tileTrackerSubset);
    }

    private void scanChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset){
        stackFrameCounter++; // DEBUG COUNTER

        for (int r = 0; r < 7; r++){
            if (tileTrackerSubset.get(r).size() == 0) continue;
            if (tileTrackerSubset.get(r + 1).size() == 0) continue;
            if (tileTrackerSubset.get(r + 2).size() == 0) continue;

            captureMeld(melds, tileTrackerSubset, CHOW, suit, r);

            scanChow(suit, suitPermutations, melds, tileTrackerSubset);
            
            releaseMeld(melds, tileTrackerSubset);
        }
        scanJointPartialChow(suit, suitPermutations, melds, tileTrackerSubset);
    }

    private void scanJointPartialChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset){
        stackFrameCounter++; // DEBUG COUNTER

        for (int r = 0; r < 8; r++){
            if (tileTrackerSubset.get(r).size() == 0) continue;
            if (tileTrackerSubset.get(r + 1).size() == 0) continue;

            captureMeld(melds, tileTrackerSubset, JOINTPARTIAL, suit, r);

            scanJointPartialChow(suit, suitPermutations, melds, tileTrackerSubset);

            releaseMeld(melds, tileTrackerSubset);
        }
        scanPartedPartialChow(suit, suitPermutations, melds, tileTrackerSubset);
    }

    private void scanPartedPartialChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset){
        stackFrameCounter++; // DEBUG COUNTER

        boolean everCreatedMeld = false;
        for (int r = 0; r < 7; r++){
            if (tileTrackerSubset.get(r).size() == 0) continue;
            if (tileTrackerSubset.get(r + 2).size() == 0) continue;
            everCreatedMeld = true;

            captureMeld(melds, tileTrackerSubset, PARTEDPARTIAL, suit, r);

            scanPartedPartialChow(suit, suitPermutations, melds, tileTrackerSubset);
            
            releaseMeld(melds, tileTrackerSubset);
        }

        // record the possible set of melds when no more sets can be formed
        if (!everCreatedMeld && !melds.isEmpty() && checkNewSequence(suitPermutations, melds)){
            addToPermutations(suitPermutations, melds, tileTrackerSubset, suit);
        }
    }

    // memoization subroutine of all scan melds functions
    private static boolean checkIfNewPath(ArrayList<int[]> memo, ArrayList<ArrayList<Tile>> tileTrackerSubset){
        int[] tileCounterSubset = new int[tileTrackerSubset.size()];
        for (int rank = 0; rank < tileTrackerSubset.size(); rank++) tileCounterSubset[rank] = tileTrackerSubset.get(rank).size(); 
        
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

    // memoization subroutine of functions which add candidate permutation, scanHonors() & scanPartedPartialChow()
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

    // subroutine of scan... Honors(), Dupes(), Chows(), JointPartialChow(), PartedPartialChow()
    private void captureMeld(ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset, int type, int suit, int rank){
        Meld newMeld = new Meld(type, suit, rank);

        // lone tile melds are created by another process
        if (type == KONG || type == PONG || type == EYES){
            ArrayList<Tile> rankTracker = tileTrackerSubset.get(rank);
            for (int i = 0; i < type; i++){
                Tile t = rankTracker.remove(0);
                newMeld.tiles.add(t);
            }
        }
        else if (type == CHOW || type == JOINTPARTIAL || type == PARTEDPARTIAL) {
            for (int i = 0; i < 3; i++){
                // CHOW will complete all 3 iterations
                if (type == JOINTPARTIAL && i == 2) continue;
                if (type == PARTEDPARTIAL && i == 1) continue;

                ArrayList<Tile> rankTracker = tileTrackerSubset.get(rank + i);
                Tile t = rankTracker.remove(0);
                newMeld.tiles.add(t);
            }
        }
        else System.err.println("<!> captureMeld: unknown meld type found");

        melds.add(newMeld);
    }

    // subroutine of scan... Honors(), Dupes(), Chows(), JointPartialChow(), PartedPartialChow()
    private void releaseMeld(ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset){
        Meld toRemove = melds.remove(melds.size() - 1);
        for (Tile t: toRemove.tiles){
            int r = t.getRank();
            tileTrackerSubset.get(r).add(t);
        }
    }

    // subroutine of functions which add candidate permutation, scanHonors() & scanPartedPartialChow()
    // gets deep copy of the permutation (excluding meld's tiles) & appends remaining lone tiles to the permutation
    private void addToPermutations(ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset, int suit){
        // record what is in the list
        ArrayList<Meld> deepCopy = new ArrayList<>();
        for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i).clone());

        // append any lone tiles to the list
        for (int r = 0; r < tileTrackerSubset.size(); r++){
            int remaining = tileTrackerSubset.get(r).size();
            if (remaining == 0) continue;
            else if (remaining == 1){
                // we dont use captureMeld() because it deducts from the tileTrackerSubset 
                Meld tempLoneMeld = new Meld(LONETILE, suit, r);
                Tile t = tileTrackerSubset.get(r).get(0);
                tempLoneMeld.tiles.add(t);

                deepCopy.add(tempLoneMeld);
            }
            else System.err.printf("<!> addToPermutation: remaining suit:%d rank:%d %d tiles not used to create larger sets\n", remaining, suit, r);
        }

        // add as new permutation
        suitPermutations.add(deepCopy);
    }

}