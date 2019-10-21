package de.bxservice.bxpos.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import de.bxservice.bxpos.R;
import de.bxservice.bxpos.logic.model.idempiere.DefaultPosData;
import de.bxservice.bxpos.logic.model.idempiere.CBPartner;
import de.bxservice.bxpos.logic.model.idempiere.Table;
import de.bxservice.bxpos.logic.model.pos.NewOrderGridItem;
import de.bxservice.bxpos.logic.model.pos.POSOrder;
import de.bxservice.bxpos.logic.model.pos.PosProperties;
import de.bxservice.bxpos.ui.adapter.CreateBPartnerPagerAdapter;
import de.bxservice.bxpos.ui.adapter.SearchItemAdapter;
import de.bxservice.bxpos.ui.decorator.DividerItemDecoration;
import de.bxservice.bxpos.ui.dialog.GuestNumberDialogFragment;
import de.bxservice.bxpos.ui.dialog.MultipleItemsDialogFragment;
import de.bxservice.bxpos.ui.utilities.PreferenceActivityHelper;

public class SelectBPartnerActivity extends AppCompatActivity implements  SearchView.OnQueryTextListener {


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private CreateBPartnerPagerAdapter mCreateBPartnerPagerAdapter;
    private static final String ORDER          = "createdOrder";
    public final static String CALLER_ACTIVITY = "SelectBPartnerActivity.CALLER";
    static final int NEW_ORDER_REQUEST  = 1;  // The request code
    private int numberOfGuests = 0;
    public final static String EXTRA_NUMBER_OF_GUESTS = "de.bxservice.bxpos.GUESTS";
    public final static String EXTRA_ASSIGNED_TABLE   = "de.bxservice.bxpos.TABLE";
    public final static String BPARTNER_INO = "de.bxservice.bxpos.BPARTNER";
    private Table selectedTable = null;

  //  private PagerSlidingTabStrip tabs;

    //RecyclerView attributes for search functionality
    private RecyclerView recyclerView;
    private SearchItemAdapter mAdapter;
    private ArrayList<NewOrderGridItem> items = new ArrayList<>();
    private HashMap<NewOrderGridItem, CBPartner> itemBPartnerHashMap;
    private SearchView mSearchView;

    //order attributes
    private POSOrder posOrder = null;
//    private FloatingActionButton sendActionButton;

    private String caller;
    private boolean itemsAdded = false;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    //If the app is running on a large-screen device
    private boolean mTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_businesspartner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.create_order_toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get Table # and # of guests
        getExtras();

        if (savedInstanceState != null) {
            posOrder = (POSOrder) savedInstanceState.getSerializable(ORDER);
        }

