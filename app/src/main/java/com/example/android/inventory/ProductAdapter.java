package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;
import com.example.android.inventory.data.ProductContract.ProductEntry;
import com.example.android.inventory.data.ProductHelper;
import com.example.android.inventory.data.ProductProvider;

import java.io.File;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static android.support.v4.app.ActivityCompat.startIntentSenderForResult;

public class ProductAdapter extends CursorAdapter {
    public ProductAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(
                R.layout.list_item,viewGroup,false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView productName = (TextView) view.findViewById(R.id.product_name);
        final TextView productQuantity = (TextView) view.findViewById(R.id.product_quantity);
        TextView productPrice = (TextView) view.findViewById(R.id.product_price);
        View sellButton= view.findViewById(R.id.sell);
        final int productId=cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID));

        final String productNameText = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        final int productQuantityText = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        final int productPriceText = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity=productQuantityText;
                if(quantity<=0){
                    Toast.makeText(context,"Buy some products first",Toast.LENGTH_SHORT).show();
                }else{
                 quantity = productQuantityText - 1;
                ContentValues values=new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                Uri currentUri=ContentUris.withAppendedId(ProductEntry.CONTENT_URI,productId);
                context.getContentResolver().update(currentUri,values,null,null);
                productQuantity.setText(quantity+" Left");}
            }
        });

        productName.setText(productNameText);
        productPrice.setText("Rs. "+productPriceText);
        productQuantity.setText(productQuantityText +" Left");
    }
}
