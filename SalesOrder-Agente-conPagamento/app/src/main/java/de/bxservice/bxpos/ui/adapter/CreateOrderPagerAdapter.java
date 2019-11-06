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
package de.bxservice.bxpos.ui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.bxservice.bxpos.logic.daomanager.PosProductCategoryManagement;
import de.bxservice.bxpos.logic.daomanager.ProductCategoryinBusinessPartnerManagement;
import de.bxservice.bxpos.logic.model.pos.NewOrderGridItem;
import de.bxservice.bxpos.ui.fragment.ProductMenuFragment;

/**
 * Created by Diego Ruiz on 19/11/15.
 */
public class CreateOrderPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private int BPartner_ID;
    private int NumOfTabs;
    private ProductCategoryinBusinessPartnerManagement dataProvider;
 //   private PosProductCategoryManagement dataProvider;
    private List<Fragment> foodFragments = new ArrayList<>();

    public CreateOrderPagerAdapter(FragmentManager fm, Context context, int bpartner_ID) {
        super(fm);
        mContext = context;
        BPartner_ID=bpartner_ID;
        dataProvider = new ProductCategoryinBusinessPartnerManagement(mContext);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a ProductMenuFragment (defined as a static inner class below).
        return ProductMenuFragment.newInstance(position);
    }

    @Override
    /**
     * Total number of tabs
     */
    public int getCount() {

        if ((int) dataProvider.getTotalCategoriesInBP(BPartner_ID)>0) {
            return (int) dataProvider.getTotalCategoriesInBP(BPartner_ID);
        } else {
            return 1;
        }
       //int b=a;
   //     return (int) dataProvider.getTotalCategories();
       // return 2;

    }

    @Override
    /**
     * Return the titles of each tab
     */
    public CharSequence getPageTitle(int position) {

        if ((int) dataProvider.getTotalCategoriesInBP(BPartner_ID)>0) {
            return dataProvider.getAllCategoriesinBP(BPartner_ID).get(position).getName();
        } else {
            return "No prodotti nel listino";
        }
 //       return "aa";
   //     return dataProvider.getAllCategories().get(position).getName();
    }

    /**
     * Updates the quantity when the item is selected from the search list
     * @param selectedItem
     * @param quantity
     */
    public void updateQty(NewOrderGridItem selectedItem, int quantity) {

        if (foodFragments != null) {
            boolean found = false;

            for (Fragment fragment : foodFragments) {

                if (fragment instanceof ProductMenuFragment && !found) {
                    ProductMenuFragment menuFragment = (ProductMenuFragment) fragment;

                    for (NewOrderGridItem item : menuFragment.getmGridData()) {

                        if (item.getKey().equals(selectedItem.getKey())) {
                            menuFragment.updateQtyOnClick(menuFragment.getmGridData().indexOf(item), quantity);
                            found = true;
                        }
                    }

                }

            }
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

        //Save a reference of the existing fragments
        if (foodFragments != null && !foodFragments.contains(createdFragment))
            foodFragments.add(createdFragment);

        return createdFragment;
    }

    /**
     * Refresh the quantity when some items have been deleted
     */
    public void refreshAllQty() {

        if (foodFragments != null) {

            for (Fragment fragment : foodFragments) {

                if (fragment instanceof ProductMenuFragment) {
                    ProductMenuFragment menuFragment = (ProductMenuFragment) fragment;
                    menuFragment.refreshAllQty();
                }
            }
        }
    }

}

