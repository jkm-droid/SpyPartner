package com.spypartner.jmtechnologies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static android.os.Build.*;

/**
 * Created by JoshT on 01/02/2019.
 */

public class LoginUser extends AppCompatActivity {
    EditText editUsername,editPassword;
    Button register,login;
    TextView toRegisterPage;

    String username,password;
    SessionManager sessionManager;
    SQLiteHandler sqLiteHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);
        //ActionBar actionBar = getActionBar();
        /*actionBar.setBackgroundDrawable(new ColorDrawable(Color.GREEN));*/
        ActivityUtils activityUtils = new ActivityUtils();
        activityUtils.setActionBarColor(this,R.color.colorToolBar);

        editUsername = findViewById(R.id.username);
        editPassword = findViewById(R.id.password);

        register = findViewById(R.id.buttonRegister);
        login = findViewById(R.id.buttonLogin);
        toRegisterPage = findViewById(R.id.link_signup);
        toRegisterPage.setMovementMethod(LinkMovementMethod.getInstance());
        toRegisterPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RegisterUser.class);
                startActivity(intent);
                finish();
            }
        });

        // SQLite database handler
        sqLiteHandler = new SQLiteHandler(getApplicationContext());

        // Session manager
        sessionManager = new SessionManager(getApplicationContext());

        //check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginUser.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //onclick perform the login operations
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if device is connected to the internet
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())){
                    //CheckEditTextIsEmptyOrNot();
                    username = editUsername.getText().toString();
                    password = editPassword.getText().toString();

                    //validating user input
                    if (TextUtils.isEmpty(username)){
                        editUsername.setError("Please enter username!");
                        editUsername.requestFocus();
                        return;
                    }

                    if (TextUtils.isEmpty(password)){
                        editPassword.setError("Please enter password!");
                        editPassword.requestFocus();
                        return;
                    }

                    //all fields are filled
                    //login user method
                    loginUser();

                }else {
                    Toast toast = Toast.makeText(LoginUser.this, "Internet connectivity is required!!", Toast.LENGTH_SHORT);
                    View viewToast = toast.getView();
                    viewToast.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

                    TextView text = viewToast.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(R.color.white));
                    toast.show();
                }
            }
        });
        //onclick redirect the user to register user activity
      /*  register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginUser.this,RegisterUser.class);
                startActivity(intent);
                finish();
            }
        });*/
    }

    private void loginUser() {
        username = editUsername.getText().toString();
        password = editPassword.getText().toString();

        String type = "login";

        BackGroundTask backGroundTask = new BackGroundTask(this);
        backGroundTask.execute(type,username,password);

        //clear the edit texts
        editUsername.setText("");
        editPassword.setText("");
    }
    public class BackGroundTask extends AsyncTask<String,String,String> {
        private ProgressDialog progressDialog;
        private Context context;

        BackGroundTask(Context ctx){
            context = ctx;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //display dialog box while performing background task
            progressDialog = new ProgressDialog(context,R.style.progressDialogColor);
            progressDialog.setMessage("Authenticating...Please wait...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];

            if(type.equals("login")){
                String username = params[1];
                String password = params[2];

                String loginUrl = "https://www.mblog.co.ke/android/login.php";//online server url
                //String loginUrl = "http://10.0.2.2/spypartner-api/android-login.php";//localhost url
                //login user
                try {
                    URL url = new URL(loginUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    String charset = "UTF-8";
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                    String post_data = URLEncoder.encode("username",charset)+"="+URLEncoder.encode(username,charset)+"&"
                            +URLEncoder.encode("password",charset)+"="+URLEncoder.encode(password,charset);
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                    //check the servers status
                    int status = httpURLConnection.getResponseCode();
                    String result = "";

                    if (status == HttpURLConnection.HTTP_OK){
                        String line;
                        while((line = bufferedReader.readLine())!= null){
                            result+=line;
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                        System.out.println(result);
                    }else {
                        System.out.println("---------------------"+status+"---------------------------------");
                        throw new IOException("Servers response: "+status);
                    }

                    return result;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else{
                Toast.makeText(context, "Something went wrong!!", Toast.LENGTH_SHORT).show();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            System.out.println("-------------------------"+s+"-----------------------------------");
            String login = "Successfully logged in";


                if(login.equalsIgnoreCase(s)){
                    //create user session
                    sessionManager.setLogin(true);
                    sqLiteHandler.addLoginUserDetails(username,password);

                    //launch user area activity
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    Toast toast = Toast.makeText(context, s, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER,0,0);
                    View view = toast.getView();
                    view.getBackground().setColorFilter(getResources().getColor(R.color.colorToolBar),PorterDuff.Mode.SRC_IN);

                    TextView text = view.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(R.color.white));
                    toast.show();
                }
                else{
                    progressDialog.dismiss();
                    Toast toast = Toast.makeText(context, "Wrong Username/Password...Try Again", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    View view = toast.getView();
                    view.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

                    TextView text = view.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(R.color.white));
                    toast.show();
                }

        }
    }


    }
