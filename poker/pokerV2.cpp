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

#define ROYALFLUSH 100000
#define STRAIGHTFLUSH 90000
#define FOUROFKIND 80000
#define FULLHOUSE 70000
#define FLUSH 60000
#define STRAIGHT 50000
#define THREEOFKIND 40000
#define TWOPAIR 30000
#define SINGLEPAIR 20000
#define HIGHCARD 10000

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
    int bet = 0;
    Card hand[2];

    public:
    Player(bool a, string b, int c):
        playable(a), name(b), chips(c){}
    
    bool getPlayable(){ return playable; }
    string getName(){ return name; }
    int getChips(){ return chips; }
    Card getHand(int i){ return hand[i]; }
    int getBet(){ return bet; }

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
                cout << "► Make decision\n  [0:Fold][1:Call][2:Check][3:Raise][4:AllIn]: ";
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
                bet = currentBet;
            }
        }
        else {
            decision = CALL;
        }

        if (decision == ALLIN){
            chips = 0;
            currentBet += chips;
        }
        else if (decision == CALL){
            chips -= currentBet - bet;
            bet = currentBet;
        }

        return decision;
    }

    int calculateHandValue(vector<Card> communityCards){
        vector<Card> allAvailableCards = communityCards;
        allAvailableCards.push_back(hand[0]); 
        allAvailableCards.push_back(hand[1]);

        // sort all cards by rank largest to smallest 
        int cardsNum = allAvailableCards.size();
        for (int i = 0; i < cardsNum; i++){
            for (int j = 0; j < cardsNum - 1; j++){
                if (allAvailableCards[j].rank < allAvailableCards[j + 1].rank) swap(allAvailableCards[j], allAvailableCards[j + 1]);
            }
        }

        // store data on suits, ranks, flush, & straights
        int reccuringSuits[4] = {0, 0, 0, 0};
        int reccuringRanks[13] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (Card c: allAvailableCards){
            reccuringSuits[c.suit]++;
            reccuringRanks[c.rank]++;
        }
        int suitOfFlush = NONE;
        for (int i = 0; i < 4; i++) if (reccuringSuits[i] >= 5) suitOfFlush = i;

        int inSequence = 0;
        vector<int> largestOfStraight;
        for (int i = 12; i >= 0; i--){
            (reccuringRanks[i] > 0) ? inSequence++ : inSequence = 0;
            if (inSequence >= 5) largestOfStraight.push_back(i + 4);
        }
        
        vector<Card> subset;

        // check for royal flush & straight flush
        if (suitOfFlush != NONE && !largestOfStraight.empty()){
            for (int desiredRank: largestOfStraight){
                for (Card c: allAvailableCards){
                    if (c.suit == suitOfFlush && c.rank == desiredRank){
                        subset.push_back(c);
                        desiredRank--;
                    }
                }
                if (subset.size() >= 5){
                    if (subset[0].rank == 12) return ROYALFLUSH;
                    else return STRAIGHTFLUSH + subset[0].rank;
                }
            }
            subset.clear();
        }

        // check for 4 of a kind
        for (int i = 12; i >= 0; i--){
            if (reccuringRanks[i] == 4) return FOUROFKIND + i;
        }

        // check for full house
        int tripl = NONE, doubl = NONE;
        for (int i = 12; i >= 0; i--){
            if (reccuringRanks[i] == 3){
                if (tripl == NONE) tripl = i;
                else doubl = i;
            }
            else if (reccuringRanks[i] == 2 && doubl == NONE) doubl = i;
        }
        if (tripl != NONE && doubl != NONE) return FULLHOUSE + tripl * 100 + doubl;

        // check for flush
        if (suitOfFlush != NONE) return FLUSH + suitOfFlush;

        // check for straight
        if (!largestOfStraight.empty()) return STRAIGHT + largestOfStraight[0];
        
        // check for 3 of a kind
        if (tripl != NONE) return THREEOFKIND + tripl;

        // check for double pair
        doubl = NONE;
        int doubl2 = NONE;
        for (int i = 12; i >= 0; i--){
            if (reccuringRanks[i] == 2){
                if (doubl == NONE) doubl = i;
                else doubl2 = i;
            }
        }
        if (doubl != NONE && doubl2 != NONE) return TWOPAIR + doubl * 100 + doubl2;

        // check for single pair
        if (doubl != NONE) return SINGLEPAIR + doubl;

        // return highcard
        if (hand[0].rank > hand[1].rank) return HIGHCARD + hand[0].rank;
        return HIGHCARD + hand[1].rank;
    }
};

