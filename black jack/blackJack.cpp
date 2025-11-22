#include <iostream>
#include <vector>
#include <ctime>
#include <chrono>
#include <thread>
using namespace std;

string SUITS[] = {"♣", "♦", "♥", "♠"};
string RANKS[] = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

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

#define NONE -1
#define BUST 0
#define HIT 1
#define STAND 2
#define SRNDR 3
#define DOUBL 4

void thrErr(string s){
    cout << "\n<!> " << s << endl;
    exit(0);
}

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

    vector<Card> getHand(){ return hand; }

    void resetGame(){
        deck.clear();
        hand.clear();
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

    void revealCards(){
        hand[0].faceUp = true;
    }

    void hit(Card c){
        if (hand.size() > 0) c.faceUp = true;
// c.faceUp = true;        /// DEBUG ALWAYS SHOW CARDS
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
    Player(bool a,string b, int c):
        playable(a), name(b), chips(c){}

    bool getPlayable(){ return playable; }
    string getName(){ return name; }
    int getBet(){ return bet; }
    int getChips(){ return chips; }
    vector<Card> getHand(){ return hand; }
    void addChips(int change){ chips += change; }

    void reset(){
        bet = 0;
        hand.clear();
    }

    void doubleBet(){
        if (chips - bet < 1){
            bet += chips;
            chips = 0;
        }
        else {
            chips -= bet;
            bet *= 2;
        }
    }

    int setBet(){
        int amount;
        if (playable){
            do {
                cout << "Make your bet: ";
                cin >> amount;
            } while (amount < 1 || amount > chips);
            bet = amount;
            chips -= amount;
        }
        else {
            amount = 10;     // PLACEHOLDER
            bet = amount;
            chips -= amount;
        }
        return amount;
    }

    void hit(Card c){
        if (playable) c.faceUp = true;
// c.faceUp = true;        /// DEBUG ALWAYS SHOW CARDS
        hand.push_back(c);
    }

    void revealCards(){
        for (Card& c : hand) c.faceUp = true;
    }

    int think(int dealerValue, bool firstTurn){
        if (playable){
            int choice;
            if (firstTurn){
                do {
                    cout << "► Pick action [1|Hit][2|Stand][3|surrender][4|double down]: ";
                    cin >> choice;
                } while(choice < 1 || choice > 4);
            }
            else {
                do {
                    cout << "► Pick action [1|Hit][2|Stand][3|surrender]: ";
                    cin >> choice;
                } while(choice < 1 || choice > 3);
            }
            return choice;            
        }

        cout << dealerValue << "!!!!!!!!";
        int myValue = getValue();
        if (myValue == 21) return STAND;
        if (dealerValue > myValue) return HIT;
        if (myValue < 12 && firstTurn) return DOUBL;
        if (dealerValue < 7 || myValue > 15) return STAND;
        return HIT; 
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

void printTable(vector<Player> players, Dealer dealer, int size, int action[], int dealerAction){
    system("cls");

    cout << "DEALER ";
    if (dealerAction == HIT) cout << "► HIT";
    else if (dealerAction == STAND) cout << "► STAND";
    else if (dealerAction == BUST) cout << "► BUST";
    cout << "\n";

    // print dealer's cards
    vector<Card> hand = dealer.getHand();
    for (int r = 0; r < 5; r++){
        for (int i = 0; i < hand.size(); i++){
            for (int c = 0; c < 4; c++){
                // if card is face down, no need to print symbols
                if (!hand[i].faceUp) cout << hide[r][c];
                // print rank at to left of card, if it's 10, leave an extra space 
                else if (r == 1 && c == 1) cout << RANKS[hand[i].rank];
                else if (hand[i].rank == 8 && r == 1 && c == 2);
                // print suit at the middle of the card
                else if (r == 2 && c == 3) cout << SUITS[hand[i].suit];
                // print anything else
                else cout << show[r][c];
            }
        }
        Card last = hand.back();
        for (int c = 4; c < 7; c++){
            if (!last.faceUp) cout << hide[r][c];
            else if (r == 3 && c == 5) cout << RANKS[last.rank];
            else if (last.rank == 8 && r == 3 && c == 4);
            else cout << show[r][c];
        }
        cout << endl;
    }

    // print players' action
    for (int i = 0; i < size; i++){
        string msg = "";
        if (action[i] == BUST) msg = "▼ Bust";
        else if (action[i] == HIT) msg = "▼ Hit";
        else if (action[i] == STAND) msg = "▼ Stand";
        else if (action[i] == SRNDR) msg = "▼ Surrender";
        else if (action[i] == DOUBL) msg = "▼ Double Down";

        int handSize = players[i].getHand().size();
        int buffer = (4 * handSize) + 8 - msg.length();

        cout << msg;
        for (int j = 0; j < buffer; j++) cout << ' ';
    }
    cout << endl;

    // print players' names
    for (int i = 0; i < size; i++){
        int handSize = players[i].getHand().size();
        int buffer = (4 * handSize) + 3 - players[i].getName().length();

        cout << players[i].getName() << "   ";
        for (int j = 0; j < buffer; j++) cout << ' ';
    }
    cout << endl;

    // print players' chips
    for (int i = 0; i < size; i++){
        int handSize = players[i].getHand().size();
        int buffer = (4 * handSize) + 3 - to_string(players[i].getChips()).length();

        cout << players[i].getChips() << "   ";
        for (int j = 0; j < buffer; j++) cout << ' ';
    }
    cout << endl;

    // print players' bet
    for (int i = 0; i < size; i++){
        int handSize = players[i].getHand().size();
        int buffer = (4 * handSize) + 3 - to_string(players[i].getBet()).length();

        cout << players[i].getBet() << "   ";
        for (int j = 0; j < buffer; j++) cout << ' ';
    }
    cout << endl;

    // print players' cards
    for (int r = 0; r < 5; r++){
        for (int i = 0; i < size; i++){
            hand = players[i].getHand();
            for (int j = 0; j < hand.size(); j++){
                for (int c = 0; c < 4; c++){
                    if (!hand[j].faceUp) cout << hide[r][c];
                    else if (r == 1 && c == 1) cout << RANKS[hand[j].rank];
                    else if (hand[j].rank == 8 && r == 1 && c == 2);
                    else if (r == 2 && c == 3) cout << SUITS[hand[j].suit];
                    else cout << show[r][c];
                }
            }
            Card last = hand.back();
            for (int c = 4; c < 7; c++){
                if (!last.faceUp) cout << hide[r][c];
                else if (r == 3 && c == 5) cout << RANKS[last.rank];
                else if (last.rank == 8 && r == 3 && c == 4);
                else cout << show[r][c];
            }
            // buffer between each player's hands
            cout << "   "; 
        }
        cout << endl;
    }
}

void playGame(vector<Player>& players, Dealer dealer){
    int size = players.size();

    // everyone draws 2 cards
    for (int i = 0; i < 2; i++){
        dealer.hit(dealer.giveCard());
        for (int j = 0; j < size; j++){
            players[j].hit(dealer.giveCard());
        }
    }

    int action[size];
    for (int& a : action) a = NONE;
    printTable(players, dealer, size, action, NONE);

    int stopped = 0;
    bool firstTurn = true;
    while (stopped < size){
        for (int& a : action){
            if (a == HIT) a = NONE;
        }

        for (int i = 0; i < size; i++){
            if (action[i] != HIT && action[i] != NONE) continue;
            if (action[i] == DOUBL) return;

            action[i] = players[i].think(dealer.getValue(false), firstTurn);

            if (action[i] == HIT || action[i] == DOUBL){
                players[i].hit(dealer.giveCard());
            }

            printTable(players, dealer, size, action, NONE);
            if (action[i] != HIT) this_thread::sleep_for(std::chrono::milliseconds(1000));
            else this_thread::sleep_for(std::chrono::milliseconds(1400));
            
            if (action[i] == DOUBL){
                if (!firstTurn) thrErr ("playGame:while: attempt to double down after 1st turn");
                else players[i].doubleBet();
            }

            if (players[i].getValue() > 21) action[i] = BUST;

            if (action[i] != HIT) stopped++;
        }
        firstTurn = false;
    }
    // all player finish drawing card at this point

    for (Player& p : players) p.revealCards();
    printTable(players, dealer, size, action, NONE);
    this_thread::sleep_for(std::chrono::milliseconds(1400));
    
    // dealer begins drawing cards
    dealer.revealCards();
    int dealerAction = (dealer.getValue(true) >= 17) ? STAND : NONE;
    printTable(players, dealer, size, action, dealerAction);
    this_thread::sleep_for(std::chrono::milliseconds(1400));

    while (dealer.getValue(true) < 17){
        dealerAction = (dealer.getValue(true) <= 16) ? HIT : STAND;
        dealer.hit(dealer.giveCard());
        printTable(players, dealer, size, action, dealerAction);
        this_thread::sleep_for(std::chrono::milliseconds(1400));
    }

    if (dealer.getValue(true) > 21) printTable(players, dealer, size, action, BUST);

    cout << endl;
    int dealerValue = dealer.getValue(true);
    if (dealerValue == 21) cout << "► DEALER [" << dealerValue << "] HITS BLACKJACK\n";
    else if (dealerValue > 21) cout << "► DEALER [" << dealerValue << "] BUST\n";
    else cout << "► DEALER [" << dealerValue << "] STOOD\n";

    for (int i = 0; i < size; i++){
        // evaluate players who stood
        if (action[i] == STAND || action[i] == DOUBL){
            // players who win blackJack earn 3:2
            if (players[i].getValue() == 21 && dealerValue != 21) {
                players[i].addChips(players[i].getBet() * 2.5);
                printf("● %s [%d] hits blackjack & wins %d chips.", players[i].getName().c_str(), players[i].getValue(), int(players[i].getBet() * 1.5));
            }
            // players who beat dealer earn 1:1
            else if (dealerValue > 21 || dealerValue < players[i].getValue()){
                players[i].addChips(players[i].getBet() * 2);
                printf("● %s [%d] beats Dealer & wins %d chips.", players[i].getName().c_str(), players[i].getValue(), players[i].getBet());
            }
            // players who tie with dealer earn nothing
            else if (dealerValue == players[i].getValue()){
                players[i].addChips(players[i].getBet());
                printf("○ %s [%d] ties & wins nothing.", players[i].getName().c_str(), players[i].getValue());
            }
            // players who lose to dealer lose their bet
            else {
                printf("○ %s [%d] gets beat & loses %d chips.", players[i].getName().c_str(), players[i].getValue(), players[i].getBet());
            }
        }
        // surrenderers lose half their bet
        else if (action[i] == SRNDR){
            players[i].addChips(players[i].getBet() / 2);
            printf("○ %s [%d] surrendered & loses %d chips.", players[i].getName().c_str(), players[i].getValue(), players[i].getBet() / 2);
        }
        // those who bust lose their bet
        else {
            printf("○ %s [%d] bust & loses %d chips.", players[i].getName().c_str(), players[i].getValue(), players[i].getBet());
        }

        cout << " [" << players[i].getChips() << " chips now]\n";
    }
    cout << endl;
}

int main(){
    srand(time(0));

    Player p0(true, "YOU", 100);
    Player p1(false, "opp1", 100);
    Player p2(false, "opp2", 100);
    Player p3(false, "opp3", 100);

    vector<Player> players = {p0, p1, p2, p3};
    Dealer dealer;

    bool hasChips = true;
    while (hasChips){
        for (Player& p : players) p.setBet();
        playGame(players, dealer);

        hasChips = false;
        for (int i = 0; i < players.size(); i++){
            players[i].reset();
            if (players[i].getPlayable() && players[i].getChips() > 0) hasChips = true;
            if (players[i].getChips() <= 0) players.erase(players.begin() + i);
        }
        dealer.resetGame();
    }

    cout << "You lost all your money. You can't play no more.\n\n";

    return 0;
}