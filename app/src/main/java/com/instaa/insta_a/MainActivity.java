package com.instaa.insta_a;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.instaa.insta_a.controller.FilterApplicator;
import com.instaa.insta_a.controller.PhotoManager;
import com.instaa.insta_a.view.BlackWhiteOptionsFilter;
import com.instaa.insta_a.view.ConvolutionOptionsFilter;
import com.instaa.insta_a.view.ImageDisplayFragment;
import com.instaa.insta_a.view.PrincipalPage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ImageDisplayFragment.OnFragmentInteractionListener,
        BlackWhiteOptionsFilter.OnFragmentInteractionListener, PrincipalPage.OnFragmentInteractionListener,
        ConvolutionOptionsFilter.OnFragmentInteractionListener, Observer{

    private static final int PICK_IMAGE = 2;
    private static final int ACTION_TAKE_PHOTO = 1;
    private ImageView mImageView;
    private PhotoManager photoManager;
    private int width;
    private int height;
    private Bitmap bitmap;
    private Snackbar mySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, new PrincipalPage()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if(photoManager != null){
                photoManager.disappearElements();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_log_out) {
            finish();
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, new PrincipalPage()).commit();
        } else if (id == R.id.nav_take_picture) {
            takePicture(null);
        } else if (id == R.id.nav_open_gallery) {
            openGallery(null);
        } else if (id == R.id.nav_log_out) {
            finish();
            System.exit(0);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void takePicture(View view){
        displayImageDisplayer();
        mImageView = (ImageView) findViewById(R.id.imageViewContainer);
        photoManager = new PhotoManager(mImageView, this);
        photoManager.dispatchTakePicture(1);
        height = photoManager.getHeight();
        width = photoManager.getWidth();
        bitmap = photoManager.getmImageBitmap();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == ACTION_TAKE_PHOTO) {
                photoManager.handleBigCameraPhoto();
            } else if (requestCode == PICK_IMAGE) {
                // Let's read picked image data - its URI
                Uri pickedImage = data.getData();
                // Let's read picked image path using content resolver
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFile(imagePath, options);
                displayImageDisplayer();
                mImageView = (ImageView) findViewById(R.id.imageViewContainer);
                mImageView.setImageBitmap(bitmap);
                showElements();
                height = bitmap.getHeight();
                width = bitmap.getWidth();
                cursor.close();
            }
        }
    }

    public void openGallery(View view){
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(pickIntent, "Seleccionar desde");

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public void showBlackWhiteFiltersOptions(View view){
        BlackWhiteOptionsFilter blackWhiteFragment = new BlackWhiteOptionsFilter();
        this.getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, blackWhiteFragment).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void displayImageDisplayer(){
        this.getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, new ImageDisplayFragment()).commit();
        this.getSupportFragmentManager().executePendingTransactions();
    }

    private void showElements(){
        ImageButton btnBlackWhite = (ImageButton) findViewById(R.id.imageButtonBlackWhite);
        btnBlackWhite.setVisibility(View.VISIBLE);
        ImageButton btnConvolution = (ImageButton) findViewById(R.id.imageButtonConvolution);
        btnConvolution.setVisibility(View.VISIBLE);
        TextView TxtVBlackWhite = (TextView) findViewById(R.id.textViewBlackWhite);
        TxtVBlackWhite.setVisibility(View.VISIBLE);
        TextView TxtVConvolution = (TextView) findViewById(R.id.textViewConvolutions);
        TxtVConvolution.setVisibility(View.VISIBLE);
    }

    public void averagingFilter(View view){
        showMessage(R.string.filter);
        FilterApplicator filterApplicator = new FilterApplicator(width, height, bitmap, this, mImageView, 1);
        filterApplicator.run();
    }

    public void desaturationFilter (View view){
        showMessage(R.string.filter);
        FilterApplicator filterApplicator = new FilterApplicator(width, height, bitmap, this, mImageView, 2);
        filterApplicator.run();
    }

    public void maxDecomposition(View view){
        showMessage(R.string.filter);
        FilterApplicator filterApplicator = new FilterApplicator(width, height, bitmap, this, mImageView, 3);
        filterApplicator.run();
    }

    public void minDecomposition(View view){
        showMessage(R.string.filter);
        FilterApplicator filterApplicator = new FilterApplicator(width, height, bitmap, this, mImageView, 4);
        filterApplicator.run();
    }

    public void gaussianFilter(View view){
        showMessage(R.string.filter);
        FilterApplicator filterApplicator = new FilterApplicator(width, height, bitmap, this, mImageView, 5);
        filterApplicator.run();
    }

    public void ownFilter(View view){
        showMessage(R.string.filter);
        FilterApplicator filterApplicator = new FilterApplicator(width, height, bitmap, this, mImageView, 6);
        filterApplicator.run();
    }

    public void showConvolutionFilters(View view){
        ConvolutionOptionsFilter convolutionOptionsFilter = new ConvolutionOptionsFilter();
        this.getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, convolutionOptionsFilter).commit();
    }

    private void showMessage(int message){
        mySnackbar = Snackbar.make(findViewById(R.id.contenedor), message, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    public void saveImage(View view){
        OutputStream fOut = null;
        try {
            System.out.println("Guardando");
            //Ruta para donde está la SD
            File pathSD = Environment.getExternalStorageDirectory();
            //Carpeta donde se guardaran as imagenes
            File directory = new File(pathSD + "/InstaA/");
            directory.mkdirs();
            //Nuevo archivo
            int consecutive = directory.listFiles().length;
            File newImage = new File(directory, "image" + (consecutive + 1) + ".jpg");
            System.out.println("Consecutivo: " + consecutive);


            fOut = new FileOutputStream(newImage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            System.out.println("Guardado");
            showMessage(R.string.imageSaved);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } finally {
            if ( fOut != null ){
                try {
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof FilterApplicator){
            displayImageDisplayer();
            mImageView = (ImageView) findViewById(R.id.imageViewContainer);
            bitmap = (Bitmap)arg;
            mImageView.setImageBitmap(bitmap);
        }
    }
}