package com.barmej.apod;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.OneShotPreDrawListener;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.barmej.apod.Tools.BitmapView;
import com.bumptech.glide.Glide;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{

    private static final int STORAGE_PERMISSION_CODE = 101;

    private static final String URL = "https://api.nasa.gov/planetary/apod?api_key=YRLtBTu1hzCe6pCGMjHfSbQ2t1rapMM1xOF2Eobm&date=";
    private String date = "";
    private String hdUrl;

    private TextView description;
    private TextView title;

    private RequestQueue requestQueue;
    private TouchImageView image;
    private WebView webView;
    private ProgressBar progressBar;


    private String theLovedDateFormat = "";
    private DatePickerDialog datePickerDialog;
    private Data data;

    private int mDay;
    private int mMonth;
    private int mYear;

    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        description = findViewById(R.id.explanation);
        title = findViewById(R.id.title);
        image = findViewById(R.id.img_picture_view);
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.wv_video_player);

        requestQueue = Volley.newRequestQueue(this);

        WebSettings webSettings = webView.getSettings(); // enable javascript to show video.
        webSettings.setJavaScriptEnabled(true);
        jsonParse();

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

    }
    // menu options create
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
    // action for every option
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

    private void jsonParse()
    {
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            data = new Data(
                                    response.getString("title"),
                                    response.getString("explanation"),
                                    response.getString("url"),
                                    response.getString("media_type"));

                            ifImageOrVideo();
                            hdUrl = response.getString("hdurl");

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);

    }


    // function to change data after choosing a date.
    public void changeDate(String chosenDate)
    {
        progressBar.setVisibility(View.VISIBLE);
        date = chosenDate;
        String finalURL = URL + date;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, finalURL, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            data = new Data(
                                    response.getString("title"),
                                    response.getString("explanation"),
                                    response.getString("url"),
                                    response.getString("media_type"));



                            ifImageOrVideo();
                            hdUrl = response.getString("hdurl");

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);


    }

    // function for making the date to the current day date and keep the chosen date next time opening the datePicker.
    public void datePicker()
    {
        datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener()
        {

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth)
            {


                mYear = year;
                mMonth = month;
                mDay = dayOfMonth;

                theLovedDateFormat = year + "-" + (month + 1) + "-" + dayOfMonth;
                changeDate(theLovedDateFormat);
            }

        }, mYear, mMonth, mDay);

        datePickerDialog.show();


    }

    // obvious name
    public void download()
    {
        if (data.getMediaType().equals("image")){
            // Create request for android download manager
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(hdUrl));
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
            Toast.makeText(this, "Download Service is only for Images!", Toast.LENGTH_SHORT).show();
        }
    }


    // change the views if image or video.
    public void ifImageOrVideo()
    {
        if (data.getMediaType().equals("image"))
        {
            description.setText(data.getDescription());
            title.setText(data.getTitle());

            webView.loadData("", "text/html", null);
            webView.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);

            // i am using glide and picasso to double the speed
            Glide.with(MainActivity.this).load(data.getImageOrVideo()).thumbnail(0.005f).into(image);
            Picasso.with(MainActivity.this).load(data.getImageOrVideo()).into(image);

            progressBar.setVisibility(View.GONE);

        }
        else if (data.getMediaType().equals("video"))
        {
            description.setText(data.getDescription());
            title.setText(data.getTitle());

            progressBar.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            webView.loadUrl(data.getImageOrVideo());
            webView.setVisibility(View.VISIBLE);
        }
        else
        {
            webView.loadData("", "text/html", null);
            webView.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    // for sharing photo using background thread.
     class backgroundTask extends Thread
     {
        @Override
        public void run() {
            if (data.getMediaType().equals("image")){

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
            } else {
                Toast.makeText(MainActivity.this, "Share Service is only for Images!", Toast.LENGTH_SHORT).show();
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

}