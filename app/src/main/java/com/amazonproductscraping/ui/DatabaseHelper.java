package com.amazonproductscraping.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ProductData.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "ProductDetails";
    private static final String COL_ID = "ID";
    private static final String COL_PRODUCT_NAME = "ProductName";
    private static final String COL_DISCOUNT = "Discount";
    private static final String COL_PRODUCT_PRICE = "ProductPrice";
    private static final String COL_MRP_PRICE = "MrpPrice";
    private static final String COL_ABOUT_THIS_ITEM = "AboutThisItem";
    private static final String COL_PRODUCT_INFO = "ProductInformation";
    private static final String COL_ADDITIONAL_INFO = "AdditionalInformation";
    private static final String COL_TECH_DETAILS = "TechnicalDetails";
    private static final String COL_PRODUCT_DETAILS = "ProductDetails";
    private static final String COL_PRODUCT_SPECIFICATIONS = "ProductSpecifications";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PRODUCT_NAME + " TEXT, " +
                COL_DISCOUNT + " TEXT, " +
                COL_PRODUCT_PRICE + " TEXT, " +
                COL_MRP_PRICE + " TEXT, " +
                COL_ABOUT_THIS_ITEM + " TEXT, " +
                COL_PRODUCT_INFO + " TEXT, " +
                COL_ADDITIONAL_INFO + " TEXT, " +
                COL_TECH_DETAILS + " TEXT, " +
                COL_PRODUCT_DETAILS + " TEXT, " +
                COL_PRODUCT_SPECIFICATIONS + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert Data into database
    public boolean insertData(String productName, String discount, String productPrice,
                              String mrpPrice, String aboutThisItem, String productInfo,
                              String additionalInfo, String techDetails, String productDetails,
                              String productSpecifications) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PRODUCT_NAME, productName);
        contentValues.put(COL_DISCOUNT, discount);
        contentValues.put(COL_PRODUCT_PRICE, productPrice);
        contentValues.put(COL_MRP_PRICE, mrpPrice);
        contentValues.put(COL_ABOUT_THIS_ITEM, aboutThisItem);
        contentValues.put(COL_PRODUCT_INFO, productInfo);
        contentValues.put(COL_ADDITIONAL_INFO, additionalInfo);
        contentValues.put(COL_TECH_DETAILS, techDetails);
        contentValues.put(COL_PRODUCT_DETAILS, productDetails);
        contentValues.put(COL_PRODUCT_SPECIFICATIONS, productSpecifications);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // Return true if insert was successful
    }

    // Fetch All Data from database
    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    // Clear all data from the table
    /*public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME); // Clear all rows
        db.close();
    }*/

    // Clear all data from the table
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction(); // Start transaction
            db.delete(TABLE_NAME, null, null); // Delete all rows
            db.setTransactionSuccessful(); // Mark transaction successful
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // End transaction
            db.close(); // Close database connection
        }
    }


}
