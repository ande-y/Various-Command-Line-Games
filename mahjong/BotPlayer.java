package mahjong;

import java.util.ArrayList;

public class BotPlayer extends Player{
    ArrayList<ArrayList<Meld>> allPossiblePaths;
    ArrayList<Double> valueOfPossiblePaths;

    final int KONG = 4;
    final int PONG = 3;
    final int EYES = 2;
    final int LONETILE = 1;
    final int CHOW = 0;
    final int JOINTPARTIAL = -1;
    final int PARTEDPARTIAL = -2;

    public BotPlayer(String n){
        super(n);
    }

    public int askToSteal(boolean canChow){
        return 1;
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

            if (t == KONG || t == PONG || t == CHOW) meldsComplete++;
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

        allPossiblePaths = Algorithm.getAllPossiblePaths(allPermutations, tileTracker);

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
        // // print permutations of individual suits
        // System.out.println("printing all Permutations of all suits: ");
        // for (int i = 0; i < allPermutations.size(); i++){
        //     ArrayList<ArrayList<Meld>> suitPermutations = allPermutations.get(i);
        //     System.out.printf("%d %-3d ", i, suitPermutations.size());
        //     for (int j = 0; j < suitPermutations.size(); j++){
        //         Print.printCodeMeldList(suitPermutations.get(j));
        //     }
        //     System.out.println();
        // }
        // System.out.println();
        // // print tiles unicode grapahics
        for (int i = 0; i < allPossiblePaths.size(); i++){
            System.out.printf("%.2f ", valueOfPossiblePaths.get(i));            
            ArrayList<Meld> possiblePlay = allPossiblePaths.get(i);
            Print.printFancyMeldList(possiblePlay);
            System.out.print(" - ");
            Print.printWantedTiles(possiblePlay);
        }
        for (int i = 0; i < hand.size(); i++) System.out.print(hand.get(i).getSymbol());
        System.out.println();
        // System.out.println("\nPossible plays from this hand: " + allPossiblePaths.size());
        // // print tileCounter
        // for (int i = 0; i < tileTracker.size(); i++){
        //     for (int j = 0; j < tileTracker.get(i).size(); j++){
        //         System.out.print(tileTracker.get(i).get(j).size() + " ");
        //     }
        //     System.out.println();
        // }
        // // print info
        // System.out.println("Total permutations: " + (craks.size() + dots.size() + sticks.size() + winds.size() + dragons.size()));
        // System.out.println();
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

    // subroutine of findWantedTiles()
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

}