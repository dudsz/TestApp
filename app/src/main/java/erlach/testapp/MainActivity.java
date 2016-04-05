package erlach.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private ProgressDialog pDialog;
    EditText username, password;
    Button loginBtn, regBtn;
    CheckBox rememberBox;
    private boolean saveLogin;
    private SharedPreferences loginPref;
    private SharedPreferences.Editor loginPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = (EditText) findViewById(R.id.login_un);
        password = (EditText) findViewById(R.id.login_pw);
        loginBtn = (Button) findViewById(R.id.btn_login);
        regBtn = (Button) findViewById(R.id.btn_login_reg);
        rememberBox = (CheckBox) findViewById(R.id.loginRememberBox);
        loginPref = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefEditor = loginPref.edit();

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        saveLogin = loginPref.getBoolean("saveLogin", false);
        if (saveLogin) {
            username.setText(loginPref.getString("username", ""));
            password.setText(loginPref.getString("password", ""));
            rememberBox.setChecked(true);
        }

        regBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent regScreen = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(regScreen);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String un = username.getText().toString().trim();
                String pw = password.getText().toString().trim();
                if (rememberBox.isChecked()) {
                    loginPrefEditor.putBoolean("saveLogin", true);
                    loginPrefEditor.putString("username", un);
                    loginPrefEditor.putString("password", pw);
                    loginPrefEditor.apply();
                } else {
                    loginPrefEditor.clear();
                    loginPrefEditor.apply();
                }
                // Create new login
                if (!un.isEmpty() && !pw.isEmpty()) {
                    AsyncLogin loginAttempt = new AsyncLogin();
                    loginAttempt.execute(un, pw);
                    //tryLogin(un, pw);
                    Log.d("LogAc", "Login successfull");
                    Toast.makeText(MainActivity.this,
                            "Successfully Logged In", Toast.LENGTH_LONG).show();
                    // Send login info?
                    Intent userScreen = new Intent(MainActivity.this, UserActivity.class);
                    startActivity(userScreen);
                } else {
                    Toast.makeText(MainActivity.this,
                            "Invalid username or password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private class AsyncLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Logging in. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn;
            String response = "";

            try {
                URL url = new URL("http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_login/login.php");
                // Set parameters to be sent
                Map<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("un", params[0]);
                parameters.put("pw", params[1]);

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
                Log.d("Logged in: ", response);
            } catch (IOException e) {
                Log.d("IO exc: ", e.getLocalizedMessage());
            } catch (Exception e) {
                Log.d("Exception: ", e.getLocalizedMessage());
            }
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            // Send info
            pDialog.dismiss();
        }
    }
}