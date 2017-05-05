package com.robertkiszelirk.universalinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;

import static com.robertkiszelirk.universalinventory.data.InventoryContract.*;


/* LIST VIEW ITEM HANDLE */
class ItemCursorAdapter extends CursorAdapter {

    /* ITEM NAME */
    private String nameText;

    /* ITEM PICTURE PATH */
    private String picturePath;

    ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        /* GET IMAGE VIEW AND SET BASE PICTURE */
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.item_picture_row);
        pictureImageView.setImageResource(R.drawable.take_photo);

        /* GET NAME TEXT VIEW */
        TextView nameTextView = (TextView) view.findViewById(R.id.item_name_row);

        /* GET PRICE TEXT VIEW */
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price_row);

        /* GET QUANTITY TEXT VIEW */
        final TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity_row);

        /* GET DATABASE COLUMN INDEXES TO POPULATE LIST ITEM */
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int pictureColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PICTURE);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
        int unitColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_UNIT);
        int currencyColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_CURRENCY);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);

        /* INITIALIZE ITEM ROW ID */
        final int rowId = cursor.getInt(idColumnIndex);

        /* INITIALIZE ITEM PICTURE FILE PATH */
        picturePath = cursor.getString(pictureColumnIndex);

        /* INITIALIZE ITEM NAME */
        nameText = cursor.getString(nameColumnIndex);

        /* INITIALIZE ITEM PRICE TEXT*/
        String priceText = String.format("%.2f", cursor.getDouble(priceColumnIndex)) +
                " " +
                cursor.getString(currencyColumnIndex) +
                "/" +
                cursor.getString(unitColumnIndex);

        /* INITIALIZE QUANTITY TEXT*/
        String quantityText = String.format("%.2f", Double.parseDouble(cursor.getString(quantityColumnIndex))) + " " + cursor.getString(unitColumnIndex);

        /* INITIALIZE UNIT */
        final String unitString = cursor.getString(unitColumnIndex);

        /* INITIALIZE ACTUAL QUANTITY */
        final double actualQuantity = Double.parseDouble(cursor.getString(quantityColumnIndex));

        /* IF PICTURE EXITS ADD TO IMAGE VIEW */
        if (!picturePath.equals(context.getString(R.string.no_picture))) {
            File imageFile = new File(picturePath);
            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (imageFile.exists()) {
                pictureImageView.setImageBitmap(imageBitmap);
            }
        }

        /* SET NAME TEXT VIEW */
        nameTextView.setText(nameText);

        /* SET PRICE TEXT VIEW */
        priceTextView.setText(priceText);

        /* SET QUANTITY TEXT VIEW */
        quantityTextView.setText(quantityText);

        /* INITIALIZE SELL BUTTON */
        final AppCompatButton buttonSellQuantity = (AppCompatButton) view.findViewById(R.id.button_sell_quantity);
        buttonSellQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* CREATE DIALOG BOX TO SELL QUANTITY */
                showSellDialogBox(context, quantityTextView, unitString, actualQuantity, rowId);
            }
        });

        /* INITIALIZE ADD BUTTON */
        final AppCompatButton buttonAddQuantity = (AppCompatButton) view.findViewById(R.id.button_add_quantity);
        buttonAddQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* CREATE DIALOG BOX TO ADD QUANTITY */
                showAddDialogBox(context, quantityTextView, unitString, actualQuantity, rowId);
            }
        });

        /* INITIALIZE DELETE BUTTON */
        final AppCompatImageButton buttonDeleteItem = (AppCompatImageButton) view.findViewById(R.id.button_delete_item);
        buttonDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* DELETE ITEM FROM LIST VIEW */
                deleteItem(context, rowId);
            }
        });

    }

    /* DELETE ITEM METHOD */
    private void deleteItem(final Context context, final int id) {

        /* CREATE ALERT DIALOG BEFORE DELETE */
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.delete_dialog, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle(R.string.delete_item);
        dialogBuilder.setMessage(R.string.delete_item_text);
        dialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                /* DELETE PICTURE FROM INTERNAL STORAGE */
                File image = new File(picturePath);
                image.delete();
                /* DELETE ITEM FROM DATABASE */
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                context.getContentResolver().delete(currentItemUri, null, null);
                /* TOAST TEXT */
                showToast(context, nameText + context.getString(R.string.was_deleted));
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    /* METHOD TO SELL QUANTITY */
    private void showSellDialogBox(final Context context, final TextView qTextView, final String uString, final double actQuantity, final int id) {

        /* CREATE SELL DIALOG BOX */
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.sell_add_dialog, null);
        dialogBuilder.setView(dialogView);

        /* INITIALIZE SELL EDIT TEXT */
        final EditText sellQuantity = (EditText) dialogView.findViewById(R.id.sell_add_edit_text);

        dialogBuilder.setTitle(R.string.selling_quantity);
        dialogBuilder.setMessage(context.getString(R.string.selling_quantit_text) + uString);
        dialogBuilder.setPositiveButton(R.string.sell, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                double newQuantity;
                if ((!sellQuantity.getText().toString().equals("")) && (!sellQuantity.getText().toString().equals("."))) {
                    /* CHECK QUANTITY >= 0 */
                    newQuantity = actQuantity - Double.parseDouble(sellQuantity.getText().toString());
                    if (newQuantity > 0) {
                        qTextView.setText(String.format("%.2f", newQuantity) + uString);
                        /* SAVE NEW QUANTITY TO DATABASE */
                        ContentValues values = new ContentValues();
                        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                        Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                        context.getContentResolver().update(currentItemUri, values, null, null);

                    } else {
                        /* SHOW TOAST IF QUANTITY UNDER 0 */
                        showToast(context, context.getString(R.string.sell_more_than_have));
                    }
                } else {
                    /* SHOW TOAST IF WRONG INPUT */
                    showToast(context, context.getString(R.string.wrong_input));
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        /* SHOW SOFT KEYBOARD */
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    private void showAddDialogBox(final Context context, final TextView qTextView, final String uString, final double actQuantity, final int id) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.sell_add_dialog, null);
        dialogBuilder.setView(dialogView);

        /* INITIALIZE ADD EDIT TEXT */
        final EditText addQuantity = (EditText) dialogView.findViewById(R.id.sell_add_edit_text);

        dialogBuilder.setTitle(R.string.add_quantity);
        dialogBuilder.setMessage(context.getString(R.string.add_quantity_text) + uString);
        dialogBuilder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                double newQuantity;
                /* CHECK VALID INPUT */
                if ((!addQuantity.getText().toString().equals("")) && (!addQuantity.getText().toString().equals("."))) {
                    newQuantity = actQuantity + Double.parseDouble(addQuantity.getText().toString());
                    /* CHECK NEW QUANTITY MAXIMUM */
                    if (newQuantity < 999999999) {
                        qTextView.setText(String.format("%.2f", newQuantity) + uString);
                        /* SAVE NEM QUANTITY TO DATABASE */
                        ContentValues values = new ContentValues();
                        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                        Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                        context.getContentResolver().update(currentItemUri, values, null, null);
                    } else {
                        /* TOAST FOR MAXIMUM QUANTITY */
                        showToast(context, context.getString(R.string.max_number));
                    }
                } else {
                    /* TOAST FOR WRONG NUMBER */
                    showToast(context, context.getString(R.string.wrong_input));
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    /* SHOW TOAST METHOD */
    private void showToast(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
}
