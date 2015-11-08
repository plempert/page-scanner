package com.example.patrick.hacknjit;


import android.app.Notification;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity Log";
    private CVImageView mImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Drawable myDrawable = getResources().getDrawable(R.drawable.image/*sample_image*/);
        Bitmap myBitmap = ((BitmapDrawable) myDrawable).getBitmap();

//        try {
//            Matrix matrix = new Matrix();
//            matrix.postRotate(90);
//            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
//        }
//        catch (Exception e) {
//
//        }

        LinearLayout container = (LinearLayout) findViewById(R.id.linear_container);

        mImageView = new CVImageView(this);
        mImageView.initialize(myBitmap);

        container.addView(mImageView);







        Log.d(TAG,"onCreate succeeded.");
    }

    //Handler for the 'Take photo' button
    public void dispatchTakePictureIntent(View view) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        // Launch activity and wait for response
        startActivityForResult(intent, 0);
    }

    //The image can be obtained from onActivityResult
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bp = (Bitmap) data.getExtras().get("data");
        //mImageView.setImageBitmap(bp);
        mImageView.initialize(bp);

    }

    public void findEdges(View view){
        if (mImageView != null) mImageView.convertToEdgeView();
    }

    public void rectilinearTransform(View view){
        if (mImageView != null) mImageView.performRectilinearTransform();
    }

    public void resetVertices(View view){
        mImageView.resetVertices();
    }

    public void sendSMS(View view){
        Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);

        sendIntent.putExtra("sms_body",mImageView.getmImage());
        sendIntent.setType("image/jpeg");

        File downloadedPic =  new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS),
                        saveToInternalStorage(mImageView.getmImage()));

        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadedPic));


        startActivity(sendIntent);
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("photoscan", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }
}