void translate(int i){
        if (i >= ROYALFLUSH) cout << "Royal Flush [A";
        else if (i >= STRAIGHTFLUSH) cout << "Straight Flush [" << RANKS[i % 10000];
        else if (i >= FOUROFKIND) cout << "Four of a Kind [" << RANKS[i % 10000];
        else if (i >= FULLHOUSE) cout << "Full House [" << RANKS[(i % 10000) / 100] << "][" << RANKS[i % 100];
        else if (i >= FLUSH) cout << "Flush [" << SUITS[i % 10000];
        else if (i >= STRAIGHT) cout << "Straight [" << RANKS[i % 10000];
        else if (i >= THREEOFKIND) cout << "Three of a Kind [" << RANKS[i % 10000];
        else if (i >= TWOPAIR) cout << "Double Pair [" << RANKS[(i % 10000) / 100] << "][" << RANKS[i % 100];
        else if (i >= SINGLEPAIR) cout << "Single Pair [" << RANKS[i % 10000];
        else cout << "High Card [" << RANKS[i % 10000];
        cout << "]\n";
}

void printTable(vector<Player> players, vector<Card> communityCards, int betSize, vector<int> action, int turn){
    system("cls");
    
    // print community cards
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
    
    int size = players.size();

    // print players' action
    for (int i = 0; i < size; i++){
        string msg = "";
        
        if (turn == i){
            switch (action[i]){
            case FOLD: msg = "▼ FOLD"; break;
            case CALL: msg = "▼ CALL"; break;
            case CHECK: msg = "▼ CHECK"; break;
            case RAISE: msg = "▼ RAISE"; break;
            case ALLIN: msg = "▼ ALL IN"; break;
            }
        }
        else {
            switch (action[i]){
            case FOLD: msg = "▼ folded"; break;
            case CALL: msg = "▼ called"; break;
            case CHECK: msg = "▼ checks"; break;
            case RAISE: msg = "▼ raised"; break;
            case ALLIN: msg = "▼ is all in"; break;
            }
        }

        // no clue why the msg lengths above are 2 greater there are
        if (action[i] == NONE) printf("%-13s", msg.c_str());
        else printf("%-15s", msg.c_str());
    }
    cout << endl;

    // print players' names
    for (Player p: players) printf("%-13s", p.getName().c_str());
    cout << endl;

    // print players' chips & bets
    for (Player p: players){
        string msg = to_string(p.getChips()) + " - " + to_string(p.getBet());
        printf("%-13s", msg.c_str());
    }
    cout << endl;

    // print players' cards
    for (int r = 0; r < 5; r++){
        for (int i = 0; i < size; i++){
            for (int j = 0; j < 2; j++){
                Card card = players[i].getHand(j);
                for (int c = 0; c < 4; c++){
                    // if (!card.show) cout << hideCard[r][c];
                    if (r == 1 && c == 1) cout << RANKS[card.rank];
                    else if (card.rank == 8 && r == 1 && c == 2);
                    else if (r == 2 && c == 3) cout << SUITS[card.suit];
                    else cout << showCard[r][c];
                }
            }

            Card last = players[i].getHand(1);
            for (int c = 4; c < 7; c++){
                // if (!last.show) cout << hideCard[r][c];
                if (r == 3 && c == 5) cout << RANKS[last.rank];
                else if (last.rank == 8 && r == 3 && c == 4);
                else cout << showCard[r][c];
            }
            cout << "  ";
        }
        cout << endl;
    }

    int sum = 0;
    for (Player p: players) sum += p.getBet();
    printf("□ The current bet size is %d\n□ The pot is worth %d\n", betSize, sum);
}

