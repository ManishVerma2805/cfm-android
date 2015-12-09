package com.getfiscal.mobile.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.getfiscal.mobile.GetFiscalApplication;
import com.getfiscal.mobile.R;
import com.getfiscal.mobile.adapter.TransactionAdapter;
import com.getfiscal.mobile.model.Constants;
import com.getfiscal.mobile.model.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";


    private static final int REQUEST_CODE_CREATE_TRANSACTION = 101;
    private static final int REQUEST_CODE_VIEW_TRANSACTION = 102;



    ListView listView;
    BaseAdapter adapter;

    float totalBalanceMoneyIn = 0;
    float totalBalanceMoneyOut = 0;

    float totalMoneyInCompleted = 0;
    float totalMoneyOutCompleted = 0;

    DecimalFormat formatter = new DecimalFormat("##,##,##,##,##,##,##0.00");

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private CharSequence mTitle;


    ArrayList<Transaction> receivables;
    ArrayList<Transaction> payables;
    ArrayList<Transaction> complete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        mTitle = getTitle();
        getUser();


        listView = (ListView) findViewById(R.id.transactionList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Transaction transaction = (Transaction) adapterView.getItemAtPosition(i);


                Bundle data = transaction.getBundle();


                data.putString(Constants.COMPANY_NAME, GetFiscalApplication.getInstance().getCompanyName());

                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                intent.putExtra(Constants.MODE, Constants.MODE_EDIT);
                intent.putExtra(Constants.TRANSACTION_TYPE, currentList);
                intent.putExtra(Constants.DATA, data);
                startActivityForResult(intent, REQUEST_CODE_VIEW_TRANSACTION);


            }
        });


        setUpInTab();
        setUpOutTab();
        setUpLoanTab();


    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
            actionBar.setIcon(R.drawable.gf_logo_actionbar);
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private DrawerLayout drawerLayout;

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);


        TextView tvCompanyName = (TextView) view.findViewById(R.id.companyName);
        tvCompanyName.setText(GetFiscalApplication.getInstance().getCompanyName());

        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
//                Snackbar.make(content, menuItem.getTitle() + " pressed", Snackbar.LENGTH_LONG).show();
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.drawer_company_profile: {
                        break;
                    }
                    case R.id.drawer_receivables: {
                        populateInTransaction();
                        break;
                    }
                    case R.id.drawer_payables: {
                        populateOutTransaction();
                        break;
                    }
                    case R.id.drawer_logout: {
                        finish();
                        break;
                    }
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    //@Override
    //public void onNavigationDrawerItemSelected(int position) {
//        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//                .commit();
    //}

//    public void onSectionAttached(int number) {
//        switch (number) {
//            case 1:
//                mTitle = getString(R.string.title_section1);
//                break;
//            case 2:
//                mTitle = getString(R.string.title_section2);
//                break;
//            case 3:
//                mTitle = getString(R.string.title_section3);
//                break;
//        }
//    }

