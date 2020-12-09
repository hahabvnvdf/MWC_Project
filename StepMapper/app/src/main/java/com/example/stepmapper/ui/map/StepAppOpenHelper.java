package com.example.stepmapper.ui.map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.Transliterator;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StepAppOpenHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "stepapp_loc";

    public static final String TABLE_NAME = "loc_track";
    public static final String KEY_ID = "id";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LON = "lon";


    // Default SQL for creating a table in a database
    public static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " (" +
            KEY_ID + " INTEGER PRIMARY KEY, " + KEY_LON + " TEXT, " + KEY_LAT + " TEXT, "
            + KEY_TIMESTAMP + " TEXT);";


    // The constructor
    public StepAppOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // onCreate
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing to do here
    }

    /**
     * Utility function to load all records in the database
     *
     * @param context: application context
     */
    public static void loadRecords(Context context){
        List<String> dates = new LinkedList<String>();
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String [] columns = new String [] {StepAppOpenHelper.KEY_TIMESTAMP};
        Cursor cursor = database.query(StepAppOpenHelper.TABLE_NAME, columns, null, null, StepAppOpenHelper.KEY_TIMESTAMP,
                null, null );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            dates.add(cursor.getString(0));
            cursor.moveToNext();
        }
        database.close();

        Log.d("STORED TIMESTAMPS: ", String.valueOf(dates));
    }

    /**
     * Utility function to delete all records from the data base
     *
     * @param context: application context
     */
    public static void deleteRecords(Context context){
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int numberDeletedRecords =0;

        numberDeletedRecords = database.delete(StepAppOpenHelper.TABLE_NAME, null, null);
        database.close();

        // display the number of deleted records with a Toast message
        Toast.makeText(context,"Deleted " + String.valueOf(numberDeletedRecords) + " steps",Toast.LENGTH_LONG).show();
    }

    /**
     * Utility function to load records from a single day
     *
     * @param context: application context
     * @param date: today's date
     * @return locations: a list of Locations
     */
    //
    public static ArrayList<Location> loadSingleDay(Context context, String date){

        ArrayList<Location> locations = new ArrayList<>();
        // Get the readable database
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String where = StepAppOpenHelper.KEY_TIMESTAMP + " = ?";
        String [] whereArgs = { date };

        Cursor cursor = database.query(StepAppOpenHelper.TABLE_NAME, null, where, whereArgs, null,
                null, KEY_TIMESTAMP );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            Location loc = new Location("");
            loc.setLatitude(Double. parseDouble(cursor.getString(1)));
            loc.setLongitude(Double. parseDouble(cursor.getString(2)));
            locations.add(loc);
            cursor.moveToNext();
        }
        database.close();

        Integer numSteps = locations.size();
        Log.d("NUMBER OF RETURNED POINTS: ", String.valueOf(numSteps));
        return locations;
    }

    /**
     * Utility function to load all records
     *
     * @param context: application context
     * @return locations: a list of Locations
     */
    //
    public static ArrayList<Location> loadAllData(Context context){

        ArrayList<Location> locations = new ArrayList<>();
        // Get the readable database
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();


        Cursor cursor = database.query(StepAppOpenHelper.TABLE_NAME, null, null, null, null,
                null, KEY_TIMESTAMP );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            Location loc = new Location("");
            loc.setLatitude(Double. parseDouble(cursor.getString(1)));
            loc.setLongitude(Double. parseDouble(cursor.getString(2)));
            locations.add(loc);
            cursor.moveToNext();
        }
        database.close();

        Integer numSteps = locations.size();
        Log.d("NUMBER OF RETURNED POINTS: ", String.valueOf(numSteps));
        return locations;
    }

    /**
     * Utility function to get the number of steps by hour for current date
     *
     * @param context: application context
     * @param date: today's date
     * @return map: map with key-value pairs hour->number of steps
     */
    //
    /*
    public static Map<Integer, Integer> loadStepsByHour(Context context, String date){
//        TODO: fix this
        // 1. Define a map to store the hour and number of steps as key-value pairs
        Map<Integer, Integer>  map = new HashMap<> ();

        // 2. Get the readable database
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        // 3. Define the query to get the data
        Cursor cursor = database.rawQuery("SELECT hour, COUNT(*)  FROM num_steps " +
                "WHERE day = ? GROUP BY hour ORDER BY  hour ASC ", new String [] {date});

        // 4. Iterate over returned elements on the cursor
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            Integer tmpKey = Integer.parseInt(cursor.getString(0));
            Integer tmpValue = Integer.parseInt(cursor.getString(1));

            //2. Put the data from the database into the map
            map.put(tmpKey, tmpValue);


            cursor.moveToNext();
        }

        // 5. Close the cursor and database
        cursor.close();
        database.close();

        // 6. Return the map with hours and number of steps
        return map;
    }
     */

    /**
     * Utility function to get the number of steps by day
     *
     * @param context: application context
     * @return map: map with key-value pairs hour->number of steps
     */
    //
    /*
    public static Map<String, Integer> loadStepsByDay(Context context){
        //        TODO: fix this

        // 1. Define a map to store the hour and number of steps as key-value pairs
        Map<String, Integer>  map = new TreeMap<>();

        // 2. Get the readable database
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        // 3. Define the query to get the data
        Cursor cursor = database.rawQuery("SELECT day, COUNT(*)  FROM num_steps " +
                "GROUP BY day ORDER BY day ASC ", new String [] {});

        // 4. Iterate over returned elements on the cursor
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            String tmpKey = cursor.getString(0);
            Integer tmpValue = Integer.parseInt(cursor.getString(1));

            // Put the data from the database into the map
            map.put(tmpKey, tmpValue);
            cursor.moveToNext();
        }

        // 5. Close the cursor and database
        cursor.close();
        database.close();

        // 6. Return the map with hours and number of steps
        return map;
    }
    */

}









