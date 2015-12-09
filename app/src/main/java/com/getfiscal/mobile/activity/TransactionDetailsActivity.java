package com.getfiscal.mobile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.getfiscal.mobile.utils.DataUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mverma1 on 10/17/15.
 */
public class TransactionDetailsActivity extends AppCompatActivity {

    Uri contactData = null;
    Bundle partyDetails = null;
    private static final int PICK_CONTACT = 1000;
    private static final int PICK_PARTY = 1001;
    private static final int ADD_PARTY = 1002;

    int mode = Constants.MODE_CREATE;
    int type = Constants.TRANSACTION_TYPE_MONEY_IN;
    Bundle viewData;
    int status = Constants.TRANSACTION_STATUS_PENDING;
    Date dueDate=null;
    String strDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);

        Bundle data = getIntent().getExtras();
        if (data == null) {
            mode = Constants.MODE_CREATE;
            type = Constants.TRANSACTION_TYPE_MONEY_IN;
        } else {
            mode = data.getInt(Constants.MODE);
            type = data.getInt(Constants.TRANSACTION_TYPE);
            viewData = data.getBundle(Constants.DATA);
            if (viewData != null) {
                status = viewData.getInt(Constants.TRANSACTION_STATUS);
                type = viewData.getInt(Constants.TRANSACTION_TYPE);
            }
        }

        if (status == Constants.TRANSACTION_STATUS_PENDING) {
            if (Constants.TRANSACTION_TYPE_MONEY_IN == type) {
                if(mode == Constants.MODE_CREATE)
                {
                    setTitle(R.string.title_activity_add_transaction_in);
                }
                else{
                    setTitle(R.string.title_activity_transaction_in);
                }
                Button markCompleted = (Button) findViewById(R.id.markCompleted);
                markCompleted.setText(R.string.markReceivePaymentCompleted);
            } else if (Constants.TRANSACTION_TYPE_MONEY_OUT == type) {

                if(mode == Constants.MODE_CREATE)
                {
                    setTitle(R.string.title_activity_add_transaction_out);
                }
                else{
                    setTitle(R.string.title_activity_transaction_out);
                }

                Button markCompleted = (Button) findViewById(R.id.markCompleted);
                markCompleted.setText(R.string.markPaidPaymentCompleted);
            }
        } else {
            mode = Constants.MODE_ONLY_VIEW;

            if (Constants.TRANSACTION_TYPE_MONEY_IN == type) {
                setTitle(R.string.title_activity_transaction_in_complete);
            } else if (Constants.TRANSACTION_TYPE_MONEY_OUT == type) {
                setTitle(R.string.title_activity_transaction_out_complete);
            }
        }

        initToolbar();
    }


    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setTitleTextAppearance(this, R.style.ActivityTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_create) {
            if(type == Constants.TRANSACTION_TYPE_MONEY_IN)
            {
                createReciveableTransaction();
            }
        } else if (id == R.id.action_edit) {
            mode = Constants.MODE_EDIT;
            makeScreenEditable();
            updateMenuOption();
        } else if (id == R.id.action_update) {
            //updateTransaction();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpView();
        setupDatePicker();
        setupCompletedButton();
        setupSendSMSButton();
        setupCallPartyButton();
        setupNotCompletedButton();
    }


    private void setupCompletedButton() {
        Button completed = (Button) findViewById(R.id.markCompleted);
        completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(Constants.UPDATED_TRANSACTION, true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        completed.setVisibility(View.GONE);
    }

    private void setupNotCompletedButton() {
        Button completed = (Button) findViewById(R.id.markNotCompleted);
        completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(Constants.UPDATED_TRANSACTION, true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        completed.setVisibility(View.GONE);
    }

    private void showCompletedButton(boolean visible) {
        Button completed = (Button) findViewById(R.id.markCompleted);
        if (visible)
            completed.setVisibility(View.VISIBLE);
        else
            completed.setVisibility(View.GONE);
    }

    private void showPendingButton(boolean visible) {
        Button pending = (Button) findViewById(R.id.markNotCompleted);
        if (visible)
            pending.setVisibility(View.VISIBLE);
        else
            pending.setVisibility(View.GONE);
    }

    private void setupDatePicker() {
        final TextView dateText = (TextView) findViewById(R.id.transactionDueDateText);
        //final Calendar dueDate = Calendar.getInstance();

        dateText.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

//                String strDueDate = null;
//                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
//                if (dateText.getText() != null) {
//                    strDueDate = dateText.getText().toString();
//                }
//                if (TextUtils.isEmpty(strDueDate)) {
//                    dueDate.add(Calendar.DATE, 30);
//                } else {
//                    try {
//                        dueDate.setTime(sdf.parse(strDueDate));
//                    } catch (ParseException e) {
//                        dueDate.add(Calendar.DATE, 30);
//                    }
//                }
                Calendar cal= Calendar.getInstance();
                if(dueDate!=null)
                {
                    cal.setTime(dueDate);
                }
                DatePickerDialog dialog = new DatePickerDialog(
                        TransactionDetailsActivity.this,
                        0,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                                //String date =  year + "-" + getMonthName(month) + "-" + (day < 10 ? "0" : "") + day ;
                                String date = day + TransactionAdapter.suffixes[day] +" " + getMonthName(month) + " " +year;
                                dateText.setText(date);
                                Calendar cal= Calendar.getInstance();
                                cal.set(year,month,day);
                                dueDate=cal.getTime();

                                month++;
                                strDueDate = year  + "-" +(month < 10 ? "0" : "") + month + "-" + (day < 10 ? "0" : "") + day;
                            }
                        },
                        cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)
                );
                dialog.show();
