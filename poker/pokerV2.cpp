#include <iostream>
#include <cmath>
#include <ctime>
#include <vector>
#include <chrono>
#include <thread>
using namespace std;

string SUITS[] = {"♣", "♦", "♥", "♠"};
string RANKS[] = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

#define NONE -1
#define FOLD 0
#define CALL 1
#define CHECK 2
#define RAISE 3
#define ALLIN 4

typedef string communityCardGraphics[5][7];
communityCardGraphics
    show = {{"┏", "━", "━", "━", "━", "━", "┓"},
            {"┃", " ", " ", " ", " ", " ", "┃"},
            {"┃", " ", " ", " ", " ", " ", "┃"},
            {"┃", " ", " ", " ", " ", " ", "┃"},
            {"┗", "━", "━", "━", "━", "━", "┛"}},
    hide = {{"┌", "┄", "┄", "┄", "┄", "┄", "┐"},
            {"┊", " ", " ", " ", " ", " ", "┊"},
            {"┊", " ", " ", " ", " ", " ", "┊"},
            {"┊", " ", " ", " ", " ", " ", "┊"},
            {"└", "┄", "┄", "┄", "┄", "┄", "┘"}};

struct Card {
    int suit;
    int rank;
    bool show;
};

void thrErr(string s){
    cout << "<!> " << s << endl;
    exit(1);
}

class Table {
    private:
    static vector<Card> deck;
    static vector<Card> communityCards;

    public:
    Table(){
        resetGame();
    }

    vector<Card> getCommunityCards(){ return communityCards; }

    void resetGame(){
        communityCards.clear();
        deck.clear();
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 13; j++){
                deck.push_back({i, j, false});
            }
        }
    }

    Card getCard(){
        int index = rand() % deck.size();
        Card c = deck[index];
        deck.erase(deck.begin() + index);
        return c;
    }

    void drawCommunityCard(){
        communityCards.push_back(getCard());
        if (communityCards.size() > 5) thrErr("Table:drawCommunityCard: attempted to insert more than 6 community cards");
    }
};

class Player {
    private:
    bool playable;
    string name;
    int chips;
    int bet;
    Card hand[0];

    public:
    Player(bool a, string b, int c):
        playable(a), name(b), chips(c){}
    
    void recieveCard(Card c, Card d){
        hand[0] = c;
        hand[1] = d;
    }

    int blindBet(int currentBet){
        int betSize;
        if (playable){
            do {
                cout << "► Make an blind bet: ";
                cin >> betSize;
            } while (betSize <= currentBet || betSize >= chips);
        }
        else {
            betSize = currentBet + 1;
        }
        bet = betSize;
        chips -= bet;

        return betSize;
    }

    int makeDecision(int& currentBet){
        int decision;
        if (playable){
            do {
                cout << "► Make decision [0:Fold][1:Call][2:Check][3:Raise][4:AllIn]: ";
                cin >> decision;
            } while (decision < 0 || decision > 4);

            if (decision == RAISE){
                int raiseBy;
                do {
                    cout << "► Raise by: ";
                    cin >> raiseBy;
                } while (raiseBy < 0 || raiseBy > chips);

                chips -= raiseBy;
                currentBet += raiseBy;
            }
            else if (decision == ALLIN){
                chips = 0;
                currentBet += chips;
            }
        }
        else {
            decision = CALL;
        }

        return decision;
    }
};

void bettingCycle(vector<Player>& players, int turn, int& betSize){
    int size = players.size();
    int action[size];
    for (int a: action) a = NONE;
    bool settled = 0;

    while (settled < size){
        for (int i = 0; i < size; i++){
            turn = (turn + 1) % size;
            // dont ask for decision if they've folded, called, or raised already
            if (action[turn] == FOLD || action[turn] == CALL || action[turn] == RAISE) continue;

            // betSize will be pass by reference for when players raise
            int decision = players[turn].makeDecision(betSize); 

            if (decision == RAISE){
                for (int a: action){
                    if (a != FOLD) a = NONE;
                }
                settled = 1;
            }
            else if (decision == CHECK);
            else {
                settled++;
            }
        }
        for (int a: action){
            if (a == CHECK) a = NONE;
        }
    }
}

void playGame(vector<Player>& players, Table table, int dealer, int betSize){
    int size = players.size();
    int turn = dealer;

    // initiate blind bets
    betSize = players[turn = (turn + 1) % size].blindBet(betSize);
    betSize = players[turn = (turn + 1) % size].blindBet(betSize);

    // give all players 2 cards
    for (Player p: players) p.recieveCard(table.getCard(), table.getCard());
    bettingCycle(players, turn, betSize);

    // the flop
    table.drawCommunityCard();
    table.drawCommunityCard();
    table.drawCommunityCard();
    bettingCycle(players, turn, betSize);

    table.drawCommunityCard();
    bettingCycle(players, turn, betSize);

    // the river
    table.drawCommunityCard();
    bettingCycle(players, turn, betSize);

    // calculate
}

int main(){
    srand(time(0));

    Player p0(true, "YOU", 100);
    Player p1(false, "opp1", 100);
    Player p2(false, "opp2", 100);
    Player p3(false, "opp3", 100);

    vector<Player> players = {p0, p1, p2, p3};
    Table table;

    int dealer = rand() % 4;
    int initBetSize = 4;

    while (true){
        playGame(players, table, dealer, initBetSize);
        dealer = (dealer + 1) % players.size();
        initBetSize *= 1.5;
    }

    return 0;
}
