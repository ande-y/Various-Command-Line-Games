package mahjong;

import java.util.Random;

public class Mahjong {
    public static final int NOSTEAL = -1;
    public static final int CHOW = 0;
    public static final int PONG = 1;
    public static final int KONG = 2;
    public static final int CHOWMAHJ = 3;
    public static final int EYESMAHJ = 4;
    public static final int PONGMAHJ = 5;
    public static final int KONGMAHJ = 6;

    public static final int INCOMPLETE = 0;
    public static final int COMPLETE = 1;
    public static final int TIE = 2;

    static class Status {
        int game;
        int steal;
        public Status(int g, int s){
            game = g;
            steal = s;
        }
    }

    public static void main(String[] args) {
        Table table = new Table();
        Player[] players = {
            // new UserPlayer("YOU"),
            // new BotPlayer("a"),
            // new BotPlayer("a"),
            new BotPlayer("a")
        };

        // for (int i = 0; i < players.length; i++){
        //     for (int j = 0; j < 13; j++) players[i].pickTile(table.giveTile());
        // }

        // playGame(table, players);


        // for (int i = 0; i <players.length; i++){
        //     if (players[i] instanceof UserPlayer) System.out.print("is user  ");
        //     else System.out.print("not user ");
        //     players[i].debug();
        // }


        // {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},

        System.out.flush();

        players[0].pickTile(new Tile(0, 0, "🀇"));
        players[0].pickTile(new Tile(0, 0, "🀇"));
        players[0].pickTile(new Tile(0, 0, "🀇"));
        players[0].pickTile(new Tile(0, 0, "🀇"));
        players[0].pickTile(new Tile(0, 1, "🀈"));
        players[0].pickTile(new Tile(0, 1, "🀈"));
        // players[0].pickTile(new Tile(0, 2, "🀈"));
        // players[0].pickTile(new Tile(0, 2, "🀈"));
        players[0].pickTile(new Tile(0, 3, "🀈"));
        players[0].pickTile(new Tile(0, 3, "🀈"));
        players[0].pickTile(new Tile(0, 3, "🀈"));
        players[0].pickTile(new Tile(0, 3, "🀈"));
        players[0].debug();
        players[0].makeDecision();
        // players[0].debug();
    }

    public static void playGame(Table table, Player[] players){
        Random rand = new Random();
        // 1st turn == the dealer
        int turn = rand.nextInt(players.length);

        Status status = new Status(INCOMPLETE, NOSTEAL);
        while (status.game == INCOMPLETE){
            status = pickAndDrop(table, players, turn);
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

    public static Status pickAndDrop(Table table, Player[] players, int turn){
        players[turn].pickTile(table.giveTile());

        // turn player evaluates if they mahjong
        if (players[turn].getMahjong()) return new Status(COMPLETE, NOSTEAL);

        Tile discard = players[turn].makeDecision();

        int playerThatStole = playersSteal(players, turn, discard);

        if (table.noMoreTiles()) return new Status(TIE, NOSTEAL);

        return new Status(INCOMPLETE, playerThatStole);
    }

    public static int playersSteal(Player[] players, int playerThatDropped, Tile Discard){
        int[] action = new int[players.length];
        for (int i = 0; i < players.length; i++){
            action[i] = NOSTEAL;

            if (i == playerThatDropped) continue;
            int playerThatCanChow = (playerThatDropped + 1) % players.length;
            action[i] = players[i].askToSteal(playerThatCanChow);
        }

        int playerThatStole = NOSTEAL;
        for (int i = 0; i < players.length; i++){
            if (action[i] > playerThatStole) playerThatStole = i;
        }

        return playerThatStole;
    }
}
