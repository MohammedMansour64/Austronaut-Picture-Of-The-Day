package com.barmej.apod.Network;

import android.content.Context;
import android.os.AsyncTask;

import com.barmej.apod.APOD.APODInformation;
import com.barmej.apod.APOD.APODParser;
import com.barmej.apod.Constants;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class NetworkThread extends AsyncTask<Void, Integer , APODInformation>
{
    Context context;
    public NetworkThread(Context context) {
        this.context = context;
    }

    @Override
    protected APODInformation doInBackground(Void... urls) {

        URL completeURL = HttpUrlConnection.buildUrl(context , "/planetary/apod");
        try {
            String response = HttpUrlConnection.getResponseFromHttpURl(completeURL);
            Constants.apodInformation = APODParser.saveData(response);
            return Constants.apodInformation;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
