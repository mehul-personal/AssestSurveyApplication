package com.analytic.assestsurveyapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SurveyFormActivity extends AppCompatActivity {
    TextView txvCompany, txvCircle, txvDivison, txvSubDivison, txvSubStation, txvFeeder, txvLineType;
    Spinner spnCompany, spnCircle, spnDivison, spnSubDivison, spnSubStation, spnFeeder, spnLineType;
    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        Locale locale = new Locale("gi");
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getApplicationContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_survey_form);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Survey Form");

        txvCompany = (TextView) findViewById(R.id.txvCompany);
        txvCircle = (TextView) findViewById(R.id.txvCircle);
        txvDivison = (TextView) findViewById(R.id.txvDivison);
        txvSubDivison = (TextView) findViewById(R.id.txvSubDivison);
        txvSubStation = (TextView) findViewById(R.id.txvSubStation);
        txvFeeder = (TextView) findViewById(R.id.txvFeeder);
        txvLineType = (TextView) findViewById(R.id.txvLineType);

        spnCompany = (Spinner) findViewById(R.id.spnCompany);
        spnCircle = (Spinner) findViewById(R.id.spnCircle);
        spnDivison = (Spinner) findViewById(R.id.spnDivison);
        spnSubDivison = (Spinner) findViewById(R.id.spnSubDivison);
        spnSubStation = (Spinner) findViewById(R.id.spnSubStation);
        spnFeeder = (Spinner) findViewById(R.id.spnFeeder);
        spnLineType = (Spinner) findViewById(R.id.spnLineType);

        btnNext = (Button) findViewById(R.id.btnNext);

        txvCompany.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvCircle.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvDivison.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvSubDivison.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvSubStation.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvFeeder.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvLineType.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));

        txvCompany.setText("કંપની");
        txvCircle.setText("સર્કલ");
        txvDivison.setText("ડિવિઝન");
        txvSubDivison.setText("સબ ડિવિઝન");
        txvSubStation.setText("સબસ્ટેશન");
        txvFeeder.setText("ફિડર");
        txvLineType.setText("લાઈન ટાઈપ");

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spnCompany.getSelectedItem().toString().equalsIgnoreCase("select company")) {
                    Toast.makeText(SurveyFormActivity.this, "Please Select Company", Toast.LENGTH_SHORT).show();
                } else if (spnCircle.getSelectedItem().toString() == null) {
                    Toast.makeText(SurveyFormActivity.this, "Please Select Circle", Toast.LENGTH_SHORT).show();
                } else if (spnDivison.getSelectedItem().toString() == null) {
                    Toast.makeText(SurveyFormActivity.this, "Please Select Division", Toast.LENGTH_SHORT).show();
                } else if (spnSubStation.getSelectedItem().toString() == null) {
                    Toast.makeText(SurveyFormActivity.this, "Please Select Substation", Toast.LENGTH_SHORT).show();
                } else if (spnLineType.getSelectedItem().toString().equalsIgnoreCase("Select Item")) {
                    Toast.makeText(SurveyFormActivity.this, "Please Line type", Toast.LENGTH_SHORT).show();
                } else if (!spnCompany.getSelectedItem().toString().equalsIgnoreCase("GETCO") &&
                        spnSubDivison.getSelectedItem().toString() == null) {
                    Toast.makeText(SurveyFormActivity.this, "Please Select Sub division", Toast.LENGTH_SHORT).show();
                } else if (!spnCompany.getSelectedItem().toString().equalsIgnoreCase("GETCO") &&
                        spnFeeder.getSelectedItem().toString() == null) {
                    Toast.makeText(SurveyFormActivity.this, "Please Select Feeder", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(SurveyFormActivity.this, SurveyFormSecondActivity.class);
                    i.putExtra("COMPANY", "" + spnCompany.getSelectedItem().toString());
                    i.putExtra("CIRCLE", "" + spnCircle.getSelectedItem().toString());
                    i.putExtra("DIVISION", "" + spnDivison.getSelectedItem().toString());
                    i.putExtra("SUBSTATION", "" + spnSubStation.getSelectedItem().toString());
                    if (!spnCompany.getSelectedItem().toString().equalsIgnoreCase("GETCO")) {
                        i.putExtra("SUBDIVISION", "" + spnSubDivison.getSelectedItem().toString());
                    }
                    i.putExtra("FEEDER", "" + spnFeeder.getSelectedItem().toString());
                    i.putExtra("LINETYPE", "" + spnLineType.getSelectedItem().toString());
                    startActivity(i);
                }
            }
        });

        spnCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    ArrayList<String> circleArray = new ArrayList<String>();
                    try {
                        DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                        dbhelper.createDatabase();
                        dbhelper.openDatabase(); ///storage/emulated/0/PTL Asset Survey Images/IMG20160622182742.jpg
                        SQLiteDatabase db = dbhelper.getDatabase();

                        Cursor cursor = db.rawQuery("select DISTINCT circle from " + spnCompany.getSelectedItem().toString() + " where company ='" + spnCompany.getSelectedItem().toString() + "'", null);
                        cursor.moveToFirst();
                        while (cursor.isAfterLast() == false) {
                            int circleID = cursor.getColumnIndex("circle");
                            circleArray.add(cursor.getString(circleID));
                            cursor.moveToNext();
                        }
                        cursor.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // String[] yourArray = getResources().getStringArray(R.array.CircleItemArray);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, circleArray);
                    spnCircle.setAdapter(dataAdapter);


                    if (spnCompany.getSelectedItem().toString().equalsIgnoreCase("GETCO")) {
                        String[] yourArray = getResources().getStringArray(R.array.GetcoLineTypeItemArray);
                        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(SurveyFormActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, yourArray);
                        spnLineType.setAdapter(dataAdapter1);
                    } else {
                        String[] yourArray11 = getResources().getStringArray(R.array.LineTypeItemArray);
                        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(SurveyFormActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, yourArray11);
                        spnLineType.setAdapter(dataAdapter1);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnCircle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ArrayList<String> divisionArray = new ArrayList<String>();
                try {
                    DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                    dbhelper.createDatabase();
                    dbhelper.openDatabase();
                    SQLiteDatabase db = dbhelper.getDatabase();

                    Cursor cursor = db.rawQuery("select DISTINCT division from " + spnCompany.getSelectedItem().toString() + " where circle ='" + spnCircle.getSelectedItem().toString() + "'", null);
                    cursor.moveToFirst();
                    while (cursor.isAfterLast() == false) {
                        int divisionID = cursor.getColumnIndex("division");
                        divisionArray.add(cursor.getString(divisionID));
                        cursor.moveToNext();
                    }
                    cursor.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }


                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, divisionArray);
                spnDivison.setAdapter(dataAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnDivison.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spnCompany.getSelectedItem().toString().equalsIgnoreCase("GETCO")) {
                    String[] yourArray = getResources().getStringArray(R.array.default_arr);

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, yourArray);
                    spnSubDivison.setAdapter(dataAdapter);
                } else {
                    ArrayList<String> subdivisionArray = new ArrayList<String>();
                    try {
                        DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                        dbhelper.createDatabase();
                        dbhelper.openDatabase();
                        SQLiteDatabase db = dbhelper.getDatabase();

                        Cursor cursor = db.rawQuery("select DISTINCT sub_division from " + spnCompany.getSelectedItem().toString() + " where division ='" + spnDivison.getSelectedItem().toString() + "' AND circle ='" + spnCircle.getSelectedItem().toString() + "'", null);
                        cursor.moveToFirst();
                        while (cursor.isAfterLast() == false) {
                            int subdivisionID = cursor.getColumnIndex("sub_division");
                            subdivisionArray.add(cursor.getString(subdivisionID));
                            cursor.moveToNext();
                        }
                        cursor.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, subdivisionArray);
                    spnSubDivison.setAdapter(dataAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnSubDivison.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ArrayList<String> subStationArray = new ArrayList<String>();
                try {
                    DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                    dbhelper.createDatabase();
                    dbhelper.openDatabase();
                    SQLiteDatabase db = dbhelper.getDatabase();
                    Cursor cursor;
                    if (spnCompany.getSelectedItem().toString().equalsIgnoreCase("GETCO"))
                        cursor = db.rawQuery("select DISTINCT sub_station from " + spnCompany.getSelectedItem().toString() + " where division ='" + spnDivison.getSelectedItem().toString() + "' AND circle ='" + spnCircle.getSelectedItem().toString() + "'", null);
                    else
                        cursor = db.rawQuery("select DISTINCT sub_station from " + spnCompany.getSelectedItem().toString() + " where sub_division ='" + spnSubDivison.getSelectedItem().toString() + "' AND division ='" + spnDivison.getSelectedItem().toString() + "' AND circle ='" + spnCircle.getSelectedItem().toString() + "'", null);
                    cursor.moveToFirst();
                    while (cursor.isAfterLast() == false) {
                        int subdivisionID = cursor.getColumnIndex("sub_station");
                        subStationArray.add(cursor.getString(subdivisionID));
                        cursor.moveToNext();
                    }
                    cursor.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, subStationArray);
                spnSubStation.setAdapter(dataAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnSubStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spnCompany.getSelectedItem().toString().equalsIgnoreCase("GETCO")) {
                    String[] yourArray = getResources().getStringArray(R.array.getco_feeder_array);

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, yourArray);
                    spnFeeder.setAdapter(dataAdapter);
                } else {
                    ArrayList<String> feederArray = new ArrayList<String>();
                    try {
                        DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                        dbhelper.createDatabase();
                        dbhelper.openDatabase();
                        SQLiteDatabase db = dbhelper.getDatabase();

                        Cursor cursor = db.rawQuery("select DISTINCT feeder from " + spnCompany.getSelectedItem().toString() + " where sub_station ='" + spnSubStation.getSelectedItem().toString() + "' AND sub_division ='" + spnSubDivison.getSelectedItem().toString() + "' AND division ='" + spnDivison.getSelectedItem().toString() + "' AND circle ='" + spnCircle.getSelectedItem().toString() + "'", null);
                        cursor.moveToFirst();
                        while (cursor.isAfterLast() == false) {
                            int feederID = cursor.getColumnIndex("feeder");
                            feederArray.add(cursor.getString(feederID));
                            cursor.moveToNext();
                        }
                        cursor.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, feederArray);
                    spnFeeder.setAdapter(dataAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
}
