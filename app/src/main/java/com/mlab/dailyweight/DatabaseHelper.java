package com.mlab.dailyweight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by KHB on 2016-04-27.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String     DATABASE_NAME   = "dailyWeight.sqlite";
    public static final String     TABLE_USERINFO  = "USERINFO";
    public static final String     TABLE_WEIGHT    = "WEIGHT";
    public static final int        DB_VERSION      = 1;

    private static DatabaseHelper mDbHelper = null;

    /**
     * singleton
     */
    public static DatabaseHelper getInstance(Context context) {
        if (mDbHelper == null) {
            mDbHelper = new DatabaseHelper(context, DATABASE_NAME, null, DB_VERSION);
        }

        return mDbHelper;
    }

    /**
     * construction
     */
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * create database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableUserInfo = "CREATE TABLE IF NOT EXISTS " + TABLE_USERINFO + "("
                + "'_id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + "`gender` NUMERIC NOT NULL, "
                + "`height` REAL NOT NULL, "
                + "`begin_weight` REAL NOT NULL); ";
        db.execSQL(tableUserInfo);

        String tableWeight = "CREATE TABLE IF NOT EXISTS " + TABLE_WEIGHT + "("
                + "`_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + "`user_id` INTEGER NOT NULL, "
                + "`date` TEXT NOT NULL, "
                + "`weight` REAL NOT NULL); ";
        db.execSQL(tableWeight);
    }

    /**
     * upgrade database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_USERINFO);
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_WEIGHT);
        onCreate(db);
    }

    /**
     * insert item
     */
    public long insert(String table, ContentValues addRowValue) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(table, null, addRowValue);
    }

    /**
     * query
     */
    public Cursor query(String table, String[] columns,
                        String selection, String[] selectionArgs,
                        String groupBy, String having, String orderBy) {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }
}