void bettingCycle(vector<Player>& players, Table table, vector<int>& action, int turn, int& betSize){
    int size = players.size();
    int settled = 0;

    for (int a: action){
        if (a == FOLD) settled++;
    }

    while (settled < size){
        for (int i = 0; i < size; i++){
            turn = (turn + 1) % size;
            // ask again for those who check
            if (action[turn] != NONE) continue;

            // betSize will be pass by reference for when players raise
            int decision = players[turn].makeDecision(betSize); 
            action[turn] = decision;

            // raising will cause everyone who's still playing to decide again
            if (decision == RAISE){
                settled = 1;
                for (int& a: action){
                    if (a != FOLD) a = NONE;
                    else settled++;
                }
            }
            // will loop back for people who check
            else if (decision == CHECK);
            // call or fold
            else settled++;

            printTable(players, table.getCommunityCards(), betSize, action, turn);
            // this_thread::sleep_for(std::chrono::milliseconds(1400));
        }
        for (int& a: action){
            if (a == CHECK) a = NONE;
        }
    }
    // folding is permanant, no more actions for the rest of the round
    for (int& a: action){
        if (a != FOLD) a = NONE;
    }
}

void playRound(vector<Player>& players, Table table, int dealer, int initialBet){
    int size = players.size();
    int currentBet = initialBet;

    // initiate blind bets
    int turn = (dealer + 1) % size;
    currentBet = players[turn].blindBet(currentBet);
    turn = (turn + 1) % size;
    currentBet = players[turn].blindBet(currentBet);

    vector<int> action;
    for (int i = 0; i < size; i++) action.push_back(NONE);

    for (Player& p: players) p.recieveCards(table.getCard(), table.getCard());
    bettingCycle(players, table, action, turn, currentBet);

    // the flop
    table.drawCommunityCard(table.getCard());
    table.drawCommunityCard(table.getCard());
    table.drawCommunityCard(table.getCard());
    bettingCycle(players, table, action, turn, currentBet);

    table.drawCommunityCard(table.getCard());
    bettingCycle(players, table, action, turn, currentBet);

    // the river
    table.drawCommunityCard(table.getCard());
    bettingCycle(players, table, action, turn, currentBet);

    // calculate each player's hand combination
    vector<int> handValues;
    vector<Card> communityCards = table.getCommunityCards();
    for (Player p: players) handValues.push_back(p.calculateHandValue(communityCards));

    // determine the best hand(s)
    vector<int> winner;
    int bestHand = 0;
    for (int i = 0; i < size; i++){
        if (handValues[i] > bestHand){
            winner.clear();
            bestHand = handValues[i];
            winner.push_back(i);
        }
        else if (handValues[i] == bestHand){
            winner.push_back(i);
        }
    }

    // print the winner(s)
    if (winner.size() > 1){
        cout << "□ ";
        for (int i = 0; i < winner.size(); i++){
            if (i != 0) cout << ", ";
            if (i + 1 == winner.size()) cout << "& ";
            cout << players[winner[i]].getName();
        }
        cout << " win with ";
    }
    else cout << "□ " << players[winner[0]].getName() << " wins with a ";
    translate(handValues[winner[0]]);

    // list out what combinations other players got
    cout << endl;
    for (int i = 0; i < size; i++){
        cout << players[i].getName() << " - ";
        translate(handValues[i]);
    }
    cout << endl;
}

int main(){
    srand(time(0));

    Player p0(true, "YOU", 100);
    Player p1(false, "opp1", 100);
    Player p2(false, "opp2", 100);
    Player p3(false, "opp3", 100);
    Player p4(false, "opp4", 100);

    vector<Player> players = {p0, p1, p2, p3, p4};
    Table table;

    int dealer = rand() % players.size(); 
    int initialBet = 4;

    playRound(players, table, dealer, initialBet);

    return 0;
}
