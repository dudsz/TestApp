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

    private ProgressDialog pDialog;
    ArrayList<HashMap<String, String>> wishArrayList;
    TextView showUsername;
    Button addBtn, delBtn;

    // JSON names
    private static final String RET_SUCCESS = "success";
    private static final String RET_WISHES = "wishes";
    private static final String RET_WID = "wID";
    private static final String RET_WISH_NAME = "wishName";
    private static final String RET_WISH_LIST = "wishList";
    private static final String RET_WISH_DESC = "wishDesc";
    private static final String RET_WISH_PLACE = "wishPlace";
    private static final String KEY_USERNAME = "un";
    private static final String KEY_WL = "wl";
    private static final String URL_ADD_WISH = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_wishes/getWl.php";

    // products JSONArray
    JSONArray wishes = null;
    ListView lw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        String username = getIntent().getStringExtra("un");
        showUsername = (TextView) findViewById(R.id.wish_username);
        addBtn = (Button) findViewById(R.id.btn_add);
        delBtn = (Button) findViewById(R.id.btn_del);

        showUsername.setText(username);
        // Hashmap for ListView
        wishArrayList = new ArrayList<HashMap<String, String>>();
        RetrieveWishes getWishList = new RetrieveWishes();
        getWishList.execute(username, "Jul");

        // Get listview
        lw = (ListView) findViewById(R.id.list);

        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get wishlist
                String wid = ((TextView) view.findViewById(R.id.wid)).getText().toString();
                String wn = ((TextView) view.findViewById(R.id.wishName)).getText().toString();
                // Starting new intent
                Intent editScreen = new Intent(UserActivity.this, EditWishActivity.class);
                // sending pid to next activity
                editScreen.putExtra(RET_WID, wid);
                //editScreen.putExtra(RET_WISH_LIST, wl);
                editScreen.putExtra(RET_WISH_NAME, wn);
                // starting new activity and expecting some response back
                startActivityForResult(editScreen, 100);
            }
        });

        // Change buttons
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent editScreen = new Intent(UserActivity.this, EditWishActivity.class);
                startActivity(editScreen);
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Delete wish
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

    private class RetrieveWishes extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(UserActivity.this);
            pDialog.setMessage("Retrieving wishList. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection conn;
            String response = "";

            try {
                java.net.URL url = new URL(URL_ADD_WISH);
                // Set parameters to be sent
                Map<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("un", params[0]); //params[0]
                parameters.put("wl", params[1]); //params[1]

                // Build and encode parameters
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : parameters.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                // Set properties of request
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.getOutputStream().write(postDataBytes);

                // Get response
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                response = sb.toString();
                Log.d("Wishes: ", response);
                JSONObject jsonObject = new JSONObject(response);

                try {
                    int success = jsonObject.getInt(RET_SUCCESS);
                    if (success == 1) {
                        wishes = jsonObject.getJSONArray(RET_WISHES);
                        for (int i = 0; i < wishes.length(); i++) {
                            JSONObject wishObj = wishes.getJSONObject(i);

                            // Storing each json item in variable
                            String wId = wishObj.getString(RET_WID);
                            String wishList = wishObj.getString(RET_WISH_LIST);
                            String wishName = wishObj.getString(RET_WISH_NAME);
                            String wishDesc = wishObj.getString(RET_WISH_DESC);
                            String wishPlace = wishObj.getString(RET_WISH_PLACE);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(RET_WID, wId);
                            map.put(RET_WISH_LIST, wishList);
                            map.put(RET_WISH_NAME, wishName);
                            map.put(RET_WISH_DESC, wishDesc);
                            map.put(RET_WISH_PLACE, wishPlace);
                            wishArrayList.add(map);
                        }
                    } else {
                        // No wishList
                        Intent userScreen = new Intent(getApplicationContext(), CreateWishListActivity.class);
                        // Closing all previous activities
                        userScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(userScreen);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            } catch (Exception e) {
                Log.d("Exception: ", e.getLocalizedMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            // Send info
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    // Update listview
                    ListAdapter adapter = new SimpleAdapter(UserActivity.this, wishArrayList, R.layout.wishitem,
                            new String[]{ RET_WID, RET_WISH_NAME, RET_WISH_DESC, RET_WISH_PLACE},
                            new int[]{R.id.wid, R.id.wishName, R.id.wishDesc, R.id.wishPlace});
                    lw.setAdapter(adapter);
                    //setListAdapter(adapter);
                }
            });
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
                                    String wishName = wishObj.getString(RET_WISH_NAME);
                                    String username = wishObj.getString("username");

                                    Log.d("Username: ", username);
                                    Log.d("Username: ", username);
                                }
                                // Launch main activity
                                Intent userScreen = new Intent(UserActivity.this, UserActivity.class);
                                startActivity(userScreen);
                                finish();
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
                params.put(KEY_USERNAME, un);
                params.put(KEY_WL, wl);
                return params;
            }
        };
        //RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        AppRequestController.getInstance().addToRequestQueue(stringRequest);
    }
}
