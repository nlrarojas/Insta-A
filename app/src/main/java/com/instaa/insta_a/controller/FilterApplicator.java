package com.instaa.insta_a.controller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import java.util.Observable;
import java.util.Observer;
import java.util.Random;

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
    private static int [][] gaussianMask = {{1, 2, 1}, {2, 4, 2}, {1, 2, 1}};
    private int [][] ownFilterMask;

    private static int SIZE_GAUSSIAN_KERNEL = 3;
    private static int SIZE_OWN_KERNEL = 5;
    private double Factor = 1;
    private double Offset = 1;

    public FilterApplicator(int width, int height, Bitmap bitmap, Activity activity, ImageView mImageView, int pFilter) {
        this.width = width;
        this.height = height;
        this.bitmap = bitmap;
        this.activity = activity;
        this.mImageView = mImageView;
        this.blockThread = false;
        this.filter = pFilter;
        this.addObserver((Observer) activity);
        ownFilterMask = new int[SIZE_OWN_KERNEL][SIZE_OWN_KERNEL];
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
                case 5:
                    gaussianFilter();
                    break;
                case 6:
                    ownFilter();
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
    }

    public void gaussianFilter(){
        System.out.println("Iniciando filtro gaussiano");
        int A, R, G, B;
        int sumR, sumG, sumB;
        int[][] pixels = new int[SIZE_GAUSSIAN_KERNEL][SIZE_GAUSSIAN_KERNEL];
        newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for(int y = 0; y < height - 2; ++y) {
            for(int x = 0; x < width - 2; ++x) {
                A = Color.alpha(pixels[1][1]);
                sumR = sumG = sumB = 0;
                for(int i = 0; i < SIZE_GAUSSIAN_KERNEL; ++i) {
                    for(int j = 0; j < SIZE_GAUSSIAN_KERNEL; ++j) {
                        pixels[i][j] = bitmap.getPixel(x + i, y + j);
                        sumR += (Color.red(pixels[i][j]) * gaussianMask[i][j]) / 16;
                        sumG += (Color.green(pixels[i][j]) * gaussianMask[i][j]) / 16;
                        sumB += (Color.blue(pixels[i][j]) * gaussianMask[i][j]) / 16;
                    }
                }
                R = (int)(sumR / Factor + Offset);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = (int)(sumG / Factor + Offset);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = (int)(sumB / Factor + Offset);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                newBitmap.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
    }

    public void computeConvolution3x3() {
        int pixel;
        int A = 0, Red, B, G;
        newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Red = 0;
                B = 0;
                G = 0;
                if(checkLimits(x - 1, y - 1, width, height)){
                    pixel = bitmap.getPixel(x - 1, y - 1);

                    Red += Color.red(pixel) * gaussianMask[0][0];
                    G += Color.green(pixel) * gaussianMask[0][0];
                    B +=  Color.blue(pixel) * gaussianMask[0][0];
                }
                if(checkLimits(x - 1, y, width, height)){
                    pixel = bitmap.getPixel(x - 1, y);
                    Red += Color.red(pixel) * gaussianMask[1][0];
                    G += Color.green(pixel) * gaussianMask[1][0];
                    B +=  Color.blue(pixel) * gaussianMask[1][0];
                }
                if(checkLimits(x - 1, y + 1, width, height)){
                    pixel = bitmap.getPixel(x - 1, y + 1);
                    Red += Color.red(pixel) * gaussianMask[2][0];
                    G += Color.green(pixel) * gaussianMask[2][0];
                    B +=  Color.blue(pixel) * gaussianMask[2][0];
                }
                if(checkLimits(x, y - 1, width, height)){
                    pixel = bitmap.getPixel(x, y - 1);
                    Red += Color.red(pixel) * gaussianMask[0][1];
                    G += Color.green(pixel) * gaussianMask[0][1];
                    B +=  Color.blue(pixel) * gaussianMask[0][1];
                }
                if(checkLimits(x, y, width, height)){
                    pixel = bitmap.getPixel(x, y);
                    A = Color.alpha(pixel);
                    Red += Color.red(pixel) * gaussianMask[1][1];
                    G += Color.green(pixel) * gaussianMask[1][1];
                    B +=  Color.blue(pixel) * gaussianMask[1][1];
                }
                if(checkLimits(x, y + 1, width, height)){
                    pixel = bitmap.getPixel(x, y + 1);
                    Red += Color.red(pixel) * gaussianMask[2][1];
                    G += Color.green(pixel) * gaussianMask[2][1];
                    B +=  Color.blue(pixel) * gaussianMask[2][1];
                }
                if(checkLimits(x + 1, y - 1, width, height)){
                    pixel = bitmap.getPixel(x + 1, y - 1);
                    Red += Color.red(pixel) * gaussianMask[0][2];
                    G += Color.green(pixel) * gaussianMask[0][2];
                    B +=  Color.blue(pixel) * gaussianMask[0][2];
                }
                if(checkLimits(x + 1, y, width, height)){
                    pixel = bitmap.getPixel(x + 1, y);
                    Red += Color.red(pixel) * gaussianMask[1][2];
                    G += Color.green(pixel) * gaussianMask[1][2];
                    B +=  Color.blue(pixel) * gaussianMask[1][2];
                }
                if(checkLimits(x + 1, y + 1, width, height)){
                    pixel = bitmap.getPixel(x + 1, y + 1);
                    Red += Color.red(pixel) * gaussianMask[2][2];
                    G += Color.green(pixel) * gaussianMask[2][2];
                    B +=  Color.blue(pixel) * gaussianMask[2][2];
                }
                Red = (int)(Red / Factor + Offset);
                if(Red < 0) { Red = 0; }
                else if(Red > 255) { Red = 255; }

                G = (int)(G / Factor + Offset);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = (int)(B / Factor + Offset);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }
                newBitmap.setPixel(x, y, Color.argb(A, Red, G, B));
            }
        }
    }

    public void ownFilter(){
        //computeConvolution3x3();
        System.out.println("Filtro propio");
        int A, R, G, B;
        int sumR, sumG, sumB;
        int[][] pixels = new int[SIZE_OWN_KERNEL][SIZE_OWN_KERNEL];
        generateKernel();
        newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for(int y = 0; y < height - 4; ++y) {
            for(int x = 0; x < width - 4; ++x) {
                A = Color.alpha(pixels[1][1]);
                sumR = sumG = sumB = 0;

                for(int i = 0; i < SIZE_OWN_KERNEL; ++i) {
                    for(int j = 0; j < SIZE_OWN_KERNEL; ++j) {
                        pixels[i][j] = bitmap.getPixel(x + i, y + j);
                        sumR += (Color.red(pixels[i][j]) * ownFilterMask[i][j]) / 24;
                        sumG += (Color.green(pixels[i][j]) * ownFilterMask[i][j]) / 24;
                        sumB += (Color.blue(pixels[i][j]) * ownFilterMask[i][j]) / 24;
                    }
                }
                R = (int)(sumR / Factor + Offset);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = (int)(sumG / Factor + Offset);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = (int)(sumB / Factor + Offset);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                newBitmap.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
    }

    public boolean checkLimits(int positionX, int positionY, int maxLimitX, int minLimitY){
        return positionX >= 0 && positionY >= 0 && positionX < maxLimitX && positionY < minLimitY;
    }

    public void generateKernel (){
        Random random = new Random();
        for (int i = 0; i < SIZE_OWN_KERNEL; i++) {
            for (int j = 0; j < SIZE_OWN_KERNEL; j++) {
                ownFilterMask[i][j] = random.nextInt(4);
            }
        }
    }
}
