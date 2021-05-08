package com.e.nche.Email;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class Connection {


    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            isConnectDialog(context);
            return false;
        }
    }

    public boolean hasInternetAccess(Context context) {
        boolean result = false;

        Object response = null;
        try {
            response = new ConnectionInternet().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        try {
            String responseMessage = response.toString();

            if (responseMessage.equals("OK")) {
                Log.println(Log.ASSERT, "Connect", "Ok");
                result = true;
            } else {
                result = false;
                isInternetDialog(context);
            }
        } catch (NullPointerException ex) {
            isInternetDialog(context);
        }

        return result;
    }

    private void isConnectDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Oops .. !")
                .setMessage("You are not connected to internet.")
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void isInternetDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Oops .. !")
                .setMessage("You are connected but no internet.")
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public class ConnectionInternet extends AsyncTask {

        boolean result = false;
        String responseMessage;

        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://neverssl.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(2000);
                urlc.connect();
                responseMessage = urlc.getResponseMessage();
                result = (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.println(Log.ASSERT, "Connect", "Error checking internet connection " + e);
                result = false;
            }

            return responseMessage;
        }
    }
}
