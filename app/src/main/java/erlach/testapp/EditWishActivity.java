package erlach.testapp;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EditWishActivity extends AppCompatActivity {

    private ProgressDialog pDialog;

    Button updBtn, delBtn;
    EditText wishName, wishWList, wishDesc, wishPlace;
    String un, wl, wn;

    // JSON names
    private static final String RET_SUCCESS = "success";
    private static final String RET_WISH = "wish";
    private static final String RET_WID = "wID";
    private static final String RET_WISH_NAME = "wishName";
    private static final String RET_WISH_DESC = "wishDesc";
    private static final String RET_WISH_PLACE = "wishPlace";
    private static final String KEY_UN = "un";
    private static final String KEY_WL = "wl";
    private static final String KEY_WN = "wn";
    private static final String URL_GET_WISH = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_wishes/getWish.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_wish);

        // Get wish id
        un = getIntent().getStringExtra("un");
        wl = getIntent().getStringExtra("wl");
        wn = getIntent().getStringExtra("wn");
        // save button
        updBtn = (Button) findViewById(R.id.btn_update);
        delBtn = (Button) findViewById(R.id.btn_delete);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setIndeterminate(false);

        Log.d("Un: ", un);

        getWish(un, wl, wn);

        // Change buttons
        updBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start async task to update info
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start async task to delete
                finish();
            }
        });
    }

    private void getWish(final String un, final String wl, final String wn) {

        pDialog.setMessage("Getting wish item. Please wait...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GET_WISH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.d("Get wish: ", response);

                        try {
                            JSONObject jObj = new JSONObject(response);
                            int success = jObj.getInt(RET_SUCCESS);

                            // If successfully added
                            if (success == 1) {
                                JSONObject wishObj = jObj.getJSONObject(RET_WISH);

                                wishWList = (EditText) findViewById(R.id.wish_wl);
                                wishName = (EditText) findViewById(R.id.wish_name);
                                wishDesc = (EditText) findViewById(R.id.wish_desc);
                                wishPlace = (EditText) findViewById(R.id.wish_place);

                                wishWList.setText(wl);
                                wishName.setText(wishObj.getString(RET_WISH_NAME));
                                wishDesc.setText(wishObj.getString(RET_WISH_DESC));
                                wishPlace.setText(wishObj.getString(RET_WISH_PLACE));

                            } else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("msg");
                                Toast.makeText(EditWishActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(EditWishActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditWishActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_UN, un);
                params.put(KEY_WL, wl);
                params.put(KEY_WN, wn);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppRequestController.getInstance().addToRequestQueue(stringRequest);
    }
}
