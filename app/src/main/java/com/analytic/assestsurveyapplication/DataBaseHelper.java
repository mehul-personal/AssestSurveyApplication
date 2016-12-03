package com.analytic.assestsurveyapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by MeHuL on 19-06-2016.
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "COMPANY_DETAILS.db";
    public static File DATABASE_PATH;
    private final Context myContext;
    private SQLiteDatabase myDataBase;

    public DataBaseHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        DATABASE_PATH = myContext.getDatabasePath(DATABASE_NAME);
        boolean dbexist = checkDataBase();
        if (dbexist) {
            //System.out.println("Database exists");
            openDatabase();
        } else {
            System.out.println("Database doesn't exist");
            createDatabase();
        }
    }

    public SQLiteDatabase getDatabase() {
        return myDataBase;

    }


    //Create a empty database on the system
    public void createDatabase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
            Log.v("DB Exists", "db exists");
            // By calling this method here onUpgrade will be called on a
            // writeable database, but only if the version number has been
            // bumped
            //onUpgrade(myDataBase, DATABASE_VERSION_old, DATABASE_VERSION);
        }
        boolean dbExist1 = checkDataBase();
        if (!dbExist1) {
            this.getReadableDatabase();
            try {
                this.close();
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    //Check database already exist or not
    private boolean checkDataBase() {
        boolean checkDB = false;
        try {
//            String myPath = DATABASE_PATH + DATABASE_NAME;
//            File dbfile = new File(myPath);
            checkDB = DATABASE_PATH.exists();
        } catch (SQLiteException e) {
        }
        return checkDB;
    }

    //Copies your database from your local assets-folder to the just created empty database in the system folder
    private void copyDataBase() throws IOException {

        InputStream mInput = myContext.getAssets().open(DATABASE_NAME);
        // String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream mOutput = new FileOutputStream(DATABASE_PATH);
        byte[] mBuffer = new byte[2024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    //delete database
    public void db_delete() {
        File file = DATABASE_PATH;
        if (file.exists()) {
            file.delete();
            System.out.println("delete database file.");
        }
    }

    //Open database
    public void openDatabase() throws SQLException {
        String myPath = DATABASE_PATH.getPath();
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public boolean insertData(String company, String circle, String division, String subdivision, String substation,
                              String feeder, String linetype, String poleid, String previouspoleid, String poletype,
                              String str_latitude, String str_longitude, String isDivideLine, String barrier,
                              String isPanchayatLastPole, String image, String user_id, String panchayat_name) {
        openDatabase();
        myDataBase.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put("company_name", company);
            values.put("circle", circle);
            values.put("division", division);
            values.put("subdivision", subdivision);
            values.put("substation", substation);
            values.put("feeder", feeder + "");
            values.put("linetype", linetype + "");
            values.put("poleid", poleid);
            values.put("previous_poleid", previouspoleid);
            values.put("pole_type", poletype);
            values.put("latitude", str_latitude);
            values.put("longitude", str_longitude);
            values.put("isdivideline", isDivideLine);
            values.put("barrier", barrier);
            values.put("isPanchayatlastpole", isPanchayatLastPole);
            values.put("image", image);
            values.put("user_id", user_id);
            values.put("panchayat_name", panchayat_name);
            myDataBase.insert("SURVEY_DATA", null, values);
            myDataBase.setTransactionSuccessful();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            myDataBase.endTransaction();
            myDataBase.close();
        }

    }

    public boolean addPoleID(String company, String feeder, String pole_id) {
        openDatabase();
        myDataBase.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put("company", company);
            values.put("feeder", feeder);
            values.put("pole_id", pole_id);
            myDataBase.insert("UNIQUE_POLE_ID", null, values);
            myDataBase.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            myDataBase.endTransaction();
            myDataBase.close();
        }
    }

    public boolean updatePoleID(String company, String feeder, String pole_id) {
        openDatabase();
        myDataBase.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put("company", company);
            values.put("feeder", feeder);
            values.put("pole_id", pole_id);
            myDataBase.update("UNIQUE_POLE_ID", values, "company ='" + company + "' AND feeder ='" + feeder + "'", null);
            myDataBase.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            myDataBase.endTransaction();
            myDataBase.close();
        }
    }

    public int serachUserID(String username, String company, String phone) {
        int poleid = 0;
        try {
            openDatabase();
            Cursor cursor = myDataBase.rawQuery("select user_id from " + "USER where company_name='" + company + "' AND username='" + username + "'AND phone='" + phone + "'",
                    null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0)
                poleid = Integer.parseInt(cursor.getString(0));
            cursor.close();
            closeDataBase();
        } catch (Exception e) {
            e.printStackTrace();
            closeDataBase();
        }
        return poleid;
    }

    public int serachPoleID(String company, String feeder) {
        int poleid = 0;
        try {
            openDatabase();
            Cursor cursor = myDataBase.rawQuery("select pole_id from " + "UNIQUE_POLE_ID where company='" + company + "' AND feeder='" + feeder + "'",
                    null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0)
                poleid = Integer.parseInt(cursor.getString(0));
            cursor.close();
            closeDataBase();
        } catch (Exception e) {
            e.printStackTrace();
            closeDataBase();
        }
        return poleid;
    }

    public int getTotalRecords() {
        int count = 0;
        try {
            openDatabase();
            Cursor cursor = myDataBase.rawQuery("select *from " + "SURVEY_DATA",
                    null);
            cursor.moveToFirst();
            count = cursor.getCount();
            String log = "";
            while (cursor.isAfterLast() == false) {

                Log.e("row data ", "data:" + cursor.getInt(0) + ":" + cursor.getString(1) + ":" + cursor.getString(2) + ":" + cursor.getString(3) + ":" +
                        cursor.getString(4) + ":" + cursor.getString(5) + ":" + cursor.getString(6) + ":" + cursor.getString(7) + ":" + cursor.getString(8) + ":" +
                        cursor.getString(9) + ":" + cursor.getString(10) + ":" + cursor.getString(11) + ":" + cursor.getString(12) + ":" + cursor.getString(13) + ":" +
                        cursor.getString(14) + ":" + cursor.getString(15) + ":" + cursor.getString(16) + ":" + cursor.getString(17) + ":" + cursor.getString(18));

                cursor.moveToNext();
            }

            cursor.close();
            closeDataBase();
            return count;
        } catch (Exception e) {
            Log.i("gettotalrecord", "" + e);
            closeDataBase();
            return count;
        }

    }

    public int deleteRecords(String ID) {
        int count = 0;
        try {
            openDatabase();
            Cursor cursor = myDataBase.rawQuery("Delete from " + "SURVEY_DATA where id=" + ID,
                    null);
            cursor.moveToFirst();
            count = cursor.getCount();
            String log = "";
           /* while (cursor.isAfterLast() == false) {

                Log.e("row data ", "data:" + cursor.getInt(0) + ":" + cursor.getString(1) + ":" + cursor.getString(2) + ":" + cursor.getString(3) + ":" +
                        cursor.getString(4) + ":" + cursor.getString(5) + ":" + cursor.getString(6) + ":" + cursor.getString(7) + ":" + cursor.getString(8) + ":" +
                        cursor.getString(9) + ":" + cursor.getString(10) + ":" + cursor.getString(11) + ":" + cursor.getString(12) + ":" + cursor.getString(13) + ":" +
                        cursor.getString(14) + ":" + cursor.getString(15) + ":" + cursor.getString(16) + ":" + cursor.getString(17));

                cursor.moveToNext();
            }*/

            cursor.close();
            closeDataBase();
            return count;
        } catch (Exception e) {
            Log.i("gettotalrecord", "" + e);
            closeDataBase();
            return count;
        }

    }

    public ArrayList<String> getAllId() {
        int count = 0;
        ArrayList<String> id = new ArrayList<String>();
        try {
            openDatabase();
            Cursor cursor = myDataBase.rawQuery("select *from " + "SURVEY_DATA LIMIT 1",
                    null);
            cursor.moveToFirst();
            count = cursor.getCount();
            String log = "";
            while (cursor.isAfterLast() == false) {

                Log.e("row data ", "data:" + cursor.getInt(0));
                id.add("" + cursor.getInt(0));
                cursor.moveToNext();
            }

            cursor.close();
            closeDataBase();
            return id;
        } catch (Exception e) {
            Log.i("gettotalrecord", "" + e);
            closeDataBase();
            return id;
        }

    }

    public void encodeByteArray(String path) {
        Bitmap bm = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
    }

    public Bitmap getBitmap(File file) {

        Bitmap bitmap = null;
        if (file != null) {
            String filePath = file.getAbsolutePath();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeFile(filePath, options);

        }
        return bitmap;
    }

    public String encodeToBase64(Bitmap bitmap) {

        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] b = baos.toByteArray();

        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        if (imageEncoded == null) {
            return "";
        }
        return imageEncoded;
    }

    public JSONObject getJsonData(String ID) {
        int count = 0;
        JSONObject jsob = null;
        try {
            openDatabase();
            Cursor cursor = myDataBase.rawQuery("select * from " + "SURVEY_DATA where id=" + ID,
                    null);
            cursor.moveToFirst();
            count = cursor.getCount();
            String log = "";
            //  while (cursor.isAfterLast() == false) {
            try {
                jsob = new JSONObject();
                jsob.put("company_name", "" + cursor.getString(1));
                jsob.put("circle", "" + cursor.getString(2));
                jsob.put("division", "" + cursor.getString(3));
                jsob.put("subdivison", "" + cursor.getString(4));
                jsob.put("substation", "" + cursor.getString(5));
                jsob.put("feeder", "" + cursor.getString(6));
                jsob.put("linetype", "" + cursor.getString(7));
                jsob.put("poleid", "" + cursor.getString(8));
                jsob.put("previous_poleid", "" + cursor.getString(9));
                jsob.put("pole_type", "" + cursor.getString(10));
                jsob.put("isdivideline", "" + cursor.getString(11));
                jsob.put("barrier", "" + cursor.getString(12));
                jsob.put("isPanchayatlastpole", "" + cursor.getString(13));
                if (cursor.getString(14).isEmpty())
                    jsob.put("image", "");
                else {
                    Log.e("Image path", "path:" + cursor.getString(14).replace("BonjourSeller Images", "ABCXYZ"));
                    jsob.put("image", "" + encodeToBase64(getBitmap(new File(cursor.getString(14).replace("BonjourSeller Images", "ABCXYZ")))));
                }
                jsob.put("user_id", "" + cursor.getString(15));
                jsob.put("latitude", "" + cursor.getString(16));
                jsob.put("longitude", "" + cursor.getString(17));
                jsob.put("panchayat_name", "" + cursor.getString(18));

                Log.e("JSON", "data:" + jsob.toString());
                Log.e("image", "image:" + encodeToBase64(getBitmap(new File(cursor.getString(14).replace("BonjourSeller Images", "ABCXYZ")))));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("row data ", "data:" + cursor.getInt(0) + ":" + cursor.getString(1) + ":" + cursor.getString(2) + ":" + cursor.getString(3) + ":" +
                    cursor.getString(4) + ":" + cursor.getString(5) + ":" + cursor.getString(6) + ":" + cursor.getString(7) + ":" + cursor.getString(8) + ":" +
                    cursor.getString(9) + ":" + cursor.getString(10) + ":" + cursor.getString(11) + ":" + cursor.getString(12) + ":" + cursor.getString(13) + ":" +
                    cursor.getString(14) + ":" + cursor.getString(15) + ":" + cursor.getString(16) + ":" + cursor.getString(17) + ":" + cursor.getString(18));

            //    cursor.moveToNext();
            // }

            cursor.close();
            closeDataBase();
            return jsob;
        } catch (Exception e) {
            Log.i("gettotalrecord", "" + e);
            closeDataBase();
            return jsob;
        }

    }

    public synchronized void closeDataBase() throws SQLException {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            Log.v("Database Upgrade", "Database version higher than old.");
            db_delete();
        }

    }

}