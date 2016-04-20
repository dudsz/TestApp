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

import com.android.volley.DefaultRetryPolicy;
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
    JSONObject jObj = null;
    private static final String RET_SUCCESS = "success";
    private static final String KEY_USERNAME = "un";
    private static final String KEY_PASSWORD = "pw";
    private static final String KEY_EMAIL = "email";
    private static final String URL_REGISTER = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_login/insert.php";

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
        pDialog.setIndeterminate(false);

        regBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String un = tUn.getText().toString().trim();
                String pw = tPw.getText().toString().trim();
                String pwc = tPwc.getText().toString().trim();
                String email = tEmail.getText().toString().trim();
                //register(un, pw, pwc, email);
                if (!un.isEmpty() && !pw.isEmpty() && !pwc.isEmpty() && !email.isEmpty() && pw.equals(pwc)) {
                    //AsyncReg regAttempt = new AsyncReg();
                    //regAttempt.execute(un, pw, email);
                    registerRequest(un, pw, email);
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

    private void registerRequest(final String un, final String pw, final String email) {

        pDialog.setMessage("Registering. Please wait...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTER,
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
                                String email = user.getString("email");

                                Log.d("Username: ", username);
                                Log.d("Email: ", email);
                                // Launch main activity
                                Intent userScreen = new Intent(RegisterActivity.this, UserActivity.class);
                                userScreen.putExtra("un", username);
                                startActivity(userScreen);
                                finish();
                            } else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("msg");
                                Toast.makeText(RegisterActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "Register error: " + response, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                })

        {
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, un);
                params.put(KEY_PASSWORD, pw);
                params.put(KEY_EMAIL, email);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppRequestController.getInstance().addToRequestQueue(stringRequest);
    }
}



