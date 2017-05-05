package com.robertkiszelirk.universalinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.robertkiszelirk.universalinventory.R;
import com.robertkiszelirk.universalinventory.data.InventoryContract.InventoryEntry;

/* CONTENT PROVIDER FOR UNIVERSAL INVENTORY */
public class ItemProvider extends ContentProvider {

    /* TAG FOR LOG MESSAGE */
    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

    /* URI MATCHER FOR THE INVENTORY TABLE */
    private static final int ITEMS = 100;

    /* URI MATCHER FOR A SINGLE ITEM FROM INVENTORY TABLE */
    private static final int ITEMS_ID = 101;

    /* URI MATCHER TO SELECT CORRECT URI */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /* ADD URIS TO URI MATCHER */
    static {

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ITEMS);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEMS_ID);
    }

    /* DATABASE HELPER OBJECT */
    private InventoryDbHelper mDbHelper;

    /* INITIALIZE THE DATABASE HELPER */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /* THE QUERY METHOD FOR URI */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        /* GET READABLE DATABASE */
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        /* CURSOR FOR QUERY RESULT */
        Cursor cursor;

        /* SELECT THE CORRECT CASE BASED ON URI MATCHER */
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                /* QUERY TABLE */
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEMS_ID:
                /* QUERY A SINGLE ITEM BASED ON THE ID */
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                /* INVALID URI */
                throw new IllegalArgumentException("" + getContext().getResources().getString(R.string.cannot_query_URI) + uri);
        }
        /* HANDLE TABLE CHANGE */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /* INSERT NEW DATA IN TABLE */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {

        /* SELECT THE CORRECT CASE BASED ON URI MATCHER */
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                /* INSERT ITEM */
                return insertItem(uri, contentValues);
            default:
                /* HANDLE WRONG URI */
                throw new IllegalArgumentException(getContext().getString(R.string.insert_item_is_not_support_URI) + uri);
        }
    }

    /* METHOD TO INSERT DATA */
    private Uri insertItem(Uri uri, ContentValues contentValues) {

        /* REACH DATABASE */
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        /* INSERT ITEM AND GET RESULT INT */
        long id = db.insert(InventoryEntry.TABLE_NAME, null, contentValues);

        /* CHECK IF INSERT IS DONE */
        if (id == -1) {
            Log.e(LOG_TAG, getContext().getString(R.string.failed_to_insert_in_db) + uri);
            return null;
        }

        /* HANDLE TABLE CHANGE */
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /* UPDATE TABLE OR SELECTED ITEM BASED ON SELECTION AD SELECTION RGS */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        /* SELECT THE CORRECT CASE BASED ON URI MATCHER */
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                /* UPDATE TABLE */
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEMS_ID:
                /* UPDATE SELECTED ITEM */
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                /* HANDLE WRONG URI */
                throw new IllegalArgumentException("" + getContext().getString(R.string.update_is_not_support_URI) + uri);
        }
    }

    /* UPDATE SELECTED ITEM METHOD */
    private int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        /* REACH DATABASE */
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        /* UPDATE SELECTED ROW AND GET RESULT*/
        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        /* CHECK RESULT IF UPDATE WAS SUCCESSFUL */
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /* DELETE TABLE DATA OR SELECTED ITEM BASED ON SELECTION AND SELECTION ARGS */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        /* SELECT THE CORRECT CASE BASED ON URI MATCHER */
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                /* DELETE TABLE DATA */
                return deleteItem(uri, selection, selectionArgs);
            case ITEMS_ID:
                /* DELETE SELECTED ROW */
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteItem(uri, selection, selectionArgs);
            default:
                /* HANDLE WRONG URI */
                throw new IllegalArgumentException("" + getContext().getString(R.string.delet_is_not_support_URI) + uri);
        }
    }

    /* DELETE SELECTED ITEM */
    public int deleteItem(Uri uri, String selection, String[] selectionArgs) {

        /* REACH DATABASE */
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        /* DELETE SELECTED ROW */
        int rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);

        /* HANDLE WRONG URI */
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    /* RETURNS MIME TYPE OF DATA */
    @Override
    public String getType(@NonNull Uri uri) {

        /* SELECT THE CORRECT CASE BASED ON URI MATCHER */
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEMS_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                /* HANDLE WRONG URI */
                throw new IllegalArgumentException("" + getContext().getString(R.string.unknown_URI) + uri + " " + getContext().getString(R.string.with_match_URI) + " " + match);
        }
    }
}