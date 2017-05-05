package com.robertkiszelirk.universalinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.robertkiszelirk.universalinventory.data.InventoryContract.InventoryEntry;

import java.io.File;


/* MAIN ACTIVITY FOR UNIVERSAL INVENTORY */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /* FLOATING ACTION BUTTON TO ADD A NEW ITEM TO THE DATABASE */
    FloatingActionButton addItem;

    /* VARIABLE TO PREPARE LOADER */
    private static final int ITEM_LOADER = 0;

    /* ADAPTER TO POPULATE LIST VIEW */
    ItemCursorAdapter itemCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* INITIALIZE FLOAT ACTION BUTTON */
        addItem = (FloatingActionButton) findViewById(R.id.fab_add_item);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* CREATE INTENT TO START EDIT ACTIVITY TO EDIT OR ADD ITEM */
                Intent intent = new Intent(MainActivity.this, EditActivity.class);

                /* PASS FLOAT ACTION BUTTON CENTER FOR ANIMATION */
                intent.putExtra(getString(R.string.fabX), (addItem.getLeft() + addItem.getRight()) / 2);
                intent.putExtra(getString(R.string.fabY), (addItem.getTop() + addItem.getBottom()) / 2);

                /* START EDIT ACTIVITY */
                startActivity(intent);
            }
        });

        /* ITEMS LIST VIEW */
        ListView itemListView = (ListView) findViewById(R.id.item_list_view);

        /* SET EMPTY VIEW FOR EMPTY LIST */
        View emptyView = findViewById(R.id.empty_view);

        /* ADD EMPTY VIEW FOR LIST VIEW */
        itemListView.setEmptyView(emptyView);

        /* INITIALIZE ADAPTER */
        itemCursorAdapter = new ItemCursorAdapter(this, null);

        /* SET ADAPTER TO LIST VIEW */
        itemListView.setAdapter(itemCursorAdapter);

        /* SET CLICK LISTENER TO LIST VIEW */
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                /* CREATE INTENT IF ITEM SELECTED */
                Intent intent = new Intent(MainActivity.this, EditActivity.class);

                /* CREATE URI TO PASS ITEM ID */
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                /* SET URI TO INTENT */
                intent.setData(currentItemUri);

                /* CREATE VIEW FOR ITEM */
                ImageView iView = (ImageView) view.findViewById(R.id.item_picture_row);

                /* CREATE ANIMATION */
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, iView, "picture");

                /* START INTENT AND ANIMATION */
                startActivity(intent, options.toBundle());

            }
        });

        /* PREPARE LOAD MANAGER */
        getLoaderManager().initLoader(ITEM_LOADER, null, this);

    }

    /* CREATE TOOLBAR MENU */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /* IF TOOLBAR MENU ITEM SELECTED */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_all_item:
                showDeleteAllConfirmationDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /* DELETE ALL DATA FROM DATABASE */
    private void showDeleteAllConfirmationDialog() {

        /* CREATE DIALOG */
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
        aBuilder.setMessage(R.string.delete_all_item);
        aBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                /* CALL DELETE ALL DATA METHOD */
                deleteAllItem();
            }
        });
        aBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();
    }

    /* DELETE ALL DATA METHOD */
    private void deleteAllItem() {

        /* GET PICTURES PATH */
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_PICTURE
        };

        Cursor cursor = getContentResolver().query(InventoryEntry.CONTENT_URI, projection, null, null, null);

        cursor.moveToFirst();

        /* CHECK IF CURSOR IS EMPTY */
        if (cursor.getCount() != 0) {

            /* DELETE PICTURES FROM INTERNAL STORAGE */
            do {
                String picturePath = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PICTURE));
                if (!picturePath.equals("noPicture")) {
                    File picture = new File(picturePath);
                    picture.delete();
                }
            } while (cursor.moveToNext());

            /* DELETE DATA FROM DATABASE */
            int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);

        }
        cursor.close();
    }

    /* LOAD DATA FROM DATABASE */
    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_PICTURE,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_UNIT,
                InventoryEntry.COLUMN_ITEM_CURRENCY,
                InventoryEntry.COLUMN_ITEM_QUANTITY
        };

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {

        itemCursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

        itemCursorAdapter.swapCursor(null);

    }
}
