#include <iostream>
#include <cmath>
#include <ctime>
#include <vector>
#include <chrono>
#include <thread>
using namespace std;

string suits[] = {"♣", "♦", "♥", "♠"};
string ranks[] = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};

typedef string cardsInHand[7][12];
typedef string cardsOnTable[5][8];
cardsInHand
    showHand = {{" ", " ", " ", " ", "┏", "━", "━", "━", "━", "━", "┓", "  "},
                {" ", " ", " ", " ", "┃", " ", " ", " ", " ", " ", "┃", "  "},
                {"┏", "━", "━", "━", "┫", " ", " ", " ", " ", " ", "┃", "  "},
                {"┃", " ", " ", " ", "┃", " ", " ", " ", " ", " ", "┃", "  "},
                {"┃", " ", " ", " ", "┗", "━", "┳", "━", "━", "━", "┛", "  "},
                {"┃", " ", " ", " ", " ", " ", "┃", " ", " ", " ", " ", "  "},
                {"┗", "━", "━", "━", "━", "━", "┛", " ", " ", " ", " ", "  "}},
    playHand = {{" ", " ", " ", " ", "┏", "━", "━", "━", "━", "━", "┓", "  "},
                {" ", " ", " ", " ", "┃", "░", "░", "░", "░", "░", "┃", "  "},
                {"┏", "━", "━", "━", "┫", "░", "░", "░", "░", "░", "┃", "  "},
                {"┃", "░", "░", "░", "┃", "░", "░", "░", "░", "░", "┃", "  "},
                {"┃", "░", "░", "░", "┗", "━", "┳", "━", "━", "━", "┛", "  "},
                {"┃", "░", "░", "░", "░", "░", "┃", " ", " ", " ", " ", "  "},
                {"┗", "━", "━", "━", "━", "━", "┛", " ", " ", " ", " ", "  "}},
    foldHand = {{" ", " ", " ", " ", "┌", "┄", "┄", "┄", "┄", "┄", "┐", "  "},
                {" ", " ", " ", " ", "┊", " ", " ", " ", " ", " ", "┊", "  "},
                {"┌", "┄", "┄", "┄", "┊", " ", " ", " ", " ", " ", "┊", "  "},
                {"┊", " ", " ", " ", "┊", " ", " ", " ", " ", " ", "┊", "  "},
                {"┊", " ", " ", " ", "└", "┄", "┄", "┄", "┄", "┄", "┘", "  "},
                {"┊", " ", " ", " ", " ", " ", "┊", " ", " ", " ", " ", "  "},
                {"└", "┄", "┄", "┄", "┄", "┄", "┘", " ", " ", " ", " ", "  "}};
cardsOnTable
    show = {{"┏", "━", "━", "━", "━", "━", "┓", "  "},
            {"┃", " ", " ", " ", " ", " ", "┃", "  "},
            {"┃", " ", " ", " ", " ", " ", "┃", "  "},
            {"┃", " ", " ", " ", " ", " ", "┃", "  "},
            {"┗", "━", "━", "━", "━", "━", "┛", "  "}},
    hide = {{"┌", "┄", "┄", "┄", "┄", "┄", "┐", "  "},
            {"┊", " ", " ", " ", " ", " ", "┊", "  "},
            {"┊", " ", " ", " ", " ", " ", "┊", "  "},
            {"┊", " ", " ", " ", " ", " ", "┊", "  "},
            {"└", "┄", "┄", "┄", "┄", "┄", "┘", "  "}};

struct Card{
    int rank;
    int suit;
};
bool drawnCards[4][13];
vector<Card> commCards;
cardsOnTable *commVis[5];
//cardsInHand *hands[num];          // located in playRound due to dependency on amount of players

struct behavior{
    double confid;                  // based on their cards & combinations
    double caution;                 // based on their chips & bet size
    double recklsns;                // personal spuratic decision making
};

class Player {
    private:
    string name;
    behavior behav;
    Card card[2];
    double handRank[2];             // value of final comparison
    double startMoney;
    int chips;
    int bet;
    bool allIn;
    bool fold;

