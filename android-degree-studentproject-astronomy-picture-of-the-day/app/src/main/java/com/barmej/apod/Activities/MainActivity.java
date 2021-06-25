package com.barmej.apod.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.barmej.apod.APOD.APODInformation;
import com.barmej.apod.APOD.APODParser;
import com.barmej.apod.Constants;
import com.barmej.apod.Network.NetworkThread;
import com.barmej.apod.R;
import com.barmej.apod.Tools.BitmapView;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import static com.barmej.apod.Constants.apodInformation;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;

    private TextView description;
    public TextView title;

    private TouchImageView image;
    private WebView webView;
    private ProgressBar progressBar;


    private String theLovedDateFormat = "";

    private int mDay;
    private int mMonth;
    private int mYear;

    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        description = findViewById(R.id.description);
        title = findViewById(R.id.title);
        image = findViewById(R.id.img_picture_view);
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.wv_video_player);


        WebSettings webSettings = webView.getSettings(); // enable javascript to show video.
        webSettings.setJavaScriptEnabled(true);

        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR); // current year
        mMonth = c.get(Calendar.MONTH); // current month
        mDay = c.get(Calendar.DAY_OF_MONTH); //current Day.

        // to request write data permission.
        if (ContextCompat.checkSelfPermission(MainActivity.this , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this ,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} , STORAGE_PERMISSION_CODE);
        }

        applyData();

    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (apodInformation.getMedia_typeInformation().equals("video")) {
            menu.getItem(1).setEnabled(false);
        } else if (apodInformation.getMedia_typeInformation().equals("image")){
            menu.getItem(1).setEnabled(true);
        }
        return true;
    }

    // menu options create
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return true;
    }
    // action for every option in the list
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_pick_day:
                datePicker();
                break;
            case R.id.action_download_hd:
                download();
                break;
            case R.id.action_about:
                Intent intent = new Intent(MainActivity.this, aboutActivity.class);
                startActivity(intent);
                break;
            case R.id.action_share:
                backgroundTask backgroundTask = new backgroundTask();
                backgroundTask.start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // function for making the date to the current day date and keep the chosen date next time opening the datePicker.
    public void datePicker()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth)
            {
                mYear = year;
                mMonth = month;
                mDay = dayOfMonth;

                theLovedDateFormat = year + "-" + (month + 1) + "-" + dayOfMonth;
                Constants.currentDate = theLovedDateFormat;
                applyData();
            }

        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    // obvious name
    public void download()
    {
        APODInformation downloadInformation = apodInformation;
        if (downloadInformation.getMedia_typeInformation().equals("image")){
            // Create request for android download manager
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadInformation.getHdUrlInformation()));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

            // set title and description
            request.setTitle("Astronomy Picture of the day");
            request.setDescription("Downloading Image...");

            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            //set the local destination for download file to a path within the application's external files directory
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloadfileName");
            request.setMimeType("image/*");
            downloadManager.enqueue(request);
        }else{
            Toast.makeText(this, "False!", Toast.LENGTH_SHORT).show();
        }
    }

    // function that takes the views and set Url's data on them.
    public void applyData(){
        NetworkThread networkThread = new NetworkThread(MainActivity.this);
        networkThread.execute();
        try {
            if (networkThread.get() != null){
                APODParser.apply(title , description , image ,webView ,progressBar , MainActivity.this);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // for sharing photo using background thread.
     class backgroundTask extends Thread
     {
        @Override
        public void run() {
            APODInformation apodInformation = Constants.apodInformation;
            if (apodInformation.getMedia_typeInformation().equals("image")){

                // convert a view into a bitmap
                BitmapView bitmapView = new BitmapView();
                Bitmap bitmap = bitmapView.getBitmapFromView(image);
                try {
                    // putting it into a file since 24+API doesn't allow to get Uri out of the application.
                    File photosDir = new File(getCacheDir() , "photos");
                    if (!photosDir.exists()){
                        photosDir.mkdirs();
                    }
                    File file = new File(photosDir, System.currentTimeMillis() + "." + "png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG , 100 , fOut);
                    fOut.flush();
                    fOut.close();
                    uri = FileProvider.getUriForFile(MainActivity.this , "com.barmej.apod" , file);
                    share();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
  }

    public void share()
    {
            // intent to share the image
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM , uri);
            intent.setType("image/png");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent , "Share image via"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        BitmapView bitmapView = new BitmapView();
        Bitmap bitmap = bitmapView.getBitmapFromView(image);
        outState.putParcelable("image", bitmap);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Bitmap bitmap = savedInstanceState.getParcelable("image");
        image.setImageBitmap(bitmap);
    }
}