#include <iostream>
#include <vector>
#include <ctime>
using namespace std;

#define CHAR 0
#define DOTS 1
#define STIX 2
#define WIND 3
#define DRAGON 4
#define FLOWER 5
#define SEASON 6

#define NONE 11
#define CHOW 22
#define PONG 33
#define KONG 44
#define EYES 55

const string BackOfTile = "🀫";
const string tileGraphics[7][9] = 
    {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
     {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
     {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
     {"🀀", "🀁", "🀂", "🀃", "X" , "X" , "X" , "X" , "X" },
     {"🀆", "🀅", "🀄", "X" , "X" , "X" , "X" , "X" , "X" },
     {"🀢", "🀣", "🀤", "🀥", "X" , "X" , "X" , "X" , "X" },
     {"🀦", "🀧", "🀨", "🀩", "X" , "X" , "X" , "X" , "X" }};

void thrErr(string s){
    cout << "<!> " << s << endl;
    exit(0);
}
    
struct Tile {
    int suit;
    int rank;
};

class Table {
    private:
    vector<Tile> wall;
    vector<Tile> pile;
    int discards[7][9] = {};

    public:
    Table(){
        for (int suit = 0; suit < 7; suit++){
            for (int rank = 0; rank < 9; rank++){
                if (suit >= 3 && rank >= 4) continue;       // skip nonexistant wind, dragon, season, & flower tiles
                if (suit == 4 && rank == 3) continue;       // skip a nonexistant dragon tiles
                if (suit >= 5){                             // add only one of each season & flower tiles
                    wall.push_back({suit, rank});
                    continue;
                }
                for (int dupe = 0; dupe < 4; dupe++){
                    wall.push_back({suit, rank});
                }
            }
        }
    }

    int remainingTiles(){ return wall.size(); }

    Tile giveTile(){
        int i = rand() % wall.size();
        Tile t = wall[i];
        wall.erase(wall.begin() + i);
        return t;
    }

    void takeDiscardedTile(Tile t){
        pile.push_back(t);
        discards[t.suit][t.rank]++;
    }

    void printPile(){
        for (int i = 0; i < pile.size(); i++){
            if (i % 23 == 0) cout << endl;
            cout << tileGraphics[pile[i].suit][pile[i].rank];
        }
        cout << endl;
    }
};

class Player {
    private:
    string name;
    bool playable = false;
    vector<Tile> hand;
    vector<Tile> revealed;

    public:
    Player(bool p, string n){
        playable = p;
        name = n;
    }

    string getName(){ return name; }
    bool isPlayable(){ return playable; }

    bool draw(Tile t){
        if (t.suit == FLOWER || t.suit == SEASON){
            revealed.push_back(t);
            return true;
        }
        hand.push_back(t);
        return false;
    }

    void steal(Tile t, int action){
        if (action == CHOW){

        }
        if (action == PONG){

        }
        if (action == KONG){

        }
        if (action == EYES){

        }
        
        thrErr("impossible steal action");
    }

    Tile drop(){
        int index;

        if (playable){
            cout << char(22) << "Discard a tile [1-14]: ";
            cin >> index;
            index--;
        }
        else {
            index = 0; // PLACEHOLDER
        }

        Tile choice = hand[index];
        hand.erase(hand.begin() + index);
        return choice;
    }

    void analyzeHand(){
        int tileCounter[5][9] = 
            {{0, 0, 0,  0,  0,  0,  0,  0,  0},
             {0, 0, 0,  0,  0,  0,  0,  0,  0},
             {0, 0, 0,  0,  0,  0,  0,  0,  0},
             {0, 0, 0,  0, -1, -1, -1, -1, -1},
             {0, 0, 0, -1, -1, -1, -1, -1, -1}};
        
        for (int i = 0; i < hand.size(); i++){
            int s = hand[i].suit;
            int r = hand[i].rank;
            if (tileCounter[s][r] == -1) thrErr("nonexistance tile detected");
            tileCounter[s][r]++;
        }

        // INCOMPLETE
    }

    int decide(Tile droppedTile, bool canChow){
        int action = NONE;

        if (playable){
            cout << "► make decision: ";
            int decision;
            cin >> decision;
        }
        else {
            // BOT algo
        }

        return action; // placeholder
    }

    void printTiles(bool gameFinished){
        if (playable || gameFinished){
            for (int i = 0; i < hand.size(); i++){
                int s = hand[i].suit, r = hand[i].rank;
                cout << tileGraphics[s][r];
            }
        }
        else {
            for (int i = 0; i < hand.size(); i++) cout << BackOfTile;
        }

        // /// DEBUG - everyone's tiles are shown
        // for (int i = 0; i < hand.size(); i++){
        //     int s = hand[i].suit, r = hand[i].rank;
        //     cout << tileGraphics[s][r];
        // }

        cout << endl;

        if (revealed.empty()) return;
        
        cout << "              ";
        for (int i = 0; i < revealed.size(); i++){
            int s = revealed[i].suit, r = revealed[i].rank;
            cout << tileGraphics[s][r];
        // cout << " ⬩ ";
        }
        cout << endl;
    }
};

void printTable(bool gameFinished, Player player[], Table table, int turn){
    printf("□ Remaining Wall Tiles: x%d %s\n", table.remainingTiles(), BackOfTile.c_str());
    printf("=============================================\n");
    printf("□ Permanently Discarded Tiles:");
    table.printPile();
    printf("=============================================\n");

    for (int i = 0; i < 4; i++){
        (i == turn) ? cout << "» " : cout << "  ";

        printf("%-12s", player[i].getName().c_str());
        player[i].printTiles(gameFinished);

        if (i != 3) printf("---------------------------------------------\n");
    }
    printf("=============================================\n");
}

string translate(int action){
    if (action == CHOW) return "CHOWs the tile!\n";
    if (action == PONG) return "PONGs the tile!\n";
    if (action == KONG) return "KONGs the tile!\n";
    if (action == EYES) return "take the tile as EYES!";
    thrErr ("impossible steal action");
    return " ";
}

void gameTie(Player player[], Table table, int turn){
    printTable(true, player, table, turn);
    cout << "□ No more tiles! The game ends with a tie.";
    exit(0);
}

void gameWin(Player player[], Table table, int turn){
    printTable(true, player, table, turn);
    cout << "□ " << player[turn].getName() << " wins!\n";
}

void playGame(Player player[], Table table, int dealer){
    int turn = dealer;
    cout << "□ " << player[turn].getName() << " starts the game.\n";

    while (true){
        // user must see their 14th tile when discarding, so print() comes before drop()
        // on the other hand, user must see what bot discards, so drop() comes before print()
        if (player[turn].isPlayable()) printTable(false, player, table, turn);
        Tile droppedTile = player[turn].drop();
        if (!player[turn].isPlayable()) printTable(false, player, table, turn);

        string t = tileGraphics[droppedTile.suit][droppedTile.rank];
        cout << "□ " << player[turn].getName() << " discards: " << t << endl;

        int action[4] = {NONE, NONE, NONE, NONE};
        for (int i = 0; i < 4; i++){
            if (i == turn) continue;
            bool canChow = ((turn + 1) % 4 == i) ? true : false;
            action[i] = player[i].decide(droppedTile, canChow);
        }

        int taker = turn, largest = NONE;
        for (int i = 0; i < 4; i++){
            if (largest < action[i]){
                largest = action[i];
                taker = i;
            }
        }

        system("cls");

        if (taker != turn){
            player[taker].steal(droppedTile, action[taker]);
            turn = taker;
            cout << "□ " << player[taker].getName() << " " << translate(action[taker]);
        }
        else {
            table.takeDiscardedTile(droppedTile);
            if (table.remainingTiles() == 0) gameTie(player, table, turn);

            turn = (turn + 1) % 4;
            while (player[turn].draw(table.giveTile()));
            cout << "□ " << player[turn].getName() << " draws a tile.\n";
        }

        // if (checkWin(player[turn])) gameWin(player, table, turn);
    }
}

int main(){
    srand(time(0));
    
    Player p0 = {true, "YOU"};
    Player p1 = {false, "opponent1"};
    Player p2 = {false, "opponent2"};
    Player p3 = {false, "opponent3"};

    Player player[4] = {p0, p1, p2, p3};
    Table table;

    for (int i = 0; i < 4; i++){
        for (int j = 0; j < 13; j++){
            while (player[i].draw(table.giveTile()));
        }
    }

    int dealer = rand() % 4;
    while(player[dealer].draw(table.giveTile()));

    playGame(player, table, dealer);

    return 0;
}