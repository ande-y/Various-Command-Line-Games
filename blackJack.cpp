#include <iostream>
#include <vector>
#include <ctime>
using namespace std;

string suits[] = {"♣", "♦", "♥", "♠"};
string ranks[] = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

typedef string cardGraphics[5][7]; 
cardGraphics hide = 
   {{"┏", "━", "━", "━", "━", "━", "┓"},
    {"┃", "░", "░", "░", "░", "░", "┃"},
    {"┃", "░", "░", "░", "░", "░", "┃"},
    {"┃", "░", "░", "░", "░", "░", "┃"},
    {"┗", "━", "━", "━", "━", "━", "┛"}};
cardGraphics show = 
   {{"┏", "━", "━", "━", "━", "━", "┓"},
    {"┃", " ", " ", " ", " ", " ", "┃"},
    {"┃", " ", " ", " ", " ", " ", "┃"},
    {"┃", " ", " ", " ", " ", " ", "┃"},
    {"┗", "━", "━", "━", "━", "━", "┛"}};

#define HIT 1
#define STAND 2
#define DOUBL 3
#define SRNDR 4

struct Card {
    int suit;
    int rank;
    bool faceUp;
};

class Dealer {
    private:
    vector<Card> deck;

    public:
    Dealer(){
        resetGame();
    }

    void resetGame(){
        deck.clear();
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 13; j++){
                deck.push_back({i, j, false});
            }
        }
    }

    Card hit(){
        int index = rand() % deck.size();
        Card c = deck[index];
        deck.erase(deck.begin() + index);
        return c;
    }
};

class Player {
    private:
    bool playable;
    string name;
    int chips;
    vector<Card> hand;

    public:
    Player(bool p,string n, int c){
        playable = p;
        name = n;
        chips = c;
    }

    void setChips(int change){
        chips += change;
    }

};

void playGame(Player players[], Dealer dealer){
    
}


int main(){
    srand(time(0));

    Player p0(true, "YOU", 100);
    Player p1(false, "opp1", 100);
    Player p2(false, "opp2", 100);
    Player p3(false, "opp3", 100);

    Player players[] = {p0, p1, p2, p3};
    Dealer dealer;

    playGame(players, dealer);

    return 0;
}

// ┏━━━┏━━━━━┓ 
// ┃2  ┃2    ┃ 
// ┃  ♣┃  ♣  ┃  
// ┃   ┃    2┃ 
// ┗━━━┗━━━━━┛ 