    public:
    Player(string a, int b, behavior c)
        : name(a), startMoney(b), chips(b), behav(c){
    }

    string getName(){return name;}
    Card getCard(int x){return card[x];}
    double getCombo(int x){return handRank[x];}
    int getChips(){return chips;}
    bool getAllIn(){return allIn;}
    bool getFold(){return fold;}
    int getBet(){return bet;}

    void setCards(int x, Card y){card[x] = y;}
    void setChips(int x){chips = x;}
    void setBet(int x){bet = x;}
    void setAllIn(bool x){allIn = x;}
    void setFold(bool x){fold = x;}

    void sortCards(){
        if (card[0].rank < card[1].rank) swap(card[0], card[1]);
    }

    void blindBet(int i, int& priorBet, int index){
        if (i == 0){
            int ans;
            do {
                cout <<"► "<< name <<", the ";
                (index == 1)? cout << "small " : cout <<"big ";
                cout <<"blind, puts down ";
                cin >> ans;
            } while (ans <= priorBet);
            bet = ans;
        }
        else {
            bet = priorBet + 1;
            cout <<"┇ "<< name <<", the ";
            (index == 1)? cout << "small " : cout <<"big ";
            cout <<"blind, puts down "<< bet;
        }
        priorBet = bet;
        chips -= bet;
    }

    int playerThink(int& roundBet){
        if (fold) return 33;
        if (allIn) return 44;

        int input;
        do {
            if (roundBet < bet + chips) cout <<"► Call[1], Raise[2], Fold[3]: ";
            else cout <<"► All-In[1], Raise[2], Fold[3]: ";
            cin >> input;
        } while(input < 1 || input > 3);

        if (input == 1 && roundBet > bet + chips){
            goAllIn:
            bet += chips;
            chips = 0;
            allIn = true;
            return 4;
        }
        if (input == 1){
            chips -= roundBet - bet;
            bet = roundBet;
            return 1;
        }
        if (input == 2){
            int raise;
            do {
                cout <<"► You raise the bet by ";
                cin >> raise;
            } while (raise < 0);
            if (raise >= chips - (roundBet - bet)) goto goAllIn;
            roundBet += raise;
            chips -= roundBet - bet;
            bet = roundBet;
            return 2;
        }
        fold = true;
        return 3;
    }

    int AiThink(int action[], int& roundBet, int commSeen, int num, int noFold){                                              // AI behavior here
        if (fold) return 33;
        if (allIn) return 44;

        findBest();
        double confid = behav.confid * .5 + .25;                                                    // correct the variance
        double caution = behav.caution;
        double recklsns = behav.recklsns * .8 + .1;

        double moneyPer = (chips + bet) / startMoney;                                               // % chips player has now to initial amount 
        double risk = (roundBet / startMoney * (caution + 1)) / (.5 + moneyPer / 2);                // significance of the raised amount

        double spur = (recklsns - (rand() % 1000) / 1000.0) * (.6 + commSeen / 10);                 // spuriousness increases as community cards are drawn
        spur += (abs(1 - moneyPer) * recklsns) / 2;                                                 // spuriousness increases as money% deviates
        double spurPer = spur / recklsns;                                                           // % which rand deviates from players' recklessness

        double conform = 1 + ((num - noFold) * (1.2 - risk) / (num * 3));
        double HRVal = (handRank[0] + (.07 * handRank[1])) / 10;                                    // real value of players' hand
        double modHRVal = HRVal * (.6 + confid) * conform;                                          // how player percieves their hand
        
        double threshHold = (2 - confid) * (1 + risk * .7) * (1 + commSeen / 15.0) / 10;            // minimum hand value for player to not fold

        // printf("\n\tV:%.4f  MV:%.4f  T:%.4f  $:%.2f  R:%.3f  S:%6.3f  S%%:%.3f\t",          /// DEBUG: player behavoir determinators
        // HRVal, modHRVal, threshHold, moneyPer, risk, spur, spurPer);                        ///

        bool willFold, willAllIn, willRaise = false;

        if (modHRVal < threshHold && spurPer < .9) willFold = true;
        if (spurPer > .98) willAllIn = true;
        if (roundBet >= bet + chips){
            if (spurPer > .3) willAllIn = true;
            else willFold = true;
        }
        if (spurPer > .8) willRaise = true;
        else if (spurPer > .6 && moneyPer > (.75 + caution * .5)) willRaise = true;
        else if (spurPer > .3 && modHRVal / threshHold > (2 - confid) * threshHold) willRaise = true;

        if (noFold == 1) willFold = false;

        if (willAllIn){
            goAllIn:
            if (chips + bet > roundBet) roundBet = chips + bet;
            bet += chips;
            chips = 0;
            allIn = true;
            return 4;
        }
        if (willFold){
            fold = true;
            return 3;
        }
        if (willRaise){
            int raise = (rand() % chips / 2) * (1 - (caution * .5 + .25)) * modHRVal / (threshHold * 5) + 1;
            if (raise + bet > chips) goto goAllIn;
            roundBet += raise;
            chips -= roundBet - bet;
            bet = roundBet;
            return 2;
        }
        chips -= roundBet - bet;
        bet = roundBet;
        return 1;
    }

