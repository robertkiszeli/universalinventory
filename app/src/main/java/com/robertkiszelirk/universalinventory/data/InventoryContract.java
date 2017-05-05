package com.robertkiszelirk.universalinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/* STATIC STRINGS FOR DATABASE TABLE AND URI */
public final class InventoryContract {

    private InventoryContract() {
    }

    /* TABLE OWNER APP */
    static final String CONTENT_AUTHORITY = "com.robertkiszelirk.universalinventory";

    /* BASE URI */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /* TABLE NAME */
    static final String PATH_INVENTORY = "inventory";

    /* DATA FOR TABLE USE */
    public static final class InventoryEntry implements BaseColumns {

        /* URI TO REACH TABLE */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /* TABLE NAME */
        final static String TABLE_NAME = "inventory";

        /* EACH COLUMN IN TABLE */
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_PICTURE = "picture";
        public final static String COLUMN_ITEM_NAME = "name";
        public final static String COLUMN_ITEM_UNIT = "unit";
        public final static String COLUMN_ITEM_PRICE = "price";
        public final static String COLUMN_ITEM_CURRENCY = "currency";
        public final static String COLUMN_ITEM_QUANTITY = "quantity";
        public final static String COLUMN_ITEM_SUP_NAME = "supname";
        public final static String COLUMN_ITEM_SUP_PHONE = "supphone";
        public final static String COLUMN_ITEM_SUP_EMAIL = "supemail";

        /* URI FOR TABLE */
        static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /* URI FOR SELECTED ROWS */
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


    }
}
