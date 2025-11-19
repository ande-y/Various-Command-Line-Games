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
const vector<vector<string>> tileGraphics = 
    {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
     {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
     {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
     {"🀀", "🀁", "🀂", "🀃"},
     {"🀆", "🀅", "🀄"},
     {"🀢", "🀣", "🀤", "🀥"},
     {"🀦", "🀧", "🀨", "🀩"}};

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
    vector<vector<int>> discardCounter =
        {{0, 0, 0, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0, 0, 0, 0, 0, 0},
         {0, 0, 0, 0},
         {0, 0, 0}};
    
    public:
    Table(){
        for (int suit = 0; suit < 7; suit++){
            for (int rank = 0; rank < tileGraphics[suit].size(); rank++){
                if (suit == FLOWER || suit == SEASON){
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
        discardCounter[t.suit][t.rank]++;
    }

    void printPile(){
        for (int i = 0; i < pile.size(); i++){
            if (i % 21 == 0) cout << endl;
            cout << tileGraphics[pile[i].suit][pile[i].rank];
        }
        cout << endl;
    }
};

class Player {
    private:
    string name;
    bool playable;
    vector<Tile> hand, revealed;
    vector<double> tileValues;

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
        
        thrErr("Player:steal: impossible steal action");
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
        vector<vector<int>> tileCounter = 
            {{0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0, 0, 0, 0, 0, 0},
             {0, 0, 0, 0},
             {0, 0, 0}};
        
        for (int i = 0; i < hand.size(); i++){
            int s = hand[i].suit;
            int r = hand[i].rank;
            tileCounter[s][r]++;
        }

        // assign default value for each tile (1 for suits, 0.8 for terminals, 0.7 for honors)
        for (int i = 0; i < hand.size(); i++){
            int s = hand[i].suit, r = hand[i].rank;
            if (s == WIND || s == DRAGON) tileValues[i] = .7;
            else if (s == CHAR || s == DOTS || s == STIX){
                if (r == 0 || r == 8) tileValues[i] = .8;
                else tileValues[i] = 1;
            } 
            else thrErr("Player:analyzeHand: attempting to play bonus tile");
        }

        for (int i = 0; i < 5; i++){
            for (int j = 0; j < 9; j++){
                // scan for completed sets, increase value of those tiles
                // scan for partial sets (2 tiles), increase value of those tiles
                // find missing tile of a partial set, scan the table's pile, tweak values of those partial sets
            }
        }

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
        // cout << "⬩";
        }
        cout << endl;
    }
};

void printTable(bool gameFinished, Player player[], Table table, int turn){
    printf("□ Remaining Wall Tiles: x%d %s\n", table.remainingTiles(), BackOfTile.c_str());
    printf("==========================================\n");
    printf("□ Permanently Discarded Tiles:");
    table.printPile();
    printf("==========================================\n");

    for (int i = 0; i < 4; i++){
        (i == turn) ? cout << "» " : cout << "  ";

        printf("%-12s", player[i].getName().c_str());
        player[i].printTiles(gameFinished);

        if (i != 3) printf("------------------------------------------\n");
    }
    printf("==========================================\n");
}

string translate(int action){
    if (action == CHOW) return "CHOWs the tile!\n";
    if (action == PONG) return "PONGs the tile!\n";
    if (action == KONG) return "KONGs the tile!\n";
    if (action == EYES) return "take the tile as EYES!\n";
    thrErr ("translate: impossible steal action");
    return "";
}

void endGame(Player player[], Table table, int turn, bool winner){
    printTable(true, player, table, turn);

    if (winner) cout << "□ " << player[turn].getName() << " wins!\n";
    else cout << "□ No more tiles! The game ends with a tie.\n";

    exit(0);
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

            // player who kongs needs to draw another tile
            if (action[taker] == KONG) while (player[turn].draw(table.giveTile()));

            cout << "□ " << player[taker].getName() << " " << translate(action[taker]);
        }
        else {
            table.takeDiscardedTile(droppedTile);
            if (table.remainingTiles() == 0) endGame(player, table, turn, false);

            turn = (turn + 1) % 4;
            while (player[turn].draw(table.giveTile()));
            cout << "□ " << player[turn].getName() << " draws a tile.\n";
        }

        // if (checkWin(player[turn])) endGame(player, table, turn, true);
    }
}

int main(){
    srand(time(0));
    
    Player p0(true, "YOU");
    Player p1(false, "opponent1");
    Player p2(false, "opponent2");
    Player p3(false, "opponent3");

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