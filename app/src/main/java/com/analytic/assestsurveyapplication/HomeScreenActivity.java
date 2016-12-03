package com.analytic.assestsurveyapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;

public class HomeScreenActivity extends AppCompatActivity {
    Button btnStartSurvey, btnSync;
    GPSTracker gps;
    TextView txvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
        btnStartSurvey = (Button) findViewById(R.id.btnStartSurvey);
        btnSync = (Button) findViewById(R.id.btnSync);
        txvCount = (TextView) findViewById(R.id.txvCount);

        gps = new GPSTracker(this);
        if (gps.getLocation() == null) {
            showSettingsAlert(this);
        }
        btnStartSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeScreenActivity.this, SurveyFormActivity.class));
            }
        });
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSync.setBackgroundColor(Color.parseColor("#00ff00"));
                btnSync.setEnabled(false);
//                HomeScreenActivity.this.runOnUiThread(new Runnable() {
                // public void run() {
                JSONObject jsob = null;
                ArrayList<JSONObject> jsobArr = new ArrayList<JSONObject>();
                try {
                    DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                    ArrayList<String> idList = dbhelper.getAllId();
                    for (int i = 0; i < idList.size(); i++) {
                        jsobArr.add(dbhelper.getJsonData("" + idList.get(i)));
                    }

                    for (int j = 0; j < jsobArr.size(); j++) {
                        if (jsobArr.get(j) != null) {
                            //if (j == (jsobArr.size() - 1)) {
                            sendData(jsobArr.get(j), idList.get(j), true);
                            //} else
                            //  sendData(jsobArr.get(j), idList.get(j), true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //  }
                // });
            }
        });

        try {
            DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
            txvCount.setText(dbhelper.getTotalRecords() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void callMoreRecords() {
        JSONObject jsob = null;
        ArrayList<JSONObject> jsobArr = new ArrayList<JSONObject>();
        try {
            DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
            ArrayList<String> idList = dbhelper.getAllId();
            for (int i = 0; i < idList.size(); i++) {
                jsobArr.add(dbhelper.getJsonData("" + idList.get(i)));
            }

            for (int j = 0; j < jsobArr.size(); j++) {
                if (jsobArr.get(j) != null) {
                    //if (j == (jsobArr.size() - 1)) {
                    sendData(jsobArr.get(j), idList.get(j), true);
                    //} else
                    //  sendData(jsobArr.get(j), idList.get(j), true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(JSONObject jsob, final String id, final boolean lastrecord) {
        String tag_json_obj = "json_obj_req";
        String url = ApplicationData.serviceURL + "FillSurvey";
        Log.e("url", url + "");
        final ProgressDialog mProgressDialog = new ProgressDialog(HomeScreenActivity.this);
        mProgressDialog.setTitle("");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(url, jsob, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.e("Login", jsonObject.toString());
                mProgressDialog.dismiss();

                try {
                    JSONObject object = jsonObject;
                    String msg = object.getString("msg");
                    if (msg.equalsIgnoreCase("success")) {
                        DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                        dbhelper.deleteRecords(id);
                        txvCount.setText(" " + dbhelper.getTotalRecords() + " ");
                        if (dbhelper.getTotalRecords() > 0) {
                            callMoreRecords();
                        }else{
                            btnSync.setEnabled(true);
                            btnSync.setBackgroundColor(Color.parseColor("#80000000"));
                            Toast.makeText(HomeScreenActivity.this, "Data sync successfully!", Toast.LENGTH_LONG).show();
                        }
                        //startActivity(new Intent(HomeScreenActivity.this, HomeScreenActivity.class));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("error in ", " :");
                    if (e instanceof TimeoutError || e instanceof NoConnectionError) {
                        Toast.makeText(HomeScreenActivity.this, "Please check your internet connection!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(HomeScreenActivity.this, "Something is wrong Please try again!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                VolleyLog.e("Sync Error", "Error: " + error.getMessage());
                error.getCause();
                error.printStackTrace();

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(HomeScreenActivity.this, "Please check your internet connection!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(HomeScreenActivity.this, "Something is wrong Please try again!", Toast.LENGTH_LONG).show();
                }
            }
        });


        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        ApplicationData.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

    public void showSettingsAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu for High Accuracy? After GPS Enable you need to reload page to get location.");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
            txvCount.setText(dbhelper.getTotalRecords() + "");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
