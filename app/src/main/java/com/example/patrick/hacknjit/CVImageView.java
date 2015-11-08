package com.example.patrick.hacknjit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Patrick on 11/8/15.
 */
public class CVImageView extends View {
    private static final String TAG = "CVImageView Log";
    private Bitmap mImage = null;
    private int width;
    private int height;
    private Activity activity;
    private Point mFirst, mSecond, mThird, mFourth;
    private boolean mVerticesSet;
    private int offset = 50;

    public Bitmap getmImage() {
        return mImage;
    }

    public class Point{
        public double x;
        public double y;
        public double x_p;
        public double y_p;
        Point(double X, double Y){
            x = X;
            y = Y;
            x_p = 0;
            y_p = 0;
        }
        Point(){
            x = 0;
            y = 0;
            x_p = 0;
            y_p = 0;
        }
    }

    public CVImageView(Context context){
        super(context);
        activity = (Activity) context;
        Log.d(TAG, "CVImageView created.");
        mVerticesSet = false;
    }

    void resetVertices(){
        mFirst = mSecond = mThird = mFourth = null;
        mVerticesSet = false;
    }

    public void initialize(Bitmap image){
        // We need to resize based on different image sizes.
        // Goal is for width to match parent. (350px)
        if(image.getWidth() > image.getHeight()){
            Matrix matrix = new Matrix();

            matrix.postRotate(90);

            image = Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight(),matrix,true);
        }
        width = 350;
        height = image.getHeight()*350/image.getWidth();//600;


        mImage = Bitmap.createScaledBitmap(image,width,height,true);
        Log.d(TAG,"initialize called.");
        invalidate();
    }

    public void convertToEdgeView(){
//        Bitmap temp = Bitmap.createBitmap(mImage);
//        convertToGrayView();
        int r, g, b;

        for (int i = 0; i < mImage.getWidth(); i++) {
            for (int j = 0; j < mImage.getHeight(); j++) {
                double red_grayify = 0.299;
                double green_grayify = 0.587;
                double blue_grayify = 0.114;
                r = Color.red(mImage.getPixel(i, j));
                g = Color.green(mImage.getPixel(i, j));
                b = Color.blue(mImage.getPixel(i, j));
                int grayVal = (int)(red_grayify*r +
                                   green_grayify*g +
                                   blue_grayify*b);
                r = grayVal;
                g = grayVal;
                b = grayVal;
                mImage.setPixel(i, j, Color.argb(Color.alpha(mImage.getPixel(i, j)), r, g, b));
            }
        }
        Log.d(TAG,"convertToGrayScale succeeded.");
//        applyGaussianFilter();
        int k = 2;
        double sigma = 1.0;
        double[][] gaussianMatrix = new double[5][5];
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                gaussianMatrix[i][j] = (1/(2*3.1415*Math.pow(sigma, 2)))*
                        Math.exp(-1 * (Math.pow(-1 * k + j, 2) + Math.pow(-1 * k + i, 2)) / (2 * Math.pow(sigma, 2)));
            }
        }
        double[][] blur_val = new double [mImage.getWidth()][mImage.getHeight()];
        double blur_sum;
        for(int i=2 ; i < mImage.getWidth()-2 ; i++){
            for( int j=2 ; j < mImage.getHeight()-2 ; j++){
                blur_sum = 0;
                for(int m=0; m<5; m++){
                    for(int n=0; n<5; n++){
                        blur_sum += gaussianMatrix[m][n] *
                                Color.red(mImage.getPixel(i-2+m, j-2+n));
                    }
                }
                blur_val[i][j]=blur_sum;
            }
        }
        for(int i=2 ; i < mImage.getWidth()-2 ; i++) {
            for (int j = 2; j < mImage.getHeight() - 2; j++) {
                r = (int) blur_val[i][j];
                g = (int) blur_val[i][j];
                b = (int) blur_val[i][j];
                mImage.setPixel(i, j, Color.argb(Color.alpha(mImage.getPixel(i, j)), r, g, b));
            }
        }
        Log.d(TAG,"gaussianBlur succeeded.");
