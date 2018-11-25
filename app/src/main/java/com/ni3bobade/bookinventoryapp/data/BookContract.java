package com.ni3bobade.bookinventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    // To prevent someone from accidentally instantiating contract class, give it an empty constructor
    private BookContract() {

    }

    // Content authority
    public static final String CONTENT_AUTHORITY = "com.ni3bobade.bookinventoryapp";

    // Using CONTENT_AUTHORITY to create base of all URI's
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible path appended to base content URI
    public static final String PATH_BOOKS = "books";

    // Inner class representing constant values for books database table
    // Each entry in the table represents single book
    public static final class BookEntry implements BaseColumns {

        // Content URI to access book data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // Name of the database table
        public static final String TABLE_NAME = "books";

        // Unique ID number for the book
        // Type: INTEGER
        public static final String _ID = BaseColumns._ID;

        // Name of the book
        // Type: TEXT
        public static final String COLUMN_BOOK_NAME = "bookName";

        // Price of the book
        // Type: INTEGER
        public static final String COLUMN_BOOK_PRICE = "bookPrice";

        // Quantity of the book
        // Type: INTEGER
        public static final String COLUMN_BOOK_QUANTITY = "bookQuantity";

        // Name of the book supplier
        // Type: TEXT
        public static final String COLUMN_BOOK_SUPPLIER_NAME = "bookSupplierName";

        // Book Supplier phone no.
        // Type: TEXT
        public static final String COLUMN_BOOK_SUPPLIER_CONTACT = "bookSupplierPhoneNumber";
    }
}

