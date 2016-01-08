package citu.teknoybuyandselluser;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import citu.teknoybuyandselluser.adapters.ItemsListAdapter;
import citu.teknoybuyandselluser.models.Category;
import citu.teknoybuyandselluser.models.Item;


public class BuyItemsActivity extends BaseActivity {

    private TextView txtCategory;

    private static final String TAG = "BuyItems";

    private ProgressBar progressBar;

    private int mItemId;
    private int mStarsRequired;
    private float mPrice;
    private String mFormatPrice;
    private String mDescription;
    private String mItemName;
    private String mPicture;
    private String user;
    private String categories[];
    private String sortBy[];

    private ItemsListAdapter listAdapter;
    private ArrayList<Item> availableItems;

    private String searchQuery = "";
    private String category = "";
    private String lowerCaseSort = "price";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_items);
        setupUI();

        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_NAME, Context.MODE_PRIVATE);
        user = prefs.getString("username", "");

        txtCategory = (TextView) findViewById(R.id.txtCategory);
        progressBar = (ProgressBar) findViewById(R.id.progressGetItems);
        progressBar.setVisibility(View.GONE);

        sortBy = getResources().getStringArray(R.array.sort_by);
        getItems();
        //getCategories();

        txtCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog displayCategories = new AlertDialog.Builder(BuyItemsActivity.this)
                        .setTitle("Categories")
                        .setItems(categories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                txtCategory.setText(categories[which]);
                                category = txtCategory.getText().toString();
                                if (category.equals("All")) {
                                    category = "";
                                }
                                listAdapter.getFilter().filter(category);
                            }
                        })
                        .create();
                displayCategories.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_buy_items, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.BLACK);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                listAdapter.getFilter().filter(searchQuery + "," + category);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                listAdapter.getFilter().filter(searchQuery + "," + category);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_search || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean checkItemClicked(MenuItem menuItem) {
        return menuItem.getItemId() != R.id.nav_buy_items;
    }

    public void getItems() {
        if (txtCategory.getText().toString().equals("Categories")) {
            //getAllItems();
        }
    }
/*
    public void getAllItems() {
        Server.getAvailableItems(user, progressBar, new Ajax.Callbacks() {
            @Override
            public void success(String responseBody) {
                availableItems = new ArrayList<Item>();
                Log.v(TAG, responseBody);
                JSONArray jsonArray = null;

                try {
                    TextView txtMessage = (TextView) findViewById(R.id.txtMessage);
                    ListView lv = (ListView) findViewById(R.id.listViewBuyItems);
                    jsonArray = new JSONArray(responseBody);
                    if (jsonArray.length() == 0) {
                        txtMessage.setText("No available items to buy");
                        txtMessage.setVisibility(View.VISIBLE);
                        lv.setVisibility(View.GONE);
                    } else {
                        txtMessage.setVisibility(View.GONE);
                        availableItems = Item.allItems(jsonArray);
                        listAdapter = new ItemsListAdapter(BuyItemsActivity.this, R.layout.list_item, availableItems);
                        listAdapter.sortItems(lowerCaseSort);
                        lv.setVisibility(View.VISIBLE);
                        lv.setAdapter(listAdapter);

                        Spinner spinnerSortBy = (Spinner) findViewById(R.id.spinnerSortBy);
                        spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                lowerCaseSort = sortBy[position].toLowerCase();
                                listAdapter.sortItems(lowerCaseSort);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Item item = listAdapter.getDisplayView().get(position);

                                mItemId = item.getId();
                                mItemName = item.getItemName();
                                mDescription = item.getDescription();
                                mPrice = item.getPrice();
                                mPicture = item.getPicture();
                                mStarsRequired = item.getStars_required();
                                mFormatPrice = item.getFormattedPrice();

                                Intent intent;
                                intent = new Intent(BuyItemsActivity.this, BuyItemActivity.class);
                                intent.putExtra(Constants.ID, mItemId);
                                intent.putExtra(Constants.ITEM_NAME, mItemName);
                                intent.putExtra(Constants.DESCRIPTION, mDescription);
                                intent.putExtra(Constants.PRICE, mPrice);
                                intent.putExtra(Constants.PICTURE, mPicture);
                                intent.putExtra(Constants.STARS_REQUIRED, mStarsRequired);
                                intent.putExtra(Constants.FORMAT_PRICE, mFormatPrice);

                                startActivity(intent);
                            }
                        });
                    }

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void error(int statusCode, String responseBody, String statusText) {
                Log.v(TAG, "Request error");
                Toast.makeText(BuyItemsActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getCategories() {
        progressBar.setVisibility(View.GONE);
        Server.getCategories(progressBar, new Ajax.Callbacks() {
            @Override
            public void success(String responseBody) {
                try {
                    JSONArray json = new JSONArray(responseBody);
                    if (json.length() != 0) {
                        categories = Category.getAllCategories(new JSONArray(responseBody));
                    } else {
                        Toast.makeText(BuyItemsActivity.this, "Empty categories", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(int statusCode, String responseBody, String statusText) {
                categories = null;
                Toast.makeText(BuyItemsActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtCategory.setText("Categories");
        getItems();
    }
    */
}
