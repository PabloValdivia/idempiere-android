package de.bxservice.bxpos.persistence.dbcontract;

import android.provider.BaseColumns;

public class BPartnerContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public BPartnerContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class BPartnerDB implements BaseColumns {
        public static final String TABLE_NAME = "bpartner";
        public static final String COLUMN_NAME_BPARTNER_ID = "bpartnerid";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "Value";
        public static final String COLUMN_IS_ACTIVE = "isActive";
        public static final String COLUMN_NAME_PRICE_LIST_ID = "pricelist_id";}
}
