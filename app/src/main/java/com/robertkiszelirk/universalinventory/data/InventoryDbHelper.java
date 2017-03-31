package com.robertkiszelirk.universalinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.robertkiszelirk.universalinventory.data.InventoryContract.InventoryEntry;

/* DATABASE HELPER FOR INVENTORY DATABASE */
public class InventoryDbHelper extends SQLiteOpenHelper{

    /* DATABASE NAME */
    private static final String DATABASE_NAME = "inventory.db";

    /* DATABASE VERSION */
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /* CREATE TABLE */
        /* CREATE STRING FOR THE SQL STATEMENT */
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + "(" +
                InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryEntry.COLUMN_ITEM_PICTURE + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_ITEM_UNIT + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL, " +
                InventoryEntry.COLUMN_ITEM_CURRENCY + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_ITEM_QUANTITY + " REAL NOT NULL, " +
                InventoryEntry.COLUMN_ITEM_SUP_NAME + " TEXT, " +
                InventoryEntry.COLUMN_ITEM_SUP_PHONE + " TEXT, " +
                InventoryEntry.COLUMN_ITEM_SUP_EMAIL + " TEXT);";

        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