        if("EditOrderActivity".equals(caller))
            setTitle(R.string.title_activity_add_items);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mCreateBPartnerPagerAdapter = new CreateBPartnerPagerAdapter(getSupportFragmentManager(), getBaseContext());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.createOrderContainer);
        mViewPager.setAdapter(mCreateBPartnerPagerAdapter);


        recyclerView = (RecyclerView) findViewById(R.id.search_item_view);

        initSearchListItems();

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SearchItemAdapter(items);
        mAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = recyclerView.getChildAdapterPosition(v);

                NewOrderGridItem bpartner = mAdapter.getSelectedItem(position);
        //        CBPartner bpartner = itemProductHashMap.get(item);
              //  ((SelectBPartnerActivity) getActivity()).createOrder(bpartner);
                createOrder(itemBPartnerHashMap.get(bpartner));
            }
        });

        recyclerView.setAdapter(mAdapter);
        recyclerView.setVisibility(View.GONE);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));


        ViewGroup fragmentContainer = (ViewGroup) findViewById(R.id.detail_order_fragment);
        mTablet = (fragmentContainer != null);


    }

    /**
     * Creates the list view that will be used when click on search
     */
    private void initSearchListItems() {

        NumberFormat currencyFormat = PosProperties.getInstance().getCurrencyFormat();

        NewOrderGridItem gridItem;
        itemBPartnerHashMap = new HashMap<>();

        for (CBPartner bpartner : CBPartner.getbPartners(getBaseContext())) {
            gridItem = new NewOrderGridItem();

            //If the key and the name are the same, don't repeat
    //        if (bpartner.getBPartnerName().equalsIgnoreCase(bpartner.getBPartnerValue()))
                gridItem.setName(bpartner.getBPartnerName());
  //          else
//                gridItem.setName(bpartner.getBPartnerValue() + " " + bpartner.getBPartnerName());

        //    if (!product.askForPrice())
         //       gridItem.setPrice(currencyFormat.format(product.getProductPriceValue()));
          //  else
                gridItem.setPrice("");

            gridItem.setKey(bpartner.getBPartnerValue());

            items.add(gridItem);
            itemBPartnerHashMap.put(gridItem, bpartner);
        }
    }

    /**
     * Method that allows to capture the text that is being
     * introduced in the search field
     * @param newText
     * @return
     */
    public boolean onQueryTextChange(String newText) {
        SearchItemAdapter adapter = (SearchItemAdapter) recyclerView.getAdapter();

        if (TextUtils.isEmpty(newText)) {
            adapter.getFilter().filter("");
        } else {
            showSearchList(true);
            adapter.getFilter().filter(newText);
        }
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * When the search button is pressed - the listview is shown and the tabs are hidden
     * When the search button loses focuses - listview hidden and tabs are shown
     * @param show
     */
    private void showSearchList(final boolean show) {
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

            recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            recyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
            mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);

        }
    }//showSearchList


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_order_activity, menu);

        MenuItem guestItem = menu.findItem(R.id.set_guests);

        if (guestItem != null) {
            if (!DefaultPosData.get(getBaseContext()).isShowGuestDialog())
                guestItem.setVisible(false);
        }

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        mSearchView = (SearchView) menu.findItem(R.id.items_search).getActionView();

        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        setupSearchView();

        return true;
    }

    /**
     * Add the needed configuration to the search view
     */
    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showSearchList(true);
            }
        });
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            onBackPressed();
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }



    @Override
    public void finish() {
        if ("EditOrderActivity".equals(caller) && itemsAdded) {
            Intent data = new Intent();
            data.putExtra(EditOrderActivity.EXTRA_ORDER, posOrder);
            setResult(RESULT_OK, data);
        }
        super.finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(ORDER, posOrder);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    public void createOrder(CBPartner bpartner){

        Intent intent = new Intent(this, CreateOrderActivity.class);
        intent.putExtra(CreateOrderActivity.CALLER_ACTIVITY, "SelectBPartnerActivity");
        intent.putExtra(EXTRA_NUMBER_OF_GUESTS, numberOfGuests);
        intent.putExtra(EXTRA_ASSIGNED_TABLE, selectedTable);
        intent.putExtra(BPARTNER_INO, bpartner);
        intent.putExtra("bpartner", bpartner);

        startActivityForResult(intent, NEW_ORDER_REQUEST);
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            caller = getIntent().getStringExtra(CALLER_ACTIVITY);

            if ("MainActivity".equals(caller)) {
                if(getIntent().getSerializableExtra(MainActivity.EXTRA_ASSIGNED_TABLE) != null)
                    selectedTable = (Table) getIntent().getSerializableExtra(MainActivity.EXTRA_ASSIGNED_TABLE);

                numberOfGuests = extras.getInt(MainActivity.EXTRA_NUMBER_OF_GUESTS);
            } else if ("EditOrderActivity".equals(caller)) {
                posOrder = (POSOrder) getIntent().getSerializableExtra(EditOrderActivity.EXTRA_ORDER);
                selectedTable = posOrder.getTable();
                numberOfGuests = posOrder.getGuestNumber();
            }
        }
    }


}
