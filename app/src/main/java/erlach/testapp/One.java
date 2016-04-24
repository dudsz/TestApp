package erlach.testapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class One extends Fragment {

    // Declarations
    JSONArray wishes = null;
    ArrayList<HashMap<String, String>> wishLists;
    private ListView lw;
    private ProgressDialog pDialog;
    String username;

    TextView showListName;
    Button createBtn;

    // JSON names
    private static final String RET_SUCCESS = "success";
    private static final String RET_WISHES = "wishes";
    private static final String RET_WL = "wishList";
    private static final String KEY_UN = "un";
    private static final String KEY_WL = "wl";
    private static final String URL_GET_LISTS = "http://ec2-54-191-47-17.us-west-2.compute.amazonaws.com/test_wishes/getLists.php";

    public One() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);

        username = getActivity().getIntent().getStringExtra(KEY_UN);
        showListName = (TextView) view.findViewById(R.id.wish_listName);
        createBtn = (Button) view.findViewById(R.id.btn_createList);

        // Hashmap for listview and listview
        wishLists = new ArrayList<HashMap<String, String>>();
        lw = (ListView) view.findViewById(R.id.list);

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        pDialog.setIndeterminate(false);

        getLists(username);

        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get wishlist
                String wl = ((TextView) view.findViewById(R.id.wishList)).getText().toString();
                Intent userScreen = new Intent(getContext(), UserActivity.class);
                // Sending un and wl to activity
                userScreen.putExtra(KEY_UN, username);
                userScreen.putExtra(KEY_WL, wl);
                // Lägg till response till activity?
                startActivity(userScreen);
                //startActivityForResult(userScreen, 100);
            }
        });
        createBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent createScreen = new Intent(getContext(), CreateWishListActivity.class);
                // Closing all previous activities
                createScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(createScreen);
            }
        });

        return view;
    }

    private void getLists(final String un) {

        pDialog.setMessage("Getting lists. Please wait...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GET_LISTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.d("WL Ac: ", response);

                        try {
                            JSONObject jObj = new JSONObject(response);
                            int success = jObj.getInt(RET_SUCCESS);

                            // If successfully added
                            if (success == 1) {
                                wishes = jObj.getJSONArray(RET_WISHES);
                                for (int i = 0; i < wishes.length(); i++) {
                                    JSONObject wishObj = wishes.getJSONObject(i);

                                    // Storing each json item in variable
                                    String wishList = wishObj.getString(RET_WL);

                                    // creating new HashMap
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(RET_WL, wishList);
                                    wishLists.add(map);
                                }
                                ListAdapter adapter = new SimpleAdapter(getActivity(), wishLists, R.layout.wishlistitem,
                                        new String[]{ RET_WL},
                                        new int[]{R.id.wishList});
                                lw.setAdapter(adapter);
                            } else {
                                // No wishLists found
                                Intent userScreen = new Intent(getContext(), CreateWishListActivity.class);
                                // Closing all previous activities
                                userScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(userScreen);
                               }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<String, String>();
                params.put(KEY_UN, un);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppRequestController.getInstance().addToRequestQueue(stringRequest);
    }
}