//    public void restoreActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(mTitle);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
                if(currentList==Constants.TRANSACTION_TYPE_MONEY_IN)
                {
                    view.getMenu().getItem(0).setChecked(true);
                }
                else if (currentList==Constants.TRANSACTION_TYPE_MONEY_OUT)
                {
                    view.getMenu().getItem(1).setChecked(true);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void getUser() {
        String tag_json_obj = "json_obj_user_req";

        String url = GetFiscalApplication.BASE_URL + "/api/users/me";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            GetFiscalApplication.getInstance().setCompanyId((String)response.get("company"));
                            pDialog.cancel();
                            getComapny();
                        } catch (JSONException e) {
                            pDialog.cancel();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        pDialog.cancel();
                        //if(error.networkResponse.statusCode==422)
                        {
                        }
                    }
                }) {

//            @Override
//            public byte[] getBody() {
//                //return "{\"email\":\"manish.verma2805@gmail.com\", \"password\": \"man123\"}".getBytes();
//                return loginJson.toString().getBytes();
//
//
//            }

            @Override
            public String getBodyContentType() {

                return "application/json;charset=UTF-8";

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + GetFiscalApplication.getInstance().getToken());
                return headers;

            }

        };

        // Adding request to request queue
        GetFiscalApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }


    private void getComapny() {
        String tag_json_obj = "json_obj_company_req";

        String url = GetFiscalApplication.BASE_URL + "/api/companies/" + GetFiscalApplication.getInstance().getCompanyId();

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Getting Company...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            GetFiscalApplication.getInstance().setCompanyName((String) response.get("name"));
                            if (response.has("phone")) {
                                GetFiscalApplication.getInstance().setCompanyPhone((String) response.get("phone"));
                            }

                            //Toast.makeText(getApplicationContext(), companyName + " : " + companyPhone, Toast.LENGTH_LONG).show();
                            pDialog.cancel();
                            setupDrawerLayout();
                            getListOfPayablesFromServer(false);
                            //getListOfReceivablesFromServer(true);
                            populateInTransaction();
                        } catch (JSONException e) {
                            pDialog.cancel();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        pDialog.cancel();
                        //if(error.networkResponse.statusCode==422)
                        {
                        }
                    }
                }) {

//            @Override
//            public byte[] getBody() {
//                //return "{\"email\":\"manish.verma2805@gmail.com\", \"password\": \"man123\"}".getBytes();
//                return loginJson.toString().getBytes();
//
//
//            }

            @Override
            public String getBodyContentType() {

                return "application/json;charset=UTF-8";

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + GetFiscalApplication.getInstance().getToken());
                return headers;

            }

        };

        // Adding request to request queue
        GetFiscalApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    int index = 0;

    private void getListOfReceivablesFromServer(final boolean updateUI) {
        String tag_json_obj = "json_obj_getListOfReceivablesFromServer_req";

        String url = GetFiscalApplication.BASE_URL + "/api/companies/" + GetFiscalApplication.getInstance().getCompanyId() + "/invoices";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Getting Receivables ...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            TextView msg = (TextView) findViewById(R.id.msg_add);
                            JSONArray invoices = response.getJSONArray("invoices");
                            if (invoices != null && invoices.length() > 0) {
                                msg.setVisibility(View.GONE);
                                if (receivables == null) {
                                    receivables = new ArrayList<>();
                                } else {
                                    receivables.clear();
                                }
                                totalBalanceMoneyIn = 0;
                                totalMoneyInCompleted=0;
                                for (int i = 0; i < invoices.length(); i++) {
                                    try {
                                        JSONObject object = invoices.getJSONObject(i);
                                        Transaction transaction = new Transaction();
                                        transaction.setType(Constants.TRANSACTION_TYPE_MONEY_IN);
                                        transaction.parseFromJsonObject(object);
                                        receivables.add(transaction);

                                        totalBalanceMoneyIn += transaction.getBalanceAmount();
                                        totalMoneyInCompleted +=transaction.getAmount();
                                    } catch (JSONException e) {

                                    }
                                }

                                if (updateUI && currentList == Constants.TRANSACTION_TYPE_MONEY_IN) {
                                    Collections.sort(receivables, new Transaction());
                                    adapter = new TransactionAdapter(MainActivity.this, receivables);
                                    listView.setAdapter(adapter);

                                }
                                //updateStatusBar();
                            } else {
                                adapter = null;
                                listView.setAdapter(adapter);
                                msg.setVisibility(View.VISIBLE);
                                msg.setText(R.string.msg_add_transaction_in);
                            }
                            pDialog.cancel();
                        } catch (JSONException e) {
                            pDialog.cancel();
                            adapter = null;
                            listView.setAdapter(adapter);
                            TextView msg = (TextView) findViewById(R.id.msg_add);
                            msg.setVisibility(View.VISIBLE);
                            msg.setText(R.string.msg_add_transaction_in);
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        pDialog.cancel();
                        adapter = null;
                        listView.setAdapter(adapter);
                        TextView msg = (TextView) findViewById(R.id.msg_add);
                        msg.setVisibility(View.VISIBLE);
                        msg.setText(R.string.msg_add_transaction_in);
                    }
                }) {

//            @Override
//            public byte[] getBody() {
//                //return "{\"email\":\"manish.verma2805@gmail.com\", \"password\": \"man123\"}".getBytes();
//                return loginJson.toString().getBytes();
//
//
//            }

            @Override
            public String getBodyContentType() {

                return "application/json;charset=UTF-8";

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + GetFiscalApplication.getInstance().getToken());
                return headers;

            }

        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        GetFiscalApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }


