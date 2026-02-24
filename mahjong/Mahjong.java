package mahjong;

import java.util.Random;

public class Mahjong {
    public static final int MAHJONGKONG = 0;
    public static final int MAHJONGPONG = 1;
    public static final int MAHJONGCHOW = 2;
    public static final int KONG = 3;
    public static final int PONG = 4;
    public static final int CHOW = 5;
    public static final int NOSTEAL = 6;

    public static final int INCOMPLETE = 0;
    public static final int COMPLETE = 1;
    public static final int TIE = 2;

    public static void main(String[] args) {
        Table table = new Table();
        Player p1 = new UserPlayer();
        Player p2 = new BotPlayer();
        Player p3 = new BotPlayer();
        Player p4 = new BotPlayer();

        Player[] players = {p1, p2, p3, p4};

        for (int i = 0; i < players.length; i++){
            for (int j = 0; j < 13; j++) players[i].pickTile(table.giveTile());
        }

        playGame(table, players);
        // p1.debug();
        // if (p1 instanceof UserPlayer) System.out.println("is User");
        // else System.out.println("not user");
    }

    public static void PlayGame(Table table, Player[] players){
        Random rand = new Random();
        // 1st turn == the dealer
        int turn = rand(players.length);

        int gameStatus = INCOMPLETE;
        while (gameStatus == INCOMPLETE){
            gameStatus = pickAndDrop(table, players, turn);
            turn++;
        }

        if (gameStatus == TIE){
            System.out.println("no more tiles, game ends with a tie.");
        }
        else if (gameStatus == COMPLETE){
            String winner;
            for (int i = 0; i < players.length; i++){
                if (player[i].getMahjong()) winner = player[i].getName();
            }
            System.out.printf("%s wins\n", winner);
        }
    }

    public static int pickAndDrop(Table table, Player[] players, int turn){
        players[turn].pickTile(table.giveTile());

        // turn player evaluates if they mahjong
        if (players[turn].getMahjong()) return COMPLETE;

        Tile discard = players[turn].makeDecision();

        playersSteal(players, turn, discard);

        if (table.noMoreTiles()) return TIE;
    }

    public static void playersSteal(Player[] players, int playerThatDropped, Tile Discard){
        int[] action = new int[players.length];
        for (int i = 0; i < players.length; i++){
            action[i] = NOSTEAL;

            if (i == playerThatDropped) continue;
            action[i] = players[i].askToSteal();
        }
    }


}
