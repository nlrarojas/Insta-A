package com.instaa.insta_a.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.instaa.insta_a.R;

import java.io.File;

/**
 * Created by Nelson on 17/9/2017.
 */

public class ListImageAdapter extends BaseAdapter {

    private File [] images;
    private Context context;
    private Bitmap bitmap;

    public ListImageAdapter(File[] images, Context context) {
        this.images = images;
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return images[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.image_container, null);
        }

        ImageView image = (ImageView) convertView.findViewById(R.id.imageViewContainer);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeFile(images[position].getAbsolutePath(), options);
        image.setImageBitmap(bitmap);
        return convertView;
    }
}
