package erlach.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WishListActivity extends Activity {

    // Declarations
    JSONArray wishes = null;
    ArrayList<HashMap<String, String>> wishLists;
    private ListView lw;
    private ProgressDialog pDialog;

    String username;
    TextView showListName;
    Button createBtn;

    // JSON names
    private static final String RET_SUCCESS = "success";
    private static final String RET_WISHES = "wishes";
    private static final String RET_WISH_LIST = "wishList";
    //private static final String RET_SHARED_LIST = "sharedList";
    private static final String KEY_USERNAME = "un";
    private static final String KEY_WL = "wl";
    private static final String URL_GET_LISTS = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_wishes/getLists.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_list);

        // Get data from main
        username = getIntent().getStringExtra("un");

        showListName = (TextView) findViewById(R.id.wish_listName);
        createBtn = (Button) findViewById(R.id.btn_createList);

        // Hashmap for listview
        wishLists = new ArrayList<HashMap<String, String>>();
        // Get listview
        lw = (ListView) findViewById(R.id.list);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setIndeterminate(false);

        getLists(username);


        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get wishlist
                String wl = ((TextView) view.findViewById(R.id.wishList)).getText().toString();
                Intent userScreen = new Intent(WishListActivity.this, UserActivity.class);
                // Sending un and wl to activity
                userScreen.putExtra(KEY_USERNAME, username);
                userScreen.putExtra(KEY_WL, wl);
                // starting new activity and expecting some response back
                startActivityForResult(userScreen, 100);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent userScreen = new Intent(getApplicationContext(), CreateWishListActivity.class);
                // Closing all previous activities
                userScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(userScreen);
            }
        });
    }

    private void getLists(final String un) {

        pDialog.setMessage("Getting lists. Please wait...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GET_LISTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.d("WL Ac: ", response);

                        try {
                            JSONObject jObj = new JSONObject(response);
                            int success = jObj.getInt(RET_SUCCESS);

                            // If successfully added
                            if (success == 1) {
                                wishes = jObj.getJSONArray(RET_WISHES);
                                for (int i = 0; i < wishes.length(); i++) {
                                    JSONObject wishObj = wishes.getJSONObject(i);

                                    // Storing each json item in variable
                                    String wishList = wishObj.getString(RET_WISH_LIST);

                                    // creating new HashMap
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(RET_WISH_LIST, wishList);
                                    wishLists.add(map);
                                }
                                ListAdapter adapter = new SimpleAdapter(WishListActivity.this, wishLists, R.layout.wishlistitem,
                                        new String[]{ RET_WISH_LIST},
                                        new int[]{R.id.wishList});
                                lw.setAdapter(adapter);
                            } else {
                                // No wishList
                                Intent userScreen = new Intent(getApplicationContext(), CreateWishListActivity.class);
                                // Closing all previous activities
                                userScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(userScreen);
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(WishListActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(WishListActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, un);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppRequestController.getInstance().addToRequestQueue(stringRequest);
    }
}
