package mahjong;

import java.util.ArrayList;

public class Print {
    static final int KONG = 4;
    static final int PONG = 3;
    static final int EYES = 2;
    static final int LONETILE = 1;
    static final int CHOW = 0;
    static final int JOINTPARTIAL = -1;
    static final int PARTEDPARTIAL = -2;

    public static void clr(){
        // System.out.flush();
        // System.out.print("\033[H\033[2J");
        // System.out.println("\n\n<<< PRIOR SESSION >>>\n\n");
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

    public static void printWantedTiles(ArrayList<Meld> list){
        final String[][] tileIndex = 
            {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
             {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
             {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
             {"🀀", "🀁", "🀂", "🀃"},
             {"🀆", "🀅", "🀄"},
             {"🀢", "🀣", "🀤", "🀥"},
             {"🀦", "🀧", "🀨", "🀩"}};

        ArrayList<WantedTile> record = new ArrayList<>();
        for (Meld meld: list){
            for (WantedTile newWanted: meld.wantedTiles){
                boolean isNew = true;
                for (WantedTile old: record){
                    if (newWanted == old) isNew = false;  
                }
                if (isNew) record.add(newWanted);
            }
        }

        System.out.print("[");
        for (WantedTile w: record){
            System.out.print(w.chance + tileIndex[w.suit][w.rank]);
            System.out.print(w.priority + " ");
        }
        System.out.println("]");
    }
}
