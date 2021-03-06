/**********************************************************************
 * This file is part of Freibier POS                                   *
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
package de.bxservice.bxpos.logic.model.pos;

import java.io.Serializable;
import java.math.BigDecimal;

import de.bxservice.bxpos.logic.model.idempiere.Tax;

/**
 * Tax in the orders. Based on MOrderTax in iDempiere
 * Created by Diego Ruiz on 14/11/16.
 */
public class POSOrderTax implements Serializable {

    private POSOrder order;
    private Tax tax;
    private BigDecimal taxAmount;
    private BigDecimal taxBaseAmount;
    private int precision;

    public POSOrder getOrder() {
        return order;
    }

    public void setOrder(POSOrder order) {
        this.order = order;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTaxBaseAmount() {
        return taxBaseAmount;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    /**************************************************************************
     * 	Calculate/Set Tax Amt from Order Lines
     * 	Always calculate it from document level
     * 	@return true if calculated
     */
    public boolean calculateTaxFromLines ()
    {
        BigDecimal taxBaseAmt = BigDecimal.ZERO;
        BigDecimal taxAmt;

        for (POSOrderLine line : order.getOrderedLines()) {
            if (line.getLineTax().getTaxID() == tax.getTaxID()) {
                BigDecimal baseAmt = line.getLineNetAmt(order.getCB_PriceList_ID());
                taxBaseAmt = taxBaseAmt.add(baseAmt);
            }
        }

        if (taxBaseAmt == null)
            return false;

        //	Calculate Tax
        taxAmt = tax.calculateTax(taxBaseAmt, true, precision);
        taxAmount = taxAmt;

        taxBaseAmount = taxBaseAmt.subtract(taxAmt);

        return true;
    }	//	calculateTaxFromLines
}
