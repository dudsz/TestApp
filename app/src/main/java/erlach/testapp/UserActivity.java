package erlach.testapp;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserActivity extends Activity {

    // products JSONArray
    JSONArray wishes = null;
    ListView lw;

    private ProgressDialog pDialog;
    ArrayList<HashMap<String, String>> wishArrayList;

    TextView showUsername, showWishList;
    Button addBtn, delBtn;
    String username, wl;

    // JSON names
    private static final String RET_SUCCESS = "success";
    private static final String RET_WISHES = "wishes";
    private static final String RET_WID = "wID";
    private static final String RET_WN = "wishName";
    private static final String RET_WL = "wishList";
    private static final String RET_WD = "wishDesc";
    private static final String RET_WPL = "wishPlace";
    private static final String KEY_UN = "un";
    private static final String KEY_WL = "wl";
    private static final String KEY_WN = "wn";
    private static final String URL_ADD_WISH = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_wishes/getWl.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setIndeterminate(false);

        // Get data from choice
        username = getIntent().getStringExtra("un");
        wl = getIntent().getStringExtra("wl");

        showUsername = (TextView) findViewById(R.id.wish_username);
        showWishList = (TextView) findViewById(R.id.wish_listName);
        addBtn = (Button) findViewById(R.id.btn_add);
        delBtn = (Button) findViewById(R.id.btn_del);

        showUsername.setText(username);
        showWishList.setText(wl);
        // Hashmap for ListView
        wishArrayList = new ArrayList<HashMap<String, String>>();
        //RetrieveWishes getWishList = new RetrieveWishes();
        //getWishList.execute(username, "Jul");
        getWishList(username, wl);
        // Get listview
        lw = (ListView) findViewById(R.id.list);

        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get wishlist
                String wid = ((TextView) view.findViewById(R.id.wid)).getText().toString();
                //String wl = ((TextView) view.findViewById(R.id.wish)).getText().toString();
                String wn = ((TextView) view.findViewById(R.id.wishName)).getText().toString();
                // Starting new intent
                Intent editScreen = new Intent(UserActivity.this, EditWishActivity.class);
                // sending data to next activity
                editScreen.putExtra(RET_WID, wid);
                editScreen.putExtra(KEY_UN, username);
                editScreen.putExtra(KEY_WL, wl);
                editScreen.putExtra(KEY_WN, wn);
                // starting new activity and expecting some response back
                //startActivityForResult(editScreen, 100);
                startActivity(editScreen);
            }
        });

        // Change buttons
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Change EditWishActivity to Add (Create add activity)
                Intent editScreen = new Intent(UserActivity.this, EditWishActivity.class);
                editScreen.putExtra(KEY_UN, username);
                editScreen.putExtra(RET_WL, wl);
                startActivity(editScreen);
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Change to back?
                Intent listScreen = new Intent(UserActivity.this, One.class);
                listScreen.putExtra(KEY_UN, username);
                startActivity(listScreen);
            }
        });
    }
    // Response from Edit Product Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    private void getWishList(final String un, final String wl) {

        pDialog.setMessage("Registering. Please wait...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ADD_WISH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.d("Login: ", response);

                        try {
                            JSONObject jObj = new JSONObject(response);
                            int success = jObj.getInt(RET_SUCCESS);

                            // If successfully added
                            if (success == 1) {
                                wishes = jObj.getJSONArray(RET_WISHES);
                                for (int i = 0; i < wishes.length(); i++) {
                                    JSONObject wishObj = wishes.getJSONObject(i);

                                    // Storing each json item in variable
                                    String wId = wishObj.getString(RET_WID);
                                    String wishName = wishObj.getString(RET_WN);
                                    String wishDesc = wishObj.getString(RET_WD);
                                    String wishPlace = wishObj.getString(RET_WPL);

                                    // creating new HashMap
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(RET_WID, wId);
                                    map.put(RET_WN, wishName);
                                    map.put(RET_WD, wishDesc);
                                    map.put(RET_WPL, wishPlace);
                                    wishArrayList.add(map);
                                }
                                ListAdapter adapter = new SimpleAdapter(UserActivity.this, wishArrayList, R.layout.wishitem,
                                        new String[]{ RET_WID, RET_WN, RET_WD, RET_WPL},
                                        new int[]{R.id.wid, R.id.wishName, R.id.wishDesc, R.id.wishPlace});
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
                            Toast.makeText(UserActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_UN, un);
                params.put(KEY_WL, wl);
                return params;
            }
        };
        AppRequestController.getInstance().addToRequestQueue(stringRequest);
    }
}
