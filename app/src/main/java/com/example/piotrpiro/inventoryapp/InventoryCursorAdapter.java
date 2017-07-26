package com.example.piotrpiro.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.piotrpiro.inventoryapp.data.InventoryContract.InventoryEntry;

import static android.content.ContentValues.TAG;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = InventoryCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.current_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView saleImageView = (ImageView) view.findViewById(R.id.sale);

        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);

        // Read the item attributes from the Cursor for the current item
        final String itemName = cursor.getString(nameColumnIndex);
        String itemQuantity = cursor.getString(quantityColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final int newQuantity;
        final long itemId = cursor.getLong(idColumnIndex);

        // If the item quantity is empty string or null, then use some default text
        // that says "0", so the TextView isn't blank.
        if (TextUtils.isEmpty(itemQuantity)) {
            itemQuantity = context.getString(R.string.zero_quantity);
        }

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        quantityTextView.setText(itemQuantity);
        priceTextView.setText(itemPrice);

        // Sale button reduces the quantity of the record in stock by -1.
        saleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (quantity >= 1) {

                    Log.i(LOG_TAG, "TEST: On sale click Quantity is: " + quantity);
                    int newQuantity = quantity - 1;
                    Log.i(LOG_TAG, "TEST: On sale click Updated Quantity is: " + newQuantity);

                    // Update table with new stock of the product
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                    Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemId);
                    Log.i(LOG_TAG, "TEST: On sale click ContentUri is: " + InventoryEntry.CONTENT_URI);
                    Log.i(LOG_TAG, "TEST: On sale click ContentUri_ID is: " + itemUri);
                    Log.i(LOG_TAG, "TEST: On sale click Item Name is: " + itemName);


                    int numRowsUpdated = context.getContentResolver().update(itemUri, contentValues, null, null);
                    Log.i(LOG_TAG, "TEST: number Rows Updated: " + numRowsUpdated);

                    if (!(numRowsUpdated > 0)) {
                        Log.e(TAG, context.getString(R.string.editor_update_item_failed));
                    }
                } else if (!(quantity >= 1)) {
                    int quantity = 0;
                    Toast.makeText(context, R.string.sold_out, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
