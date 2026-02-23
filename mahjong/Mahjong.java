package mahjong;

public class Mahjong {
    public static void main(String[] args) {
        Table table = new Table();
        Player p1 = new BotPlayer();

        for (int i = 0; i < 13; i++) p1.pickTile(table.giveTile());
        p1.debug();

        if (p1 instanceof UserPlayer) System.out.println("is User");
        else System.out.println("not user");
    }
}
