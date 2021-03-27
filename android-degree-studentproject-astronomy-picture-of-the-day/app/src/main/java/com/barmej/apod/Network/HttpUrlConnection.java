package com.barmej.apod.Network;

import android.content.Context;
import android.net.Uri;

import com.barmej.apod.Constants;
import com.barmej.apod.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class HttpUrlConnection {

    private static final String BASE_URL = "https://api.nasa.gov";
    private static final String API_PARAM = "api_key";
    private static final String DATE_PARAM = "date";

    public static URL buildUrl(Context context , String endPoint){
        Uri.Builder uriBuilder = Uri.parse(BASE_URL + endPoint).buildUpon();
        Uri uri = uriBuilder
                .appendQueryParameter(API_PARAM , context.getString(R.string.apikey))
                .appendQueryParameter(DATE_PARAM , Constants.currentDate)
                .build();


        try {
            URL url = new URL(uri.toString());
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getResponseFromHttpURl(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();

        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");
            String response = null;
            boolean hasInput = scanner.hasNext();
            if (hasInput){
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            httpURLConnection.disconnect();
        }
    }
}
