package com.ni3bobade.bookinventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.app.LoaderManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ni3bobade.bookinventoryapp.data.BookContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the book data loader
    public static final int EXISTING_BOOK_LOADER = 0;

    // Content URI for existing book
    private Uri currentBookUri;

    private TextInputLayout bookNameTextInput;
    private TextInputLayout bookPriceTextInput;
    private TextInputLayout bookQuantityTextInput;
    private TextInputLayout bookSupplierNameTextInput;
    private TextInputLayout bookSupplierContactTextInput;

    private EditText bookNameEditText;
    private EditText bookPriceEditText;
    private EditText bookQuantityEditText;
    private EditText bookSupplierNameEditText;
    private EditText bookSupplierContactEditText;

    // Boolean flag that keeps track of whether the book has been edited or not
    private boolean bookHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            bookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent to figure out whether we are creating a new book or editing the existing one
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        // Find all relevant views that are needed to read user input
        bookNameTextInput = findViewById(R.id.bookNameTextInput);
        bookPriceTextInput = findViewById(R.id.bookPriceTextInput);
        bookQuantityTextInput = findViewById(R.id.bookQuantityTextInput);
        bookSupplierNameTextInput = findViewById(R.id.bookSupplierNameTextInput);
        bookSupplierContactTextInput = findViewById(R.id.bookSupplierContactTextInput);

        bookNameEditText = findViewById(R.id.bookNameEditText);
        bookPriceEditText = findViewById(R.id.bookPriceEditText);
        bookQuantityEditText = findViewById(R.id.bookQuantityEditText);
        bookSupplierNameEditText = findViewById(R.id.bookSupplierNameEditText);
        bookSupplierContactEditText = findViewById(R.id.bookSupplierContactEditText);

        Button addBookToInventoryButton = findViewById(R.id.addBookToInventoryButton);
        addBookToInventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookToInventory();
            }
        });

        // If the intent does not contain a book content URI, create a new book
        if (currentBookUri == null) {
            // This is a new book, so change the app bar title to say Add a Book
            setTitle(getString(R.string.add_book_appbar_title));
            addBookToInventoryButton.setText(getString(R.string.add_book));

            // Invalidate options menu, so the delete menu option can be hidden
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, change app bar title to Edit Book
            setTitle(R.string.edit_book_appbar_title);
            addBookToInventoryButton.setText(getString(R.string.update_book));

            // Initialize a loader to read the book data from the database and display current values in editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Setup OnTouchListeners on all the input fields, to determine if the user has touched or modified them
        // This will let us know any unsaved changes, if the user tries to leave the editor without saving
        bookNameEditText.setOnTouchListener(touchListener);
        bookPriceEditText.setOnTouchListener(touchListener);
        bookQuantityEditText.setOnTouchListener(touchListener);
        bookSupplierNameEditText.setOnTouchListener(touchListener);
        bookSupplierContactEditText.setOnTouchListener(touchListener);

    }

    private void addBookToInventory() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String bookNameString;
        if (TextUtils.isEmpty(bookNameEditText.getText())) {
            bookNameTextInput.setError(getString(R.string.book_name_required));
            return;
        } else {
            bookNameTextInput.setError(null);   // Clear the error
            bookNameString = bookNameEditText.getText().toString().trim();
        }

        String bookPriceString;
        if (TextUtils.isEmpty(bookPriceEditText.getText())) {
            bookPriceTextInput.setError(getString(R.string.book_price_required));
            return;
        } else {
            bookPriceTextInput.setError(null);   // Clear the error
            bookPriceString = bookPriceEditText.getText().toString().trim();
        }

        int bookPrice = Integer.parseInt(bookPriceString);
        if (bookPrice < 0) {
            Toast.makeText(this, getString(R.string.book_price_can_not_be_less), Toast.LENGTH_SHORT).show();
        }

        String bookQuantityString;
        if (TextUtils.isEmpty(bookQuantityEditText.getText())) {
            bookQuantityTextInput.setError(getString(R.string.book_quantity_required));
            return;
        } else {
            bookQuantityTextInput.setError(null);   // Clear the error
            bookQuantityString = bookQuantityEditText.getText().toString().trim();
        }

        int bookQuantity = Integer.parseInt(bookQuantityString);
        if (bookQuantity < 0) {
            Toast.makeText(this, getString(R.string.book_quantity_can_not_be_less), Toast.LENGTH_SHORT).show();
        }

        String bookSupplierNameString;
        if (TextUtils.isEmpty(bookSupplierNameEditText.getText())) {
            bookSupplierNameTextInput.setError(getString(R.string.book_supplier_name_required));
            return;
        } else {
            bookSupplierNameTextInput.setError(null);   // Clear the error
            bookSupplierNameString = bookSupplierNameEditText.getText().toString().trim();
        }

        String bookSupplierContactString;
        if (TextUtils.isEmpty(bookSupplierContactEditText.getText())) {
            bookSupplierContactTextInput.setError(getString(R.string.book_supplier_contact_required));
            return;
        } else {
            bookSupplierContactTextInput.setError(null);   // Clear the error
            bookSupplierContactString = bookSupplierContactEditText.getText().toString().trim();
        }

        // Create a ContentValues object where column names are keys,
        // and book attributes from editor are the values
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, bookNameString);
        values.put(BookEntry.COLUMN_BOOK_PRICE, bookPrice);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, bookSupplierNameString);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT, bookSupplierContactString);

        // Determine if this is a new or existing book
        if (currentBookUri == null) {
            // This is a new book
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a Toast depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, there was an error with insertion
                Toast.makeText(this, getString(R.string.error_saving_book), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise insertion was successful
                Toast.makeText(this, getString(R.string.book_saved), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Otherwise this is an existing book, so update the book with content URI
            int rowsAffected = getContentResolver().update(currentBookUri, values, null, null);

            // Show a Toast depending on whether or not the update was successful
            if (rowsAffected == 0) {
                // If no rows were affected, there was an error with update
                Toast.makeText(this, getString(R.string.error_updating_book), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise update was successful
                Toast.makeText(this, getString(R.string.book_updated), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    // This method is called after invalidateOptionsMenu(), so that menu can be updated
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the Delete menu deleteBookButton
        if (currentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on Delete menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If the book hasn't changed, continue navigating to parent activity
                if (!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user
                DialogInterface.OnClickListener discardButtonOnClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Discard deleteBookButton, navigate to parent activity
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                // Show a dialog that notifies the user that they have unsaved changes
                showUnsavedChangesDialog(discardButtonOnClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // This method is called when back deleteBookButton is pressed
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back deleteBookButton press
        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user
        DialogInterface.OnClickListener discardButtonOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Discard deleteBookButton, close current activity
                finish();
            }
        };

        // Show a dialog that notifies the user that they have unsaved changes
        showUnsavedChangesDialog(discardButtonOnClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all book attributes, define a projection that contains all columns from book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT,

        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, currentBookUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of cursor and reading data from it
        if (data.moveToFirst()) {
            // figure out the index of each column
            int idColumnIndex = data.getColumnIndex(BookEntry._ID);
            int bookNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int bookPriceColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int bookQuantityColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int bookSupplierNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int bookSupplierContactColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT);

            // Extract out the value from cursor for given column index
            int currentID = data.getInt(idColumnIndex);
            String currentBookName = data.getString(bookNameColumnIndex);
            int currentBookPrice = data.getInt(bookPriceColumnIndex);
            int currentBookQuantity = data.getInt(bookQuantityColumnIndex);
            String currentBookSupplierName = data.getString(bookSupplierNameColumnIndex);
            String currentBookSupplierContact = data.getString(bookSupplierContactColumnIndex);

            // Update the views on the screen with values from database
            bookNameEditText.setText(currentBookName);
            bookPriceEditText.setText(Integer.toString(currentBookPrice));
            bookQuantityEditText.setText(Integer.toString(currentBookQuantity));
            bookSupplierNameEditText.setText(currentBookSupplierName);
            bookSupplierContactEditText.setText(currentBookSupplierContact);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // If the loader is invalidated, clear out all the data from input fields
        bookNameEditText.setText("");
        bookPriceEditText.setText("");
        bookQuantityEditText.setText("");
        bookSupplierNameEditText.setText("");
        bookSupplierContactEditText.setText("");

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonOnClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.discard_changes));
        builder.setPositiveButton(getString(R.string.discard), discardButtonOnClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Keep Editing deleteBookButton, dismiss the dialog and continue editing the book
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_this_book));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Delete deleteBookButton, so delete the book
                deleteBook();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Keep Editing deleteBookButton, dismiss the dialog and continue editing the book
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Perform the deletion of the book
    private void deleteBook() {
        // Only perform the delete if this is an existing book
        if (currentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);

            // Show a Toast depending on whether or not the delete operation was successful
            if (rowsDeleted == 0) {
                // If no rows were deleted, there was an error with delete operation
                Toast.makeText(this, getString(R.string.error_deleting_book), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.book_deleted), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
