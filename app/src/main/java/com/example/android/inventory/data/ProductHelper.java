package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Created by Rachit on 12/12/2016.
 */
public class ProductHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="products.db";
    public static final int DATABASE_VERSION=1;
    public ProductHelper(Context context) {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }
    public static final String CREATE_TABLE =
            "CREATE TABLE "+ ProductEntry.TABLE_NAME+"("
            +ProductEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +ProductEntry.COLUMN_PRODUCT_NAME+" TEXT NOT NULL, "
            +ProductEntry.COLUMN_PRODUCT_QUANTITY+" INTEGER DEFAULT 0, "
            +ProductEntry.COLUMN_PRODUCT_PRICE+" INTEGER NOT NULL, " +
            ProductEntry.COLUMN_PRODUCT_IMAGE+" TEXT);";
    public static final String DELETE_TABLE=
            "DROP TABLE IF EXISTS "+ProductEntry.TABLE_NAME+";";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
