package com.spypartner.jmtechnologies;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * Created by JoshT on 31/01/2019.
 */

public class RegisterUser extends AppCompatActivity {

    EditText editUsername,editEmail,editPassword;
    Button buttonRegister,buttonLogin;
    TextView toLoginPage;

    String username,email,password;

    SessionManager sessionManager;
    SQLiteHandler sqLiteHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        //changing the actionbar color
        ActivityUtils activityUtils = new ActivityUtils();
        activityUtils.setActionBarColor(this,R.color.colorToolBar);

        editUsername = findViewById(R.id.username);
        editEmail = findViewById(R.id.email);
        editPassword = findViewById(R.id.password);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        toLoginPage = findViewById(R.id.link_login);
        toLoginPage.setMovementMethod(LinkMovementMethod.getInstance());
        toLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LoginUser.class);
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
            Intent intent = new Intent(RegisterUser.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checking if device is connected to the internet
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())){
                    //getting user input
                    username = editUsername.getText().toString();
                    email = editEmail.getText().toString();
                    password = editPassword.getText().toString();

                    //make sure all fields are filled
                    if (TextUtils.isEmpty(username)){
                        editUsername.setError("Please enter username!");
                        editUsername.requestFocus();
                        return;
                    }

                    if (TextUtils.isEmpty(email)){
                        editEmail.setError("Please enter email!");
                        editEmail.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(password)){
                        editPassword.setError("Please enter password!");
                        editPassword.requestFocus();
                        return;
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        editEmail.setError("Enter valid email address");
                        editEmail.requestFocus();
                        return;
                    }

                    if (username.length() <= 4){
                        editUsername.setError("Username must be more than 5 characters");
                        editUsername.requestFocus();
                        return;
                    }
                        //if all fields are filled
                        //execute register user method
                        registerUser();
                }else{
                    Toast toast = Toast.makeText(RegisterUser.this, "Internet connectivity is required!!", Toast.LENGTH_LONG);
                    View viewToast = toast.getView();
                    viewToast.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

                    TextView text = viewToast.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(R.color.white));
                    toast.show();
                }
            }
        });
        //onclick redirect user to login page
       /* buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterUser.this,LoginUser.class);
                startActivity(intent);
                finish();
            }
        });*/
    }

    private void registerUser() {
        username = editUsername.getText().toString();
        email = editEmail.getText().toString();
        password = editPassword.getText().toString();

        String type = "register";
        BackGroundTask backGroundTask = new BackGroundTask(this);
        backGroundTask.execute(type,username,email,password);

        //clear all the edit texts
        editUsername.setText("");
        editEmail.setText("");
        editPassword.setText("");
    }

    public class BackGroundTask extends AsyncTask<String,String,String> {
        private ProgressDialog progressDialog;
        private Context context;
        Handler handler;

        BackGroundTask(Context ctx){
            context = ctx;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //progressDialog = ProgressDialog.show(context,"Loading Data...",null,true,true);
            progressDialog = new ProgressDialog(context,R.style.progressDialogColor);
            progressDialog.setMessage("Creating Account...Please wait...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
            //Toast.makeText(context, "Registering...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];

            String registerUrl = "https://www.mblog.co.ke/android/register.php";//online server url
            //String registerUrl = "http://10.0.2.2/spypartner-api/android-register.php";//localhost url

            if (type.equals("register")) {
                String username = params[1];
                String email = params[2];
                String password = params[3];
                //register user
                try {
                    URL url = new URL(registerUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    String charset = "UTF-8";
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("username", charset) + "=" + URLEncoder.encode(username, charset) + "&"
                            + URLEncoder.encode("email", charset) + "=" + URLEncoder.encode(email, charset) + "&"
                            + URLEncoder.encode("password", charset) + "=" + URLEncoder.encode(password, charset);
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    int status = httpURLConnection.getResponseCode();
                    String result = "";

                    if (status == HttpURLConnection.HTTP_OK){
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line;
                        }
                        bufferedReader.close();
                        inputStream.close();
                        httpURLConnection.disconnect();
                    }else {
                        System.out.println("-----------------------"+status+"-----------------");
                        throw new IOException("Servers response: "+status);
                    }

                    return result;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else{
                Toast.makeText(context, "Something went wrong!!", Toast.LENGTH_SHORT).show();
            }
            return null;

        }

        @SuppressLint("HandlerLeak")
        @Override
        protected void onPostExecute(String s) {
            String registered = "Successfully registered!";
            String usernameExists = "Username already exists";
            String emailExists = "Email already exists";
            if (registered.equals(s)){
            progressDialog.dismiss();
            System.out.println(s);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(),LoginUser.class);
                        startActivity(intent);
                        finish();
                    }
                });
                Toast toast = Toast.makeText(context, s, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                View view = toast.getView();
                view.getBackground().setColorFilter(getResources().getColor(R.color.colorToolBar), PorterDuff.Mode.SRC_IN);

                TextView text = view.findViewById(android.R.id.message);
                text.setTextColor(getResources().getColor(R.color.white));
                toast.show();
            } else if (usernameExists.equals(s)){
                progressDialog.dismiss();
                Toast toast = Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                View view = toast.getView();
                view.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

                TextView text = view.findViewById(android.R.id.message);
                text.setTextColor(getResources().getColor(R.color.white));
                toast.show();
            }else if (emailExists.equals(s)){
                progressDialog.dismiss();
                Toast toast = Toast.makeText(context, "Email already exists", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                View view = toast.getView();
                view.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

                TextView text = view.findViewById(android.R.id.message);
                text.setTextColor(getResources().getColor(R.color.white));
                toast.show();
            }else{
                progressDialog.dismiss();
                Toast toast = Toast.makeText(context, "An error occurred during registration", Toast.LENGTH_SHORT);
                View view = toast.getView();
                view.getBackground().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

                TextView text = view.findViewById(android.R.id.message);
                text.setTextColor(getResources().getColor(R.color.white));
                toast.show();
            }

        }
    }

}