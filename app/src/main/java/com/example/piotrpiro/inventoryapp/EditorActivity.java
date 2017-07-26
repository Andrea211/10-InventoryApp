package com.example.piotrpiro.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.piotrpiro.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentValues.TAG;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    // Identifier for the item image data loader
    public static final int IMAGE_GALLERY_REQUEST = 20;

    // Identifier for the item data loader
    private static final int EXISTING_ITEM_LOADER = 0;

    // Identifier for the item image URI loader
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";

    final Context mContext = this;

    // Content URI for the existing item image (null if it's a new record)
    private Uri mImageUri;

    // Image Path of the item fetched from the Uri
    private String imagePath;

    // Bitmap value of the image fetched from the Uri
    private Bitmap image;

    // Content URI for the existing item (null if it's a new record)
    private Uri mCurrentItemUri;

    // EditText field to enter the item name
    EditText mNameEditText = (EditText) findViewById(R.id.edit_item_name);

    // EditText field to enter the item quantity
    EditText mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);

    // EditText field to enter the item price
    EditText mPriceEditText = (EditText) findViewById(R.id.edit_item_price);

    // ImageView field to enter the item image
    ImageView mItemImage = (ImageView) findViewById(R.id.edit_item_image);

    // EditText field to enter the supplier email
    EditText mContactEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);

    // Button to add an image to the edit record activity
    Button mAddImage = (Button) findViewById(R.id.add_image);

    // Button to order more records from the supplier
    Button mOrder = (Button) findViewById(R.id.email_button);

    // Button to increase Stock
    Button mAddStock = (Button) findViewById(R.id.plus);

    // Button to decrease Stock
    Button mMinusStock = (Button) findViewById(R.id.minus);

    // Boolean flag that keeps track of whether the item has been edited (true) or not (false)
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent DOES NOT contain an item content URI, then we know that we are
        // creating a new item.
        if (mCurrentItemUri == null) {
            // This is a new item, so change the app bar to say "Add an Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a record that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            setTitle(getString(R.string.editor_activity_title_edit_item));

            // Initialize a loader to read the record data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mItemImage.setOnTouchListener(mTouchListener);
        mContactEmailEditText.setOnTouchListener(mTouchListener);
        mAddImage.setOnTouchListener(mTouchListener);
        mOrder.setOnTouchListener(mTouchListener);

        //Open camera when you press on Add image button
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Invoke an implicit intent to open the photo gallery
                Intent openPhotoGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                //Where do we find the data?
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                //Get a String of the pictureDirectoryPath
                String pictureDirectoryPath = pictureDirectory.getPath();

                //Get the Uri representation
                Uri data = Uri.parse(pictureDirectoryPath);

                //Set the data and type
                openPhotoGallery.setDataAndType(data, "image/*");

                //We will invoke this activity and get something back from it
                startActivityForResult(openPhotoGallery, IMAGE_GALLERY_REQUEST);
            }

        });

        //Open the email app to send a message with pre populated fields
        mOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Invoke an implicit intent to send an email
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                String to = mContactEmailEditText.getText().toString();
                String itemName = mNameEditText.getText().toString();
                String quantity = mQuantityEditText.getText().toString();
                String sep = System.getProperty("line.separator");
                String message = "Dear Sir/Madame," + sep + "I would like to order " + quantity +
                        " more pieces of " + itemName + ". " + sep + "Regards," + sep + "Andrea";
                emailIntent.setData(Uri.parse("mailto:" + to));

                try {
                    startActivity(emailIntent);
                    finish();
                    Log.i(LOG_TAG, "Finished sending email...");
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(EditorActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null)
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = mItemImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mItemImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mItemImage.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mItemImage));
                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //if we are here our request was successful
        if (requestCode == IMAGE_GALLERY_REQUEST && (resultCode == RESULT_OK)) {
            try {
                //this is the address of the image on the sd cards
                mImageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                imagePath = mImageUri.toString();
                //Declare a stream to read the data from the card
                InputStream inputStream;
                //We are getting an input stream based on the Uri of the image
                inputStream = getContentResolver().openInputStream(mImageUri);
                //Get a bitmap from the stream
                image = BitmapFactory.decodeStream(inputStream);
                //Show the image to the user
                mItemImage.setImageBitmap(image);
                imagePath = mImageUri.toString();
                try {
                    getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                mItemImage.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mItemImage));

            } catch (Exception e) {
                e.printStackTrace();
                //Show the user a Toast mewssage that the Image is not available
                Toast.makeText(EditorActivity.this, "Unable to open image", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Method to add clear top flag so it doesn't create new instance of parent
     *
     * @return intent
     */
    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

    public Bitmap getBitmapFromUri(Uri uri, Context context, ImageView imageView) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * Get user input from editor and save record into database.
     */
    private void saveRecord() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String itemNameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierEmailString = mContactEmailEditText.getText().toString().trim();


        if ((!TextUtils.isEmpty(itemNameString)) && (!TextUtils.isEmpty(quantityString)) && (!TextUtils.isEmpty(imagePath)) &&
                (!TextUtils.isEmpty(priceString)) && (!TextUtils.isEmpty(supplierEmailString))) {
            // Exit activity only when all the fields have been filled
            finish();

        } else {
            // Check if this is supposed to be a new record
            // and check if all the fields in the editor are blank
            if (mCurrentItemUri == null ||
                    TextUtils.isEmpty(itemNameString) || TextUtils.isEmpty(quantityString) ||
                    TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierEmailString)) {
                // if any of the fields are empty le the user know with a Toast message
                Toast.makeText(getApplicationContext(), "Please fill in all the missing entry fields", Toast.LENGTH_LONG).show();
            }
        }
        //make sure the image uri is not null
        if (mImageUri == null) {
            return;
        }

        // Get the imagePath
        imagePath = mImageUri.toString();

        Log.i(LOG_TAG, "TEST: Album Cover string is: " + imagePath);

        // Create a ContentValues object where column names are the keys,
        // and record attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, itemNameString);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceString);

        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);

        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, price);
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE, imagePath);
        values.put(InventoryEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString );


        // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a NEW item, so insert a new record into the provider,
            // returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();

            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();

            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new record, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save record to database
                saveRecord();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the record hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the record hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all record attributes, define a projection that contains
        // all columns from the record table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_SUPPLIER_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,      // Query the content URI for the current record
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        ViewTreeObserver viewTreeObserver = mItemImage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mItemImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mItemImage.setImageBitmap(getBitmapFromUri(mImageUri, mContext, mItemImage));
            }
        });

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of record attributes that we're interested in
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int itemNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);
            int supplierEmailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_EMAIL);

            // Extract out the value from the Cursor for the given column index
            final long recordId = cursor.getLong(idColumnIndex);
            String itemName = cursor.getString(itemNameColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            final String image = cursor.getString(imageColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(itemName);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            mContactEmailEditText.setText(supplierEmail);
            mItemImage.setImageBitmap(getBitmapFromUri(Uri.parse(image), mContext, mItemImage));
            mImageUri = Uri.parse(image);

            mAddStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quantity >= 0) {
                        int newQuantity = quantity + 1;
                        ContentValues values = new ContentValues();
                        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                        Uri recordUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, recordId);
                        int numRowsUpdated = EditorActivity.this.getContentResolver().update(recordUri, values, null, null);
                        if (!(numRowsUpdated > 0)) {
                            Log.e(TAG, EditorActivity.this.getString(R.string.editor_update_item_failed));
                        }
                    }
                    int newQuantity = 0;

                }
            });

            mMinusStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quantity >= 1) {
                        int newQuantity = quantity - 1;
                        ContentValues values = new ContentValues();
                        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                        Uri recordUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, recordId);
                        int numRowsUpdated = EditorActivity.this.getContentResolver().update(recordUri, values, null, null);
                        if (!(numRowsUpdated > 0)) {
                            Log.e(TAG, EditorActivity.this.getString(R.string.editor_update_item_failed));
                        }
                    } else if (!(quantity >= 1)) {
                        Toast.makeText(EditorActivity.this, getString(R.string.negative_stock), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mContactEmailEditText.setText("");}

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the record.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this record.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the record.
                deleteRecord();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);


        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the record in the database.
     */
    private void deleteRecord() {
        // Only perform the delete if this is an existing record.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the record at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentRecordUri
            // content URI already identifies the record that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}