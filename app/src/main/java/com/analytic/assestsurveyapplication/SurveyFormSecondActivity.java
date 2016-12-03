package com.analytic.assestsurveyapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SurveyFormSecondActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;
    static String strCompany = "", strCircle = "", strDivision = "", strSubDivision = "", strSubStation = "", strFeeder = "", strLineType = "";
    static String selectedImage = "";
    EditText edtPoleId, edtPreviousPoleId, edtLatitude, edtLongitude, edtPanchayatName;
    Button btnSelectImage, btnSaveAndContinue, btnExit;
    TextView txvPoleType, txvPoleID, txvPreviousPoleId, txvIsDivideLine, txvBarrier, txvIsPanchayatLastPole, txvPanchayatName;
    Spinner spnPoleType, spnBarrier;
    GPSTracker gps;
    RadioButton divideLineYes, divideLineNo, rdbLastPoleYes, rdbLastPoleNo;
    private Uri mFileUri;


    public  Uri getOutputMediaFile(int type) {


        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),"PTLAssetSurvey" );

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("survey form second", "could not create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File mediaFile;
        if (type == 1) {
            String imageStoragePath = mediaStorageDir + "/Images/";
            createDirectory(imageStoragePath);
            mediaFile = new File(imageStoragePath + "IMG" + timeStamp + ".jpg");

        } else if (type == 2) {
            String videoStoragePath = mediaStorageDir + "/Videos/";
            createDirectory(videoStoragePath);
            mediaFile = new File(videoStoragePath + "VID" + timeStamp + ".mp4");

        } else {
            return null;
        }
        return Uri.fromFile(mediaFile);
    }

    public static void createDirectory(String filePath) {
        if (!new File(filePath).exists()) {
            new File(filePath).mkdirs();
        }
    }

    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }

        }// MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_form_second);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Survey Form");

        edtPoleId = (EditText) findViewById(R.id.edtPoleId);
        edtPreviousPoleId = (EditText) findViewById(R.id.edtPreviousPoleId);
        edtLatitude = (EditText) findViewById(R.id.edtLatitude);
        edtLongitude = (EditText) findViewById(R.id.edtLongitude);
        edtPanchayatName = (EditText) findViewById(R.id.edtPanchayatName);
        txvIsDivideLine = (TextView) findViewById(R.id.txvIsDivideLine);
        txvBarrier = (TextView) findViewById(R.id.txvBarrier);
        txvIsPanchayatLastPole = (TextView) findViewById(R.id.txvIsPanchayatLastPole);
        txvPanchayatName = (TextView) findViewById(R.id.txvPanchayatName);

        btnSelectImage = (Button) findViewById(R.id.btnSelectImage);
        btnSaveAndContinue = (Button) findViewById(R.id.btnSaveAndContinue);
        btnExit = (Button) findViewById(R.id.btnExit);

        spnPoleType = (Spinner) findViewById(R.id.spnPoleType);
        spnBarrier = (Spinner) findViewById(R.id.spnBarrier);
        divideLineYes = (RadioButton) findViewById(R.id.rdbYes);
        divideLineNo = (RadioButton) findViewById(R.id.rdbNo);
        rdbLastPoleYes = (RadioButton) findViewById(R.id.rdbLastPoleYes);
        rdbLastPoleNo = (RadioButton) findViewById(R.id.rdbLastPoleNo);

        txvPoleType = (TextView) findViewById(R.id.txvPoleType);
        txvPoleID = (TextView) findViewById(R.id.txvPoleID);
        txvPreviousPoleId = (TextView) findViewById(R.id.txvPreviousPoleId);

        gps = new GPSTracker(this);
       /* if (gps.getLocation() == null) {
            showSettingsAlert(this);
        }*/
        selectedImage = "";
        Intent i = getIntent();
        strCompany = i.getStringExtra("COMPANY");
        strCircle = i.getStringExtra("CIRCLE");
        strDivision = i.getStringExtra("DIVISION");
        strSubDivision = i.getStringExtra("SUBDIVISION");
        strSubStation = i.getStringExtra("SUBSTATION");
        strFeeder = i.getStringExtra("FEEDER");
        strLineType = i.getStringExtra("LINETYPE");

        edtPoleId.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        edtPreviousPoleId.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvPoleType.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvPoleID.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvPreviousPoleId.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvIsDivideLine.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvIsPanchayatLastPole.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvBarrier.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));
        txvPanchayatName.setTypeface(Typeface.createFromAsset(getAssets(), "shruti.TTF"));

        edtPoleId.setHint("પેાલ  નં");
        edtPreviousPoleId.setHint("પાછલેા  પેાલ  નં");
        txvPoleType.setText("પેાલ  ટાઈપ");
        txvPoleID.setText("પેાલ  નં");
        txvPreviousPoleId.setText("પાછલેા  પેાલ  નં");
        txvIsDivideLine.setText("અહીંયા લાઈન છુટી પડે છે.");
        txvBarrier.setText("ખાસ જરુરીયાત");
        txvIsPanchayatLastPole.setText("પંચાયત થી છેલ્લો પાેલ");
        txvPanchayatName.setText("પંચાયત નુ નામ");

        edtPoleId.setEnabled(false);
        //edtPreviousPoleId.setEnabled(false);
        try {
            DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
            int poleid = dbhelper.serachPoleID(strCompany, strFeeder);

            if (poleid == 0) {
                edtPoleId.setText("00001");
                edtPreviousPoleId.setText("00000");
            } else {
                int count = poleid;
                edtPreviousPoleId.setText(String.format("%04d", count));
                count = count + 1;
                edtPoleId.setText(String.format("%04d", count));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (strCompany.equalsIgnoreCase("UGVCL") ||
                strCompany.equalsIgnoreCase("PGVCL") ||
                strCompany.equalsIgnoreCase("MGVCL") ||
                strCompany.equalsIgnoreCase("DGVCL")) {

            ArrayList<String> poleTypeArray = new ArrayList<String>();
            poleTypeArray.add("PSC Pole");
            poleTypeArray.add("Girder Pole");
            poleTypeArray.add("Rail Pole");
            poleTypeArray.add("Spun Concrete Pole");
            poleTypeArray.add("Round MS Pole");
            poleTypeArray.add("T/C");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormSecondActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, poleTypeArray);
            spnPoleType.setAdapter(dataAdapter);

        } else if (strCompany.equalsIgnoreCase("GETCO")) {
            ArrayList<String> poleTypeArray = new ArrayList<String>();
            poleTypeArray.add("DP");
            poleTypeArray.add("Tower");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(SurveyFormSecondActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, poleTypeArray);
            spnPoleType.setAdapter(dataAdapter);
        }

        rdbLastPoleYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txvPanchayatName.setVisibility(View.VISIBLE);
                edtPanchayatName.setVisibility(View.VISIBLE);
            }
        });
        rdbLastPoleNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtPanchayatName.setText("");
                txvPanchayatName.setVisibility(View.GONE);
                edtPanchayatName.setVisibility(View.GONE);
            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Take Photo",
                        "Choose from Gallery", "Cancel"};

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
                        SurveyFormSecondActivity.this);
                builder.setTitle("Add Photo!");
                builder.setItems(options,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (options[item].equals("Take Photo")) {
                                    startCamera();
                                } else if (options[item]
                                        .equals("Choose from Gallery")) {
                                    startGallery();
                                } else if (options[item].equals("Cancel")) {
                                    dialog.dismiss();
                                }
                            }
                        });
                builder.show();
            }
        });
        btnSaveAndContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_divideline = "NO", str_lastpole_panchyat = "NO";
                boolean chkStatus = false;
                if (divideLineNo.isChecked()) {
                    str_divideline = "NO";
                } else if (divideLineYes.isChecked()) {
                    str_divideline = "YES";
                }
                if (rdbLastPoleNo.isChecked()) {
                    str_lastpole_panchyat = "NO";
                } else if (rdbLastPoleYes.isChecked()) {
                    str_lastpole_panchyat = "YES";
                }
                if (spnPoleType.getSelectedItem().toString() == null) {
                    Toast.makeText(SurveyFormSecondActivity.this, "Please select Pole Type", Toast.LENGTH_SHORT).show();
                } else if (gps.getLocation() == null) {
                    Toast.makeText(SurveyFormSecondActivity.this, "We are troubling to get your locaiton \nPlease start your GPS or Internet connection", Toast.LENGTH_SHORT).show();
                } else if (!divideLineYes.isChecked() && !divideLineNo.isChecked()) {
                    Toast.makeText(SurveyFormSecondActivity.this, "Is Divide line for this pole? \nPlease select yes/no", Toast.LENGTH_SHORT).show();
                } else if (spnBarrier.getSelectedItem().toString() == null) {
                    Toast.makeText(SurveyFormSecondActivity.this, "Please select Barrier", Toast.LENGTH_SHORT).show();
                } else if (!rdbLastPoleNo.isChecked() && !rdbLastPoleYes.isChecked()) {
                    Toast.makeText(SurveyFormSecondActivity.this, "Is it last pole of panchyat? \nPlease select yes/no", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences pref = getSharedPreferences("POLE_DATA", 0);
                    if (pref.getInt("ID", 0) == 0) {
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putInt("ID", 00001);
                        edit.commit();
                    } else {
                        int count = pref.getInt("ID", 00000);
                        count = count + 1;
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putInt("ID", count);
                        edit.commit();
                    }
                    try {
                        DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                        chkStatus = dbhelper.insertData(strCompany, strCircle, strDivision, strSubDivision,
                                strSubStation, strFeeder, strLineType, strCompany.substring(0, 2) + strCircle.substring(0, 2) + strDivision.substring(0, 2) + edtPoleId.getText().toString(),
                                edtPreviousPoleId.getText().toString(), spnPoleType.getSelectedItem().toString(),
                                gps.getLatitude() + "", gps.getLongitude() + "", str_divideline, spnBarrier.getSelectedItem().toString(), str_lastpole_panchyat,
                                selectedImage, getSharedPreferences("LOGIN_DETAIL", 0).getString("USER_ID", ""), edtPanchayatName.getText().toString());
                        dbhelper.getTotalRecords();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(SurveyFormSecondActivity.this);
                    builder.setCancelable(false);
                    if (chkStatus) {
                        builder.setMessage("Your Record Inserted Successfully!! \n\n Your Location is " + gps.getLatitude() + "," + gps.getLongitude());
                        try {
                            DataBaseHelper dbhelper = new DataBaseHelper(getApplicationContext());
                            int poleid = dbhelper.serachPoleID(strCompany, strFeeder);
                            if(poleid==0){
                                dbhelper.addPoleID(strCompany,strFeeder,edtPoleId.getText().toString());
                            }else{
                                dbhelper.updatePoleID(strCompany,strFeeder,edtPoleId.getText().toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        builder.setMessage("Oops Your Record Inserting Failure.. Please try again!! \n Your Location is " + gps.getLatitude() + "," + gps.getLongitude());
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SurveyFormSecondActivity.this);
                builder.setCancelable(false);
                builder.setMessage("Do you want to Exit Application?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
    }

    private void startCamera() {

        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mFileUri = getOutputMediaFile(1);
        if (mFileUri != null) {
            intent1.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            startActivityForResult(intent1, REQUEST_CAMERA);
        } else {
            Log.e("image camera", "file not available");
        }

    }

    public String getPath(Uri uri, boolean isImage) {
        if (uri == null) {
            return null;
        }
        String[] projection;
        String coloumnName, selection;
        if (isImage) {
            selection = MediaStore.Images.Media._ID + "=?";
            coloumnName = MediaStore.Images.Media.DATA;
        } else {
            selection = MediaStore.Video.Media._ID + "=?";
            coloumnName = MediaStore.Video.Media.DATA;
        }
        projection = new String[]{coloumnName};
        Cursor cursor;
        if (Build.VERSION.SDK_INT > 19) {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            // where id is equal to
            if (isImage) {
                cursor = getContentResolver()
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection,
                                new String[]{id}, null);
            } else {
                cursor = getContentResolver()
                        .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, new String[]{id},
                                null);
            }
        } else {
            cursor = getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try {
            int column_index = cursor.getColumnIndex(coloumnName);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void startGallery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (resultCode == 15) {
                edtLatitude.setText(data.getStringExtra("LATITUDE"));
                edtLongitude.setText(data.getStringExtra("LONGITUDE"));
            }
        }
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_CAMERA) {
                if (mFileUri != null) {
                    Log.d("upload image", "file: " + mFileUri);
                    selectedImage = getRealPathFromURI(mFileUri);
                } else {
                    if (data != null) {
                        try {
                            selectedImage = getPath(data.getData(), true);
                        } catch (Exception e) {
                            selectedImage = getRealPathFromURI(data.getData());
                        }
                    }
                }

            } else if (requestCode == REQUEST_GALLERY) {
                if (data != null && data.getData() != null) {
                    try {
                        selectedImage = getPath(data.getData(), true);
                    } catch (Exception e) {
                        selectedImage = getRealPathFromURI(data.getData());
                    }
                }

            }
        }
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
