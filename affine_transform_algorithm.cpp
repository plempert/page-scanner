#include <iostream>
#define n 5

using namespace std;

struct point{
    double x_p;
    double y_p;
    double x;
    double y;
    point(int X=0, int Y=0){
        x_p=0;
        y_p=0;
        x=X;
        y=Y;
    }
};


void generateAffine(){
    point second(0,2);
    point first(0,0);
    point fourth(2,0);
    point third(4,4);

    point arr[n][n];

    for(int y_i = 0; y_i < n; y_i++){
        arr[0][y_i].y_p = (double) y_i / (n-1);
        arr[0][y_i].y = first.y+(second.y-first.y) * arr[0][y_i].y_p;

        arr[0][y_i].x_p = 0;
        arr[0][y_i].x = first.x+(second.x-first.x) * arr[0][y_i].y_p;

        arr[n-1][y_i].y_p = arr[0][y_i].y_p;
        arr[n-1][y_i].y = fourth.y+(third.y-fourth.y) * arr[0][y_i].y_p;

        arr[n-1][y_i].x_p = 1;
        arr[n-1][y_i].x = fourth.x+(third.x-fourth.x) * arr[0][y_i].y_p;

    }

    for(int y_i = 0; y_i < n; y_i++){
        for(int x_i = 1; x_i < n-1; x_i++){
            arr[x_i][y_i].y_p = arr[0][y_i].y_p;
            arr[x_i][y_i].x_p = (double) x_i / (n-1);
            arr[x_i][y_i].y = arr[0][y_i].y + (arr[n-1][y_i].y - arr[0][y_i].y) * arr[x_i][y_i].x_p;
            arr[x_i][y_i].x = arr[0][y_i].x + (arr[n-1][y_i].x - arr[0][y_i].x) * arr[x_i][y_i].x_p;
        }
    }

    for(int y_i = n-1; y_i >= 0; y_i--){
        for(int x_i = 0; x_i < n; x_i++){
            cout << arr[x_i][y_i].x << "," << arr[x_i][y_i].y << "\t\t";
        }
        cout << endl;
    }
    cout << endl;

}

int main(){

    generateAffine();

    return 0;
}