    void findBest(){
        vector<Card> allCards;                                                  // make copy array of table cards
        for (int i = 0; i < commCards.size(); i++){                             // add community cards to the array
            Card temp = commCards[i];
            allCards.push_back(temp);
        }
        for (int i = 0; i < 2; i++){                                            // add player's card to the array
            Card temp = card[i];
            allCards.push_back(temp);
        }
        for (int i = 0; i < allCards.size(); i++){                              // sort them, by rank high to low
            for (int j = 0; j < allCards.size() - 1; j++){
                if (allCards[j].rank < allCards[j+1].rank) swap(allCards[j], allCards[j+1]);
            }
        }
        // cout <<"● "<< name <<" ";                                   /// DEBUG: player's hand + community cards
        // for (int i = 0; i < allCards.size(); i++){                  ///
        //     cout << allCards[i].rank <<" ";                         ///
        // } cout << endl;                                             ///

        int suitRepeat[4] = {0};                                                // array of suit frequency
        for (int i = 0; i < allCards.size(); i++){
            suitRepeat[allCards[i].suit]++;
        }
        int rankRepeat[13] = {0};                                               // array of rank frequency
        for (int i = 0; i < allCards.size(); i++){
            rankRepeat[allCards[i].rank]++;
        }
        int flushSuit = -1;                                                     // detect a flush
        for (int i = 0; i < 4; i++){
            if (suitRepeat[i] >= 5) flushSuit = i;
        }
        // cout <<"● flushes: "<< flushSuit << endl;                   /// DEBUG player's possible flushes
        int inSequence = 0;                                                     // detect all straights, may be multiple
        vector<int> startOfStraight;
        for (int i = 12; i >= 0; i--){
            if (rankRepeat[i] > 0) inSequence++;
            else inSequence = 0;
            if (inSequence >= 5) startOfStraight.push_back(i + 4);
        }
        // cout <<"● start of straights: ";                            /// DEBUG: player's possible straights
        // for (int i = 0; i < startOfStraight.size(); i++){           ///
        //     cout << startOfStraight[i] <<" ";                       ///
        // } cout << endl;                                             ///

        vector<Card> copy;                                                      // for temporary combinations to confirm patterns

        if (flushSuit != -1 && !startOfStraight.empty()){                       // construct straight flush if theres flush & straight
            for (int j = 0; j < startOfStraight.size(); j++){
                int start = startOfStraight[j];
                for (int desiredRank = start; desiredRank > start - 5; desiredRank--){
                    for (int i = 0; i < allCards.size(); i++){
                        if (allCards[i].rank == desiredRank && allCards[i].suit == flushSuit){
                            if (!copy.empty()){
                                Card prev = copy.back();
                                if (prev.rank == desiredRank) continue;
                                else copy.push_back(allCards[i]);
                            }
                            else copy.push_back(allCards[i]);
                        }
                    }
                }
                if (copy.size() == 5){                                          // contruction may fail if straight & flush dont correspond
                    if (copy[0].rank == 12){                                    // if straight flush start with ace, its a royal flush
                        handRank[0] = 10;
                        handRank[1] = 0;
                        return;
                    }
                    else {                                                      // otherwise its a regular straight flush
                        handRank[0] = 9;
                        handRank[1] = copy[0].rank;
                        return;
                    }
                }
                copy.clear();
            }
        }

        for (int i = 12; i >= 0; i--){                                          // find 4 of kind
            if (rankRepeat[i] == 4){
                handRank[0] = 8;
                handRank[1] = i + 2;
                return;
            }
        }

        int tripl = -1;
        int doubl = -1;

        for (int i = 12; i >= 0; i--){                                          // find full house
            if (rankRepeat[i] == 3){
                if (tripl == -1) tripl = i;
                else doubl = i;
            }
            else if (rankRepeat[i] == 2 && doubl == -1)  doubl = i;
        }
        if (tripl != -1 && doubl != -1){
            handRank[0] = 7;
            handRank[1] = (tripl + 2) + (doubl + 2) / 100.0;
            return;
        }

        if (flushSuit != -1){                                                   // find flush
            int temp = 0;
            for (int i = 0; i < 2; i++){                                        // get player's highest rank flush suit card in case of tie
                if (card[i].suit == flushSuit){
                    temp = card[i].rank;
                    break;
                }
            }
            handRank[0] = 6;
            handRank[1] = flushSuit + temp / 100.0;
            return;
        }

        if (!startOfStraight.empty()){                                          // find straight
            handRank[0] = 5;
            handRank[1] = startOfStraight[0] + 2;
            return;
        }

        if (tripl != -1){                                                       // find 3 of kind
            handRank[0] = 4;
            handRank[1] = tripl + 2;
            return;
        }

        doubl = -1;
        int doubl2 = -1;

        for (int i = 12; i >= 0; i--){                                          // find double pairs
            if (rankRepeat[i] == 2){
                if (doubl == -1) doubl = i;
                else doubl2 = i;
            }
            if (doubl != -1 && doubl2 != -1){
                handRank[0] = 3;
                handRank[1] = (doubl + 2) + (doubl2 + 2) / 100.0;
                return;
            }
        }

        if (doubl != -1){                                                       // single pair
            handRank[0] = 2;
            handRank[1] = doubl + 2;
            return;
        }

        handRank[0] = 1;                                                        // find high card
        handRank[1] = card[0].rank + 2;
        return;
    }
};

