package com.instaa.insta_a.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;

import com.instaa.insta_a.R;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Nelson on 16/9/2017.
 */

public class FilterApplicator extends Observable implements Runnable {
    private int width;
    private int height;
    private Bitmap bitmap, newBitmap;
    private Activity activity;
    private ImageView mImageView;
    private boolean blockThread;
    private int filter;

    public FilterApplicator(int width, int height, Bitmap bitmap, Activity activity, ImageView mImageView, int pFilter) {
        this.width = width;
        this.height = height;
        this.bitmap = bitmap;
        this.activity = activity;
        this.mImageView = mImageView;
        this.blockThread = false;
        this.filter = pFilter;
        this.addObserver((Observer) activity);
    }

    @Override
    public void run() {
        while(!blockThread){
            switch (filter){
                case 1:
                    averagingFilter();
                    break;
                case 2:
                    desaturationFilter();
                    break;
                case 3:
                    maxDecomposition();
                    break;
                case 4:
                    minDecomposition();
                    break;
            }
            blockThread = true;
            setChanged();
            notifyObservers(newBitmap);
        }
    }

    public void averagingFilter(){
        int colorPixel, A, Red, G, B;
        newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                colorPixel = bitmap.getPixel(x, y);
                A = Color.alpha(colorPixel);
                Red = Color.red(colorPixel);
                G = Color.green(colorPixel);
                B = Color.blue(colorPixel);

                Red = (Red + G + B) / 3;
                G = Red;
                B = Red;
                newBitmap.setPixel(x, y, Color.argb(A, Red, G, B));
            }
        }

    }

    public void desaturationFilter (){
        int colorPixel, A, Red, G, B;
        newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                colorPixel = bitmap.getPixel(x, y);
                A = Color.alpha(colorPixel);
                Red = Color.red(colorPixel);
                G = Color.green(colorPixel);
                B = Color.blue(colorPixel);

                Red = (Math.max(Math.max(Red, G), B) + Math.min(Math.min(Red, G), B)) / 2;
                G = Red;
                B = Red;
                newBitmap.setPixel(x, y, Color.argb(A, Red, G, B));
            }
        }
        mImageView.setImageBitmap(newBitmap);
    }

    public void maxDecomposition(){
        int colorPixel, A, Red, G, B;
        newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                colorPixel = bitmap.getPixel(x, y);
                A = Color.alpha(colorPixel);
                Red = Color.red(colorPixel);
                G = Color.green(colorPixel);
                B = Color.blue(colorPixel);

                Red = Math.max(Math.max(Red, G), B);
                G = Red;
                B = Red;
                newBitmap.setPixel(x, y, Color.argb(A, Red, G, B));
            }
        }
        mImageView.setImageBitmap(newBitmap);
    }

    public void minDecomposition(){
        int colorPixel, A, Red, G, B;
        newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                colorPixel = bitmap.getPixel(x, y);
                A = Color.alpha(colorPixel);
                Red = Color.red(colorPixel);
                G = Color.green(colorPixel);
                B = Color.blue(colorPixel);

                Red = Math.min(Math.max(Red, G), B);
                G = Red;
                B = Red;
                newBitmap.setPixel(x, y, Color.argb(A, Red, G, B));
            }
        }
        mImageView.setImageBitmap(newBitmap);
    }
}
