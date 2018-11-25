package com.ni3bobade.bookinventoryapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ni3bobade.bookinventoryapp.data.BookContract.BookEntry;

import java.text.MessageFormat;

public class BookDetailActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for the book data loader
    public static final int EXISTING_BOOK_LOADER = 0;

    // Content URI for existing book
    private Uri currentBookUri;

    private TextView bookDetailsNameTextView;
    private TextView bookDetailsPriceTextView;
    private TextView bookDetailsQuantityTextView;
    private TextView bookDetailsSupplierNameTextView;
    private TextView bookDetailsSupplierContactTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        bookDetailsNameTextView = findViewById(R.id.bookDetailsNameTextView);
        bookDetailsPriceTextView = findViewById(R.id.bookDetailsPriceTextView);
        bookDetailsQuantityTextView = findViewById(R.id.bookDetailsQuantityTextView);
        bookDetailsSupplierNameTextView = findViewById(R.id.bookDetailsSupplierNameTextView);
        bookDetailsSupplierContactTextView = findViewById(R.id.bookDetailsSupplierContactTextView);

        Button deleteBookButton = findViewById(R.id.deleteBookButton);

        Intent intent = getIntent();
        currentBookUri = intent.getData();

        // If the intent does not contain a book content URI, create a new book
        if (currentBookUri == null) {
            invalidateOptionsMenu();
        } else {
            // Initialize a loader to read the book data from the database and display current values in editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        deleteBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
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
            int bookNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int bookPriceColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int bookQuantityColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int bookSupplierNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int bookSupplierContactColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT);

            // Extract out the value from cursor for given column index
            String currentBookName = data.getString(bookNameColumnIndex);
            int currentBookPrice = data.getInt(bookPriceColumnIndex);
            final int currentBookQuantity = data.getInt(bookQuantityColumnIndex);
            String currentBookSupplierName = data.getString(bookSupplierNameColumnIndex);
            final String currentBookSupplierContact = data.getString(bookSupplierContactColumnIndex);

            // Update the views on the screen with values from database
            bookDetailsNameTextView.setText(currentBookName);
            bookDetailsPriceTextView.setText(MessageFormat.format("${0}", currentBookPrice));
            bookDetailsQuantityTextView.setText(MessageFormat.format("{0}", currentBookQuantity));
            bookDetailsSupplierNameTextView.setText(currentBookSupplierName);
            bookDetailsSupplierContactTextView.setText(currentBookSupplierContact);

            ImageView bookDetailsIncrementQuantityButtonImageView = findViewById(R.id.bookDetailsIncrementQuantityButtonImageView);
            bookDetailsIncrementQuantityButtonImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    incrementBookQuantity(currentBookQuantity);
                }
            });

            ImageView bookDetailsDecrementQuantityButtonImageView = findViewById(R.id.bookDetailsDecrementQuantityButtonImageView);
            bookDetailsDecrementQuantityButtonImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    decrementBookQuantity(currentBookQuantity);
                }
            });

            bookDetailsSupplierContactTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.fromParts(getString(R.string.tel), currentBookSupplierContact, null));
                    startActivity(intent);
                }
            });
        }
    }

    private void incrementBookQuantity(int bookQuantity) {
        bookQuantity = bookQuantity + 1;
        if (bookQuantity >= 0) {
            updateBook(bookQuantity);
            Toast.makeText(this, getString(R.string.book_quantity_increment_successful), Toast.LENGTH_SHORT).show();
        }
    }

    private void decrementBookQuantity(int bookQuantity) {
        bookQuantity = bookQuantity - 1;
        if (bookQuantity >= 0) {
            updateBook(bookQuantity);
            Toast.makeText(this, getString(R.string.book_quantity_decrement_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBook(int bookQuantity) {
        if (currentBookUri == null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity);

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
            }
        }
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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from TextView
        bookDetailsNameTextView.setText("");
        bookDetailsPriceTextView.setText("");
        bookDetailsQuantityTextView.setText("");
        bookDetailsSupplierNameTextView.setText("");
        bookDetailsSupplierContactTextView.setText("");
    }
}
