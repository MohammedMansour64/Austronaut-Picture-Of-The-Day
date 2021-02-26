package com.barmej.apod;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(this);
        description = findViewById(R.id.explanation);
        title = findViewById(R.id.title);
        image = findViewById(R.id.img_picture_view);
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.wv_video_player);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        jsonParse();

        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR); // current year
        mMonth = c.get(Calendar.MONTH); // current month
        mDay = c.get(Calendar.DAY_OF_MONTH); //current Day.
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                share();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void jsonParse() {
        progressBar.setVisibility(View.VISIBLE);

        test();
        ifImageOrVideo();

    }


    public void changeDate(String chosenDate) {
        progressBar.setVisibility(View.VISIBLE);
        date = chosenDate;
        String finalURL = URL + date;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, finalURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            data = new Data(
                                    response.getString("title"),
                                    response.getString("explanation"),
                                    response.getString("url"),
                                    response.getString("media_type"));



                            ifImageOrVideo();
                            hdUrl = response.getString("hdurl");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);


    }

    public void datePicker() {
        datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {


                mYear = year;
                mMonth = month;
                mDay = dayOfMonth;

                theLovedDateFormat = year + "-" + (month + 1) + "-" + dayOfMonth;
                changeDate(theLovedDateFormat);
            }

        }, mYear, mMonth, mDay);

        datePickerDialog.show();


    }

    public void download() {
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
    }
    private void share(){
        Bitmap bitmap = getBitmapFromView(image);
        try {
            File file = new File(this.getExternalCacheDir() , "a.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG , 100 , fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true , false);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM , Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent , "Share image via"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Bitmap getBitmapFromView(View view){
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth() , view.getHeight() , Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable!=null){
            bgDrawable.draw(canvas);
        }else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }
    public void ifImageOrVideo(){
        if (data.getMediaType().equals("image")) {
            description.setText(data.getDescription());
            title.setText(data.getTitle());
            webView.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            webView.loadData("", "text/html", null);
            // i am using glide and picasso to double the speed
            Glide.with(MainActivity.this).load(data.getImageOrVideo()).thumbnail(0.005f).into(image);
            Picasso.with(MainActivity.this).load(data.getImageOrVideo()).into(image);
            progressBar.setVisibility(View.GONE);

        } else if (data.getMediaType().equals("video")) {
            description.setText(data.getDescription());
            title.setText(data.getTitle());
            progressBar.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            webView.loadUrl(data.getImageOrVideo());
            webView.setVisibility(View.VISIBLE);
        }else {
            webView.loadData("", "text/html", null);
            webView.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void test(){
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(MainActivity.this);
        myJsonObjectRequest.bringTheInformationFromJsonPlease(URL);

        webView.loadUrl(data.getImageOrVideo());
    }

}
