package com.ni3bobade.bookinventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ni3bobade.bookinventoryapp.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = BookDbHelper.class.getSimpleName();

    // Name of the database file
    public static final String DATABASE_NAME = "inventory.db";

    // Database version
    public static final int DATABASE_VERSION = 1;

    // Constructor
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This is called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains SQL statement to create books table
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " (" + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, " + BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL DEFAULT 0, " + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " + BookEntry.COLUMN_BOOK_SUPPLIER_NAME + " TEXT NOT NULL, " + BookEntry.COLUMN_BOOK_SUPPLIER_CONTACT + " TEXT NOT NULL); ";

        // Execute SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there is nothing to be done here
    }

}