//    private void mockGetRecievables()
//    {
//        {"invoices":[{"_id":"561242d621f51d6012763858","totalAmt":100000,"refNumber":"100","balance":100000,"dueDate":"2015-08-01","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Liu Kang","phone":"+91123456789"}},{"_id":"561242d621f51d6012763859","totalAmt":12345.78,"refNumber":"101","balance":12345.78,"dueDate":"2015-08-02","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Johnny Cage","phone":"(080)1234567890"}},{"_id":"561242d621f51d601276385a","totalAmt":55555,"refNumber":"102","balance":55555,"dueDate":"2015-08-03","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Sonya Blade","phone":"1234567890"}},{"_id":"561242d621f51d601276385c","totalAmt":123.11,"refNumber":"104","balance":123.11,"dueDate":"2015-08-05","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Princess Katana","phone":"9886 123 456"}},{"_id":"561242d621f51d601276385d","totalAmt":45000.15,"refNumber":"105","balance":45000.15,"dueDate":"2015-11-01","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Kano","phone":"+91123456789"}},{"_id":"561242d621f51d601276385e","totalAmt":98765,"refNumber":"106","balance":98765,"dueDate":"2015-11-02","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Scorpion","phone":"(080)1234567890"}},{"_id":"561242d621f51d601276385f","totalAmt":38292.33,"refNumber":"107","balance":38292.33,"dueDate":"2015-11-03","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Sub Zero","phone":"1234567890"}},{"_id":"561242d621f51d6012763861","totalAmt":3456789.11,"refNumber":"109","balance":3456789.11,"dueDate":"2015-11-05","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Baraka","phone":"9886 123 456"}},{"_id":"561242d621f51d6012763862","totalAmt":9876.01,"refNumber":"110","balance":9876.01,"dueDate":"2015-11-06","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Goro","phone":"91-80-1234567890"}},{"_id":"561242d621f51d601276385b", "totalAmt":323456.9,"refNumber":"103","balance":323456.9,"dueDate":
//            "2015-08-04","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Rayden","phone":"91-80-1234567890"}},{"_id":"561242d621f51d6012763860","totalAmt":5628.004,"refNumber":"108","balance":5628.004,"dueDate":"2015-11-04","company":"560d1e358b9f4def7aa133d0","__v":0,"customer":{"name":"Shao Kahn","phone":"91-80-1234567890"}}]}""
//    }

    private void getListOfPayablesFromServer(final boolean updateUI) {
        String tag_json_obj = "json_obj_getListOfPayablesFromServer_req";

        String url = GetFiscalApplication.BASE_URL + "/api/companies/" + GetFiscalApplication.getInstance().getCompanyId() + "/bills";
        final ProgressDialog pDialog;
        if (updateUI) {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Getting Payables...");
            pDialog.show();
        } else {
            pDialog = null;
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            TextView msg = (TextView) findViewById(R.id.msg_add);
                            JSONArray invoices = response.getJSONArray("bills");
                            if (invoices != null && invoices.length() > 0) {
                                msg.setVisibility(View.GONE);
                                if (payables == null) {
                                    payables = new ArrayList<>();
                                } else {
                                    payables.clear();
                                }
                                totalBalanceMoneyOut = 0;
                                totalMoneyOutCompleted=0;
                                for (int i = 0; i < invoices.length(); i++) {
                                    try {
                                        JSONObject object = invoices.getJSONObject(i);
                                        Transaction transaction = new Transaction();
                                        transaction.setType(Constants.TRANSACTION_TYPE_MONEY_OUT);
                                        transaction.parseAsPayableFromJsonObject(object);
                                        payables.add(transaction);

                                        totalBalanceMoneyOut += transaction.getBalanceAmount();
                                        totalMoneyOutCompleted +=transaction.getAmount();
                                    } catch (JSONException e) {

                                    }
                                }

                                if (updateUI && currentList == Constants.TRANSACTION_TYPE_MONEY_OUT) {
                                    Collections.sort(payables, new Transaction());
                                    adapter = new TransactionAdapter(MainActivity.this, payables);
                                    listView.setAdapter(adapter);


                                }
                                //updateStatusBar();
                            } else {
                                adapter = null;
                                listView.setAdapter(adapter);
                                msg.setVisibility(View.VISIBLE);
                                msg.setText(R.string.msg_add_transaction_out);
                            }
                            if (pDialog != null) {
                                pDialog.cancel();
                            }
                        } catch (JSONException e) {
                            if (pDialog != null) {
                                pDialog.cancel();
                            }
                            adapter = null;
                            listView.setAdapter(adapter);
                            TextView msg = (TextView) findViewById(R.id.msg_add);
                            msg.setVisibility(View.VISIBLE);
                            msg.setText(R.string.msg_add_transaction_out);
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        if (pDialog != null) {
                            pDialog.cancel();
                            adapter = null;
                            listView.setAdapter(adapter);
                            TextView msg = (TextView) findViewById(R.id.msg_add);
                            msg.setVisibility(View.VISIBLE);
                            msg.setText(R.string.msg_add_transaction_out);
                        }
                        //if(error.networkResponse.statusCode==422)
                        {
                        }
                    }
                }) {

//            @Override
//            public byte[] getBody() {
//                //return "{\"email\":\"manish.verma2805@gmail.com\", \"password\": \"man123\"}".getBytes();
//                return loginJson.toString().getBytes();
//
//
//            }

            @Override
            public String getBodyContentType() {

                return "application/json;charset=UTF-8";

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + GetFiscalApplication.getInstance().getToken());
                return headers;

            }

        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        GetFiscalApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }


    private void setUpInTab() {
        ViewGroup tabBar = (ViewGroup) findViewById(R.id.tabBar);
        final ViewGroup inFlowtabHolder = (ViewGroup) tabBar.findViewById(R.id.inflowHolder);

        inFlowtabHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateInTransaction();

            }
        });

    }

    private void setUpOutTab() {
        ViewGroup tabBar = (ViewGroup) findViewById(R.id.tabBar);
        final ViewGroup outFlowtabHolder = (ViewGroup) tabBar.findViewById(R.id.outflowHolder);

        outFlowtabHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateOutTransaction();
            }
        });

    }

    private void setUpLoanTab() {
        ViewGroup tabBar = (ViewGroup) findViewById(R.id.tabBar);
        final ViewGroup loantabHolder = (ViewGroup) tabBar.findViewById(R.id.loanHolder);

        loantabHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                populateCompletedTransaction();
            }
        });

    }


    void setSelection(View view, boolean selected) {
        if (view != null) {
            view.setSelected(selected);
        }
    }

    int currentList = Constants.TRANSACTION_TYPE_MONEY_IN;

    private void populateInTransaction() {
        currentList = Constants.TRANSACTION_TYPE_MONEY_IN;
        ViewGroup tabBar = (ViewGroup) findViewById(R.id.tabBar);
        final ViewGroup inFlowtabHolder = (ViewGroup) tabBar.findViewById(R.id.inflowHolder);
        final ViewGroup outFlowtabHolder = (ViewGroup) tabBar.findViewById(R.id.outflowHolder);
        final ViewGroup loantabHolder = (ViewGroup) tabBar.findViewById(R.id.loanHolder);

        setSelection(loantabHolder, false);
        setSelection(inFlowtabHolder, true);
        setSelection(outFlowtabHolder, false);

        View create = findViewById(R.id.createTransaction);
        create.setVisibility(View.VISIBLE);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                intent.putExtra(Constants.MODE, Constants.MODE_CREATE);
                intent.putExtra(Constants.TRANSACTION_TYPE, Constants.TRANSACTION_TYPE_MONEY_IN);
                startActivityForResult(intent, REQUEST_CODE_CREATE_TRANSACTION);
            }
        });
        getListOfReceivablesFromServer(true);
