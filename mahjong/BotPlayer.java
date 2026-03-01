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

    public static class meld {
        public meld(int t, int s, int r){
            type = t;
            suit = s;
            rank = r;
        }
        int type;
        int suit;
        int rank;
    }

    public BotPlayer(String n){
        super(n);
    }

    public int askToSteal(int playThatCanChow){
        return 1;
    }

    public static ArrayList<meld> combineArrayLists(ArrayList<meld> craks, ArrayList<meld> dots, ArrayList<meld> sticks, ArrayList<meld> honors){
        ArrayList<meld> result = new ArrayList<>(); 
        for (int i = 0; i < craks.size(); i++) result.add(craks.get(i));
        for (int i = 0; i < dots.size(); i++) result.add(dots.get(i));
        for (int i = 0; i < sticks.size(); i++) result.add(sticks.get(i));
        for (int i = 0; i < honors.size(); i++) result.add(honors.get(i));
        return result;
    }

    public static void printMeldList(ArrayList<meld> list){
        System.out.print("[");
        for (int i = 0; i < list.size(); i++){
            meld group = list.get(i);
            System.out.print("{" + group.type + "," + group.rank + "}");                
        }
        System.out.print("]");
    }

    private static String[][] tileIndex = 
        {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
         {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
         {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
         {"🀀", "🀁", "🀂", "🀃"},
         {"🀆", "🀅", "🀄"},
         {"🀢", "🀣", "🀤", "🀥"},
         {"🀦", "🀧", "🀨", "🀩"}};

    public static void printFancyMeldList(ArrayList<meld> list){
        System.out.print("[");
        for (int i = 0; i < list.size(); i++){
            meld group = list.get(i);
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

    public Tile makeDecision(){
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

        // find permutations possible for each suit disjointly
        ArrayList<ArrayList<meld>> craks = new ArrayList<>();
        ArrayList<ArrayList<meld>> dots = new ArrayList<>();
        ArrayList<ArrayList<meld>> sticks = new ArrayList<>();
        ArrayList<ArrayList<meld>> honors = new ArrayList<>();        
        ArrayList[] allPermutations = {craks, dots, sticks, honors};

        for (int i = 0; i < 5; i++){
            int[] tileCounterSubset = tileCounter[i];
            ArrayList<meld> melds = new ArrayList<>(); 
            
            if (i <= 2) scanDupes(i , allPermutations[i], melds, tileCounterSubset, 4, true);
            else scanHonors(i , honors, melds, tileCounterSubset, 4, true);

            memoEyes.clear();
            memoChows.clear();
            memoJoint.clear();
            memoDisjoint.clear();
        }

        for (int i = 0; i < allPermutations.length; i++){
            if (allPermutations[i].size() == 0){
                ArrayList<meld> empty = new ArrayList<>(); 
                allPermutations[i].add(empty);
            }
        }

        // print allSuitsAllPermutations
        System.out.println("printing all Permutations of all suits: ");
        for (int i = 0; i < allPermutations.length; i++){
            ArrayList<ArrayList<meld>> suitPermutations = allPermutations[i];
            System.out.printf("%d %-2d ", i, suitPermutations.size());
            for (int j = 0; j < suitPermutations.size(); j++){
                printMeldList(suitPermutations.get(j));
            }
            System.out.println();
        }
        
        ArrayList<ArrayList<meld>> allPossiblePlays = new ArrayList<>();
        for (int i = 0; i < craks.size(); i++){
            for (int j = 0; j < dots.size(); j++){
                for (int k = 0; k < sticks.size(); k++){
                    for (int l = 0; l < honors.size(); l++){
                        ArrayList<meld> temp = combineArrayLists(craks.get(i), dots.get(j), sticks.get(k), honors.get(l));
                        allPossiblePlays.add(temp);
                    }
                }
            }
        }

        System.out.println("printing all " + allPossiblePlays.size() + " possible plays: ");
        for (int i = 0; i < allPossiblePlays.size(); i++){
            printFancyMeldList(allPossiblePlays.get(i));
            System.out.println();
        }

        System.out.println("\nPossible plays from this hand: " + allPossiblePlays.size());
        System.out.println("Total permutations: " + (craks.size() + dots.size() + sticks.size() + honors.size()));
        System.out.println("Total tileCounterSubset[]s memoized: " + gCount2);
        System.out.println("Total stack frames invoked: " + gCount1);

        for (int i = 0; i < 5; i++){
            for (int j = 0; j < tileCounter[i].length; j++){
                System.out.print(tileCounter[i][j] + " ");
            }
            System.out.println();
        }
        debug();


        return hand.remove(0);
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

    public static boolean checkIfNewPath(ArrayList<int[]> memo, int[] tileCounterSubset){
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

    // in order, scan the subset for melds
    // scanDupes(4): eyes, 4 identicals
    // scanDupes(3): pongs, 3 identicals
    // scanDupes(2): kong, 2 identicals
    // scanChows(): complete chows, 3 of same suit in sequence 
    // scanPartialNeighborChow(): partial chows [n, n+1], implies if no this = no complete chows
    // scanPartialPartedChow() partial chow, tile missing in middle [n, n+2]

    public void scanHonors(int suit, ArrayList<ArrayList<meld>> suitPermutations, ArrayList<meld> melds, int[] tileCounterSubset, int copies, boolean isRootStackFrame){
        gCount1++;
        if (copies == 2 && !checkIfNewPath(memoEyes, tileCounterSubset)) return;

        boolean everCreatedMeld = false;
        for (int i = 0; i < tileCounterSubset.length; i++){
            if (tileCounterSubset[i] >= copies){
                everCreatedMeld = true;
                tileCounterSubset[i] -= copies;

                melds.add(new meld(copies, suit, i));
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
            ArrayList<meld> deepCopy = new ArrayList<>();
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i));
            suitPermutations.add(deepCopy);
        }
    }

    public void scanDupes(int suit, ArrayList<ArrayList<meld>> suitPermutations, ArrayList<meld> melds, int[] tileCounterSubset, int copies, boolean isRootStackFrame){
        gCount1++;
        if (copies == 2 && !checkIfNewPath(memoEyes, tileCounterSubset)) return;

        for (int i = 0; i < 9; i++){
            if (tileCounterSubset[i] >= copies){
                tileCounterSubset[i] -= copies;

                // copies: 4 = kong, 3 = pong, 2 = eyes
                melds.add(new meld(copies, suit, i));
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

    public void scanChow(int suit, ArrayList<ArrayList<meld>> suitPermutations, ArrayList<meld> melds, int[] tileCounterSubset, boolean isRootStackFrame){
        gCount1++;
        if (!checkIfNewPath(memoChows, tileCounterSubset)) return;

        for (int i = 0; i < 7; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;
            tileCounterSubset[i + 2] -= 1;

            melds.add(new meld(FULLCHOW, suit, i));

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

    public void scanJointPartialChow(int suit, ArrayList<ArrayList<meld>> suitPermutations, ArrayList<meld> melds, int[] tileCounterSubset, boolean isRootStackFrame){
        gCount1++;
        if (!checkIfNewPath(memoJoint, tileCounterSubset)) return;

        for (int i = 0; i < 8; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 1] == 0) continue;

            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 1] -= 1;

            melds.add(new meld(JOINTPARTIAL, suit, i));

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

    public void scanPartedPartialChow(int suit, ArrayList<ArrayList<meld>> suitPermutations, ArrayList<meld> melds, int[] tileCounterSubset, boolean isRootStackFrame){
        gCount1++;
        if (!checkIfNewPath(memoDisjoint, tileCounterSubset)) return;

        boolean everCreatedMeld = false;
        for (int i = 0; i < 7; i++){
            if (tileCounterSubset[i] == 0) continue;
            if (tileCounterSubset[i + 2] == 0) continue;

            everCreatedMeld = true;
            tileCounterSubset[i] -= 1;
            tileCounterSubset[i + 2] -= 1;

            melds.add(new meld(PARTEDPARTIAL, suit, i));

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
            ArrayList<meld> deepCopy = new ArrayList<>();
            for(int i = 0; i < melds.size(); i++) deepCopy.add(melds.get(i));
            suitPermutations.add(deepCopy);
        }
    }
}