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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class CreateWishListActivity extends Activity {

    // Progress Dialog
    private ProgressDialog pDialog;
    EditText inWishName, inWishDesc, inWishPlace;
    private static final String RET_SUCCESS = "success";
    JSONObject jObj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wish_list);

        // Edit Text
        inWishName = (EditText) findViewById(R.id.cr_wishName);
        inWishDesc = (EditText) findViewById(R.id.cr_wishDesc);
        inWishPlace = (EditText) findViewById(R.id.cr_wishPL);

        // Create button
        Button btn_createWish = (Button) findViewById(R.id.btn_create_save);
        Button btn_createCancel = (Button) findViewById(R.id.btn_create_cancel);

        btn_createWish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wN = inWishName.getText().toString();
                String wD = inWishDesc.getText().toString();
                String wPl = inWishPlace.getText().toString();
                AddWish addWish = new AddWish();
                addWish.execute();
            }
        });

        btn_createCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    class AddWish extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(CreateWishListActivity.this);
            pDialog.setMessage("Creating wishList. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn;
            String result = "";

            try {
                URL url = new URL("http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_login/createWish.php");
                Map<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("wName", params[0]);
                parameters.put("wDesc", params[1]);
                parameters.put("wPl", params[2]);

                //JSONObject json = jsonParser.makeHttpRequest(urlString, "POST", parameters);
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
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                result = sb.toString();
                Log.d("Create response: ", result);

            } catch (IOException e) {
                Log.d("IO exc: ", e.getLocalizedMessage());
            } catch (Exception e) {
                Log.d("Exception: ", e.getLocalizedMessage());
            }

            // Parse to json object
            try {
                jObj = new JSONObject(result);
            } catch (JSONException e) {
                Log.e("JSON Parsing error", e.toString());
            }

            try {
                int success = jObj.getInt(RET_SUCCESS);
                if (success == 1) {
                    // Successfully added wish
                    Intent i = new Intent(getApplicationContext(), UserActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(CreateWishListActivity.this, "Failed to add wish", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
        }
    }
}
