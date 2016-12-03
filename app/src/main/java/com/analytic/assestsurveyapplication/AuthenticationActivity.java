package com.analytic.assestsurveyapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class AuthenticationActivity extends AppCompatActivity {
    EditText edtName, edtPhone;
    Button btnSubmit;
    Spinner spnCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Authentication");

        edtName = (EditText) findViewById(R.id.edtName);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        spnCompany = (Spinner) findViewById(R.id.spnCompany);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtName.getText().toString().isEmpty()) {
                    Toast.makeText(AuthenticationActivity.this, "Please Enter Name", Toast.LENGTH_LONG).show();
                } else if (edtPhone.getText().toString().isEmpty()) {
                    Toast.makeText(AuthenticationActivity.this, "Please Enter Phone", Toast.LENGTH_LONG).show();
                } else if (spnCompany.getSelectedItem().toString() == null || spnCompany.getSelectedItem().toString().equalsIgnoreCase("Select Company")) {
                    Toast.makeText(AuthenticationActivity.this, "Please Select Company", Toast.LENGTH_LONG).show();
                } else {
                    sendData(spnCompany.getSelectedItem().toString(), edtPhone.getText().toString(), edtName.getText().toString());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sendData(final String companyname, final String phone, final String username) {
        String tag_json_obj = "json_obj_req";
        String url = ApplicationData.serviceURL + "UserReg";
        Log.e("url", url + "");
        final ProgressDialog mProgressDialog = new ProgressDialog(AuthenticationActivity.this);
        mProgressDialog.setTitle("");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.show();
        JSONObject jsob = null;
        try {
            jsob = new JSONObject();
            jsob.put("IMEI", "" + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            jsob.put("company_name", "" + companyname);
            jsob.put("phone", "" + phone);
            jsob.put("username", "" + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(url, jsob, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.e("Login", jsonObject.toString());
                mProgressDialog.dismiss();

                try {
                    JSONObject object = jsonObject;
                    String msg = object.getString("msg");
                    if (msg.equalsIgnoreCase("success")) {
                        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN_DETAIL", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("USER_ID", object.getString("userid"));
                        editor.commit();
                        startActivity(new Intent(AuthenticationActivity.this, HomeScreenActivity.class));
                    } else {
                       /* DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                        int userid = dbhelper.serachUserID(username, companyname, phone);
                        if (userid > 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("LOGIN_DETAIL", 0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("USER_ID", userid + "");
                            editor.commit();
                            startActivity(new Intent(AuthenticationActivity.this, HomeScreenActivity.class));
                        } else*/
                            Toast.makeText(AuthenticationActivity.this, "Your Authentication Failure Please Check Your Credentials!", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("error in ", " :");
                   /* try {
                        DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                        int userid = dbhelper.serachUserID(username, companyname, phone);
                        if (userid > 0) {
                            SharedPreferences sharedPreferences = getSharedPreferences("LOGIN_DETAIL", 0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("USER_ID", userid + "");
                            editor.commit();
                            startActivity(new Intent(AuthenticationActivity.this, HomeScreenActivity.class));
                        } else
                            Toast.makeText(AuthenticationActivity.this, "Your Authentication Failure Please Check Your Credentials!", Toast.LENGTH_LONG).show();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }*/
//                    if (e instanceof TimeoutError || e instanceof NoConnectionError) {
//                        Toast.makeText(AuthenticationActivity.this, "Please check your internet connection!", Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(AuthenticationActivity.this, "Something is wrong Please try again!", Toast.LENGTH_LONG).show();
//                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                VolleyLog.e("Login Error", "Error: " + error.getMessage());
                error.getCause();
                error.printStackTrace();

               /* try {
                    DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                    int userid = dbhelper.serachUserID(username, companyname, phone);
                    if (userid > 0) {
                        SharedPreferences sharedPreferences = getSharedPreferences("LOGIN_DETAIL", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("USER_ID", userid + "");
                        editor.commit();
                        startActivity(new Intent(AuthenticationActivity.this, HomeScreenActivity.class));
                    } else
                        Toast.makeText(AuthenticationActivity.this, "Your Authentication Failure Please Check Your Credentials!", Toast.LENGTH_LONG).show();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }*/
            }
        });


        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        ApplicationData.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

}
