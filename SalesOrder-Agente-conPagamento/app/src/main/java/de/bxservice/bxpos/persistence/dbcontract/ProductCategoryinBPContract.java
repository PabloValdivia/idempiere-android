package de.bxservice.bxpos.persistence.dbcontract;

import android.provider.BaseColumns;

public class ProductCategoryinBPContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ProductCategoryinBPContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class ProductCategoryinBP implements BaseColumns {
        public static final String TABLE_NAME = "productcategoryinBP";
        public static final String COLUMN_NAME_PRODUCT_CATEGORYINBPARTNER_ID ="productcategoryinbpid";
        public static final String COLUMN_NAME_BPARTNER_ID = "bpartnerid";
        public static final String COLUMN_NAME_PRODUCT_CATEGORY_ID = "productcategoryid";
        public static final String COLUMN_NAME_CATEGORYNAME = "categoryname";
        public static final String COLUMN_NAME_PRODUCT_ID = "productd";
    }
}
