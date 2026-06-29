package com.example.darnelljohnsoninventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory_app.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_ITEMS = "items";

    public static final String USER_ID = "user_id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static final String ITEM_ID = "item_id";
    public static final String ITEM_NAME = "item_name";
    public static final String ITEM_QUANTITY = "quantity";
    public static final String ITEM_LOCATION = "location";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USERNAME + " TEXT UNIQUE, "
                + PASSWORD + " TEXT)";

        String createItemsTable = "CREATE TABLE " + TABLE_ITEMS + " ("
                + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ITEM_NAME + " TEXT, "
                + ITEM_QUANTITY + " INTEGER, "
                + ITEM_LOCATION + " TEXT)";

        db.execSQL(createUsersTable);
        db.execSQL(createItemsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USERNAME + " = ? AND " + PASSWORD + " = ?",
                new String[]{username, password},
                null,
                null,
                null
        );

        boolean validLogin = cursor.getCount() > 0;
        cursor.close();
        return validLogin;
    }

    public long addItem(String itemName, int quantity, String location) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, itemName);
        values.put(ITEM_QUANTITY, quantity);
        values.put(ITEM_LOCATION, location);

        return db.insert(TABLE_ITEMS, null, values);
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_ITEMS,
                null,
                null,
                null,
                null,
                null,
                ITEM_NAME + " ASC"
        );
    }

    public int updateQuantity(int itemId, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEM_QUANTITY, quantity);

        return db.update(TABLE_ITEMS, values, ITEM_ID + " = ?", new String[]{String.valueOf(itemId)});
    }

    public int deleteItem(int itemId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_ITEMS, ITEM_ID + " = ?", new String[]{String.valueOf(itemId)});
    }
}
