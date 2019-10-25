package de.bxservice.bxpos.logic.webservices;

import android.util.Log;

import org.idempiere.webservice.client.base.Enums;
import org.idempiere.webservice.client.base.Field;
import org.idempiere.webservice.client.net.WebServiceConnection;
import org.idempiere.webservice.client.request.QueryDataRequest;
import org.idempiere.webservice.client.response.WindowTabDataResponse;

import java.util.ArrayList;
import java.util.List;

import de.bxservice.bxpos.logic.model.idempiere.ProductCategoryinBusinessPartner;

public class ProductCategoryinBusinessPartnerWebServiceAdapter extends  AbstractWSObject{

    //Associated record in Web Service Security in iDempiere
    private static final String SERVICE_TYPE = "QueryProductCategory";

    private List<ProductCategoryinBusinessPartner> productCategoryList;

    public ProductCategoryinBusinessPartnerWebServiceAdapter(WebServiceRequestData wsData) {
        super(wsData);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public void queryPerformed() {

        QueryDataRequest ws = new QueryDataRequest();
        ws.setWebServiceType(getServiceType());
        ws.setLogin(getLogin());

        WebServiceConnection client = getClient();
        productCategoryList = new ArrayList<>();

        try {
            WindowTabDataResponse response = client.sendRequest(ws);

            if (response.getStatus() == Enums.WebServiceResponseStatus.Error) {
                Log.e("Error ws response", response.getErrorMessage());
                success = false;
            } else {

                Log.i("info", "Total rows: " + response.getNumRows());
                int bpartnerID;
                int categoryID;
                String categoryName;
                int productID;

                for (int i = 0; i < response.getDataSet().getRowsCount(); i++) {

                    Log.i("info", "Row: " + (i + 1));
                    bpartnerID=0;
                    categoryID = 0;
                    categoryName = null;
                    productID=0;

                    for (int j = 0; j < response.getDataSet().getRow(i).getFieldsCount(); j++) {

                        Field field = response.getDataSet().getRow(i).getFields().get(j);
                        Log.i("info", "Column: " + field.getColumn() + " = " + field.getValue());

                        if("C_BPartner_ID".equalsIgnoreCase(field.getColumn()))
                            bpartnerID = Integer.valueOf(field.getStringValue());
                        else if ("M_Product_Category_ID".equalsIgnoreCase(field.getColumn()))
                            categoryID = Integer.valueOf(field.getStringValue());
                        else if ("Name".equalsIgnoreCase(field.getColumn()))
                            categoryName = field.getStringValue();
                        else if ("M_Product_ID".equalsIgnoreCase(field.getColumn()))
                            productID = Integer.valueOf(field.getStringValue());
                    }

                    if(categoryName != null &&  categoryID!= 0){
                        ProductCategoryinBusinessPartner p = new ProductCategoryinBusinessPartner(categoryID, categoryName);
                        productCategoryList.add(p);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
    }

    public List<ProductCategoryinBusinessPartner> getProductCategoryList() {
        return productCategoryList;
    }


}
