/**********************************************************************
 * This file is part of FreiBier POS                                   *
 *                                                                     *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Diego Ruiz - Bx Service GmbH                                      *
 **********************************************************************/
package de.bxservice.bxpos.logic.model.idempiere;

import android.content.Context;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import de.bxservice.bxpos.logic.daomanager.BPartnerManagement;

/**
 * This is the product that is read from iDempiere
 * Created by Diego Ruiz on 9/11/15.
 */
public class CBPartner implements Serializable {

    public static final String C_BPartner_ID = "C_BPartner_ID";

    private int bpartnerID;
    private String bpName;
    private String bpValue;
    private boolean isActive;
    private int priceListID;
    private BPartnerManagement bpartnerManager;


    public int getBPartnerID() {
        return bpartnerID;
    }

    public void setBPartnerID(int bpartnerID) {
        this.bpartnerID = bpartnerID;
    }


    public String getBPartnerName() {
        return bpName;
    }

    public void setBPartnerName(String bpName) {
        this.bpName = bpName;
    }

    public String getBPartnerValue() {
        return bpValue;
    }

    public void setBPartnerValue(String bpValue) {
        this.bpValue = bpValue;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getPriceListID() {
        return priceListID;
    }

    public void setPriceListID(int priceListID) {
        this.priceListID = priceListID;
    }

    /**
     * Save if the object does not exist it creates it
     * otherwise it updates it
     * @param ctx
     * @return
     */
    public boolean save(Context ctx) {
        bpartnerManager = new BPartnerManagement(ctx);

        if (bpartnerManager.get(bpartnerID) == null)
            return createBPartner();
        else
            return updateBPartner();
    }

    private boolean updateBPartner() {
        return bpartnerManager.update(this);
    }

    /**
     * Communicates with the manager to create the product in the database
     * @return
     */
    private boolean createBPartner() {
        return bpartnerManager.create(this);
    }

    public static List<CBPartner> getbPartners(Context ctx) {
        BPartnerManagement dataProvider = new BPartnerManagement(ctx);
        return dataProvider.getBPartners();
    }

    public static List<CBPartner> getBPartnerInfo(Context ctx, int BPartner_ID) {
        BPartnerManagement dataProvider = new BPartnerManagement(ctx);
        return dataProvider.getBPartnerInfo(BPartner_ID);
    }
}