//                if(dueDate!=null)
//                {
//                    Calendar cal= Calendar.getInstance();
//                    cal.setTime(dueDate);
//                    dialog.updateDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
//                }

            }
        });
    }


    public static String getMonthName(int i) {
        switch (i) {
            case 0:
                return "Jan";
            case 1:
                return "Feb";
            case 2:
                return "Mar";
            case 3:
                return "Apr";
            case 4:
                return "May";
            case 5:
                return "Jun";
            case 6:
                return "Jul";
            case 7:
                return "Aug";
            case 8:
                return "Sep";
            case 9:
                return "Oct";
            case 10:
                return "Nov";
            case 11:
                return "Dec";
        }
        return "Jan";
    }

    private Menu menu;

    private void hideOption(int id) {
        if (menu != null) {
            MenuItem item = menu.findItem(id);
            item.setVisible(false);
        }
    }

    private void showOption(int id) {
        if (menu != null) {
            MenuItem item = menu.findItem(id);
            item.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_transaction, menu);

        updateMenuOption();
        return true;
    }

    private void updateMenuOption() {
        switch (mode) {
            case Constants.MODE_CREATE: {
                showOption(R.id.action_create);
                hideOption(R.id.action_edit);
                hideOption(R.id.action_update);
                break;
            }
            case Constants.MODE_EDIT: {
                showOption(R.id.action_update);
                hideOption(R.id.action_create);
                hideOption(R.id.action_edit);
                break;
            }
            case Constants.MODE_ONLY_VIEW: {
                hideOption(R.id.action_update);
                hideOption(R.id.action_create);
                hideOption(R.id.action_edit);
                break;
            }
        }
    }

    private void setUpView() {
        setUpKeyboardType();
        switch (mode) {
            case Constants.MODE_CREATE: {
                makeScreenEditable();
                findViewById(R.id.contactPartyActionLayout).setVisibility(View.GONE);
                break;
            }
            case Constants.MODE_ONLY_VIEW: {
                makeScreenReadOnly();
                findViewById(R.id.contactPartyActionLayout).setVisibility(View.GONE);
                showCompletedButton(false);
        //        findViewById(R.id.contactPartyActionLayout).setVisibility(View.GONE);
        //        findViewById(R.id.transactionDueDateHolder).setVisibility(View.GONE);
                showPendingButton(true);
                break;
            }
            case Constants.MODE_EDIT: {
                makeScreenEditable();
                setDataToViews();
                findViewById(R.id.contactPartyActionLayout).setVisibility(View.VISIBLE);
                break;
            }
        }
    }


    private void setDataToViews() {
        DataUtils.setDoubleFieldsFromBundle(R.id.transactionAmountText, Constants.TRANSACTION_BALANCE_AMOUNT, viewData, 0, this);

        DataUtils.setDateFieldsFromBundle(R.id.transactionDueDateText, Constants.TRANSACTION_DUE_DATE, viewData, this);

        dueDate =(Date)viewData.getSerializable(Constants.TRANSACTION_DUE_DATE);

        DataUtils.setFieldsFromBundle(R.id.transactionPartyText, Constants.CUSTOMER_NAME, viewData, "", this);
        DataUtils.setFieldsFromBundle(R.id.transactionRefNoText, Constants.TRANSACTION_REF_NO, viewData, "", this);
        DataUtils.setFieldsFromBundle(R.id.transactionPartyPhoneText, Constants.CUSTOMER_PHONE, viewData, "", this);
        EditText partyEditBox=(EditText)findViewById(R.id.transactionPartyText);
        if(Constants.TRANSACTION_TYPE_MONEY_IN == type)
        {
            findViewById(R.id.contactPartyActionLayout).setVisibility(View.VISIBLE);
            partyEditBox.setHint(getString(R.string.transactionParty));

        }
        else
        {
            findViewById(R.id.contactPartyActionLayout).setVisibility(View.GONE);
            partyEditBox.setHint(getString(R.string.transactionPartyVendor));
        }

    }

    private void makeScreenReadOnly() {
        enableView(R.id.transactionAmountText, false);
        DataUtils.setDoubleFieldsFromBundle(R.id.transactionAmountText, Constants.TRANSACTION_BALANCE_AMOUNT, viewData, 0, this);


        enableView(R.id.transactionDueDateText, false);
        DataUtils.setFieldsFromBundle(R.id.transactionDueDateText, Constants.TRANSACTION_DUE_DATE, viewData, "", this);


        enableView(R.id.transactionPartyText, false);
        DataUtils.setFieldsFromBundle(R.id.transactionPartyText, Constants.CUSTOMER_NAME, viewData, "", this);

        enableView( R.id.transactionRefNoText, false);
        DataUtils.setFieldsFromBundle(R.id.transactionRefNoText, Constants.TRANSACTION_REF_NO, viewData, "", this);

        enableView(R.id.transactionPartyPhoneText, false);
        DataUtils.setFieldsFromBundle(R.id.transactionPartyPhoneText, Constants.CUSTOMER_PHONE, viewData, "", this);

        EditText partyEditBox=(EditText)findViewById(R.id.transactionPartyText);
        if(Constants.TRANSACTION_TYPE_MONEY_IN == type)
        {
            findViewById(R.id.contactPartyActionLayout).setVisibility(View.VISIBLE);
            partyEditBox.setHint(getString(R.string.transactionParty));

        }
        else
        {
            findViewById(R.id.contactPartyActionLayout).setVisibility(View.GONE);
            partyEditBox.setHint(getString(R.string.transactionPartyVendor));
        }

    }

    private void enableView(int editTextId, boolean enable) {
        //View til = findViewById(holderId);
        View editText = findViewById(editTextId);
        //til.setEnabled(enable);
        editText.setEnabled(enable);
        if (editText instanceof EditText) {
            ((EditText) editText).setTextColor(Color.BLACK);
        }
    }

    private void makeScreenEditable() {
        enableView( R.id.transactionAmountText, true);
        enableView( R.id.transactionDueDateText, true);
        enableView(R.id.transactionPartyText, true);
        enableView( R.id.transactionRefNoText, true);
        enableView(R.id.transactionPartyPhoneText, true);


        //findViewById(R.id.markCompleted).setVisibility(View.GONE);

        EditText partyEditBox=(EditText)findViewById(R.id.transactionPartyText);
        if(Constants.TRANSACTION_TYPE_MONEY_IN == type)
        {
            partyEditBox.setHint(getString(R.string.transactionParty));

        }
        else
        {
            partyEditBox.setHint(getString(R.string.transactionPartyVendor));
        }
    }

    private void setUpKeyboardType() {
        DataUtils.setInputFilterForCurrency(this, R.id.transactionAmountText, true);
    }



    private void setupSendSMSButton() {
        View sendSMS = findViewById(R.id.sendSMS);
        sendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contactNumber = viewData.getString(Constants.CUSTOMER_PHONE);
                if (TextUtils.isEmpty(contactNumber) == true) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            TransactionDetailsActivity.this);
                    alertDialog.setTitle("Send SMS");
                    alertDialog.setMessage("Please add phone number to this party or contact");
                    alertDialog.show();
                } else {
                    String body = "Dear " + viewData.getString(Constants.CUSTOMER_NAME)
                            + " payment of " + viewData.getDouble(Constants.TRANSACTION_AMOUNT)
                            + " for an invoice " + viewData.getString(Constants.TRANSACTION_REF_NO)
                            + " is due on " + viewData.getString(Constants.TRANSACTION_DUE_DATE)
                            + ". Please pay the same at earliest. Thank You. - " + viewData.getString(Constants.COMPANY_NAME);

                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", contactNumber);
                    smsIntent.putExtra("sms_body", body);

                    try {
                        startActivity(smsIntent);
                    } catch (ActivityNotFoundException e) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                TransactionDetailsActivity.this);
                        alertDialog.setTitle("Send SMS");
                        alertDialog.setMessage("Cannot send SMS from your phone, no application to send SMS");
                        alertDialog.setPositiveButton("Ok", null);
                        alertDialog.show();
                    }

                }
            }
        });

    }

    private void setupCallPartyButton() {
        View makeCall = findViewById(R.id.makeCall);
        makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contactNumber = viewData.getString(Constants.CUSTOMER_PHONE);
                if (TextUtils.isEmpty(contactNumber) == true) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            TransactionDetailsActivity.this);
                    alertDialog.setTitle("Call");
                    alertDialog.setMessage("Please add phone number to this party or contact");
                    alertDialog.show();
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + contactNumber));

                    try {
                        startActivity(callIntent);
                    }
                    catch (ActivityNotFoundException e)
                    {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                                TransactionDetailsActivity.this);
                        alertDialog.setTitle("Call");
                        alertDialog.setMessage("Cannot make Call from your phone, no application to call.");
                        alertDialog.setPositiveButton("Ok", null);
                        alertDialog.show();
                    }
                }
            }
        });

    }


    private void createReciveableTransaction()
    {
        //{"__v":0,"totalAmt":1000,"refNumber":"inv20001","dueDate":"2016-01-02T18:30:00.000Z","company":"56684b4a78bbe9cc417c452b","_id":"5668578f78bbe9cc417c4542","lineItems":[],"customer":{"name":"Baskar"}}
        if(DataUtils.isConnected(this)==false)
        {
            showNoNetworkDialog();
            return;
        }
        EditText transactionAmountText =(EditText)findViewById(R.id.transactionAmountText);
        String amount=transactionAmountText.getText().toString();
        amount=amount.trim();
        if(TextUtils.isEmpty(amount))
        {
            transactionAmountText.setError("Please enter the amount");
            return;
        }

        final String TAG = "json_obj_req";

        String url = GetFiscalApplication.BASE_URL + "/api/companies/" + GetFiscalApplication.getInstance().getCompanyId() + "/invoices";


        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Creating Invoice");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        pDialog.dismiss();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());

                    }
                }) {

            @Override
            public byte[] getBody() {

                EditText transactionPartyText =(EditText)findViewById(R.id.transactionPartyText);
                EditText transactionAmountText =(EditText)findViewById(R.id.transactionAmountText);
                EditText transactionRefNoText =(EditText)findViewById(R.id.transactionRefNoText);
                //TextView transactionDueDateText =(TextView)findViewById(R.id.transactionDueDateText);
                EditText transactionPartyPhoneText =(EditText)findViewById(R.id.transactionPartyPhoneText);


                try {
                    JSONObject object = new JSONObject();
                    object.put("company",GetFiscalApplication.getInstance().getCompanyId());
                    if(TextUtils.isEmpty(strDueDate)!=false)
                    {
                        object.put("dueDate",strDueDate);
                    }
                    object.put("refNumber",transactionRefNoText.getText());
                    object.put("totalAmt",transactionAmountText.getText());


                    JSONObject customer = new JSONObject();
                    customer.put("name",transactionPartyText.getText());
                    if(transactionPartyPhoneText.getText()!=null) {
                        String phone = transactionPartyPhoneText.getText().toString();
                        if (TextUtils.isEmpty(phone)!=false)
                        {
                            customer.put("phone", phone);
                        }
                    }
                    object.put("customer",customer);

                    return  object.toString().getBytes();
                }
                catch (JSONException e)
                {

                }


                return "{}".getBytes();
                //return loginJson.toString().getBytes();


            }

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
        GetFiscalApplication.getInstance().addToRequestQueue(jsonObjReq, TAG);
    }

    private void showNoNetworkDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Oops! No Network, Please check your network connection.");

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}