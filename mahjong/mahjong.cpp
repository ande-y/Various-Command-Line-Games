#include <iostream>
#include <vector>
#include <ctime>
using namespace std;

#define CHAR 1
#define DOTS 2
#define STIX 3
#define WIND 4
#define DRAGON 5
#define FLOWER 6
#define SEASON 7

#define NONE 11
#define CHOW 22
#define PONG 33
#define KONG 44

const string BackOfTile = "🀫";
const string tileGraphics[7][9] = 
    {{"🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏"},
     {"🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡"},
     {"🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘"},
     {"🀀", "🀁", "🀂", "🀃", "X" , "X" , "X" , "X" , "X" },
     {"🀆", "🀅", "🀄", "X" , "X" , "X" , "X" , "X" , "X" },
     {"🀢", "🀣", "🀤", "🀥", "X" , "X" , "X" , "X" , "X" },
     {"🀦", "🀧", "🀨", "🀩", "X" , "X" , "X" , "X" , "X" }};

struct Tile {
    int suit;
    int rank;
};

class Table {
    private:
    vector<Tile> wall;
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

    void discardTile(Tile t){
        discards[t.suit][t.rank]++;
    }

};

class Player {
    private:
    vector<Tile> hand;

    public:
    void draw(Tile t){
        hand.push_back(t);
    }

    Tile drop(){
                                            // !!!
    }

    int decide(Tile droppedTile){
                                            // !!!
    }
};

void playGame(Player player[], Table table, int dealer){
    int turn = dealer;
    
    while (true){
        Tile droppedTile = player[turn].drop();

        int action[4] = {NONE, NONE, NONE, NONE};
        for (int i = 0; i < 4; i++){
            if (i == turn) continue;
            action[i] = player[i].decide(droppedTile);
        }

        int taker = turn, largest = NONE;
        for (int i = 0; i < 4; i++){
            if (largest < action[i]){
                largest = action[i];
                taker = i;
            }
        }

        if (taker != turn){
            player[taker].draw(droppedTile);
            turn = taker;
        }
        else turn = (turn + 1) % 4;
    }
}

int main(){
    Player p0, p1, p2, p3;
    Player player[4] = {p0, p1, p2, p3};
    Table table;

    for (int i = 0; i < 4; i++){
        for (int j = 0; j < 13; j++){
            player[i].draw(table.giveTile());
        }
    }
    srand(time(0));
    int dealer = rand() % 4;
    player[dealer].draw(table.giveTile());

    playGame(player, table, dealer);

    return 0;
}