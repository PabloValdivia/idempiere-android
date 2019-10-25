package de.bxservice.bxpos.persistence.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.bxservice.bxpos.logic.model.idempiere.ProductCategoryinBusinessPartner;
import de.bxservice.bxpos.persistence.dbcontract.ProductCategoryinBPContract;
import de.bxservice.bxpos.persistence.definition.Tables;

public class ProductCategoryinBPHelper extends PosObjectHelper {

    private static final String LOG_TAG = "Product Category Helper";

    public ProductCategoryinBPHelper(Context mContext) {
        super(mContext);
    }

    /*
     * Creating a product category
     */
    public long createProductCategoryinBP (ProductCategoryinBusinessPartner productCategoryinBP) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_BPARTNER_ID, productCategoryinBP.getBPartnerID());
        values.put(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_CATEGORY_ID, productCategoryinBP.getProductCategoryID());
        values.put(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_CATEGORYNAME, productCategoryinBP.getName());
        values.put(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_ID, productCategoryinBP.getProductID());


        // insert row
        return db.insert(Tables.TABLE_PRODUCT_CATEGORYINBPARTNER, null, values);
    }

    /*
     * get single product category
     */
    public ProductCategoryinBusinessPartner getProductCategoryinBP(long productCategory_id) {
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + Tables.TABLE_PRODUCT_CATEGORYINBPARTNER + " WHERE "
                + ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_CATEGORY_ID + " = ?";

        Log.d(LOG_TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, new String[] { String.valueOf(productCategory_id) });

        if (c != null && c.getCount() > 0)
            c.moveToFirst();
        else {
            if (c != null)
                c.close();
            return null;
        }

        ProductCategoryinBusinessPartner productCategoryinBusinessPartner = new ProductCategoryinBusinessPartner(c.getInt(c.getColumnIndex(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_CATEGORY_ID)),
                c.getString(c.getColumnIndex(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_CATEGORYNAME)));
        productCategoryinBusinessPartner.setBPartnerID(c.getInt(c.getColumnIndex(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_BPARTNER_ID)));
        productCategoryinBusinessPartner.setProductID(c.getInt(c.getColumnIndex(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_ID)));

        c.close();

        return productCategoryinBusinessPartner;
    }

    /*


    /**
     * Getting all product category
     */
    public List<ProductCategoryinBusinessPartner> getAllProductCategories() {
        List<ProductCategoryinBusinessPartner> productCategoriesinBP = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + Tables.TABLE_PRODUCT_CATEGORYINBPARTNER +
                " WHERE "+ ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_CATEGORY_ID + " = 1000000" +
                " OR "+ ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_CATEGORY_ID + " = 1000068";


        Log.d(LOG_TAG, selectQuery);

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
             PosProductHelper productHelper = new PosProductHelper(mContext);
            do {
                ProductCategoryinBusinessPartner productCategoryinBP = new ProductCategoryinBusinessPartner();
                productCategoryinBP.setBPartnerID(c.getInt(c.getColumnIndex(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_BPARTNER_ID)));
                productCategoryinBP.setProductCategoryID(c.getInt(c.getColumnIndex(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_CATEGORY_ID)));
                productCategoryinBP.setName(c.getString(c.getColumnIndex(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_CATEGORYNAME)));
                productCategoryinBP.setProducts(productHelper.getAllProductsinBP(productCategoryinBP));

                productCategoryinBP.setProductID(c.getInt(c.getColumnIndex(ProductCategoryinBPContract.ProductCategoryinBP.COLUMN_NAME_PRODUCT_ID)));




                // adding to category list
                productCategoriesinBP.add(productCategoryinBP);
            } while (c.moveToNext());
        }

        if (c != null)
            c.close();

        return productCategoriesinBP;
    }

    /**
     * getting total of rows in the table product category
     */
    public long getTotalCategories() {
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, Tables.TABLE_PRODUCT_CATEGORYINBPARTNER);
    }

}