Card drawCard(){
cout << ".";
    Card card;
    bool isNew = false;
    while (!isNew){
        card.suit = rand() % 4;
        card.rank = rand() % 13;                                                               // generate random card
        isNew = true;
        if (drawnCards[card.suit][card.rank]) isNew = false;
    }
    drawnCards[card.suit][card.rank] = true;                                                   // add card to list of drawn cards
    return card;
}

string comboName(int x){
    switch (x){
        case 1: 
            return "High Card";
        case 2: 
            return "Single Pair";
        case 3: 
            return "Double Pairs";
        case 4: 
            return "3 of a Kind";
        case 5: 
            return "Straight";
        case 6: 
            return "Flush";
        case 7: 
            return "Fullhouse";
        case 8: 
            return "4 of a Kind";
        case 9: 
            return "Straight Flush";
        case 10: 
            return "Royal Flush";
    }
    return "ERROR";
}

void printPlayers(Player players[], int num){
    for (int i = 0; i < num; i++){                                                      // print names
        cout << players[i].getName();
        for (int j = 0; j < 13 - players[i].getName().length(); j++) cout << " ";
    }
    cout << endl;
    for (int i = 0; i < num; i++){                                                      // print amount of chips
        printf("%-13d", players[i].getChips());
    }
    cout << endl;
}

