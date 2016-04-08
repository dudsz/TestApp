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
    ArrayList<HashMap<String, String>> wishArrayList;

    Button updBtn, delBtn;
    EditText wishName, wishWList, wishDesc, wishPlace;
    String wid;

    // JSON names
    private static final String RET_SUCCESS = "success";
    private static final String RET_WISHES = "wishes";
    private static final String RET_WID = "wID";
    private static final String RET_WISH_WL = "wishList";
    private static final String RET_WISH_NAME = "wishName";
    private static final String RET_WISH_DESC = "wishName";
    private static final String RET_WISH_PLACE = "wishName";
    private static final String KEY_USERNAME = "un";
    private static final String KEY_WL = "wl";
    private static final String URL_ADD_WISH = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_wishes/getWl.php";

    // products JSONArray
    JSONArray wishes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_wish);

        // Get wish id
        wid = getIntent().getStringExtra("wID");
        // save button
        updBtn = (Button) findViewById(R.id.btn_update);
        delBtn = (Button) findViewById(R.id.btn_delete);

        wishArrayList = new ArrayList<HashMap<String, String>>();

        // Change buttons
        updBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start async task to update info
            }
        });
        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start async task to delete
            }
        });
    }
    private class RetrieveWishes extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(EditWishActivity.this);
            pDialog.setMessage("Retrieving wishList. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            runOnUiThread(new Runnable() {
                public void run() {
                    HttpURLConnection conn;
                    String response = "";
                    try {
                        java.net.URL url = new URL(URL_ADD_WISH);
                        // Set parameters to be sent
                        Map<String, Object> parameters = new LinkedHashMap<>();
                        parameters.put("un", "markus"); //params[0]
                        parameters.put("wl", "Jul"); //params[1]

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
                                    String wNO = wishObj.getString(RET_WISH_NAME);
                                    String wLO = wishObj.getString(RET_WISH_WL);
                                    String wDO = wishObj.getString(RET_WISH_DESC);
                                    String wPO = wishObj.getString(RET_WISH_PLACE);

                                    wishName = (EditText) findViewById(R.id.wish_name);
                                    wishWList = (EditText) findViewById(R.id.wish_wl);
                                    wishDesc = (EditText) findViewById(R.id.wish_desc);
                                    wishPlace = (EditText) findViewById(R.id.wish_place);

                                    wishName.setText(wNO);
                                    wishWList.setText(wLO);
                                    wishDesc.setText(wDO);
                                    wishPlace.setText(wPO);
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
                }
            });
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
        }
    }
}