//        list = DataUtils.getListOfMoneyInTransactions(MainActivity.this);
//        adapter = new TransactionAdapter(MainActivity.this, list);
//        listView.setAdapter(adapter);
    }


    private void populateOutTransaction() {
        currentList = Constants.TRANSACTION_TYPE_MONEY_OUT;
        ViewGroup tabBar = (ViewGroup) findViewById(R.id.tabBar);
        final ViewGroup outFlowtabHolder = (ViewGroup) tabBar.findViewById(R.id.outflowHolder);
        final ViewGroup inFlowtabHolder = (ViewGroup) tabBar.findViewById(R.id.inflowHolder);
        final ViewGroup loantabHolder = (ViewGroup) tabBar.findViewById(R.id.loanHolder);

        setSelection(loantabHolder, false);
        setSelection(inFlowtabHolder, false);
        setSelection(outFlowtabHolder, true);

        View create = findViewById(R.id.createTransaction);
        create.setVisibility(View.VISIBLE);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                intent.putExtra(Constants.MODE, Constants.MODE_CREATE);
                intent.putExtra(Constants.TRANSACTION_TYPE, Constants.TRANSACTION_TYPE_MONEY_OUT);
                startActivityForResult(intent, REQUEST_CODE_CREATE_TRANSACTION);
            }
        });
        getListOfPayablesFromServer(true);