void printCards(Player players[], cardsInHand *hands[], int action[], int num, int dealer, bool river){
    system("cls");
    for (int i = 0; i < 5; i++){                                                        // print community cards
        for (int k = 0; k < 5; k++){
            int cSuit, cRank;
            if (commCards.size() < k + 1);
            else {
                cSuit = commCards[k].suit;
                cRank = commCards[k].rank;
            }
            for (int j = 0; j < 8; j++){
                if (commVis[k] == &hide) cout << (*commVis[k])[i][j];
                else {
                    if ((i == 1 && j == 1) || (i == 3 && j == 5)) cout << ranks[cRank];
                    else if (cRank == 8 && ((i == 1 && j == 2) || (i == 3 && j == 4))) continue;
                    else if (i == 2 && j == 3) cout << suits[cSuit];
                    else cout << (*commVis[k])[i][j];
                }
            }
        }
        cout << endl;
    }
    cout << endl; 

    if (river){                                                                         // set all hands to show when round ends
        for (int i = 0; i < num; i++){
            hands[i] = &showHand;
        }
    }

    if (!river){                                                                        // no decisions are made during river
        for (int i = 0; i < num; i++){                                                  // print players' actions
            string str = ""; 
            if (action[i] == 1) str = "▼ calls";
            else if (action[i] == 2) str = "▼ raises";
            else if (action[i] == 3) str = "▼ folds";
            else if (action[i] == 4) str = "▼ all-in";
            else if (action[i] == 33) str = "▼ folded";
            else if (action[i] == 44) str = "▼ all-in'd";

            if (action[i] == 0) printf("%-13s", str.c_str());
            else printf("%-15s", str.c_str());
        }
        cout << endl;
    }

    printPlayers(players, num);                                                         // print players' name & chips
    for (int i = 0; i < num; i++){                                                      // print players' bet
        printf("%-13d", players[i].getBet());
    }
    cout << endl;

    for (int i = 0; i < 7; i++){                                                        // print player's cards
        for (int k = 0; k < num; k++){
            int c1Rank = players[k].getCard(0).rank;
            int c1Suit = players[k].getCard(0).suit;
            int c2Rank = players[k].getCard(1).rank;
            int c2Suit = players[k].getCard(1).suit;
            for (int j = 0; j < 12; j++){
                if (i == 0 && j == 0 && k == dealer) cout <<"ᴅ";                                // dealer get a symbol beside the cards
                else if (hands[k] != &showHand) cout << (*hands[k])[i][j];                      // print just the back of cards 
                else {
                    if ((i == 1 && j == 5) || (i == 3 && j == 9)) cout << ranks[c2Rank];        // print cards with rank & suit
                    else if ((i == 3 && j == 1) || (i == 5 && j == 5)) cout << ranks[c1Rank];
                    else if (i == 2 && j == 7) cout << suits[c2Suit];
                    else if (i == 4 && j == 3) cout << suits[c1Suit];
                    else if (c2Rank == 8 && ((i == 1 && j == 6) || (i == 3 && j == 8)));
                    else if (c1Rank == 8 && ((i == 3 && j == 2) || (i == 5 && j == 4)));
                    else cout << (*hands[k])[i][j];
                }
            }
        }
        cout << endl;
    }
}

