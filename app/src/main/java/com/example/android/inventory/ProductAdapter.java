package com.example.android.inventory;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

public class ProductAdapter extends CursorAdapter {
    ProductAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(
                R.layout.list_item,viewGroup,false);
    }

    @SuppressLint("SetTextI18n")
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
