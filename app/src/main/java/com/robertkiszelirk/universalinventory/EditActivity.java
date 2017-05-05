package com.robertkiszelirk.universalinventory;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.telephony.PhoneNumberUtils;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.robertkiszelirk.universalinventory.data.InventoryContract.*;

/* EDIT ACTIVITY TO EDIT OR ADD AN ITEM TO THE DATABASE */
public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /* IMAGE BUTTON TO ROTATE RIGHT RIGHT */
    AppCompatImageButton rotateImageRight;

    /* IMAGE BUTTON TO ROTATE IMAGE LEFT */
    AppCompatImageButton rotateImageLeft;

    /* CHECK IF IMAGE IS ROTATED OR NOT */
    boolean imageRotated = false;

    /* FILE TO STORE IMAGE */
    File imageFile;

    /* FILE TO STORE NEW IMAGE */
    File imageNewFile;

    /* IMAGE BUTTON TO DISPLAY IMAGE AND START CAMERA */
    AppCompatImageButton itemPicture;

    /* BITMAP FOR CURRENT PICTURE */
    Bitmap itemPictureBitmap;

    /* BITMAP FOR NEW PICTURE */
    Bitmap itemNewPictureBitmap;

    /* PATH OF THE CURRENT IMAGE */
    String mCurrentPhotoPath = null;

    /* PATH OF THE NEW IMAGE */
    String mNewPhotoPath = null;

    /* EDIT TEXT FOR ITEM NAME */
    private EditText itemName;

    /* SPINNER TO SET UNIT */
    Spinner unitSpinner;

    /* ARRAY ADAPTER TO FILL UNIT SPINNER */
    ArrayAdapter<CharSequence> unitAdapter;

    /* EDIT TEXT TO SET ITEM PRICE/UNIT */
    EditText itemPriceUnit;

    /* SPINNER TO SET CURRENCY */
    Spinner currencySpinner;

    /* ARRAY ADAPTER TO FILL CURRENCY SPINNER */
    ArrayAdapter<CharSequence> currencyAdapter;

    /* EDIT TEXT FOR ITEM CURRENT QUANTITY */
    EditText itemCurrentQuantity;

    /* EDIT TEXT FOR ITEM SUPPLIER NAME */
    EditText supplierName;

    /* EDIT TEXT FOR ITEM SUPPLIER PHONE NUMBER */
    EditText supplierPhoneNumber;

    /* EDIT TEXT FOR ITEM ORDER QUANTITY */
    EditText itemOrderQuantity;

    /* SPINNER FOR ITEM ORDER UNIT */
    Spinner orderUnitSpinner;

    /* EDIT TEXT FOR SUPPLIER EMAIL ADDRESS */
    EditText supplierEmailAddress;

    /* PASSED URI FROM MAIN ACTIVITY TO CHECK THAT IF ADD ITEM OR EDIT ITEM */
    Uri currentItemUri;

    /* BOOLEAN FOR WATCHING THAT ITEM HAS CHANGED IN EDIT MODE TO SAVE */
    private boolean itemHasChanged = false;

    /* INTENT FOR CAMERA */
    Intent cameraData = null;

    /* CHECK IF CAMERA IS ON OR NOT */
    boolean cameraOn = false;

    /* INT TO CAMERA INTENT */
    static final int REQUEST_IMAGE_CAPTURE = 1;

    /* IF TOUCH HAPPEN THE ITEM WILL SAVE */
    private View.OnTouchListener viewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };


    /* SHOW CIRCLE ANIMATION WHEN ITEM IS ADDING */
    boolean showCircleRevealAnim = false;

    /* ANIMATION START x */
    int animStartX = 0;

    /* ANIMATION START y */
    int animStartY = 0;

    /* LINEAR LAYOUT FOR ANIMATION */
    LinearLayout editView = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* GET URI FROM INTENT TO CHECK IF ADD ITEM OR EDIT ITEM HAPPENING */
        final Intent intent = getIntent();
        currentItemUri = intent.getData();

        /* SET LAYOUT */
        setContentView(R.layout.activity_edit);

        /* INITIALIZE ROTATE IMAGE RIGHT */
        rotateImageRight = (AppCompatImageButton) findViewById(R.id.rotate_right);
        rotateImageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateImage(true);
            }
        });

        /* INITIALIZE IMAGE BUTTON TO DISPLAY ITEM PICTURE AND TAKE PICTURE */
        itemPicture = (AppCompatImageButton) findViewById(R.id.picture);
        itemPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraOn = true;
                dispatchTakePicture();
            }
        });

        /* INITIALIZE ROTATE IMAGE LEFT */
        rotateImageLeft = (AppCompatImageButton) findViewById(R.id.rotate_left);
        rotateImageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateImage(false);
            }
        });

        /* INITIALIZE EDIT TEXT FOR ITEM NAME */
        itemName = (EditText) findViewById(R.id.item_name);
        itemName.setOnTouchListener(viewTouchListener);
        itemName.clearFocus();

        /* INITIALIZE SPINNER FOR UNIT SELECT */
        unitSpinner = (Spinner) findViewById(R.id.unit_spinner);
        unitAdapter = ArrayAdapter.createFromResource(this, R.array.unit_array, R.layout.spinner_layout);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setOnTouchListener(viewTouchListener);

        /* INITIALIZE EDIT TEXT FOR PRICE SET */
        itemPriceUnit = (EditText) findViewById(R.id.price_unit);
        itemPriceUnit.setOnTouchListener(viewTouchListener);

        /* INITIALIZE SPINNER FOR CURRENCY SELECT */
        currencySpinner = (Spinner) findViewById(R.id.currency_spinner);
        currencyAdapter = ArrayAdapter.createFromResource(this, R.array.currency_array, R.layout.spinner_layout);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setOnTouchListener(viewTouchListener);

        /* INITIALIZE EDIT TEXT FOR CURRENT QUANTITY SET */
        itemCurrentQuantity = (EditText) findViewById(R.id.current_quantity);
        itemCurrentQuantity.setOnTouchListener(viewTouchListener);

        /* INITIALIZE EDIT TEXT FOR SUPPLIER NAME */
        supplierName = (EditText) findViewById(R.id.supplier_name);
        supplierName.setOnTouchListener(viewTouchListener);

        /* INITIALIZE EDIT TEXT FOR SUPPLIER PHONE NUMBER */
        supplierPhoneNumber = (EditText) findViewById(R.id.phone_number);
        supplierPhoneNumber.setOnTouchListener(viewTouchListener);

        /* SET CALL BUTTON FOR SUPPLIER PHONE NUMBER */
        AppCompatButton supplierCall = (AppCompatButton) findViewById(R.id.call_button);
        supplierCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* GET PHONE NUMBER FROM EDIT TEXT */
                String phoneNumber = PhoneNumberUtils.formatNumber(supplierPhoneNumber.getText().toString(), Locale.getDefault().getCountry());
                /* SET INTENT AND ADD PHONE NUMBER */
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                if (ActivityCompat.checkSelfPermission(EditActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    showToast(getString(R.string.check_permission));
                } else {
                    try {
                        /* CHECK VALID PHONE NUMBER AND START INTENT */
                        if (Patterns.PHONE.matcher(phoneNumber).matches()) {
                            supplierPhoneNumber.setBackgroundColor(Color.TRANSPARENT);
                            startActivity(intent);
                        } else {
                            showToast(getString(R.string.invalid_phone_number));
                            supplierPhoneNumber.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));
                        }
                    } catch (Exception e) {
                        showToast(getString(R.string.invalid_phone_number));
                        supplierPhoneNumber.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));
                    }
                }

            }
        });

        /* INITIALIZE EDIT TEXT FOR ORDER QUANTITY */
        itemOrderQuantity = (EditText) findViewById(R.id.order_quantity);

        /* INITIALIZE ORDER UNIT SPINNER */
        orderUnitSpinner = (Spinner) findViewById(R.id.order_unit_spinner);
        ArrayAdapter<CharSequence> orderUnitAdapter = ArrayAdapter.createFromResource(this, R.array.unit_array, R.layout.spinner_layout);
        orderUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderUnitSpinner.setAdapter(unitAdapter);

        /* INITIALIZE EDIT TEXT FOR SUPPLIER EMAIL */
        supplierEmailAddress = (EditText) findViewById(R.id.email_address);
        supplierEmailAddress.setOnTouchListener(viewTouchListener);

        /* SET BUTTON TO SEND EMAIL */
        AppCompatButton supplierSendEmail = (AppCompatButton) findViewById(R.id.send_email);
        supplierSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* CHECK VALID TAXTS */
                String toastText = "";

                String name = itemName.getText().toString().trim();

                /* CHECK IF ITEM HAS A NAME */
                boolean validName = true;

                if (name.length() == 0) {
                    validName = false;

                    toastText += getString(R.string.give_a_name);

                    itemName.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));

                } else {
                    itemName.setBackgroundColor(Color.TRANSPARENT);
                }

                /* CHECK IF ITEM HAS A SUPPLIER */
                String supplier = supplierName.getText().toString().trim();

                boolean validSupplier = true;

                if (supplier.length() == 0) {
                    validSupplier = false;

                    if (toastText.equals("")) {
                        toastText += getString(R.string.give_a_supplier_name);
                    } else {
                        toastText += "\n\n" + getString(R.string.give_a_supplier_name);
                    }

                    supplierName.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));

                } else {
                    supplierName.setBackgroundColor(Color.TRANSPARENT);
                }

                /* CHECK IF ORDER QUANTITY IS VALID */
                Double orderQuantity = null;

                boolean validQuantity = false;

                try {
                    itemOrderQuantity.setBackgroundColor(Color.TRANSPARENT);
                    orderQuantity = Double.parseDouble(itemOrderQuantity.getText().toString());
                    validQuantity = true;
                } catch (NumberFormatException e) {

                    if (toastText.equals("")) {
                        toastText += getString(R.string.give_valid_order_quantity);
                    } else {
                        toastText += "\n\n" + getString(R.string.give_valid_order_quantity);
                    }
                    itemOrderQuantity.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));
                }

                /* CHECK IF EMAIL IS VALID */
                boolean emailValid = Patterns.EMAIL_ADDRESS.matcher(supplierEmailAddress.getText().toString()).matches();

                if (!emailValid) {
                    if (toastText.equals("")) {
                        toastText += getString(R.string.give_valid_email);
                    } else {
                        toastText += "\n\n" + getString(R.string.give_valid_email);
                    }
                    supplierEmailAddress.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));
                }

                /* IF ALL VALID START INTENT FOR EMAIL SEND */
                if ((validName) && (validSupplier) && (emailValid) && (validQuantity)) {
                    supplierEmailAddress.setBackgroundColor(Color.TRANSPARENT);
                    String message = getString(R.string.good_day) + supplier + getString(R.string.like_to_order) + orderQuantity + " " + orderUnitSpinner.getSelectedItem().toString() + " " + getString(R.string.of) + " " + name;
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{supplierEmailAddress.getText().toString()});
                    intent.putExtra(Intent.EXTRA_TEXT, message);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else {

                    showToast(toastText);

                }
            }
        });

        /* IF SCREEN ROTATION OR SOMETHING ELSE RESTART THE ACTIVITY */
        if (savedInstanceState != null) {

            /* RESET IMAGE */
            resetAfterChange(savedInstanceState);

            /* SELECT USAGE */
            if (currentItemUri == null) {
                setTitle(getString(R.string.add_item));
            } else {
                setTitle(getString(R.string.edit_item));
            }

            /* SET CAMERA BACK */
            if (cameraOn) {
                cameraData = savedInstanceState.getParcelable(getString(R.string.camera_data));
                onActivityResult(REQUEST_IMAGE_CAPTURE, Activity.RESULT_OK, cameraData);
            }


        } else {
            /* AT FIRST START*/
            if (currentItemUri == null) {
                setTitle(getString(R.string.add_item));
                setNullData();
                showCircleRevealAnim = true;
                editView = (LinearLayout) findViewById(R.id.edit_main_view);
                editView.setVisibility(View.INVISIBLE);
                animStartX = intent.getIntExtra(getString(R.string.fabX), 0);
                animStartY = intent.getIntExtra(getString(R.string.fabY), 0);
                circleRevealAnimIn();
            } else {
                setTitle(getString(R.string.edit_item));
                setNullData();
                getLoaderManager().initLoader(0, null, this);
            }
        }

        /* HIDE SOFT KEYBOARD */
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        /* SET ASTERISK COLOR AND PLACES */
        setAsterisk(R.id.item_name_text_in_edit, getResources().getString(R.string.item_name_text));
        setAsterisk(R.id.item_price_unit_in_edit, getResources().getString(R.string.item_price_unit_text));
        setAsterisk(R.id.item_current_quantity_in_edit, getResources().getString(R.string.item_current_quantity_text));

    }

    private void resetAfterChange(Bundle sInstanceState) {

        /* GET CAMERA POSITION */
        cameraOn = sInstanceState.getBoolean(getString(R.string.camera_on));

        /* GET IMAGES PATH AND SET THEM*/
        mCurrentPhotoPath = sInstanceState.getString(getString(R.string.current_path));
        mNewPhotoPath = sInstanceState.getString(getString(R.string.new_path));
        if (cameraOn) {
            imageFile = new File(mCurrentPhotoPath);
            itemPictureBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            imageNewFile = new File(mNewPhotoPath);
            itemNewPictureBitmap = BitmapFactory.decodeFile(imageNewFile.getAbsolutePath());
        } else {
            if (mCurrentPhotoPath.equals(mNewPhotoPath)) {
                if (!mCurrentPhotoPath.equals(getString(R.string.no_picture))) {
                    imageFile = new File(mCurrentPhotoPath);
                    itemPictureBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    itemPicture.setImageBitmap(itemPictureBitmap);
                }
            } else {
                imageFile = new File(mCurrentPhotoPath);
                itemPictureBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                itemPicture.setImageBitmap(itemPictureBitmap);

                imageNewFile = new File(mNewPhotoPath);
                itemNewPictureBitmap = BitmapFactory.decodeFile(imageNewFile.getAbsolutePath());
            }
        }
    }

    /* SET ASTERISK METHOD */
    private void setAsterisk(int textId, String string) {

        string += "*";
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(string);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        stringBuilder.setSpan(foregroundColorSpan, string.length() - 1, string.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        TextView nameText = (TextView) findViewById(textId);
        nameText.setText(stringBuilder);


    }

    /* ROTATE IMAGE METHOD */
    private void rotateImage(boolean b) {

        int rotateValue;

        if (b) {
            rotateValue = +90;
        } else {
            rotateValue = -90;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(rotateValue);

        if ((!mCurrentPhotoPath.equals(getString(R.string.no_picture))) || (!mNewPhotoPath.equals(getString(R.string.no_picture)))) {
            if (mCurrentPhotoPath.equals(mNewPhotoPath)) {

                itemPictureBitmap = Bitmap.createBitmap(itemPictureBitmap, 0, 0, itemPictureBitmap.getWidth(), itemPictureBitmap.getHeight(), matrix, true);
                itemPicture.setImageBitmap(itemPictureBitmap);

            } else {

                itemNewPictureBitmap = Bitmap.createBitmap(itemNewPictureBitmap, 0, 0, itemNewPictureBitmap.getWidth(), itemNewPictureBitmap.getHeight(), matrix, true);
                itemPicture.setImageBitmap(itemNewPictureBitmap);

            }
            imageRotated = true;
        }
    }

    /* SET DATA FOR ADD ITEM START */
    private void setNullData() {

        mCurrentPhotoPath = getString(R.string.no_picture);
        imageFile = new File(mCurrentPhotoPath);
        mNewPhotoPath = getString(R.string.no_picture);
        itemName.setHint(R.string.item_name_hint);
        unitSpinner.setSelection(0);
        itemPriceUnit.setHint("" + 0.0);
        currencySpinner.setSelection(0);
        itemCurrentQuantity.setHint("" + 0.0);
        itemOrderQuantity.setHint("" + 0.0);
        supplierName.setHint(R.string.supplier_name_hint);
        supplierPhoneNumber.setHint(R.string.phone_number_hint);
        supplierEmailAddress.setHint(R.string.email_hint);

    }

    /* START CAMERA TO TAKE PICTURE */
    private void dispatchTakePicture() {

        /* CREATE INTENT FOR CAMERA */
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        /* IF PHONE HAS CAMERA ACTIVITY */
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            imageNewFile = null;
            try {
                /* CREATE FILE TO STORE PICTURE */
                imageNewFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageNewFile != null) {
                /* GET PICTURE */
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.robertkiszelirk.universalinventory.fileprovider",
                        imageNewFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /* CAMERA ACTIVITY GIVES BACK PICTURE */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /* IF EVERYTHING IS OK */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            /* STORE DATA FOR RESTART */
            cameraData = data;

            /* IMAGE HAS CHANGED */
            itemHasChanged = true;

            if (cameraOn) {
                /* SET PICTURE TO IMAGE BUTTON AND SAVE IT */
                itemNewPictureBitmap = BitmapFactory.decodeFile(imageNewFile.getAbsolutePath());
                /* RESIZE IMAGE */
                itemNewPictureBitmap = getResizedBitmap(itemNewPictureBitmap);
                try {
                    OutputStream oSteam = new FileOutputStream(imageNewFile);
                    itemNewPictureBitmap.compress(Bitmap.CompressFormat.JPEG, 30, oSteam);
                    oSteam.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mNewPhotoPath = imageNewFile.getAbsolutePath();
                itemPicture.setImageBitmap(itemNewPictureBitmap);
                cameraOn = false;
            }

        }

    }

    /* RESIZE IMAGE METHOD */
    public Bitmap getResizedBitmap(Bitmap bm) {

        /* GET IMAGE SIZES */
        int width = bm.getWidth();
        int height = bm.getHeight();

        /* SCALE SIZES */
        float scaleWidth = ((float) width / 10) / width;
        float scaleHeight = ((float) height / 10) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();

        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    /* CREATE FILE METHOD */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = getString(R.string.picture_file_for_name) + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File iFile = File.createTempFile(
                imageFileName,
                getString(R.string.picture_file_format),
                storageDir
        );

        /* SET NEW PHOTO PATH */
        mNewPhotoPath = iFile.getAbsolutePath();
        return iFile;
    }

    /* CREATE TOOLBAR MENU */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            /* SAVE ITEM */
            case R.id.save_item:
                saveItem();
                break;
            /* RETURN TO MAIN ACTIVITY */
            case android.R.id.home:
                /* IF NOTHING HAS CHANGED */
                if (!itemHasChanged) {
                    if (showCircleRevealAnim) {
                        /* START ANIMATION FOR FLOAT ACTION BUTTON */
                        circleRevealAnimOut();
                    } else {
                        /* START ANIMATION */
                        supportFinishAfterTransition();
                    }
                    return true;
                }

                /* CREATE DIALOG IF SOMETHING HAS CHANGED */
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                        if (!mCurrentPhotoPath.equals(mNewPhotoPath)) {
                            if (imageNewFile.exists()) {
                                imageNewFile.delete();
                            }
                        }
                        if (showCircleRevealAnim) {
                            /* START ANIMATION FOR FLOAT ACTION BUTTON */
                            circleRevealAnimOut();

                        } else {
                            /* START ANIMATION */
                            supportFinishAfterTransition();
                        }
                    }
                };

                /* SHOW DIALOG */
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* DIALOG METHOD */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
        aBuilder.setMessage(R.string.discard_changes);
        aBuilder.setPositiveButton(R.string.discard, discardButtonClickListener);
        aBuilder.setNegativeButton(R.string.keep_edit, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();

    }

    /* SAVE CHANGES ON ITEM */
    private void saveItem() {

        boolean hasData = true;

        String toastText = "";

        /* CHECK NAME CHANGE */
        String name = itemName.getText().toString().trim();

        if (name.length() == 0) {
            hasData = false;

            toastText = getString(R.string.give_a_name);

            itemName.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));

        } else {
            itemName.setBackgroundColor(Color.TRANSPARENT);
        }

        /* CHECK PRICE CHANGE */
        Double price = 0.0;

        String priceString = itemPriceUnit.getText().toString();

        try {
            itemPriceUnit.setBackgroundColor(Color.TRANSPARENT);

            price = Double.parseDouble(priceString);

        } catch (NumberFormatException e) {

            hasData = false;

            if (toastText.equals("")) {
                toastText += getString(R.string.give_valid_price);
            } else {
                toastText += "\n\n";
                toastText += getString(R.string.give_valid_price);
            }

            itemPriceUnit.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));

        }

        /* CHECK VALID QUANTITY */
        Double quantity = 0.0;

        String quantityString = itemCurrentQuantity.getText().toString();

        try {
            itemCurrentQuantity.setBackgroundColor(Color.TRANSPARENT);

            quantity = Double.parseDouble(quantityString);

        } catch (NumberFormatException e) {

            hasData = false;

            if (toastText.equals("")) {
                toastText += getString(R.string.give_valid_quantity);
            } else {
                toastText += "\n\n";
                toastText += getString(R.string.give_valid_quantity);
            }

            itemCurrentQuantity.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAlert, null));

        }

        /* IF EVERYTHING IS VALID */
        if (hasData) {

            /* SAVE PICTURE IF NEW ONE AND DELETE OLD ONE */
            String picturePath;
            if (!mCurrentPhotoPath.equals(mNewPhotoPath)) {
                if (!mCurrentPhotoPath.equals("noPicture")) {
                    File deleteFile = new File(mCurrentPhotoPath);
                    deleteFile.delete();
                }
                picturePath = mNewPhotoPath;
                try {
                    if (itemNewPictureBitmap != null) {
                        OutputStream oSteam;
                        oSteam = new FileOutputStream(imageNewFile);
                        itemNewPictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, oSteam);
                        oSteam.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (imageRotated) {
                    if (!mCurrentPhotoPath.equals("noPicture")) {
                        File deleteFile = new File(mCurrentPhotoPath);
                        deleteFile.delete();
                    }
                    try {
                        imageFile = createImageFile();
                        mCurrentPhotoPath = imageFile.getAbsolutePath();
                        if (itemPictureBitmap != null) {
                            OutputStream oSteam;
                            oSteam = new FileOutputStream(imageFile);
                            itemPictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, oSteam);
                            oSteam.close();
                        }
                        itemHasChanged = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                picturePath = mCurrentPhotoPath;
            }
            /* SET DATA TO SAVE */
            String currency = currencySpinner.getSelectedItem().toString();
            String unit = unitSpinner.getSelectedItem().toString();
            String supName = supplierName.getText().toString();
            String supPhone = supplierPhoneNumber.getText().toString();
            String supEmail = supplierEmailAddress.getText().toString();

            /* PUT VARIABLES IN VALUES */
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_ITEM_PICTURE, picturePath);
            values.put(InventoryEntry.COLUMN_ITEM_NAME, name);
            values.put(InventoryEntry.COLUMN_ITEM_UNIT, unit);
            values.put(InventoryEntry.COLUMN_ITEM_PRICE, price);
            values.put(InventoryEntry.COLUMN_ITEM_CURRENCY, currency);
            values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
            values.put(InventoryEntry.COLUMN_ITEM_SUP_NAME, supName);
            values.put(InventoryEntry.COLUMN_ITEM_SUP_PHONE, supPhone);
            values.put(InventoryEntry.COLUMN_ITEM_SUP_EMAIL, supEmail);

            if (currentItemUri == null) {

                /* SAVE ITEM IN ADD MODE */
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                if (newUri == null) {
                    showToast(getString(R.string.error_with_saving));
                } else {
                    showToast(getString(R.string.item_saved));
                }

            } else {
                /* SAVE ITEM IN EDIT MODE */
                if (itemHasChanged) {

                    int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);

                    if (rowsAffected == 0) {
                        showToast(getString(R.string.error_at_update));
                    } else {
                        showToast(getString(R.string.update_success));
                    }
                }
            }

            if (showCircleRevealAnim) {
                /* SHOW CIRCLE ANIMATION IN ADD ITEM */
                circleRevealAnimOut();

            } else {
                /* SHOW ANIMATION IN EDIT MODE */
                supportFinishAfterTransition();
            }

        } else {
            /* SHOW WARNINGS */
            showToast(toastText);

        }
    }

    /* LOAD DATA FROM DATABASE */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_PICTURE,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_UNIT,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_CURRENCY,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_SUP_NAME,
                InventoryEntry.COLUMN_ITEM_SUP_PHONE,
                InventoryEntry.COLUMN_ITEM_SUP_EMAIL
        };

        return new CursorLoader(this,
                currentItemUri,
                projection,
                null,
                null,
                null);
    }

    /* ON LOAD FINISHED */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {

            /* GET DATA FROM CURSOR */
            int pictureColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PICTURE);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int unitColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_UNIT);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int currencyColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_CURRENCY);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int supNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUP_NAME);
            int supPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUP_PHONE);
            int supEmailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUP_EMAIL);

            /* SET DATA TO VARIABLES */
            String picturePath = cursor.getString(pictureColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String unit = cursor.getString(unitColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            String currency = cursor.getString(currencyColumnIndex);
            Double quantity = cursor.getDouble(quantityColumnIndex);
            String supName = cursor.getString(supNameColumnIndex);
            String supPhone = cursor.getString(supPhoneColumnIndex);
            String supEmail = cursor.getString(supEmailColumnIndex);

            /* SET VARIABLES TO THE CORRECT VIEW */
            mCurrentPhotoPath = picturePath;
            mNewPhotoPath = picturePath;
            if (!mCurrentPhotoPath.equals(getString(R.string.no_picture))) {
                imageFile = new File(picturePath);
                itemPictureBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                itemPicture.setImageBitmap(itemPictureBitmap);
            }
            itemName.setText(name);
            unitSpinner.setSelection(unitAdapter.getPosition(unit));
            orderUnitSpinner.setSelection(unitAdapter.getPosition(unit));
            itemPriceUnit.setText("" + price);
            currencySpinner.setSelection(currencyAdapter.getPosition(currency));
            itemCurrentQuantity.setText("" + quantity);
            supplierName.setText(supName);
            supplierPhoneNumber.setText(supPhone);
            supplierEmailAddress.setText(supEmail);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {

        /* CHECK IF SOMETHING HAS CHANGED */
        if (!itemHasChanged) {

            if (showCircleRevealAnim) {

                circleRevealAnimOut();

            } else {
                supportFinishAfterTransition();
            }

        } else {

            /* SHOW DIALOG TO DISCARD OR KEEP EDITING */
            DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                    if (!mCurrentPhotoPath.equals(mNewPhotoPath)) {
                        if (imageNewFile.exists()) {
                            imageNewFile.delete();
                        }
                    }

                    itemPicture.setImageResource(R.drawable.take_photo);

                    if (showCircleRevealAnim) {

                        circleRevealAnimOut();

                    } else {
                        supportFinishAfterTransition();
                    }
                }
            };

            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    /* ANIMATION FROM FLOAT ACTION BUTTON */
    private void circleRevealAnimIn() {

        editView.post(new Runnable() {
            @Override
            public void run() {

                int finalRadius = Math.max(editView.getWidth(), editView.getHeight());

                Animator animator = ViewAnimationUtils.createCircularReveal(editView, animStartX, animStartY, 0, finalRadius);

                editView.setVisibility(View.VISIBLE);

                animator.start();
            }
        });

    }

    /* ANIMATION TO FLOAT ACTION BUTTON */
    private void circleRevealAnimOut() {

        int initialRadius = editView.getHeight();

        Animator animator = ViewAnimationUtils.createCircularReveal(editView, animStartX, animStartY, initialRadius, 0);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                editView.setVisibility(View.INVISIBLE);
                finish();
            }

        });

        animator.start();

    }

    private void showToast(String string) {

        Toast.makeText(EditActivity.this, string, Toast.LENGTH_LONG).show();

    }

    /* HANDEL RESTART DATA */
    @Override
    protected void onSaveInstanceState(Bundle outState) {


        outState.putString(getString(R.string.current_path), mCurrentPhotoPath);
        outState.putString(getString(R.string.new_path), mNewPhotoPath);
        outState.putParcelable(getString(R.string.camera_data), cameraData);
        outState.putBoolean(getString(R.string.camera_on), cameraOn);

        super.onSaveInstanceState(outState);
    }
}
