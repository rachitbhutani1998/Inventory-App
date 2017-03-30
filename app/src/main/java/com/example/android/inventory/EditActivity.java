package com.example.android.inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.io.File;
//
//import static android.support.v4.app.ActivityCompat.startActivityForResult;
//import static android.support.v4.content.ContextCompat.getDrawable;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private EditText mProductName;
    private EditText mProductQuantity;
    private EditText mProductPrice;
    private Uri mCurrentProductUri;
    private ImageView mImageView;
    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };
    int quantity = 0;
    private static final int TAKE_PICTURE=1;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri == null) {
            imageUri= Uri.parse("android.resource://"+this.getPackageName()+"/drawable/pic");
            Log.i("MainActivity", "onCreate: " + mCurrentProductUri);
            setTitle("Add Product");
        } else {
            setTitle("Edit Product");
            Log.i("MainActivity", "onCreate: " + mCurrentProductUri);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
        mProductName = (EditText) findViewById(R.id.product_name_text);
        mProductPrice = (EditText) findViewById(R.id.price_text);
        mProductQuantity = (EditText) findViewById(R.id.quantity_text);
        mProductName.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);
        Button addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
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
        Button addImage=(Button) findViewById(R.id.add_image_button);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                final File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photo));
                imageUri = Uri.fromFile(photo);
                startActivityForResult(intent, TAKE_PICTURE);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ImageView imageView = (ImageView) findViewById(R.id.product_image);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        imageView.setImageBitmap(bitmap);
                        Toast.makeText(this, selectedImage.toString(),
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }
                }
        }
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
        String imageUriString=imageUri.toString();
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString)
                || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(imageUriString)) {
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
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE,imageUriString);

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
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };
        return new CursorLoader(this, mCurrentProductUri, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex=cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String image=cursor.getString(imageColumnIndex);

            imageUri=Uri.parse(image);
            mImageView=(ImageView) findViewById(R.id.product_image);
            mImageView.setImageURI(imageUri);
            mProductName.setText(name);
            mProductQuantity.setText(Integer.toString(quantity));
            mProductPrice.setText(Integer.toString(price));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mProductPrice.setText("");
        mProductQuantity.setText("");
        mImageView.setImageResource(R.drawable.pic);
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
        EditText editName = (EditText) findViewById(R.id.product_name_text);
        EditText quantity = (EditText) findViewById(R.id.quantity_text);
        String productName = editName.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:rachitbhutani1998@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, productName);
        intent.putExtra(Intent.EXTRA_TEXT, quantity.getText().toString() + " more " + productName + " required.");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
