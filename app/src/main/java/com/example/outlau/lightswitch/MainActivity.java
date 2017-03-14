package com.example.outlau.lightswitch;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;

import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import java.text.DateFormat;
import java.util.Calendar;


import android.app.AlertDialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.ToggleButton;

import java.util.Hashtable;
import java.util.Set;
import java.util.Enumeration;
import java.util.zip.Inflater;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.view.MotionEvent;


import com.triggertrap.seekarc.SeekArc;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.outlau.lightswitch.R;

import org.w3c.dom.Text;


//TODO set output to 0 at server startup

public class MainActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    String changeStateURL = "http://192.168.0.69/execpy.php?request=";

    Hashtable<String, Integer> buttonIDs = new Hashtable<String, Integer>();
    String[] buttonName = {"Lights", "TV"};
    int[] buttonKey = {R.id.Lights, R.id.TV};

    AlertDialog.Builder connectingAlert;
    AlertDialog connectingAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        connectingAlert = new AlertDialog.Builder(this);
        connectingAlert.setTitle("Connecting to server");
        connectingAlert.setMessage(Globals.errormsgs[Globals.errorMsgCount]);
        connectingAlert.setCancelable(false);
        connectingAlertDialog = connectingAlert.create();

        connectingAlertBuilder(false);

        setSupportActionBar(toolbar);
        for (int i = 0; i < buttonName.length; i++){

        }


        if (buttonName.length == buttonKey.length) {
            for (int i = 0; i < buttonName.length; i++) {
                buttonIDs.put(buttonName[i], buttonKey[i]);
            }
        } else {
            //Gotta put the same amount of button names as keys
        }

        Switch button = (Switch) findViewById((R.id.Lights));

        sendRequest(false, false, true, button);

        final Handler updateHandler = new Handler();

        final Runnable r = new Runnable() {
            Switch button = (Switch) findViewById(R.id.Lights); //must start with some button - Lights is is arbitrary


            public void run() {
                sendRequest(false, false, true, button);
                updateHandler.postDelayed(this, 10000);

            }
        };
        updateHandler.postDelayed(r, 10000);


    }

    public void changeState(View view) {
        Switch button = (Switch) findViewById(view.getId());
        sendRequest(false, button.isChecked(), false, button);

        TextView thisText = (TextView) findViewById(view.getId()+3);

        if (!Globals.isTimerSet) {
            if (button.isChecked() == true) {
                thisText.setText("Set a time to turn off this appliance");
            } else {
                thisText.setText("Set a time to turn on this appliance");
            }
        }
    }


    public void connectingAlertBuilder(boolean isConnected){
        if(!isConnected) {
            connectingAlert.setTitle("Connecting to server");
            Globals.errorMsgCount++;
            Globals.errorMsgCount %= Globals.errormsgs.length;
            connectingAlert.setMessage(Globals.errormsgs[Globals.errorMsgCount]);
            connectingAlert.setCancelable(false);

            connectingAlertDialog.show();
        }
        else{
            connectingAlertDialog.dismiss();
        }
    }

    public void refreshButtons(String GETresponse) {

        String[] split = GETresponse.split("/");

        Hashtable<String, Integer> buttonAndState = new Hashtable<String, Integer>();

        if (split.length / 2 == buttonName.length) {


            for (int i = 0; i < split.length; i += 2) {

                buttonAndState.put(split[i], Integer.parseInt(split[i + 1]));

                if (buttonAndState.get(split[i]) != null) {

                    Switch tempButtonID = (Switch) findViewById(buttonIDs.get(split[i]));

                    if (buttonAndState.get(split[i]) == 0) {

                        tempButtonID.setChecked(false);

                    } else if (buttonAndState.get(split[i]) == 1) {

                        tempButtonID.setChecked(true);

                    }

                }
            }
        } else {
            //You gotta have an equal number of buttons as lights
        }
    }





    public void sendRequest(final boolean get, boolean isChecked, final boolean refresh, final Switch thisButton) {
        requestQueue = Volley.newRequestQueue(MainActivity.this);

        final String buttonID = getResources().getResourceEntryName(thisButton.getId());

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                changeStateURL + (refresh ? "refresh" : get ? "" : isChecked ? "turnOn" : "turnOff") + "/" + buttonID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (refresh) {
                            refreshButtons(response);
                            System.out.println("Get refresh response : " + response);
                        } else {
                            if (response == "turnOn") {
                                //turnOn protocol
                            }
                        }
                        connectingAlertBuilder(true);
                        requestQueue.stop();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                connectingAlertBuilder(false);
                requestQueue.stop();
            }

        });
        requestQueue.add(stringRequest);
    }


    //UI - open timer view
    public void openTimer(final View view){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        //set the top text of alert box
        TextView title = new TextView(this);
        title.setText("HOW MUCH MINS?");
        title.setPadding(40,40,0,0);

        //set a frame layout to add views to
        FrameLayout layout = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);
        layout.setPadding(2, 2, 2, 2);

        //TODO set time to server time

        /*
         * HOURS AND MINUTES
         */

        //Get hours from system
        String hourTimePattern = "HH";
        SimpleDateFormat hourTimeFormat = new SimpleDateFormat(hourTimePattern);
        int hour = Integer.parseInt(hourTimeFormat.format(new Date()));
        String hourTime = hourTimeFormat.format(new Date());

        //Get minutes from system
        String minuteTimePattern = "mm";
        SimpleDateFormat minuteTimeFormat = new SimpleDateFormat(minuteTimePattern);
        int minute = Integer.parseInt(minuteTimeFormat.format(new Date()));
        String minuteTime = minuteTimeFormat.format(new Date());

        //Set time to time textview
        final TextView timeText = new TextView(this);
        timeText.setText(hourTime+":"+minuteTime);
        timeText.setGravity(Gravity.CENTER);
        timeText.setPadding(0,0,0,50);

        /*
         * DATE
         */

        //Get date from system
        String datePattern = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        String date = dateFormat.format(new Date());

        //Set date to date textview
        final TextView dateText = new TextView(this);
        dateText.setText(date);
        dateText.setGravity(Gravity.CENTER);
        dateText.setPadding(0,50,0,0);

        //Add time views to layout
        layout.addView(timeText);
        layout.addView(dateText);

        View tv = (View) getLayoutInflater().inflate(R.layout.constructors, layout, true);
        final SeekArc arc = (SeekArc) tv.findViewById(R.id.seekArc);

        //ON CREATE SET TO TIME
        arc.setProgress((hour%12)*30 + minute/2);

        alert.setCustomTitle(title);
        alert.setView(layout);
        alert.setCancelable(true);

        if (hour >= 12){
            Globals.isPM = 1;
        }
        else{
            Globals.isPM = 0;
        }

        Globals.calendar = Calendar.getInstance();
        Globals.startupCalendar = Calendar.getInstance();
        Globals.hour = hour;
        Globals.minute = minute;
        Globals.startingProgress = arc.getProgress();

        arc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String time = timeFormat.format(Globals.calendar.getTime());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String date = dateFormat.format(Globals.calendar.getTime());
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                int curMins = seekArc.getProgress();

                double hours = Math.floor(12*curMins/360);
                double minutes = 2*curMins % 60;

                Globals.isPM += arc.getChangeSignState() + 2;
                Globals.isPM %= 2;

                if (Globals.isPM == 1){
                    hours += 12;
                }
                if(hours > 13 || hours <11 ){
                    Globals.calendar.add(Calendar.DAY_OF_YEAR, arc.getChangeSignState());

                }

                Globals.calendar.set(Calendar.HOUR_OF_DAY,(int)hours);
                Globals.calendar.set(Calendar.MINUTE,(int)minutes);

                try{

                    SimpleDateFormat dateFormatConst = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    String curDateStr = dateFormatConst.format(Globals.calendar.getTime());
                    String dateConstStr = dateFormatConst.format(Globals.startupCalendar.getTime());

                    Date curDate = dateFormatConst.parse(curDateStr);
                    Date dateConst = dateFormatConst.parse(dateConstStr);

                    if (curDate.compareTo(dateConst)<0){
                        //TODO eliminate preceeding seekarc - right now the arc before startHour is visible - dont want that
                        Globals.isBelow = true;
                    }
                    else{
                        Globals.isBelow = false;
                    }


                }catch (ParseException e1){
                    e1.printStackTrace();
                }

                time = timeFormat.format(Globals.calendar.getTime());
                date = dateFormat.format(Globals.calendar.getTime());

                timeText.setText(time.toString());
                dateText.setText(date.toString());

            }


            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                arc.setPositive(arc.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                if(Globals.isBelow) {

                    arc.setProgress(Globals.startingProgress);
                    time = timeFormat.format(Globals.startupCalendar.getTime());
                    date = dateFormat.format(Globals.startupCalendar.getTime());

                    timeText.setText(time.toString());
                    dateText.setText(date.toString());
                    Globals.isBelow = false;
                }
            }
        });



        // Setting Negative "Cancel" Button
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        // Setting Positive "OK" Button
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {


                Switch thisSwitch = (Switch) findViewById(view.getId() - 3);

                try{


                    SimpleDateFormat dateFormatConst = new SimpleDateFormat("HH:mm dd/MM/yyyy");

                    String curDateStr = dateFormatConst.format(Globals.calendar.getTime());
                    String dateConstStr = dateFormatConst.format(Globals.startupCalendar.getTime());

                    Date curDate = dateFormatConst.parse(curDateStr);
                    Date dateConst = dateFormatConst.parse(dateConstStr);

                    TextView changeStateTime = (TextView) findViewById(view.getId());

                    changeStateTime.setGravity(Gravity.CENTER);
                    changeStateTime.setPadding(0,5,5,5);

                    if (curDate.compareTo(dateConst)<=0) {
                        changeStateTime.setText("Silly rabbit! That's right now.");
                    }

                    else{
                        changeStateTime.setText("This appliance will turn " + (thisSwitch.isChecked() ? "off" : "on") + " at " + curDateStr.toString());

                        long timeSinceEpoch = Globals.calendar.getTime().getTime()/1000;

                        final String buttonID = getResources().getResourceEntryName(thisSwitch.getId());

                        requestQueue = Volley.newRequestQueue(MainActivity.this);

                        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                                "http://192.168.0.69/exec_at_time.php?time=" + timeSinceEpoch + "&state=" + (thisSwitch.isChecked() ? 0 : 1) + "&ID=" + buttonID,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        System.out.println(response);

                                        requestQueue.stop();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println("error");
                                requestQueue.stop();
                            }

                        });
                        requestQueue.add(stringRequest);
                    }
                }catch (ParseException e1){
                    e1.printStackTrace();
                }
            }
        });

        AlertDialog alertDialog = alert.create();

        try {
            alertDialog.show();
        } catch (Exception e) {
            // WindowManager$BadTokenException will be caught and the app would
            // not display the 'Force Close' message
            e.printStackTrace();
        }
    }

    public void showTable(View view) {

        TableRow buttonTableRow = (TableRow) findViewById(view.getId() - 1); // table row that contains button pressed to open table
        ToggleButton thisButton = (ToggleButton) findViewById(view.getId()); // the button that is pressed
        final Switch thisSwitch = (Switch) findViewById(view.getId() + 1);         // the switch that toggles the lights - relative to the button
        TableRow thisTableRow = (TableRow) findViewById(view.getId() + 2);   // tablerow that contains the text view that is pressed
        TableLayout thisTable = (TableLayout) findViewById(view.getId() + 3); //+3 : the table is the 3rd element after toggleButton
        final TextView setTimerText = (TextView) findViewById(view.getId() + 4);  // text that show the timer


        //FOR TESTING
        /*
        System.out.println("thisButton:  " + view.getId());
        System.out.println("Switch:  " + R.id.LED_green);
        System.out.println("thisTable:  " + R.id.LED_green_table);
        System.out.println("thisTableRow:  " + R.id.LED_green_table_row);
        System.out.println("setTimerText:  " + R.id.LED_green_timer);
        */

        TableLayout parentTable = (TableLayout) findViewById(R.id.lights_table);

        for(int i = 0; i<parentTable.getChildCount(); i++){
            TableRow tempRow = (TableRow) parentTable.getChildAt(i);
            if (i%2 == 0) {
                if (tempRow.getId() != buttonTableRow.getId()) {
                    ToggleButton tempButton = (ToggleButton) tempRow.getChildAt(0);
                    tempButton.setChecked(false);
                }
            }
            else{
                tempRow.setVisibility(View.GONE);
            }
        }

        final String buttonID = getResources().getResourceEntryName(thisSwitch.getId());

        requestQueue = Volley.newRequestQueue(MainActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                "http://192.168.0.69/exec_at_time.php?ID=" + buttonID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] split = response.split("/"); //[ID - TIME - STATE]
                        if (Long.parseLong(split[1]) > 0) {

                            Globals.isTimerSet = true;
                            SimpleDateFormat thisDateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy ");

                            String thisDate = thisDateFormat.format(Long.parseLong(split[1]) * 1000);
                            setTimerText.setText("This appliance will turn " + (Integer.parseInt(split[2]) == 1 ? "on" : "off") + " at " + thisDate);
                        }
                        else{
                            Globals.isTimerSet = false;

                            //TODO make text switch when row is visible and appliance turns on/off on time
                            if (thisSwitch.isChecked() == true) {
                                setTimerText.setText("Set a time to turn off this appliance");
                            } else {
                                setTimerText.setText("Set a time to turn on this appliance");
                            }
                        }
                        System.out.println(response);

                        requestQueue.stop();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error");
                requestQueue.stop();
            }

        });
        requestQueue.add(stringRequest);

        if(!Globals.isTimerSet) {

            if (thisSwitch.isChecked() == true) {
                setTimerText.setText("Set a time to turn off this appliance");
            } else {
                setTimerText.setText("Set a time to turn on this appliance");
            }
        }

        if (thisButton.isChecked()) {
            thisTableRow.setVisibility(View.VISIBLE);
        } else {
            thisTableRow.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();


    }
}