//        findIntensityGradient();

        int[][] Gx = new int[3][3];
        Gx[0][0]=-1;    Gx[0][1]=0;     Gx[0][2]=1;
        Gx[1][0]=-2;    Gx[1][1]=0;     Gx[1][2]=2;
        Gx[2][0]=-1;    Gx[2][1]=0;     Gx[2][2]=1;

        int[][] Gy = new int[3][3];
        Gy[0][0]=1;     Gy[0][1]=2;     Gy[0][2]=1;
        Gy[1][0]=0;     Gy[1][1]=0;     Gy[1][2]=0;
        Gy[2][0]=-1;    Gy[2][1]=-2;    Gy[2][2]=-1;

        //results of convolution
        double[][] Gxc = new double [mImage.getWidth()][mImage.getHeight()];
        double[][] Gyc = new double [mImage.getWidth()][mImage.getHeight()];
        double[][] Gc = new double [mImage.getWidth()][mImage.getHeight()];
        double[][] theta = new double [mImage.getWidth()][mImage.getHeight()];

        double convolution_value;

        for(int i=2 ; i < mImage.getWidth()-2 ; i++) {
            for (int j = 2; j < mImage.getHeight() - 2; j++) {

                convolution_value = 0;
                for(int m=0; m<3; m++){
                    for(int n=0; n<3; n++){
                        convolution_value += Gx[m][n] *
                                Color.red(mImage.getPixel(i-2+m, j-2+n));
                    }
                }
                Gxc[i][j] = Math.abs(convolution_value);

                convolution_value = 0;
                for(int m=0; m<3; m++){
                    for(int n=0; n<3; n++){
                        convolution_value += Gy[m][n] *
                                Color.red(mImage.getPixel(i-2+m, j-2+n));
                    }
                }
                Gyc[i][j] = Math.abs(convolution_value);

                Gc[i][j] = Math.sqrt(Math.pow(Gxc[i][j],2)+Math.pow(Gyc[i][j],2));
                theta[i][j] = Math.atan(Gyc[i][j] / Gxc[i][j]);
            }
        }

        for(int i=2 ; i < mImage.getWidth()-2 ; i++) {
            for (int j = 2; j < mImage.getHeight() - 2; j++) {
                r = (int) Gc[i][j];
                g = (int) Gc[i][j];
                b = (int) Gc[i][j];
                mImage.setPixel(i, j, Color.argb(Color.alpha(mImage.getPixel(i, j)), r, g, b));
            }
        }
//        applyNonMaximumSuppression();
        double[][] thin = new double [mImage.getWidth()][mImage.getHeight()];
        for(int i=2 ; i < mImage.getWidth()-2 ; i++) {
            for (int j = 2; j < mImage.getHeight() - 2; j++) {
                theta[i][j] = theta[i][j] * 180.0 / (2*3.1415);
                if(theta[i][j]>=157.5 || theta[i][j]<22.5){ //edge runs W-E
                    if(Color.red(mImage.getPixel(i, j))>Color.red(mImage.getPixel(i, j-1)) &&
                       Color.red(mImage.getPixel(i, j))>Color.red(mImage.getPixel(i, j+1)))
                        thin[i][j] = Color.red(mImage.getPixel(i, j));
                    else thin[i][j] = 0;

                }
                else if(theta[i][j]>=22.5 && theta[i][j]<67.5){ //edge runs SW-NE
                    if(Color.red(mImage.getPixel(i, j))>Color.red(mImage.getPixel(i-1, j-1)) &&
                       Color.red(mImage.getPixel(i, j))>Color.red(mImage.getPixel(i+1, j+1)))
                        thin[i][j] = Color.red(mImage.getPixel(i, j));
                    else thin[i][j] = 0;
                }
                else if(theta[i][j]>=67.5 && theta[i][j]<112.5){ //edge runs N-S
                    if(Color.red(mImage.getPixel(i, j))>Color.red(mImage.getPixel(i-1, j)) &&
                       Color.red(mImage.getPixel(i, j))>Color.red(mImage.getPixel(i+1, j)))
                        thin[i][j] = Color.red(mImage.getPixel(i, j));
                    else thin[i][j] = 0;
                }
                else if(theta[i][j]>=112.5 && theta[i][j]<157.5){ //edge runs NW-SE
                    if(Color.red(mImage.getPixel(i, j))>Color.red(mImage.getPixel(i+1, j-1)) &&
                       Color.red(mImage.getPixel(i, j))>Color.red(mImage.getPixel(i-1, j+1)))
                        thin[i][j] = Color.red(mImage.getPixel(i, j));
                    else thin[i][j] = 0;
                }
            }
        }
        for(int i=2 ; i < mImage.getWidth()-2 ; i++) {
            for (int j = 2; j < mImage.getHeight() - 2; j++) {
                r = (int) thin[i][j];
                g = (int) thin[i][j];
                b = (int) thin[i][j];
                mImage.setPixel(i, j, Color.argb(Color.alpha(mImage.getPixel(i, j)), r, g, b));
            }
        }
        Log.d(TAG,"nonMaximumSuppression succeeded.");
