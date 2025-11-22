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

typedef string cardGraphics[5][7];
cardGraphics
    hideCard = {{"┏", "━", "━", "━", "━", "━", "┓"},
                {"┃", "░", "░", "░", "░", "░", "┃"},
                {"┃", "░", "░", "░", "░", "░", "┃"},
                {"┃", "░", "░", "░", "░", "░", "┃"},
                {"┗", "━", "━", "━", "━", "━", "┛"}},
    showCard = {{"┏", "━", "━", "━", "━", "━", "┓"},
                {"┃", " ", " ", " ", " ", " ", "┃"},
                {"┃", " ", " ", " ", " ", " ", "┃"},
                {"┃", " ", " ", " ", " ", " ", "┃"},
                {"┗", "━", "━", "━", "━", "━", "┛"}},
    foldCard = {{"┌", "┄", "┄", "┄", "┄", "┄", "┐"},
                {"┊", " ", " ", " ", " ", " ", "┊"},
                {"┊", " ", " ", " ", " ", " ", "┊"},
                {"┊", " ", " ", " ", " ", " ", "┊"},
                {"└", "┄", "┄", "┄", "┄", "┄", "┘"}};

struct Card {
    int suit;
    int rank;
    bool show;
};

class Table {
    private:
    vector<Card> deck;
    vector<Card> communityCards;

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

    void drawCommunityCard(Card c){
        communityCards.push_back(c);
    }
};

class Player {
    private:
    bool playable;
    string name;
    int chips;
    int bet;
    Card hand[2];

    public:
    Player(bool a, string b, int c):
        playable(a), name(b), chips(c){}
    
    string getName(){ return name; }

    void recieveCards(Card c, Card d){
        hand[0] = c;
        hand[1] = d;
    }

    int blindBet(int currentBet){
        int betSize;
        if (playable){
            do {
                cout << "□ The current bet is " << currentBet << "\n► Make an blind bet: ";
                cin >> betSize;
            } while (betSize < currentBet || betSize >= chips);
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
            } while (decision < 0 && decision > 4);
            if (decision == RAISE){
                int raiseBy;
                do {
                    cout << "► Raise by: ";
                    cin >> raiseBy;
                } while (raiseBy < 0 && raiseBy > chips);

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

void printTable(vector<Player> players, Table table){
    vector<Card> communityCards = table.getCommunityCards();
    for (int r = 0; r < 5; r++){
        for (int i = 0; i < 5; i++){
            for (int c = 0; c < 7; c++){
                if (i + 1 > communityCards.size()){
                    cout << foldCard[r][c];
                }
                else {
                    if (r == 1 && c == 1 || r == 3 && c == 5) cout << RANKS[communityCards[i].rank];
                    else if (communityCards[i].rank == 8 && (r == 1 && c == 2 || r == 3 && c == 3)); 
                    else if (r == 2 && c == 3) cout << SUITS[communityCards[i].suit];
                    else cout << showCard[r][c];
                }
            }
        }
        cout << endl;
    }
}

void bettingCycle(vector<Player>& players, Table table, int turn, int& betSize){
    int size = players.size();
    int action[size];
    for (int& a: action) a = NONE;
    int settled = 0;

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

            printTable(players, table);
        }
        for (int& a: action){
            if (a == CHECK) a = NONE;
        }
    }
}

void playGame(vector<Player>& players, Table table, int dealer, int initialBet){
    int size = players.size();
    int currentBet = initialBet;

    // initiate blind bets
    int turn = (dealer + 1) % size;
    players[turn].blindBet(currentBet);
    turn = (dealer + 1) % size;
    players[turn].blindBet(currentBet);

    for (Player& p: players) p.recieveCards(table.getCard(), table.getCard());
    bettingCycle(players, table, turn, currentBet);

    // the flop
    table.drawCommunityCard(table.getCard());
    table.drawCommunityCard(table.getCard());
    table.drawCommunityCard(table.getCard());
    bettingCycle(players, table, turn, currentBet);

    table.drawCommunityCard(table.getCard());
    bettingCycle(players, table, turn, currentBet);

    // the river
    table.drawCommunityCard(table.getCard());
    bettingCycle(players, table, turn, currentBet);

    // calculate
    cout << "completed\n";
}

int main(){
    srand(time(0));

    Player p0(true, "YOU", 100);
    Player p1(false, "opp1", 100);
    Player p2(false, "opp2", 100);
    Player p3(false, "opp3", 100);


    vector<Player> players = {p0, p1, p2, p3};
    Table table;

    int dealer = rand() % players.size(); 
    int initialBet = 4;

    playGame(players, table, dealer, initialBet);

    return 0;
}
