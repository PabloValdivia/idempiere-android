package de.bxservice.bxpos.logic.webservices;

import android.util.Log;

import org.idempiere.webservice.client.base.Enums;
import org.idempiere.webservice.client.base.Field;
import org.idempiere.webservice.client.net.WebServiceConnection;
import org.idempiere.webservice.client.request.QueryDataRequest;
import org.idempiere.webservice.client.response.WindowTabDataResponse;

import java.util.ArrayList;
import java.util.List;

import de.bxservice.bxpos.logic.model.idempiere.CBPartner;

public class BPartnerWebServicesAdapter extends AbstractWSObject {

    //Associated record in Web Service Security in iDempiere
    private static final String SERVICE_TYPE = "QueryBusinessPartner";

    private List<CBPartner> bpartnerList;

    public BPartnerWebServicesAdapter(WebServiceRequestData wsData) {
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

        bpartnerList = new ArrayList<>();

        try {
            WindowTabDataResponse response = client.sendRequest(ws);

            if (response.getStatus() == Enums.WebServiceResponseStatus.Error) {
                Log.e("Error ws response", response.getErrorMessage());
                success = false;
            } else {

                Log.i("info", "Total rows: " + response.getNumRows());
                String bpartnerName;
                String bpartnerKey;
                int bpartnerId;
                int priceListID;
                boolean isActive;

                for (int i = 0; i < response.getDataSet().getRowsCount(); i++) {

                    Log.i("info", "Row: " + (i + 1));
                    bpartnerId = 0;
                    bpartnerName = null;
                    bpartnerKey = null;
                    priceListID = 0;
                    isActive = false;

                    for (int j = 0; j < response.getDataSet().getRow(i).getFieldsCount(); j++) {

                        Field field = response.getDataSet().getRow(i).getFields().get(j);
                        Log.i("info", "Column: " + field.getColumn() + " = " + field.getValue());

                        if("Name".equalsIgnoreCase(field.getColumn()))
                            bpartnerName = field.getStringValue();
                        else if (CBPartner.C_BPartner_ID.equalsIgnoreCase(field.getColumn()))
                            bpartnerId = Integer.valueOf(field.getStringValue());
                        else if ("IsActive".equalsIgnoreCase(field.getColumn()))
                            isActive = "Y".equalsIgnoreCase(field.getStringValue());
                        else if ("Value".equalsIgnoreCase(field.getColumn()))
                            bpartnerKey = field.getStringValue();
                        else if ("M_PriceList_ID".equalsIgnoreCase(field.getColumn())&& !field.getStringValue().isEmpty())
                            priceListID = Integer.valueOf(field.getStringValue());
                    }

                    //se nel BP non c' il listino allora non vedo il BPartner
                    if(bpartnerName != null && bpartnerId != 0 && bpartnerKey != null && priceListID != 0) {
                        CBPartner p = new CBPartner();
                        p.setBPartnerID(bpartnerId);
                        p.setBPartnerName(bpartnerName);
                        p.setBPartnerValue(bpartnerKey);
                        p.setPriceListID(priceListID);
                        p.setActive(isActive);
                        bpartnerList.add(p);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
    }

    public List<CBPartner> getProductList() {
        return bpartnerList;
    }

}
