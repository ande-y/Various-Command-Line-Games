package mahjong;

import java.util.ArrayList;

public class Algorithm {
    final static int KONG = 4;
    final static int PONG = 3;
    final static int EYES = 2;
    final static int LONETILE = 1;
    final static int CHOW = 0;
    final static int JOINTPARTIAL = -1;
    final static int PARTEDPARTIAL = -2;

    public static ArrayList<ArrayList<Meld>> getAllPossiblePaths(ArrayList<ArrayList<ArrayList<Meld>>> allPermutations, ArrayList<ArrayList<ArrayList<Tile>>> tileTracker){
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

    private static void scanHonors(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset, ArrayList<int[]> memoEyes, int copies, boolean isFunc1stInstance){
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

    private static void scanDupes(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset, ArrayList<int[]> memoEyes, int copies, boolean isFunc1stInstance){
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

    private static void scanChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset){
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

    private static void scanJointPartialChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset){
        for (int r = 0; r < 8; r++){
            if (tileTrackerSubset.get(r).size() == 0) continue;
            if (tileTrackerSubset.get(r + 1).size() == 0) continue;

            captureMeld(melds, tileTrackerSubset, JOINTPARTIAL, suit, r);

            scanJointPartialChow(suit, suitPermutations, melds, tileTrackerSubset);

            releaseMeld(melds, tileTrackerSubset);
        }
        scanPartedPartialChow(suit, suitPermutations, melds, tileTrackerSubset);
    }

    private static void scanPartedPartialChow(int suit, ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset){
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
                for (int k = 0; k < tileCounterSubset.length; k++){
                if (memoElt[k] != tileCounterSubset[k]) break;
                if (k == tileCounterSubset.length - 1) return false;
                }
            }
        }
        memo.add(tileCounterSubset.clone());
        return true;
    }

    // memoization subroutine of functions which add candidate permutation, scanHonors() & scanPartedPartialChow()
    // ensures no subsets are added to the suitPermutations
    private static boolean checkNewSequence(ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds){
        if (suitPermutations.isEmpty()) return true;

        int uniqueToAllPermutations = 0;

        for (ArrayList<Meld> onePermutation: suitPermutations){
            if (melds.size() > onePermutation.size()){
                uniqueToAllPermutations++;
                continue;
            }
    
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
    private static void captureMeld(ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset, int type, int suit, int rank){
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
    private static void releaseMeld(ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset){
        Meld toRemove = melds.remove(melds.size() - 1);
        for (Tile t: toRemove.tiles){
            int r = t.getRank();
            tileTrackerSubset.get(r).add(t);
        }
    }

    // subroutine of functions which add candidate permutation, scanHonors() & scanPartedPartialChow()
    // gets deep copy of the permutation (excluding meld's tiles) & appends remaining lone tiles to the permutation
    private static void addToPermutations(ArrayList<ArrayList<Meld>> suitPermutations, ArrayList<Meld> melds, ArrayList<ArrayList<Tile>> tileTrackerSubset, int suit){
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
