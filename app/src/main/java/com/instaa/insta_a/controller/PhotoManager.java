package com.instaa.insta_a.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.instaa.insta_a.R;


public class PhotoManager {
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;

    private Bitmap mImageBitmap;
    private Activity activity;

	private String mCurrentPhotoPath;

    private int width;
    private int height;

    private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

	public static final int RESULT_OK = -1;

    public PhotoManager(ImageView pImageView, Activity pActivity) {
        this.mImageView = pImageView;
        this.activity = pActivity;

        //mImageView = (ImageView) findViewById(R.id.imageView1);
        mImageBitmap = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
    }

    /* Photo album for this application */
	private String getAlbumName() {
		return activity.getString(R.string.album_name);
	}

	
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.v(activity.getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}

	private void setPic() {
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		width = mImageBitmap.getWidth();
		height = mImageBitmap.getHeight();

		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(View.VISIBLE);
	}

	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.activity.sendBroadcast(mediaScanIntent);
	}

	public void dispatchTakePicture(int actionCode) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;

		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			e.printStackTrace();
			mCurrentPhotoPath = null;
		}
        activity.startActivityForResult(takePictureIntent, actionCode);
	}

	public void handleBigCameraPhoto() {
		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			mCurrentPhotoPath = null;
            showElements();
		}
	}

    public void disappearElements() {
        ImageButton btnBlackWhite = (ImageButton) activity.findViewById(R.id.imageButtonBlackWhite);
        btnBlackWhite.setVisibility(View.INVISIBLE);
        ImageButton btnConvolution = (ImageButton) activity.findViewById(R.id.imageButtonConvolution);
        btnConvolution.setVisibility(View.INVISIBLE);
        TextView TxtVBlackWhite = (TextView) activity.findViewById(R.id.textViewBlackWhite);
        TxtVBlackWhite.setVisibility(View.INVISIBLE);
        TextView TxtVConvolution = (TextView) activity.findViewById(R.id.textViewConvolutions);
        TxtVConvolution.setVisibility(View.INVISIBLE);
    }

    private void showElements(){
        ImageButton btnBlackWhite = (ImageButton) activity.findViewById(R.id.imageButtonBlackWhite);
        btnBlackWhite.setVisibility(View.VISIBLE);
        ImageButton btnConvolution = (ImageButton) activity.findViewById(R.id.imageButtonConvolution);
        btnConvolution.setVisibility(View.VISIBLE);
        TextView TxtVBlackWhite = (TextView) activity.findViewById(R.id.textViewBlackWhite);
        TxtVBlackWhite.setVisibility(View.VISIBLE);
        TextView TxtVConvolution = (TextView) activity.findViewById(R.id.textViewConvolutions);
        TxtVConvolution.setVisibility(View.VISIBLE);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Bitmap getmImageBitmap() {
        return mImageBitmap;
    }

    public void setmImageBitmap(Bitmap mImageBitmap) {
        this.mImageBitmap = mImageBitmap;
    }
}