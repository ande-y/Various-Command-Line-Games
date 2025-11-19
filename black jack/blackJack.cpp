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

#define HIT 0
#define BUST 1
#define STAND 2
#define DOUBL 3
#define DOUBLED 4
#define SRNDR 5

struct Card {
    int suit;
    int rank;
    bool faceUp;
};

class Dealer {
    private:
    vector<Card> deck;
    vector<Card> hand;

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

    Card giveCard(){
        int index = rand() % deck.size();
        Card c = deck[index];
        deck.erase(deck.begin() + index);
        return c;
    }

    void hit(Card c){
        if (hand.size() > 1) c.faceUp = true;
        hand.push_back(c);
    }

    int getValue(bool seeAll){
        int sum = 0;
        for (int i = 0; i < hand.size(); i++){
            if (!seeAll && !hand[i].faceUp) continue;

            int value = hand[i].rank + 2;
            if (value == 14){
                if (sum + 11 > 21) value = 1;
                else value = 11;
            }
            else if (value > 10) value = 10;

            sum += value;
        }
        return sum;
    }

};

class Player {
    private:
    bool playable;
    string name;
    int bet;
    int chips;
    vector<Card> hand;

    public:
    Player(bool p,string n, int c){
        playable = p;
        name = n;
        chips = c;
    }

    int getBet(){ return bet; }
    void doubleBet(){ bet *= 2; }
    void addChips(int change){ chips += change; }

    void hit(Card c){
        if (playable) c.faceUp = true;
        hand.push_back(c);
    }

    int think(int dealerValue){
        return HIT;     // PLACEHOLDER
    }

    int getValue(){
        int sum = 0;
        for (int i = 0; i < hand.size(); i++){
            int value = hand[i].rank + 2;
            if (value == 14){
                if (sum + 11 > 21) value = 1;
                else value = 11;
            }
            else if (value > 10) value = 10;

            sum += value;
        }
        return sum;
    }

};

void playGame(Player players[], Dealer dealer){
    int size =  sizeof(players) / sizeof(players[0]);

    // everyone draws 2 cards
    for (int i = 0; i < 2; i++){
        dealer.hit(dealer.giveCard());
        for (int j = 0; j < size; j++){
            players[i].hit(dealer.giveCard());
        }
    }

    int action[size];
    for (int a : action) a = HIT;

    int stopped = 0;
    while (stopped < size){
        for (int i = 0; i < size; i++){
            if (action[i] != HIT) continue;

            action[i] = players[i].think(dealer.getValue(false));

            if (action[i] == HIT || action[i] == DOUBL) players[i].hit(dealer.giveCard());
            if (action[i] == DOUBL){
                players[i].doubleBet();
                action[i] = STAND;
            }

            if (action != HIT) stopped++;
        }
    }
    
    while (dealer.getValue(true) < 17){
        dealer.hit(dealer.giveCard());
    }

    int dealerValue = dealer.getValue(true);
    for (int i = 0; i < size; i++){
        if (action[i] == STAND){
            if (dealerValue < players[i].getValue()) players[i].addChips(players[i].getBet() * 2);
            else if (dealerValue == players[i].getValue()) players[i].addChips(players[i].getBet());
            else players[i].addChips(players[i].getBet() * -1);
        }
        else if (action[i] == SRNDR){
            players[i].addChips(players[i].getBet() / 2);
        }
        else players[i].addChips(players[i].getBet() * -1);
    }
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
