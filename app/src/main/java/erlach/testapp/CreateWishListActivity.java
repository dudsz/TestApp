package erlach.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CreateWishListActivity extends Activity {

    // Progress Dialog
    private ProgressDialog pDialog;
    EditText inWishName, inWishDesc, inWishPlace;
    String un, wl;
    JSONObject jObj = null;

    private static final String RET_SUCCESS = "success";
    private static final String KEY_UN = "un";
    private static final String KEY_WL = "wl";
    private static final String KEY_WN = "wn";
    private static final String KEY_WD = "wd";
    private static final String KEY_WPL = "wpl";
    private static final String URL_CREATE_LIST = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_wishes/addWish.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wish_list);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setIndeterminate(false);

        // Get data from activity
        wl = getIntent().getStringExtra("wl");
        un = getIntent().getStringExtra("un");

        // Edit Text
        inWishName = (EditText) findViewById(R.id.cr_list_wishName);
        inWishDesc = (EditText) findViewById(R.id.cr_list_wishDesc);
        inWishPlace = (EditText) findViewById(R.id.cr_list_wishPlace);

        // Create button
        Button btn_createWish = (Button) findViewById(R.id.btn_create_create);
        Button btn_createCancel = (Button) findViewById(R.id.btn_create_cancel);

        btn_createWish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wn = inWishName.getText().toString();
                String wd = inWishDesc.getText().toString();
                String wpl = inWishPlace.getText().toString();
                if (!wn.isEmpty() && !wd.isEmpty() && !wpl.isEmpty()) {
                    createWishList(un, wl, wn, wd, wpl);
                } else {
                    Toast.makeText(CreateWishListActivity.this, "Please add a wish", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_createCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listScreen = new Intent(CreateWishListActivity.this, WishListActivity.class);
                listScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(listScreen);
            }
        });
    }

    private void createWishList(final String un, final String wl, final String wn, final String wd, final String wpl) {

        pDialog.setMessage("Creating wishlist. Please wait...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CREATE_LIST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.d("Create wl + add wish: ", response);

                        try {
                            JSONObject jObj = new JSONObject(response);
                            int success = jObj.getInt(RET_SUCCESS);

                            // If successfully added
                            if (success == 1) {
                                // Launch main activity
                                Intent userScreen = new Intent(CreateWishListActivity.this, UserActivity.class);
                                userScreen.putExtra("un", un);
                                userScreen.putExtra("wl", wl);
                                startActivity(userScreen);
                                finish();
                            } else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("msg");
                                Toast.makeText(CreateWishListActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(CreateWishListActivity.this, "Register error: " + response, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CreateWishListActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_UN, un);
                params.put(KEY_WL, wl);
                params.put(KEY_WN, wn);
                params.put(KEY_WD, wd);
                params.put(KEY_WPL, wpl);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppRequestController.getInstance().addToRequestQueue(stringRequest);
    }
}
