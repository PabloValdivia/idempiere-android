package de.bxservice.bxpos.logic.model.idempiere;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.bxservice.bxpos.logic.daomanager.ProductCategoryinBusinessPartnerManagement;

public class ProductCategoryinBusinessPartner {
    private int bpartnerID;
    private int productCategoryID;
    private String name;
    private int productID;
    private List<MProduct> products = new ArrayList<>();
    private ProductCategoryinBusinessPartnerManagement productCategoryManager;

    public ProductCategoryinBusinessPartner(){
    }

    public ProductCategoryinBusinessPartner(int id, String name){
        productCategoryID = id;
        this.name = name;

    }

    public int getProductCategoryID() {
        return productCategoryID;
    }

    public void setProductCategoryID(int productCategoryID) {
        this.productCategoryID = productCategoryID;
    }

    public int getBPartnerID() {
        return bpartnerID;
    }

    public void setBPartnerID(int bpartnerID) {
        this.bpartnerID = bpartnerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }


    public List<MProduct> getProducts() {
        return products;
    }

    public void setProducts(List<MProduct> products) {
        this.products = products;
    }


    /**
     * Save if the object does not exist it creates it
     * otherwise it updates it
     * @param ctx
     * @return
     */
    public boolean save(Context ctx) {

        //No save in the database if the Product Category does not have products
        if (products.isEmpty())
            return false;

        productCategoryManager = new ProductCategoryinBusinessPartnerManagement(ctx);

        if (productCategoryManager.get(productCategoryID) == null)
            return createProductCategory();
        else
            return updateProductCategory();
    }

    private boolean updateProductCategory() {
        return productCategoryManager.update(this);
    }

    private boolean createProductCategory() {
        return productCategoryManager.create(this);
    }

    public static List<ProductCategoryinBusinessPartner> getAllCategories(Context ctx) {
        ProductCategoryinBusinessPartnerManagement categoryManager = new ProductCategoryinBusinessPartnerManagement(ctx);
        return categoryManager.getAllCategories();
    }
}

