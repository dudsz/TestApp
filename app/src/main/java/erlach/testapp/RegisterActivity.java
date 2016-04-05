package erlach.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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

public class RegisterActivity extends Activity {

    private ProgressDialog pDialog;
    EditText tUn, tPw, tPwc, tEmail;
    Button cancelBtn, regBtn;
    String registerUrl = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_login/insert.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tUn = (EditText) findViewById(R.id.reg_un);
        tPw = (EditText) findViewById(R.id.reg_pw);
        tPwc = (EditText) findViewById(R.id.reg_pwc);
        tEmail = (EditText) findViewById(R.id.reg_email);
        cancelBtn = (Button) findViewById(R.id.btn_register_cancel);
        regBtn = (Button) findViewById(R.id.btn_register_reg);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        regBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String un = tUn.getText().toString().trim();
                String pw = tPw.getText().toString().trim();
                String pwc = tPwc.getText().toString().trim();
                String email = tEmail.getText().toString().trim();
                //register(un, pw, pwc, email);
                if (!un.isEmpty() && !pw.isEmpty() && !pwc.isEmpty() && !email.isEmpty() && pw.equals(pwc)) {
                    AsyncReg regAttempt = new AsyncReg();
                    regAttempt.execute(un, pw, email);
                } else {
                    Toast.makeText(RegisterActivity.this, "Required field empty", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent userScreen = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(userScreen);
            }
        });
    }
    private class AsyncReg extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Registering account. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn;
            String result = "";

            try {
                URL url = new URL("http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_login/insert.php");
                // Set parameters to be sent
                Map<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("un", params[0]);
                parameters.put("pw", params[1]);
                parameters.put("email", params[2]);

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
                Log.d("Logged in: ", result);
            } catch (IOException e) {
                Log.d("IO exc: ", e.getLocalizedMessage());
            } catch (Exception e) {
                Log.d("Exception: ", e.getLocalizedMessage());
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            // Send info
            pDialog.dismiss();
        }
    }
}



