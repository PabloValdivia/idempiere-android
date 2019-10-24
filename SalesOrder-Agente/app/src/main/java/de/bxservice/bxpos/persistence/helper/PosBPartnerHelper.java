package de.bxservice.bxpos.persistence.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import de.bxservice.bxpos.logic.model.idempiere.CBPartner;
import de.bxservice.bxpos.persistence.dbcontract.BPartnerContract;
import de.bxservice.bxpos.persistence.definition.Tables;

public class PosBPartnerHelper extends PosObjectHelper{

    private static final String LOG_TAG = "BPartner Helper";

    public PosBPartnerHelper(Context mContext) {
        super(mContext);
    }

    /*
     * Creating a bpartner
     */
    public long createBPartner(CBPartner bpartner) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BPartnerContract.BPartnerDB.COLUMN_NAME_BPARTNER_ID, bpartner.getBPartnerID());
        values.put(BPartnerContract.BPartnerDB.COLUMN_NAME_NAME, bpartner.getBPartnerName());
        values.put(BPartnerContract.BPartnerDB.COLUMN_NAME_VALUE, bpartner.getBPartnerValue());
        values.put(BPartnerContract.BPartnerDB.COLUMN_NAME_PRICE_LIST_ID, bpartner.getBPartnerID());

        int flag = (bpartner.isActive()) ? 1 : 0;
        values.put(BPartnerContract.BPartnerDB.COLUMN_IS_ACTIVE, flag);

        // insert row
        return db.insert(Tables.TABLE_BPARTNER, null, values);
    }

    /*
     * get single BPartner
     */
    public CBPartner getBPartner(long bpartner_id) {
        SQLiteDatabase db = getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + Tables.TABLE_BPARTNER + " WHERE "
                + BPartnerContract.BPartnerDB.COLUMN_NAME_BPARTNER_ID + " = ?";

        Log.d(LOG_TAG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, new String[] { String.valueOf(bpartner_id) });

        if (c != null && c.getCount() > 0)
            c.moveToFirst();
        else {
            if (c != null)
                c.close();
            return null;
        }

        CBPartner bpartner = new CBPartner();
        bpartner.setBPartnerID(c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_BPARTNER_ID)));
        bpartner.setBPartnerName(c.getString(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_NAME)));
        bpartner.setBPartnerValue(c.getString(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_VALUE)));
        bpartner.setPriceListID(c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_PRICE_LIST_ID)));

        Boolean flag = (c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_IS_ACTIVE)) != 0);
        bpartner.setActive(flag);

        c.close();

        return bpartner;
    }

    /*
     * Updating a product
     */
    public int updateBPartner(CBPartner bpartner) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BPartnerContract.BPartnerDB.COLUMN_NAME_NAME, bpartner.getBPartnerName());
        values.put(BPartnerContract.BPartnerDB.COLUMN_NAME_VALUE, bpartner.getBPartnerValue());
        values.put(BPartnerContract.BPartnerDB.COLUMN_NAME_PRICE_LIST_ID, bpartner.getPriceListID());

        int flag = (bpartner.isActive()) ? 1 : 0;
        values.put(BPartnerContract.BPartnerDB.COLUMN_IS_ACTIVE, flag);


        // updating row
        return db.update(Tables.TABLE_BPARTNER, values, BPartnerContract.BPartnerDB.COLUMN_NAME_BPARTNER_ID + " = ?",
                new String[] { String.valueOf(bpartner.getBPartnerID()) });
    }


    /**
     * Get all BP
     * @return
     */
    public ArrayList<CBPartner> getBPartners() {
        ArrayList<CBPartner> cbPartners = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Tables.TABLE_BPARTNER +
                " WHERE " + BPartnerContract.BPartnerDB.COLUMN_IS_ACTIVE + " = 1" +
                " ORDER BY " + BPartnerContract.BPartnerDB.COLUMN_NAME_NAME;


        Log.d(LOG_TAG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                CBPartner bpartner = new CBPartner();
                bpartner.setBPartnerID(c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_BPARTNER_ID)));
                bpartner.setBPartnerName(c.getString(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_NAME)));
                bpartner.setBPartnerValue(c.getString(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_VALUE)));
                bpartner.setPriceListID(c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_PRICE_LIST_ID)));

                Boolean flag = (c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_IS_ACTIVE)) != 0);
                bpartner.setActive(flag);

                cbPartners.add(bpartner);
            } while (c.moveToNext());
        }

        if (c != null)
            c.close();

        return cbPartners;
    }




    public ArrayList<CBPartner> getBPartnerInfo(int BPartner_ID) {
        ArrayList<CBPartner> cbPartners = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Tables.TABLE_BPARTNER +
                " WHERE " + BPartnerContract.BPartnerDB.COLUMN_IS_ACTIVE + " = 1" +
                " AND " + BPartnerContract.BPartnerDB.COLUMN_NAME_BPARTNER_ID + " = ?" +
                " ORDER BY " + BPartnerContract.BPartnerDB.COLUMN_NAME_NAME;


        Log.d(LOG_TAG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[] { String.valueOf(BPartner_ID) });

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                CBPartner bpartner = new CBPartner();
                bpartner.setBPartnerID(c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_BPARTNER_ID)));
                bpartner.setBPartnerName(c.getString(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_NAME)));
                bpartner.setBPartnerValue(c.getString(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_VALUE)));
                bpartner.setPriceListID(c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_NAME_PRICE_LIST_ID)));

                Boolean flag = (c.getInt(c.getColumnIndex(BPartnerContract.BPartnerDB.COLUMN_IS_ACTIVE)) != 0);
                bpartner.setActive(flag);

                cbPartners.add(bpartner);
            } while (c.moveToNext());
        }

        if (c != null)
            c.close();

        return cbPartners;
    }

}

