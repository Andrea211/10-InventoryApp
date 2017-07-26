package com.example.piotrpiro.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventory app.
 */
public final class InventoryContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.piotrpiro.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.piotrpiro.inventoryapp/inventoryapp/ is a valid path for
     * looking at item data. content://com.example.piotrpiro.inventoryapp/stuff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "stuff".
     */
    public static final String PATH_ITEMS = "inventoryapp";

    /**
     * To prevent someone from accidentally instantiating the contract class,
     * give it an empty constructor.
     */
    private InventoryContract() {
    }

    /**
     * Inner class that defines constant values for the inventoryapp database table.
     * Each entry in the table represents a single item.
     */
    public static final class InventoryEntry implements BaseColumns {

        // The content URI to access the item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        // The MIME type of the {@link #CONTENT_URI} for a list of items.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // The MIME type of the {@link #CONTENT_URI} for a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // Name of database table for items
        public final static String TABLE_NAME = "inventoryapp";

        /**
         * Unique ID number for the item (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the item.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_NAME = "name";

        /**
         * Quantity of the items.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_QUANTITY = "quantity";

        /**
         * Image of the item.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_IMAGE = "image";

        /**
         * Price of the item.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_PRICE = "price";

        /**
         * Supplier email.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_EMAIL = "supplier_email";

    }

}

