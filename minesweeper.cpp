#include <iostream>
#include <ctime>
using namespace std;

struct Tile{
    int danger = 0;
    bool cleared = false;
    bool flagged = false;
};

void printField(int row, int col, int gameOver, Tile **field){
    for (int i = 0; i < col; i++){
        if (i == 0) cout <<"\n   ";
        cout <<" ";
        printf("%-3d", i + 1);
    }
    cout << endl;
    for (int i = 0; i < row * 2 + 1; i++){
        for (int j = -1; j < col * 4 + 1; j++){
            if (j < 0){
                if (i % 2 == 1) printf("%-3d", i / 2 + 1);
                else cout <<"   ";
            }
            else if (i == 0 && j == 0) cout <<"╔";
            else if (i == 0 && j == col * 4) cout <<"╗";
            else if (i == 0 && j % 4 == 0) cout <<"╦";
            else if (i == row * 2 && j == 0) cout <<"╚";
            else if (i == row * 2 && j == col * 4) cout <<"╝";
            else if (i == row * 2 && j % 4 == 0) cout <<"╩";
            else if (i % 2 == 0 && j == 0) cout <<"╠";
            else if (i % 2 == 0 && j == col * 4) cout <<"╣";
            else if (i % 2 == 0 && j % 4 == 0) cout <<"╬";
            else if (i % 2 == 0) cout <<"═";
            else if (j % 4 == 0) cout <<"║";

            else if (i % 2 == 1 && j % 4 == 2){
                if (field[i/2][j/4].cleared){
                    if (field[i/2][j/4].danger == 9) cout <<"۞";
                    else if (field[i/2][j/4].danger == 0) cout <<" ";
                    else cout << field[i/2][j/4].danger;
                }
                else if (gameOver == -1 && field[i/2][j/4].danger == 9) cout <<"۞";
                else if (gameOver == 1 && field[i/2][j/4].danger == 9) cout <<"ꟼ";
                else if (field[i/2][j/4].flagged) cout <<"ꟼ";
                else cout <<"░";
            }
            else cout <<" ";
        }
        cout << endl;
    }
    return;
}

void sweep(int R, int C, Tile **field, int row, int col, int& tilesCleared){
    if (R < 0 || R >= row || C < 0 || C >= col) return;
    if (field[R][C].cleared) return;

    field[R][C].cleared = true;
    tilesCleared++;
    if (field[R][C].danger != 0) return;

    for (int i = -1; i < 2; i++){
        for (int j = -1; j < 2; j++){
            if (i == 0 && j == 0) continue;
            sweep(R + i, C + j, field, row, col, tilesCleared);
        }
    }
    return;
}

void sweepArea(int R, int C, Tile **field, int row, int col, int& tilesCleared, int& gameOver){
    for (int i = -1; i < 2; i++){
        for (int j = -1; j < 2; j++){
            if (R + i < 0 || R + i >= row || C + j < 0 || C + j >= col) continue;
            if (field[R+i][C+j].danger == 9){
                if (!field[R+i][C+j].flagged) gameOver = -1;
                else continue;
            }
            else sweep(R+i, C+j, field, row, col, tilesCleared);
        }
    }
}

int main(){
    srand(time(0));

    int row, col;
    do {
        cout <<"Enter size of field [height width]: ";
        cin >> row >> col;
    } while (row < 1 || row > 30 || col < 1 || col > 30);
    Tile **field = new Tile*[row];
    for (int i = 0; i < col; i++) field[i] = new Tile[col];

    int mines;
    do {
        cout <<"Enter amount of mines: ";
        cin >> mines;
    } while (mines < 1 || mines >= row * col);

    int gameOver = 0;
    int tilesCleared = 0;
    int minesFlagged = 0;

    printField(row, col, gameOver, field);

    int R, C;
    string action;
    do {                                                                        // player's 1st move guaranteed not blow up
        cout <<"decision [row col [c]]: ";
        cin >> R >> C >> action;
        R--, C--;
    } while (R < 0 || R > row || C < 0 || C > col || action != "c");

    for (int i = 0; i < mines; i++){                                            // place the mines randomly
        int rRow, rCol;
        do {
            rRow = rand() % row;
            rCol = rand() % col;
        } while (field[rRow][rCol].danger == 9 || (R == rRow && C == rCol));    // redo if mine alredy placed there
        field[rRow][rCol].danger = 9;                                           // or if its on player's 1st move

        for (int k = -1; k < 2; k++){                                           // update mines' surrounding tiles
            for (int l = -1; l < 2; l++){
                if (k == 0 && l == 0) continue;
                if (rRow + k < 0 || rRow + k >= row || rCol + l < 0 || rCol + l >= col) continue;
                if (field[rRow+k][rCol+l].danger == 9) continue;
                else field[rRow+k][rCol+l].danger++;
            }
        }
    }

    sweep(R, C, field, row, col, tilesCleared);
    system("cls");

    while (gameOver == 0){
        if (row * col - mines == tilesCleared || minesFlagged == mines){        // win condition
            gameOver = 1;
            printField(row, col, gameOver, field);
            cout << "\nYou Win :D\n\n";
            return 0;
        }

        printField(row, col, gameOver, field);

        again:
        do {
            cout <<"decision [row col [f/c]]: ";
            cin >> R >> C >> action;
            R--, C--;
        } while (R < 0 || R > row || C < 0 || C > col || (action != "f" && action != "c"));
        
        if (field[R][C].cleared && field[R][C].danger == 0) goto again;

        if (action == "f"){
            if (field[R][C].cleared) goto again;
            if (!field[R][C].flagged){
                field[R][C].flagged = true;
                if (field[R][C].danger == 9) minesFlagged++;
            }
            else {
                field[R][C].flagged = false;
                if (field[R][C].danger == 9) minesFlagged--;
            }
        }
        else {
            if (field[R][C].cleared) sweepArea(R, C, field, row, col, tilesCleared, gameOver);
            else if (field[R][C].danger == 9) gameOver = -1;
            else sweep(R, C, field, row, col, tilesCleared);
        }
        system("cls");
    }

    printField(row, col, gameOver, field);
    cout <<"\nYou Exploded :(\n\n";

    return 0;
}
