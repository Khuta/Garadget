package volpis.com.garadget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import volpis.com.garadget.models.DoorLocation;

public class Database {
    private final Context mCtx;
    private static Database instance = null;
    public static final String DB_NAME = "garadget_db";
    public static final int DB_VERSION = 1;
    private DBHelper mDBHelper;
    public static SQLiteDatabase mDB;

    public Database(Context ctx) {
        mCtx = ctx;
    }

    public static Database getInstance(Context cnt) {
        if (instance == null) {
            instance = new Database(cnt);
            instance.open();
        }
        return instance;
    }

    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    public void close() {
        if (mDBHelper != null)
            mDBHelper.close();
    }

    public void addDoorLocation(DoorLocation doorLocation) {
        ContentValues cv = new ContentValues();
        cv.put(Tables.DOOR_LOCATION.DOOR_ID, doorLocation.getDoorId());
        cv.put(Tables.DOOR_LOCATION.LATITUDE, doorLocation.getLatitude());
        cv.put(Tables.DOOR_LOCATION.LONGITUDE, doorLocation.getLongitude());
        cv.put(Tables.DOOR_LOCATION.RADIUS, doorLocation.getRadius());
        cv.put(Tables.DOOR_LOCATION.ENABLED, doorLocation.isEnabled());
        mDB.insert(Tables.TABLE_DOOR_LOCATION, null, cv);
    }

    public void removeDoorLocation(String doorId) {
        mDB.delete(Tables.TABLE_DOOR_LOCATION, Tables.DOOR_LOCATION.DOOR_ID + "=" + "'" + doorId + "'", null);
    }

    public void updateOrInsertDoorLocation(DoorLocation doorLocation) {
        ContentValues cv = new ContentValues();
        cv.put(Tables.DOOR_LOCATION.DOOR_ID, doorLocation.getDoorId());
        cv.put(Tables.DOOR_LOCATION.LATITUDE, doorLocation.getLatitude());
        cv.put(Tables.DOOR_LOCATION.LONGITUDE, doorLocation.getLongitude());
        cv.put(Tables.DOOR_LOCATION.RADIUS, doorLocation.getRadius());
        cv.put(Tables.DOOR_LOCATION.ENABLED, doorLocation.isEnabled());
        int updated = mDB.update(Tables.TABLE_DOOR_LOCATION, cv, Tables.DOOR_LOCATION.DOOR_ID + "=" + "'" + doorLocation.getDoorId() + "'", null);
        if (updated == 0)
            addDoorLocation(doorLocation);
    }

    public DoorLocation getDoorLocation(String doorID) {
        DoorLocation doorLocation = null;
        Cursor c = mDB.rawQuery("Select * FROM " + Tables.TABLE_DOOR_LOCATION + " WHERE " + Tables.DOOR_LOCATION.DOOR_ID + "=" + "'" + doorID + "'", null);
        if (c != null && c.getCount() != 0) {
            c.moveToFirst();
            String doorId = c.getString(c.getColumnIndex(Tables.DOOR_LOCATION.DOOR_ID));
            double latitude = c.getDouble(c.getColumnIndex(Tables.DOOR_LOCATION.LATITUDE));
            double longitude = c.getDouble(c.getColumnIndex(Tables.DOOR_LOCATION.LONGITUDE));
            int radius = c.getInt(c.getColumnIndex(Tables.DOOR_LOCATION.RADIUS));
            boolean enabled = c.getInt(c.getColumnIndex(Tables.DOOR_LOCATION.ENABLED)) > 0;
            doorLocation = new DoorLocation(doorId, latitude, longitude, radius, enabled);
            c.close();
        }
        return doorLocation;
    }

    public ArrayList<DoorLocation> getDoorLocations() {
        ArrayList<DoorLocation> doorLocations = new ArrayList<>();
        Cursor c = mDB.rawQuery("Select * FROM " + Tables.TABLE_DOOR_LOCATION, null);
        if (c != null && c.getCount() != 0) {
            if (c.moveToFirst()) {
                do {
                    String doorId = c.getString(c.getColumnIndex(Tables.DOOR_LOCATION.DOOR_ID));
                    double latitude = c.getDouble(c.getColumnIndex(Tables.DOOR_LOCATION.LATITUDE));
                    double longitude = c.getDouble(c.getColumnIndex(Tables.DOOR_LOCATION.LONGITUDE));
                    int radius = c.getInt(c.getColumnIndex(Tables.DOOR_LOCATION.RADIUS));
                    boolean enabled = c.getInt(c.getColumnIndex(Tables.DOOR_LOCATION.ENABLED)) > 0;
                    doorLocations.add(new DoorLocation(doorId, latitude, longitude, radius, enabled));
                }
                while (c.moveToNext());
            }
            c.close();
        }
        return doorLocations;
    }

    public static class Tables {

        public static final String TABLE_DOOR_LOCATION = "table_door_location";

        public interface DOOR_LOCATION {
            String ID = "_id";
            String DOOR_ID = "door_id";
            String LATITUDE = "latitude";
            String LONGITUDE = "longitude";
            String RADIUS = "radius";
            String ENABLED = "enabled";
        }

        public interface _DOOR_LOCATION {
            String ID = "INTEGER PRIMARY KEY AUTOINCREMENT";
            String DOOR_ID = "TEXT";
            String LATITUDE = "REAL";
            String LONGITUDE = "REAL";
            String RADIUS = "INTEGER";
            String ENABLED = "INTEGER";
        }
    }

    private static final String[] PATCH_CREATE_DOOR_LOCATION = new String[]{
            "CREATE TABLE " + Tables.TABLE_DOOR_LOCATION + "("
                    + Tables.DOOR_LOCATION.ID + " " + Tables._DOOR_LOCATION.ID + ","
                    + Tables.DOOR_LOCATION.DOOR_ID + " " + Tables._DOOR_LOCATION.DOOR_ID + ","
                    + Tables.DOOR_LOCATION.LATITUDE + " " + Tables._DOOR_LOCATION.LATITUDE + ","
                    + Tables.DOOR_LOCATION.LONGITUDE + " " + Tables._DOOR_LOCATION.LONGITUDE + ","
                    + Tables.DOOR_LOCATION.RADIUS + " " + Tables._DOOR_LOCATION.RADIUS + ","
                    + Tables.DOOR_LOCATION.ENABLED + " " + Tables._DOOR_LOCATION.ENABLED
                    + ")"
    };

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String sql : PATCH_CREATE_DOOR_LOCATION) {
                db.execSQL(sql);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
