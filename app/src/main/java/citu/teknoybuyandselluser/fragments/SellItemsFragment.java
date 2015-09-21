package citu.teknoybuyandselluser.fragments;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import citu.teknoybuyandselluser.Ajax;
import citu.teknoybuyandselluser.DashboardActivity;
import citu.teknoybuyandselluser.LoginActivity;
import citu.teknoybuyandselluser.R;
import citu.teknoybuyandselluser.Server;
import citu.teknoybuyandselluser.listAdapters.ItemsListAdapter;
import citu.teknoybuyandselluser.models.Item;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SellItemsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SellItemsFragment extends Fragment {

    private static final String ARG_PARAM1 = "user";
    private static final String TAG = "SellItemsFragment";
    private View view = null;

    private static final String ITEM_NAME = "item_name";
    private static final String DESCRIPTION = "description";
    private static final String PRICE = "price";
    private static final String PICTURE = "picture";
    private static final String STARS_REQUIRED = "stars_required";

    private String itemName;
    private String description;
    private float price;
    private String picture;
    private int stars_required;


    public static SellItemsFragment newInstance(String itemName, String description, float price, String picture, int stars_required) {
        SellItemsFragment fragment = new SellItemsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ITEM_NAME, itemName);
        bundle.putString(DESCRIPTION, description);
        bundle.putFloat(PRICE, price);
        bundle.putString(PICTURE, picture);
        bundle.putInt(STARS_REQUIRED, stars_required);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sell_items, container, false);

        SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.MY_PREFS_NAME, Context.MODE_PRIVATE);
        String user = prefs.getString("username", "");

        Server.getItemsToSell(user, new Ajax.Callbacks() {
            @Override
            public void success(String responseBody) {
                ArrayList<Item> ownedItems = new ArrayList<Item>();
                Log.v(TAG, responseBody);
                JSONArray jsonArray = null;

                try {
                    jsonArray = new JSONArray(responseBody);
                    if(jsonArray.length()==0){
                        TextView txtMessage = (TextView) view.findViewById(R.id.txtMessage);
                        txtMessage.setText("You have no available items to be sold");
                        txtMessage.setVisibility(View.VISIBLE);
                    } else {
                        ownedItems = Item.allItems(jsonArray);

                        ListView lv = (ListView) view.findViewById(R.id.listViewSellItems);
                        ItemsListAdapter listAdapter = new ItemsListAdapter(getActivity().getBaseContext(), R.layout.activity_item, ownedItems);
                        lv.setAdapter(listAdapter);
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Item item = (Item) parent.getItemAtPosition(position);
                                itemName = item.getItemName();
                                description = item.getDescription();
                                price = item.getPrice();
                                picture = item.getPicture();
                                stars_required = item.getStars_required();

                                Fragment fragment = null;
                                fragment = SellItemDetailsFragment.newInstance(itemName, description, price, picture, stars_required);
                                ((DashboardActivity) getActivity()).setActionBarTitle(itemName);

                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.flContent, fragment);
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                ft.commit();
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
            }
        });

        FloatingActionButton fab= (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = null;
                Class fragmentClass = null;
                fragmentClass = SellItemFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.flContent, fragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        });

        return view;
    }
}
