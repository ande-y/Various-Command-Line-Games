package mahjong;

import java.util.Random;

public class Mahjong {
    private static final int NOSTEAL = -1;
    private static final int CHOW = 0;
    private static final int PONG = 1;
    private static final int KONG = 2;
    private static final int CHOWMAHJ = 3;
    private static final int EYESMAHJ = 4;
    private static final int PONGMAHJ = 5;
    private static final int KONGMAHJ = 6;

    private static final int INCOMPLETE = 0;
    private static final int COMPLETE = 1;
    private static final int TIE = 2;

    static class Status {
        int game;
        int steal;
        public Status(int g, int s){
            game = g;
            steal = s;
        }
    }

    public static void main(String[] args){
        Print.clr();

        Table table = new Table();
        Player[] players = {
            // new UserPlayer("YOU"),
            // new BotPlayer("a"),
            // new BotPlayer("a"),
            new BotPlayer("a")
        };

        for (int i = 0; i < players.length; i++){
            for (int j = 0; j < 13; j++){
                while (players[i].pickTile(table.giveTile()));
            }
        }

        while (players[0].pickTile(table.giveTile()));
        long start = System.currentTimeMillis();
        players[0].evaluate(table);
        long end = System.currentTimeMillis();
        System.out.println("Time elapsed for eval: " + (end - start));

        // playGame(table, players);
    }

    public static void playGame(Table table, Player[] players){
        Random rand = new Random();
        // find the dealer
        int turn = rand.nextInt(players.length);

        Status status = new Status(INCOMPLETE, NOSTEAL);
        while (status.game == INCOMPLETE){
            status = pickAndDrop(table, players, turn, status);
            if (status.steal == NOSTEAL) turn++;
            else turn = status.steal + 1;
        }            

        if (status.game == TIE){
            System.out.println("no more tiles, game ends with a tie.");
        }
        else if (status.game == COMPLETE){
            String winner = "";
            for (int i = 0; i < players.length; i++){
                if (players[i].getMahjong()) winner = players[i].getName();
            }
            System.out.printf("%s wins\n", winner);
        }
    }

    public static Status pickAndDrop(Table table, Player[] players, int turn, Status status){
        // next player picks tile if no one steal
        if (status.steal == NOSTEAL) players[turn].pickTile(table.giveTile());
        // player takes discarded tile if they steal
        else players[turn].pickTile(table.giveMostRecentDiscard());

        // player evaluates their hand 
        players[turn].evaluate(table);
        // turn player evaluates if they win, return function prematurely if so
        if (players[turn].getMahjong()) return new Status(COMPLETE, NOSTEAL);

        // else wise player drops a tile
        Tile discard = players[turn].makeDecision(table);

        // see & determine any of the other plays will steal the discarded tile
        int playerThatStole = playersSteal(players, turn, discard);

        if (table.noMoreTiles()) return new Status(TIE, NOSTEAL);

        return new Status(INCOMPLETE, playerThatStole);
    }

    public static int playersSteal(Player[] players, int PlayerThatDropped, Tile discard){
        int[] action = new int[players.length];

        // check if the player after can chow

        int PlayerThatCanChow = (PlayerThatDropped + 1) % players.length;
        
        for (int i = 0; i < players.length; i++){
            action[i] = NOSTEAL;
            if (i == PlayerThatDropped) continue;
            
            boolean canChow = false;
            if (i == PlayerThatCanChow) canChow = true;

            // action[i] = players[i].askToSteal(canChow);
        }

        // evaluate the player who gets the steal
        int stealer = -1;
        for (int i = 0; i < players.length; i++){
            if (action[i] > stealer) stealer = i;
        }

        // stealer of the tile must expose their meld
        // players[stealer].exposeMeld(discard);

        // if the steal is a kong, player draws another tile
        if (action[stealer] == KONG);

        return 10;

    }
}
