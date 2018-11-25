package com.ni3bobade.bookinventoryapp;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.ni3bobade.bookinventoryapp.data.BookContract.BookEntry;

import java.text.MessageFormat;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using layout specified in book_list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify
        TextView bookNameTextView = view.findViewById(R.id.bookNameTextView);
        TextView bookPriceTextView = view.findViewById(R.id.bookPriceTextView);
        TextView bookQuantityTextView = view.findViewById(R.id.bookQuantityTextView);
        TextView bookSupplierNameTextView = view.findViewById(R.id.bookSupplierNameTextView);
        Button saleButton = view.findViewById(R.id.saleButton);
        Button editButton = view.findViewById(R.id.editButton);


        // Find the columns of book attributes that we are interested in
        final int bookIdColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int bookPriceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int bookQuantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        int bookSupplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);

        // Read the book attributes from the Cursor for current book
        final int bookId = cursor.getInt(bookIdColumnIndex);
        String bookName = cursor.getString(bookNameColumnIndex);
        int bookPrice = cursor.getInt(bookPriceColumnIndex);
        final int bookQuantity = cursor.getInt(bookQuantityColumnIndex);
        String bookSupplierName = cursor.getString(bookSupplierNameColumnIndex);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookCatalogActivity bookCatalogActivity = (BookCatalogActivity) context;
                // Track the book sale
                bookCatalogActivity.bookSoldCount(bookId, bookQuantity);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditorActivity.class);
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookId);
                intent.setData(currentBookUri);
                context.startActivity(intent);
            }
        });

        // Update the TextViews with book attributes for current book
        bookNameTextView.setText(bookName);
        bookPriceTextView.setText(MessageFormat.format("${0}", bookPrice));
        bookQuantityTextView.setText(MessageFormat.format("{0}", bookQuantity));
        bookSupplierNameTextView.setText(bookSupplierName);
    }
}