void playRound(Player players[], int& num, int initial, int dealer){
    for (int i = 0; i < 5; i++) commVis[i] = &hide;                         // reset community card print
    commCards.clear();                                                      // remove prior community card
    for (int i = 0; i < 4; i++){
        for (int j = 0; j < 13; j++) drawnCards[i][j] = false;              // set all cards to not drawn
    }

    cardsInHand *hands[num];
    hands[0] = &showHand;                                                   // hide all players' cards except yours
    for (int i = 1; i < num; i++) hands[i] = &playHand;
    // for (int i = 1; i < num; i++) hands[i] = &showHand;           /// DEBUG: reveal all players' cards
    for (int i = 0; i < num; i++){
        players[i].setBet(0);                                               // reset player's bet
        players[i].setCards(0, drawCard());                                 // draw cards for each player
        players[i].setCards(1, drawCard());
        players[i].sortCards();
        if (players[i].getChips() >= initial){                              // reset player's status
            players[i].setFold(false);
            players[i].setAllIn(false);
        }
        else {
            players[i].setAllIn(true);
        }
    }
    cout << endl;

    int roundBet = initial;
    cout <<"□ The dealer is "<< players[dealer].getName() <<".\n";          // dealer initialization
    printf("□ The bet is %d.\n", roundBet);

    int pot = 0;
    for (int i = 1; i < 3; i++){                                            // small & big blind initialization
        int currPlayer = (dealer + i) % num;
        players[currPlayer].blindBet(currPlayer, roundBet, i);
        if (currPlayer != 0) cout << endl;
        pot += players[currPlayer].getBet();
    }

    int action[num] = {0};                                                  // list of players' actions (to print)

    for (int commSeen = 0; commSeen <= 5; commSeen++){                      // commSeen = # of community cards drawn
        int currPlayer = (dealer + 1) % num;                                // small & big blinds' wagers skipped in 1st go
        int blindOffset = 0;
        if (commSeen == 0){
            currPlayer = (currPlayer + 2) % num;
            blindOffset = 2;
        }
        for (int i = 0; i < num - blindOffset; i++){
            if (currPlayer == 0) action[0] = 0;
            printCards(players, hands, action, num, dealer, false);
            printf("□ The bet is %d.\n", roundBet);
            printf("□ The is pot is worth %d.\n", pot);

            int noFold = 0;
            for (int j = 0; j < num; j++){
                if (!players[j].getFold()) noFold++;
            }
            if (currPlayer == 0) action[0] = players[0].playerThink(roundBet);                                  // prompt player's input
            else action[currPlayer] = players[currPlayer].AiThink(action, roundBet, commSeen, num, noFold);     // prompt AI's input
            
            if (players[currPlayer].getFold()) hands[currPlayer] = &foldHand;
            else if (players[currPlayer].getBet() > roundBet) roundBet = players[currPlayer].getBet();
            
            if (currPlayer == 0) for (int i = 1; i < num; i++) action[i] = 0;

            currPlayer = (currPlayer + 1) % num;
            this_thread::sleep_for(chrono::milliseconds(400));
        }
        cout << endl;

        pot = 0;
        for (int i = 0; i < num; i++){
            pot += players[i].getBet();
        }

        if (commSeen == 0){                                             // start with on your cards
            commSeen = 2;
            for (int i = 0; i < 3; i++){                                // draw & reveal 3 community cards
                commCards.push_back(drawCard());
                commVis[i] = &show;
            }
        }
        else if (commSeen < 5){                                         // draw & reveal 2 more one by one
            commCards.push_back(drawCard());                            // skip this during river (commseen = 5) 
            commVis[commSeen] = &show;
        }
    }

    int dummy[num] = {0};                                               // dummy array (unused)
    printCards(players, hands, dummy, num, dealer, true);               // print table all cards show
    printf("□ The bet is %d.\n", roundBet);
    printf("□ The is pot is worth %d.\n", pot);

    for (int i = 0; i < num; i++){                                      // calculate players' best combination
        players[i].findBest();
    }
    cout << endl;                                                           /// DEBUG: player's best combination
    for (int i = 0; i < num; i++){                                          ///
        (players[i].getFold())? cout <<"○ " : cout <<"● ";                  ///
        cout << players[i].getName() <<"\t";                                ///
        if ((players[i].getName()).length() < 6) cout <<"\t";               ///
        cout << comboName(players[i].getCombo(0)) <<"\t";                   ///
        if (comboName(players[i].getCombo(0)).length() < 8) cout <<"\t";    ///
        cout << players[i].getCombo(1) << endl;                             ///
    }                                                                       ///

    vector<int> noFold;                                                 // players who've folded aren't considered for winners
    for (int i = 0; i < num; i++){
        if (!players[i].getFold()) noFold.push_back(i);
    }
    vector<int> best;
    for (int i = 0; i < noFold.size(); i++){                            // compares all players' combination
        best.push_back(noFold[i]);                                      // builds a vector of winners
        for (int j = 0; j < noFold.size(); j++){
            if (noFold[i] == noFold[j]) continue;
            if (players[noFold[i]].getCombo(0) < players[noFold[j]].getCombo(0)){
                best.clear();
                break;
            }
            else if (players[noFold[i]].getCombo(0) == players[noFold[j]].getCombo(0)){
                if (players[noFold[i]].getCombo(1) < players[noFold[j]].getCombo(1)){
                    best.clear();
                    break;
                }
                else if (players[noFold[i]].getCombo(1) == players[noFold[j]].getCombo(1)) best.push_back(noFold[j]);
            }
            if (j == noFold.size() - 1) goto finish;                    // if a player beats/ties all other player's combo, they're a winner
        }
    }
    finish:

    for (int i = 0; i < best.size(); i++){                              // split earnings with winners
        if (players[best[i]].getAllIn()){                               // if winner's all in, at max they win proportional to their bet
            int reward = 0;
            for (int j = 0; j < num; j++){
                int winnerBet = players[best[i]].getBet();
                int loserLoss = players[j].getBet() / best.size();
                if (loserLoss >= winnerBet){                            // chips returned if losers' bet larger than all in bet
                    reward += winnerBet;
                    players[j].setChips(players[j].getChips() + loserLoss - winnerBet);
                }
                else reward += players[j].getBet() / best.size();
            }
            players[best[i]].setChips(players[best[i]].getChips() + reward);
        }
        else players[best[i]].setChips(players[best[i]].getChips() + pot / best.size());
    }

    if (best.size() == 1){
        cout <<"\n□ "<< players[best[0]].getName() <<" wins with "<< comboName(players[best[0]].getCombo(0)) <<".\n";
        cout <<"□ "<< players[best[0]].getName() <<" wins "<< pot <<" chips.\n";
    }
    else {
        cout <<"\n□ ";
        for (int i = 0; i < best.size(); i++){
            if (i == best.size() - 2) cout << players[best[i]].getName() <<" & ";
            else if (i != best.size() - 1) cout << players[best[i]].getName() <<", ";
            else cout << players[best[i]].getName() <<" ";
        }
        cout <<"tie with "<< comboName(players[best[0]].getCombo(0)) <<".\n";
        cout <<"□ They each win "<< pot / best.size() <<" chips.\n";
    }
    cout << endl;
    printPlayers(players, num);

    bool gameOver = false;
    for (int i = 0; i < num; i++){                                      // check for player with no money
        if (players[i].getChips() < 1){
            if (i == 0){                                                // ends game when you have no chips
                gameOver = true;
                continue;
            }
            cout <<"□ "<< players[i].getName() <<" leaves the table with nothing.\n";
            for (int j = i; j < num; j++){                              // otherwise "remove" a player
                if (j == num - 1) num --;
                else players[j] = players[j + 1];
            }
        }
    }
    if (gameOver){
        cout <<"\n□ You leave the table with nothing.\n\n";
        exit(0);
    }

    if (num == 1){                                                      // win condition
        printf("\n□ You outplayed everyone & walk away with $%d.\n\n", players[0].getChips());
        exit(0);
    }
}

