#include <iostream>
#include <ctime>
#include <conio.h>
using namespace std;

int map[4][4] = {{0, 0, 0, 0},
                 {0, 0, 0, 0},
                 {0, 0, 0, 0},
                 {0, 0, 0, 0}};

void gravity(int temp[], int& points, bool& didAnything){
    for (int i = 0; i < 4; i++){
        if (temp[i] == 0){                                  // on copy strip, check for free tiles
            for (int j = i + 1; j < 4; j++){
                if (temp[j] != 0){                          // if theres # tiles after free tiles
                    swap(temp[i], temp[j]);                 // # tile slide to free space
                    didAnything = true;                     // input is valid since a tile moved
                    break;
                }
            }
        }
    }
    for (int i = 0; i < 3; i++){
        if (temp[i] != 0 && temp[i] == temp[i + 1]){        // check if theres same # tiles next to another on the strip
            points += temp[i];
            temp[i] *= 2;                                   // if so, merge them
            for (int k = i + 1; k < 3; k++){                // then tiles behind them slide into gap created by the merge
                temp[k] = temp[k + 1];
            }
            temp[3] = 0;                                    // insert a free tile at the end of the strip
            didAnything = true;                             // input is valid since tiles merged
        }
    }
    return;
}

bool action(int& points){
    char in = _getch();
    while (in != 'w' && in != 'a' && in != 's' && in != 'd' && in != ' '){
        in = _getch();
    }
    if (in == ' ') exit(0);

    bool didAnything = false;
    int temp[4];                        // copy a row/col of the board, apply gravity to the strip, apply strip back onto the board
    if (in == 'w'){                     // row/col & order  which tiles are copied depends on the direction tiles are swiped to
        for (int c = 0; c < 4; c++){
            for (int r = 0; r < 4; r++) temp[r] = map[r][c];
            gravity(temp, points, didAnything);
            for (int r = 0; r < 4; r++) map[r][c] = temp[r];
        }
    }
    else if (in == 'a'){
        for (int r = 0; r < 4; r++){
            for (int c = 0; c < 4; c++) temp[c] = map[r][c];
            gravity(temp, points, didAnything);
            for (int c = 0; c < 4; c++) map[r][c] = temp[c];
        }
    }
    else if (in == 's'){
        for (int c = 0; c < 4; c++){
            for (int r = 0, k = 3; r < 4; r++, k--) temp[k] = map[r][c];
            gravity(temp, points, didAnything);
            for (int r = 0, k = 3; r < 4; r++, k--) map[r][c] = temp[k];
        }
    }
    else if (in == 'd'){
        for (int r = 0; r < 4; r++){
            for (int c = 0, k = 3; c < 4; c++, k--) temp[k] = map[r][c];
            gravity(temp, points, didAnything);
            for (int c = 0, k = 3; c < 4; c++, k--) map[r][c] = temp[k];
        }
    }
    return didAnything;
}

void print(int points){
    for (int i = 0; i <= 8; i++){
        for (int j = 0; j <= 20; j++){
            int ii = i % 2;
            int jj = j % 5;

            if (i == 0 && j == 0) cout <<"╔";
            else if (i == 0 && j == 20) cout <<"╗";
            else if (i == 8 && j == 0) cout <<"╚";
            else if (i == 8 && j == 20) cout <<"╝";

            else if (i == 0 && jj == 0) cout <<"╦";
            else if (i == 8 && jj == 0) cout <<"╩";
            else if (ii == 0 && j == 0) cout <<"╠";
            else if (ii == 0 && j == 20) cout <<"╣";

            else if (ii == 0 && jj == 0) cout <<"╬";
            else if (ii == 0) cout <<"═";
            else if (jj == 0) cout <<"║";

            else if (jj == 1){
                int I = i / 2;
                int J = j / 5;
                if (map[I][J] == 0) cout <<"    ";
                else if (map[I][J] < 10) cout <<" "<< map[I][J] <<"  ";
                else if (map[I][J] < 100) cout <<" "<< map[I][J] <<" ";
                else if (map[I][J] < 1000) cout << map[I][J] <<" ";
                else if (map[I][J] >= 1000) cout << map[I][J];
            }
        }
        cout << endl;
    }
    cout <<"points: "<< points << endl;
    cout << "\npress WASD to play, [space] to end game.\n";

}

bool doubleCheck(){
    for (int i = 0; i < 4; i++){                                            // check if there 2 tile of same value next to another
        for (int j = 0; j < 4; j++){                                        // if so, theres more moves & game is not over yet
            if (i + 1 <= 3 && map[i][j] == map[i + 1][j]) return true;
            if (i - 1 >= 0 && map[i][j] == map[i - 1][j]) return true;
            if (j + 1 <= 3 && map[i][j] == map[i][j + 1]) return true;
            if (j - 1 >= 0 && map[i][j] == map[i][j - 1]) return true;
        }
    }
    return false;
}

void add2(bool& gameEnd, int& points){
    bool added = false;
    int r, c;
    while (!added){
        r = rand() % 4;
        c = rand() % 4;
        added = true;
        if (map[r][c] != 0) added = false;      // only generate the 2 tile on a free tile
    }
    map[r][c] = 2;
    points++;
    for (int i = 0; i < 4; i++){
        for (int j = 0; j < 4; j++){
            if (map[i][j] == 0) return;         // if theres a free tile, continue game
            if (i == 3 && j == 3){
                if (doubleCheck()) return;      // if theres tiles that can be merged, continue game
                gameEnd = true;                 // game over if both conditions above fails
            }
        }
    }
    return;
}

int main(){
    srand(time(0));

    bool gameEnd = false;
    int points = 0;
    add2(gameEnd, points);
    print(points);
    while (!gameEnd){
        redo:
        if (!action(points)) goto redo;     // prompt for input, if input does nothing, ask again
        system("cls");
        add2(gameEnd, points);              // generate a 2 tile onto the board
        print(points);
    }

    cout << "Game Over\n\n";

    return 0;
}