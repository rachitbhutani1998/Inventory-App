package com.example.android.inventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private EditText mProductName;
    private EditText mProductQuantity;
    private EditText mProductPrice;
    private EditText mSupplierText;
    private EditText mContactText;
    private Uri mCurrentProductUri;
    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };
    int quantity = 0;
    Uri imageUri;

    private String supplierContact;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri == null) {
            imageUri = Uri.parse("android.resource://" + this.getPackageName() + "/drawable/pic");
            setTitle("Add Product");
        } else {
            setTitle("Edit Product");
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
        mProductName = (EditText) findViewById(R.id.product_name_text);
        mProductPrice = (EditText) findViewById(R.id.price_text);
        mContactText = (EditText) findViewById(R.id.contact_text);
        mSupplierText = (EditText) findViewById(R.id.supplier_text);
        mProductQuantity = (EditText) findViewById(R.id.quantity_text);

        mSupplierText.setOnTouchListener(mTouchListener);
        mContactText.setOnTouchListener(mTouchListener);
        mProductName.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);

        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mProductQuantity.getText().toString())) {
                    mProductQuantity.setText("0");
                }
                quantity = Integer.parseInt(mProductQuantity.getText().toString());
                if (quantity >= 500) {
                    Toast.makeText(EditActivity.this, "More quantity cannot be added", Toast.LENGTH_SHORT).show();
                    return;
                }
                quantity += 1;
                mProductQuantity.setText("" + quantity);
            }
        });
        Button minusButton = (Button) findViewById(R.id.sub_button);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mProductQuantity.getText().toString())) {
                    mProductQuantity.setText("0");
                }
                quantity = Integer.parseInt(mProductQuantity.getText().toString());
                if (quantity <= 0) {
                    Toast.makeText(EditActivity.this, "Quantity cannot be less than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                quantity -= 1;
                mProductQuantity.setText("" + quantity);
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_product);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteProduct() {
        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void saveProduct() {
        String nameString = mProductName.getText().toString().trim();
        String priceString = mProductPrice.getText().toString().trim();
        String quantityString = mProductQuantity.getText().toString().trim();
        String supplierName = mSupplierText.getText().toString().trim();
        String supplierContact = mContactText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString)
                || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierContact)
                || TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, "The fields cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(ProductEntry.COLUMN_SUPPLIER_CONTACT, supplierContact);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Error Saving Data", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product Added Successfully", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, "Error Updating Product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, " Product Updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.order_more:
                orderMore();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_CONTACT
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null,
                null, null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int contactColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_CONTACT);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            mProductName.setText(name);
            mProductQuantity.setText(Integer.toString(quantity));
            mProductPrice.setText(Integer.toString(price));
            mContactText.setText(cursor.getString(contactColumnIndex));
            mSupplierText.setText(cursor.getString(supplierColumnIndex));
            supplierContact = cursor.getString(contactColumnIndex);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mProductPrice.setText("");
        mProductQuantity.setText("");
        mSupplierText.setText("");
        mContactText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void orderMore() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + supplierContact));
        if (intent.resolveActivity(getPackageManager()) != null) {
            if (!TextUtils.isEmpty(supplierContact))
                startActivity(intent);
            else Toast.makeText(this, "Required supplier's contact.", Toast.LENGTH_SHORT).show();
        }
    }
}