int main(){
    srand(time(0));

    int money = 300;
    Player p0("YOU", money, {0, 0, 0});
    Player p1("Arif", money, {.65, .2, .5});
    Player p2("Jet", money, {.35, .7, .3});
    Player p3("Gio", money, {.5, .9, .1});
    Player p4("Neville", money, {.85, 0, 1});
    Player p5("Joselyn", money, {.5, .6, .2});
    Player p6("Rene", money, {.95, .1, .8});
    Player p7("Mei", money, {.6, .8, .4});

    Player players[] = {p0, p1, p2, p3, p4, p5, p6, p7};
    int num = size(players);
    
    int dealer = rand()%num;
    double initial = 3;
    int ans = 1;
    while (ans == 1){
        playRound(players, num, (int)initial, dealer);
        initial *= 1.3;
        dealer = (dealer + 1) % num;
        // return 0;                                       /// DEBUG: play only 1 round
        do {
            cout <<"► Another round? [1/0]: ";
            cin >> ans;
            if (ans == 0) break;
        } while (ans != 1);
    }

    printf("\n□ You walk away with $%d, ", players[0].getChips());
    int difference = players[0].getChips() - money;
    if (difference >= 0) printf("having won $%d.\n\n", difference);
    else printf("having lost $%d.\n\n", abs(difference));
    
    return 0;
}
