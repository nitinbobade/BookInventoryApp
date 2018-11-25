package com.ni3bobade.bookinventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ni3bobade.bookinventoryapp.data.BookContract.BookEntry;

public class BookCatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Identifier for book data loader
    public static final int BOOK_LOADER = 0;

    ListView bookListView;

    // Adapter for the ListView
    BookCursorAdapter bookCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openEditorActivityIntent = new Intent(BookCatalogActivity.this, EditorActivity.class);
                startActivity(openEditorActivityIntent);
            }
        });

        // Find ListView which will be populated with book data
        bookListView = findViewById(R.id.bookListView);

        // Find and set the empty view on the ListView, so that it only shows when list has 0 items.
        View emptyStateView = findViewById(R.id.emptyStateView);
        bookListView.setEmptyView(emptyStateView);

        // Setup an adapter to create a list item for each row of book data in the cursor
        bookCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(bookCursorAdapter);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openEditorActivityOnItemClickIntent = new Intent(BookCatalogActivity.this, BookDetailActivity.class);

                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // Set the URI on data field of the intent
                openEditorActivityOnItemClickIntent.setData(currentBookUri);
                startActivity(openEditorActivityOnItemClickIntent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT,

        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, BookEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update BookCursorAdapter with this new cursor containing updated book data
        bookCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        bookCursorAdapter.swapCursor(null);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_catalog, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:

                if (bookListView.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.book_inventory_is_empty), Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    showDeleteConfirmationDialog();
                }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_all_book_entries));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Delete deleteBookButton, so delete the book
                deleteAllBooks();
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

    // Keep the track of books sold
    public void bookSoldCount(int bookId, int bookQuantity) {
        bookQuantity = bookQuantity - 1;
        if (bookQuantity >= 0) {
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity);
            Uri updateUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookId);
            getContentResolver().update(updateUri, values, null, null);
            Toast.makeText(this, getString(R.string.one_copy_sold), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to delete all the books in the database
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v("BookCatalogActivity", rowsDeleted + " rows deleted from book database");
    }
}