//        list = DataUtils.getListOfMoneyOutTransactions(MainActivity.this);
//        adapter = new TransactionAdapter(MainActivity.this, list);
//        listView.setAdapter(adapter);
    }


    private void populateCompletedTransaction() {
        currentList = Constants.COMPLETED_TRANSACTION;
        ViewGroup tabBar = (ViewGroup) findViewById(R.id.tabBar);
        final ViewGroup loantabHolder = (ViewGroup) tabBar.findViewById(R.id.loanHolder);
        final ViewGroup inFlowtabHolder = (ViewGroup) tabBar.findViewById(R.id.inflowHolder);
        final ViewGroup outFlowtabHolder = (ViewGroup) tabBar.findViewById(R.id.outflowHolder);

        setSelection(loantabHolder, true);
        setSelection(inFlowtabHolder, false);
        setSelection(outFlowtabHolder, false);

        ListView listView = (ListView) findViewById(R.id.transactionList);
        listView.setAdapter(null);

        View create = findViewById(R.id.createTransaction);
        create.setVisibility(View.INVISIBLE);

        //list = DataUtils.getListOfCompletedTransactions(MainActivity.this);
        //adapter = new CompletedTransactionAdapter(MainActivity.this, list);
        listView.setAdapter(null);
        TextView msg = (TextView) findViewById(R.id.msg_add);
        msg.setText("THIS FEATURE IS COMING SOON");
        msg.setVisibility(View.VISIBLE);

    }


    private void updateStatusBar() {
//        float cashflow = totalBalanceMoneyIn - totalBalanceMoneyOut;
//
//        String formatedAmount = formatter.format(cashflow);
//        setTitle("Cash Flow : " + formatedAmount);
//
//        TextView moneyinValue = (TextView) findViewById(R.id.inflowValue);
//
//        formatedAmount = formatter.format(totalBalanceMoneyIn);
//        moneyinValue.setText(formatedAmount);
//
//
//        TextView moneyoutValue = (TextView) findViewById(R.id.outflowValue);
//        formatedAmount = formatter.format(totalBalanceMoneyOut);
//        moneyoutValue.setText(formatedAmount);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            //drawer is open
            drawerLayout.closeDrawers();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_CREATE_TRANSACTION)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                if(currentList == Constants.TRANSACTION_TYPE_MONEY_IN)
                {
                    populateInTransaction();
                }
            }
        }

    }
}
