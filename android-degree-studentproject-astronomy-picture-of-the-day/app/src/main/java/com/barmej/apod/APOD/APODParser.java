package com.barmej.apod.APOD;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.barmej.apod.Constants;
import com.barmej.apod.R;
import com.bumptech.glide.Glide;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class APODParser {
    private static final String APOD_DATE = "date";
    private static final String APOD_EXPLANATION = "explanation";
    private static final String APOD_MEDIA_TYPE = "media_type";
    private static final String APOD_URL = "url";
    private static final String APOD_VERSION = "service_version";
    private static final String APOD_TITLE = "title";
    private static final String APOD_HDURL = "hdurl";

    public static APODInformation saveData(String httpResponse) throws JSONException {
        APODInformation apodInformation = new APODInformation();
        JSONObject jsonObject = new JSONObject(httpResponse);
        if (jsonObject.getString(APOD_MEDIA_TYPE).equals("video")){
                apodInformation.setMedia_typeInformation(jsonObject.getString(APOD_MEDIA_TYPE));
                apodInformation.setDateInformation(jsonObject.getString(APOD_DATE));
                apodInformation.setUrlInformation(jsonObject.getString(APOD_URL));
                apodInformation.setDescriptionInformation(jsonObject.getString(APOD_EXPLANATION));
                apodInformation.setService_versionInformation(jsonObject.getString(APOD_VERSION));
                apodInformation.setTitleInformation(jsonObject.getString(APOD_TITLE));
                apodInformation.setService_versionInformation(jsonObject.getString(APOD_VERSION));
        }else {
            apodInformation.setMedia_typeInformation(jsonObject.getString(APOD_MEDIA_TYPE));
            apodInformation.setDateInformation(jsonObject.getString(APOD_DATE));
            apodInformation.setUrlInformation(jsonObject.getString(APOD_URL));
            apodInformation.setDescriptionInformation(jsonObject.getString(APOD_EXPLANATION));
            apodInformation.setService_versionInformation(jsonObject.getString(APOD_VERSION));
            apodInformation.setTitleInformation(jsonObject.getString(APOD_TITLE));
            apodInformation.setService_versionInformation(jsonObject.getString(APOD_VERSION));
            apodInformation.setHdUrlInformation(jsonObject.getString(APOD_HDURL));
        }

        return apodInformation;
    }

    public static void apply(TextView title, TextView description, TouchImageView imageView, WebView webview, ProgressBar progressBar, Context context){
        APODInformation apodInformation = Constants.apodInformation;
        System.out.println("APODParser"+apodInformation.getTitleInformation());
        progressBar.setVisibility(View.VISIBLE);
        if (Constants.apodInformation != null){
                title.setText(apodInformation.getTitleInformation());
                description.setText(apodInformation.getDescriptionInformation());

                if (apodInformation.getMedia_typeInformation().equals("image")){
                    imageView.setVisibility(View.VISIBLE);

                    // i use glide and picasso for double speed
                    Glide.with(context).load(apodInformation.getUrlInformation()).thumbnail(0.005f).into(imageView);
                    Picasso.with(context).load(apodInformation.getUrlInformation()).error(R.drawable.ic_launcher_background).into(imageView);
                    webview.setVisibility(View.GONE);
                    webview.loadData("", "text/html", null);
                    progressBar.setVisibility(View.GONE);

                } else {
                    imageView.setVisibility(View.GONE);
                    webview.loadUrl(apodInformation.getUrlInformation());
                    webview.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                }

        }else{
            Toast.makeText(context, "Hi", Toast.LENGTH_SHORT).show();
        }
    }

}
