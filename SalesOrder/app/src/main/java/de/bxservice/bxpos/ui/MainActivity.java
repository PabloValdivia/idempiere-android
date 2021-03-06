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
package de.bxservice.bxpos.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.bxservice.bxpos.R;
import de.bxservice.bxpos.logic.daomanager.PosSessionPreferenceManagement;
import de.bxservice.bxpos.logic.model.idempiere.Table;
import de.bxservice.bxpos.logic.model.pos.POSOrder;
import de.bxservice.bxpos.logic.model.pos.PosUser;
import de.bxservice.bxpos.logic.tasks.CreateOrderTask;
import de.bxservice.bxpos.persistence.helper.PosObjectHelper;
import de.bxservice.bxpos.ui.adapter.MainPagerAdapter;
import de.bxservice.bxpos.ui.dialog.GuestNumberDialogFragment;
import de.bxservice.bxpos.ui.dialog.MultipleOrdersTableDialogFragment;
import de.bxservice.bxpos.ui.fragment.AsyncFragment;
import de.bxservice.bxpos.ui.utilities.PreferenceActivityHelper;

/**
 * First Activity after login
 * It displays the tables in a tabbed activity with the different groups
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GuestNumberDialogFragment.GuestNumberDialogListener,
        MultipleOrdersTableDialogFragment.MultipleOrdersTableDialogListener, AsyncFragment.ParentActivity {

    private static final String LOG_TAG = "Main Activity";
    private static final boolean DEVELOPER_MODE = true;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private static final String SELECTED_TABLE = "selectedTable";
    private static final String ASYNC_FRAGMENT_TAG = "MAIN_ASYNC_FRAGMENT_TAG";

    static final int NEW_ORDER_REQUEST  = 1;  // The request code
    static final int EDIT_ORDER_REQUEST = 2;  // The request code
    static final int OPEN_ORDER_REQUEST = 3;  // The request code

    public final static String EXTRA_NUMBER_OF_GUESTS = "de.bxservice.bxpos.GUESTS";
    public final static String EXTRA_ASSIGNED_TABLE   = "de.bxservice.bxpos.TABLE";
    public final static String EXTRA_UPDATED_TABLE    = "de.bxservice.bxpos.TABLEUPDATE";
    public final static String TABLE_UPDATED_ACTION   = "TABLE_UPDATED";

    private MainPagerAdapter mMainPagerAdapter;
    private ViewPager mViewPager;
    private View mProgressView;

    private int numberOfGuests = 0;
    private Table selectedTable = null;

    private String syncConnPref;
    private long backPressed;

    private CreateOrderTask createOrderTask = null;
    private AsyncFragment mAsyncFragment;

    private IntentFilter myFilter;
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Update table request received");
            Table table = (Table) intent.getSerializableExtra(EXTRA_UPDATED_TABLE);
            if (table != null)
                mMainPagerAdapter.updateStatus(table);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyDialog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                    .penaltyLog()
                    .build());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), getBaseContext());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.mainTabViewPager);
        mViewPager.setAdapter(mMainPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.mainTabs);
        tabs.setViewPager(mViewPager);

        FloatingActionButton newOrderButton = (FloatingActionButton) findViewById(R.id.new_order_button);
        newOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numberOfGuests = 0;
                selectedTable = null;
                createOrder();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0); // 0-index header
        TextView userNameTextView = (TextView) headerLayout.findViewById(R.id.usernameTextView);

        userNameTextView.setText(PosUser.getCurrentUserDisplayName(getBaseContext()));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        syncConnPref = sharedPref.getString(PreferenceActivityHelper.KEY_PREF_SYNC_CONN, "");
        callAsynchronousTask();

        mProgressView = findViewById(R.id.load_data_progress);

        if (savedInstanceState != null) {
            selectedTable = (Table) savedInstanceState.getSerializable(SELECTED_TABLE);
        }

        mAsyncFragment = (AsyncFragment) getSupportFragmentManager().findFragmentByTag(ASYNC_FRAGMENT_TAG);
        if (mAsyncFragment == null) {
            mAsyncFragment = new AsyncFragment();
            getSupportFragmentManager().beginTransaction().add(mAsyncFragment, ASYNC_FRAGMENT_TAG).commit();
        }

        if (mAsyncFragment.isTaskRunning())
            showProgress(true);

        //Configuration to recreate UI on push notification
        myFilter = new IntentFilter(TABLE_UPDATED_ACTION);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                cleanSessionPreference();
                return;
            }
            else {
                Toast.makeText(getBaseContext(), getString(R.string.confirm_exit), Toast.LENGTH_SHORT).show();
            }

            backPressed = System.currentTimeMillis();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.open_orders) {

            Intent intent = new Intent(this, ViewOpenOrdersActivity.class);
            startActivityForResult(intent, OPEN_ORDER_REQUEST);

        } else if (id == R.id.closed_orders) {
            Intent intent = new Intent(this, ClosedOrdersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_report) {
            Intent intent = new Intent(this, ReportActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {

            if (isCreateOrderTaskRunning())
                Toast.makeText(getBaseContext(), getString(R.string.sync_task_running),
                        Toast.LENGTH_SHORT).show();
            else {

                final List<POSOrder> unsynchronizedOrders = POSOrder.getUnsynchronizedOrders(getBaseContext());

                if (unsynchronizedOrders != null && unsynchronizedOrders.size() != 0) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.synchronize_orders_title)
                            .setMessage(getString(R.string.synchronize_orders_message, unsynchronizedOrders.size()))
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.synchronize, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    synchronizePendingOrders(unsynchronizedOrders, false);
                                }
                            }).create().show();
                } else
                    Toast.makeText(getBaseContext(), getString(R.string.no_unsync_orders),
                            Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_reload_data) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reload_data_title)
                    .setMessage(getString(R.string.reload_data_message))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.reload, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            reloadPOSData();
                        }
                    }).create().show();
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            cleanSessionPreference();
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
            finish();
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Updates the POS data from iDempiere if there's internet connection
     */
    private void reloadPOSData() {
        //Check if network connection is available
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        //Check the internet connection
        if (networkInfo != null && networkInfo.isConnected()) {
            showProgress(true);
            mAsyncFragment.runAsyncTask();
        }
        else
            Toast.makeText(getBaseContext(), getString(R.string.error_no_connection_on_sync_order),
                    Toast.LENGTH_SHORT).show();
    }

    /**
     * If there are orders to be synchronized. Checks if there is internet connection
     * and synchronizes them
     * @param unsynchronizedOrders Array list with the orders that has not been synchronized with iDempiere
     */
    private void synchronizePendingOrders(List<POSOrder> unsynchronizedOrders, boolean automatic) {
        //Check if network connection is available
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        //When no internet connection
        if (networkInfo != null && networkInfo.isConnected()) {
            createOrderTask = new CreateOrderTask(this);
            //Convert from List to POSOrder[] to send as a parameter to the async task
            POSOrder[] orderArray = unsynchronizedOrders.toArray(new POSOrder[unsynchronizedOrders.size()]);
            createOrderTask.execute(orderArray);
        }
        else if (!automatic)
            Toast.makeText(getBaseContext(), getString(R.string.error_no_connection_on_sync_order),
                    Toast.LENGTH_SHORT).show();
    }

    private boolean isCreateOrderTaskRunning() {
        return (createOrderTask != null) && (createOrderTask.getStatus() == AsyncTask.Status.RUNNING);
    }

    public void showGuestNumberDialog() {
        // Create an instance of the dialog fragment and show it
        GuestNumberDialogFragment guestDialog = new GuestNumberDialogFragment();
        guestDialog.show(getFragmentManager(), "NumberOfGuestDialogFragment");
    }

    public void showSelectOrderDialog(List<POSOrder> orders) {
        MultipleOrdersTableDialogFragment ordersDialog = new MultipleOrdersTableDialogFragment();
        ordersDialog.setTableOrders(orders);
        ordersDialog.show(getFragmentManager(), "MultipleOrdersTableDialogFragment");
    }

    public void setSelectedTable(Table selectedTable) {
        this.selectedTable = selectedTable;
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    @Override
    public void onDialogPositiveClick(GuestNumberDialogFragment dialog) {
        // User touched the dialog's positive button
        numberOfGuests = dialog.getNumberOfGuests();
        createOrder();
    }

    @Override
    public void onDialogPositiveClick(MultipleOrdersTableDialogFragment dialog) {
        if(dialog.getOrder() != null)
            editOrder(dialog.getOrder());
    }

    public void createOrder(){

        Intent intent = new Intent(this, CreateOrderActivity.class);
        intent.putExtra(CreateOrderActivity.CALLER_ACTIVITY,"MainActivity");
        intent.putExtra(EXTRA_NUMBER_OF_GUESTS, numberOfGuests);
        intent.putExtra(EXTRA_ASSIGNED_TABLE, selectedTable);

        startActivityForResult(intent, NEW_ORDER_REQUEST);
    }

    public void editOrder(List<POSOrder> orders){

        //No orders in the array -> free table - new order
        if (orders.isEmpty())
            createOrder();
        //Only one order in the table -> Open edit activity
        else if (orders.size() == 1) {
            editOrder(orders.get(0));
        } else {
            showSelectOrderDialog(orders);
        }
    }

    public void editOrder(POSOrder order) {
        Intent intent = new Intent(this, EditOrderActivity.class);
        intent.putExtra(EditOrderActivity.CALLER_ACTIVITY,"MainActivity");
        intent.putExtra(EditOrderActivity.DRAFT_ORDER, order);
        startActivityForResult(intent, EDIT_ORDER_REQUEST);
    }

    /**
     * On destroy closes the db connection
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        //If the activity is destroyed by closing -> no by rotating the screen and is not only being destroyed temporarily by Android
        if (!isChangingConfigurations() && isFinishing()) {
            PosObjectHelper.closeDB(getBaseContext());
        }
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                myReceiver);
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        // Register to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                myReceiver, myFilter);

        //Close the drawer when coming back from
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SELECTED_TABLE, selectedTable);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == NEW_ORDER_REQUEST ||
                requestCode == EDIT_ORDER_REQUEST ||
                requestCode == OPEN_ORDER_REQUEST) {

            //Notify changes -> always in case of a change in a different device
            mMainPagerAdapter.updateAllTables();
        }
    }

    /**
     * Make sure that session preference only last one session
     */
    private void cleanSessionPreference() {
        PosSessionPreferenceManagement preferenceManager = new PosSessionPreferenceManagement(getBaseContext());
        preferenceManager.cleanSession();
    }

    /**
     * Shows the progress UI and hides the table form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
            mViewPager.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * gets call after the create order task finishes
     * @param success flag to check if the sync was successful
     */
    public void postExecuteCreateOrderTask(boolean success, boolean connectionError, String errorMessage) {
        if(success)
            Toast.makeText(getBaseContext(), getString(R.string.success_on_sync_order),
                    Toast.LENGTH_SHORT).show();
        else {
            if (connectionError)
                Toast.makeText(getBaseContext(), getString(R.string.no_connection_on_sync_order),
                        Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getBaseContext(), getString(R.string.no_success_on_sync_order) + errorMessage,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void handleReadDataTaskFinish(boolean success) {
        showProgress(false);

        if (success)
            Toast.makeText(getBaseContext(), getString(R.string.success_on_load_data),
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getBaseContext(), getString(R.string.no_connection_on_sync_order),
                    Toast.LENGTH_LONG).show();
    }

    /**
     * Calls the sync order task every x amount of time
     * as chosen in the settings
     */
    public void callAsynchronousTask() {

        //If the settings are set as always or never sync do not create the timer
        if("-1".equals(syncConnPref) || "0".equals(syncConnPref))
            return;

        int syncTime = Integer.parseInt(syncConnPref);

        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            final List<POSOrder> unsynchronizedOrders = POSOrder.getUnsynchronizedOrders(getBaseContext());

                            if (unsynchronizedOrders != null && unsynchronizedOrders.size() != 0) {
                                synchronizePendingOrders(unsynchronizedOrders, true);
                            }

                        } catch (Exception e) {
                            Log.e(LOG_TAG, e.toString());
                        }
                    }
                });
            }
        };

        timer.schedule(doAsynchronousTask, syncTime * 60000, syncTime * 60000 /*one minute in ms*/);
    }

}
