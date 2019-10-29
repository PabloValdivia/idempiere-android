package de.bxservice.bxpos.logic.daomanager;

import android.content.Context;

import java.util.List;

import de.bxservice.bxpos.logic.model.idempiere.ProductCategoryinBusinessPartner;

public class ProductCategoryinBusinessPartnerManagement  extends AbstractObjectManagement {

    public ProductCategoryinBusinessPartnerManagement(Context ctx) {
        super(ctx);
    }

    @Override
    public ProductCategoryinBusinessPartner get(long id){
        return dataMapper.getProductCategoryinBPartner(id);
    }

    @Override
    public boolean remove(Object object) {
        return true;
    }

    public long getTotalCategoriesInBP(int bpartner_id) {
        return dataMapper.getTotalCategoriesinBP(bpartner_id);
    }

    public List<ProductCategoryinBusinessPartner> getAllCategoriesinBP(int bpartner_id) {
        return dataMapper.getAllCategoriesinBP(bpartner_id);
    }
}
