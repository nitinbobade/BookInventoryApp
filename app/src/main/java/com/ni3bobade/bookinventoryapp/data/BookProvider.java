package com.ni3bobade.bookinventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.ni3bobade.bookinventoryapp.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    // Tag for Log Messages
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    // URI matcher code for content URI for books table
    public static final int BOOKS = 100;

    // URI matcher code for content URI for single book in the books table
    public static final int BOOK_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer
    static {

        // Accessing multiple rows of Books table
        uriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);

        // Accessing one single row of Books table
        uriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    // Database helper object
    private BookDbHelper bookDbHelper;

    @Override
    public boolean onCreate() {
        bookDbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase db = bookDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        // Set notification URI on the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    // Insert a book into the database
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        // check if the name is not null
        String bookName = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (bookName == null) {
            throw new IllegalArgumentException("Book name is required");
        }

        // Check if book price is greater than zero
        Integer bookPrice = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
        if (bookPrice != null && bookPrice < 0) {
            throw new IllegalArgumentException("A valid book price is required");
        }

        // Check if book quantity is greater than zero
        Integer bookQuantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if (bookQuantity != null && bookQuantity < 0) {
            throw new IllegalArgumentException("A valid book quantity is required");
        }

        // check if the supplier name is not null
        String bookSupplierName = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
        if (bookSupplierName == null) {
            throw new IllegalArgumentException("Book supplier name is required");
        }

        // check if the supplier contact is not null
        String bookSupplierContact = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT);
        if (bookSupplierContact == null) {
            throw new IllegalArgumentException("Book supplier contact is required");
        }

        // Get writable database
        SQLiteDatabase db = bookDbHelper.getWritableDatabase();

        // Insert the new book with given values
        long id = db.insert(BookEntry.TABLE_NAME, null, values);

        // If the ID is -1, the insertion fails.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for book content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // check if the name is not null
        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String bookName = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if (bookName == null) {
                throw new IllegalArgumentException("Book name is required");
            }
        }

        // Check if book price is greater than zero
        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            Integer bookPrice = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
            if (bookPrice != null && bookPrice < 0) {
                throw new IllegalArgumentException("A valid book price is required");
            }
        }

        // Check if book quantity is greater than zero
        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            Integer bookQuantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (bookQuantity != null && bookQuantity < 0) {
                throw new IllegalArgumentException("A valid book quantity is required");
            }
        }

        // check if the supplier name is not null
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPPLIER_NAME)) {
            String bookSupplierName = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            if (bookSupplierName == null) {
                throw new IllegalArgumentException("Book supplier name is required");
            }
        }

        // check if the supplier contact is not null
        if (values.containsKey(BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT)) {
            String bookSupplierContact = values.getAsString(BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT);
            if (bookSupplierContact == null) {
                throw new IllegalArgumentException("Book supplier contact is required");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise get writable database to update data
        SQLiteDatabase db = bookDbHelper.getWritableDatabase();

        // Perform the update on database and get the number of rows affected
        int rowsUpdated = db.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        // If one or more rows were updated, notify all listeners about the data change at given URI
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;


    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writable database to update data
        SQLiteDatabase db = bookDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match selection and selectionArgs
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        }

        // If one or more rows were deleted, notify all listeners about the data change at given URI
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {

        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
