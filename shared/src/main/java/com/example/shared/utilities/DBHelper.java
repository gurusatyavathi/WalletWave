package com.example.shared.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper instance;
    public static final String DATABASE_NAME = "wallet_wave.db";
    public static final int DATABASE_VERSION = 6;
    // Table names
    public static final String TABLE_TRANSACTIONS = "transactions";
    // Common column names
    public static final String KEY_ID = "id";

    // Transactions table column names
    public static String KEY_TYPE = "type";
    public static String KEY_CATEGORY = "category";
    public static String KEY_AMOUNT = "amount";
    public static String KEY_NOTE = "note";
    public static String KEY_DATE = "date";
    public static String KEY_DELETE = "is_deleted";

    // transaction table schema
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS +
            "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_TYPE + " TEXT NOT NULL," +
            KEY_CATEGORY + " TEXT NOT NULL," +
            KEY_AMOUNT + " INTEGER NOT NULL," +
            KEY_NOTE + " TEXT," +
            KEY_DATE + " TEXT NOT NULL," +
            KEY_DELETE + " INTEGER DEFAULT 0)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    //singleton pattern for creating only instance
    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);

        // Create new tables
        onCreate(db);
    }
}