//        applyDoubleThreshold();
        for(int i=2 ; i < mImage.getWidth()-2 ; i++) {
            for (int j = 2; j < mImage.getHeight() - 2; j++) {
                int high = 50;
                int low = 50;
                if(Color.red(mImage.getPixel(i, j))>high){
                    mImage.setPixel(i, j, Color.argb(Color.alpha(mImage.getPixel(i, j)), 255, 255, 255));
                }
                if(Color.red(mImage.getPixel(i, j))<low){
                    mImage.setPixel(i, j, Color.argb(Color.alpha(mImage.getPixel(i, j)), 0, 0, 0));
                }
            }
        }
        Log.d(TAG,"CannyEdgeDetection succeeded.");
        invalidate();
    }
    int n = 500;
    Point[][] generateAffineMatrix(Point first, Point second, Point third, Point fourth){

        Point[][] arr = new Point[n][n];

        for(int y_i = 0; y_i < n; y_i++) {
            for (int x_i = 0; x_i < n; x_i++) {
                arr[x_i][y_i] = new Point();
            }
        }

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

        return arr;
    }
    
    public void performRectilinearTransform(){
        if(mVerticesSet){
            mFirst.x -= offset;
            mFirst.y -= offset;
            mSecond.x -= offset;
            mSecond.y -= offset;
            mThird.x -= offset;
            mThird.y -= offset;
            mFourth.x -= offset;
            mFourth.y -= offset;
            Point[][] selected_points = generateAffineMatrix(mFirst,mSecond,mThird,mFourth);
            Point[][] target_points = generateAffineMatrix(
                    new Point(0, mImage.getHeight()),
                    new Point(0,0),
                    new Point(mImage.getWidth(),0),
                    new Point(mImage.getWidth(),mImage.getHeight()));
            for(int y_i = 0; y_i < n; y_i++){
                for(int x_i = 0; x_i < n; x_i++){
                    int r = Color.red(mImage.getPixel(
                            (int)selected_points[x_i][y_i].x,
                            (int)selected_points[x_i][y_i].y));
                    int g = Color.green(mImage.getPixel(
                            (int) selected_points[x_i][y_i].x,
                            (int) selected_points[x_i][y_i].y));
                    int b = Color.blue(mImage.getPixel(
                            (int) selected_points[x_i][y_i].x,
                            (int) selected_points[x_i][y_i].y));
                    if(target_points[x_i][y_i].y < mImage.getHeight() &&
                            target_points[x_i][y_i].x < mImage.getWidth()) {
                        mImage.setPixel(
                                (int) target_points[x_i][y_i].x,
                                (int) target_points[x_i][y_i].y,
                                Color.argb(Color.alpha(mImage.getPixel(
                                        (int) selected_points[x_i][y_i].x,
                                        (int) selected_points[x_i][y_i].y)), r, g, b));
                    }
                }
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mImage != null){
            canvas.drawBitmap(mImage,offset,offset,null);
        }
        Log.d(TAG,"onDraw called.");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        if (mFirst == null) {
            mFirst = new Point();
            mFirst.x = x;
            mFirst.y = y;
            Log.d(TAG,"mFirst vertex set.");
        } else if (mSecond == null) {
            mSecond = new Point();
            mSecond.x = x;
            mSecond.y = y;
            Log.d(TAG,"mSecond vertex set.");
        } else if (mThird == null) {
            mThird = new Point();
            mThird.x = x;
            mThird.y = y;
            Log.d(TAG,"mThird vertex set.");
        } else if (mFourth == null) {
            mFourth = new Point();
            mFourth.x = x;
            mFourth.y = y;
            mVerticesSet = true;
            Log.d(TAG,"mFourth vertex set.");
        } else {
            Log.d(TAG,"All vertices filled.");
        }

        Log.d(TAG,"x:"+x+" y:"+y);
        return false;
    }
}
