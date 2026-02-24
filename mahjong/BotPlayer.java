package mahjong;

public class BotPlayer extends Player{
    public BotPlayer(){
        super();
    }

    public int askToSteal(){
        return 1;
    }

    public void makeDecision(){
        int[][] tileCounter = 
            {{0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0},
             {0, 0, 0}};

        for (Tile t: hand) tileCounter[t.getSuit()][t.getRank()]++;
        
        // run evaluations for each suit disjointly
        for (int i = 0; i < 3; i++){
            int[] tileCounterSubset = tileCounter[i];

            // in order, with precedence, scan the subset for set
            // kong, 4 identicals 
            // pongs, 3 identicals 
            // eyes, 2 identicals 
            // chows, 3 of same suit in sequence 
            // partial neighbor chows, chow missing sides [n, n+1]
            // partial separated chow, chow missing middle [n, n+2]
            //      OR terminal chows, neighboring chows with terminals [1, 2] or [8, 9]
            
            scanKong(tileCounterSubset, 0);
        }
    }
    public void scanKong(int[] tileCounterSubset, int start){
        for (int i = start; i < 9; i++){
            if (tileCounterSubset[i] == 4){
                tileCounterSubset[i] -= 4;
                if (i < 8) scanKong(tileCounterSubset, i + 1);
            }
        }
        scanPong(tileCounterSubset, 0);
    }
    public void scanPong(int[] tileCounterSubset, int start){
        for (int i = start; i < 9; i++){
            if (tileCounterSubset[i] == 3){
                tileCounterSubset[i] -= 3;
                if (i < 8) scanPong(tileCounterSubset, i + 1);
            }
        }
        scanChow(tileCounterSubset, 0);
    }
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