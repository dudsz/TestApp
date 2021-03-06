package erlach.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private ProgressDialog pDialog;
    EditText username, password;
    Button loginBtn, regBtn;
    CheckBox rememberBox;
    private boolean saveLogin;
    private SharedPreferences loginPref;
    private SharedPreferences.Editor loginPrefEditor;
    JSONObject jObj = null;
    private static final String RET_SUCCESS = "success";
    private static final String KEY_USERNAME = "un";
    private static final String KEY_PASSWORD = "pw";
    private static final String URL_LOGIN = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_login/login.php";

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

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setIndeterminate(false);

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
                    loginRequest(un, pw);
                } else {
                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loginRequest(final String un, final String pw) {

        pDialog.setMessage("Logging in. Please wait...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
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
                                JSONObject user = jObj.getJSONObject("user");
                                String username = user.getString("username");

                                Log.d("Username: ", username);
                                // Launch wishlist activity
                                Intent userScreen = new Intent(MainActivity.this, ChooseListActivity.class);
                                userScreen.putExtra("un", username);
                                startActivity(userScreen);
                                finish();
                            } else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("msg");
                                Toast.makeText(MainActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, un);
                params.put(KEY_PASSWORD, pw);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppRequestController.getInstance().addToRequestQueue(stringRequest);
    }
}