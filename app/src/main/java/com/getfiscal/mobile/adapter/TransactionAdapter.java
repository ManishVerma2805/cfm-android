package com.getfiscal.mobile.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.getfiscal.mobile.GetFiscalApplication;
import com.getfiscal.mobile.R;
import com.getfiscal.mobile.model.Constants;
import com.getfiscal.mobile.model.Transaction;
import com.getfiscal.mobile.utils.DataUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mverma1 on 10/17/15.
 */
public class TransactionAdapter extends BaseAdapter
{
    DecimalFormat formatter = new DecimalFormat("##,##,##,##,##,##,##0.00");
    ArrayList<Transaction> mDataList;
    Context mContext;
    private LayoutInflater mInflater;
    View.OnClickListener mListner;

    AlertDialog applyAlertDialog;

    String invoiceRef;
    public TransactionAdapter(final Context context,ArrayList<Transaction> list)
    {
        mDataList=list;
        mContext=context;
        mInflater = LayoutInflater.from(context);
        mListner=new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Transaction transaction= (Transaction)v.getTag();

                final String type =(transaction.getType()==Constants.TRANSACTION_TYPE_MONEY_OUT)?"invoiceRef":"billRef";
                invoiceRef=transaction.getRefNo();
                String msg="Please confirm your request to get an advance on invoice  #" + transaction.getRefNo() +" for the amount \u20B9" +transaction.getBalanceAmount() + " by submitting your contact details.";

                if(transaction.getType()==Constants.TRANSACTION_TYPE_MONEY_OUT)
                {
                    msg="Please confirm your request to get advance on bill #" + transaction.getRefNo() +" for the amount \u20B9" +transaction.getBalanceAmount() + " by selecting apply.";
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                View view=mInflater.inflate(R.layout.dialog_custom_layout_apply, null);
                alertDialogBuilder.setView(view);
                TextView msgView=(TextView)view.findViewById(R.id.message);
                msgView.setText(msg);

                alertDialogBuilder.setPositiveButton("Submit", null);

                alertDialogBuilder.setNegativeButton("Cancel", null);

                applyAlertDialog = alertDialogBuilder.create();

                applyAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button b = applyAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                EditText emailView = (EditText) applyAlertDialog.findViewById(R.id.email);
                                EditText phoneView = (EditText) applyAlertDialog.findViewById(R.id.phone);

                                String email = null;
                                String phone = null;
                                if (emailView != null && emailView.getText() != null) {
                                    email = emailView.getText().toString();
                                }
                                if (phoneView != null && phoneView.getText() != null) {
                                    phone = phoneView.getText().toString();
                                }

                                View error = applyAlertDialog.findViewById(R.id.error);
                                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
                                    if (error != null) {
                                        error.setVisibility(View.VISIBLE);
                                    }
                                } else {

                                    if (error != null) {
                                        error.setVisibility(View.GONE);
                                    }

                                    //Dismiss once everything is OK.
                                    applyAlertDialog.dismiss();

                                    submitLoanRequest(type,email,phone);


                                }
                            }
                        });
                    }
                });
                applyAlertDialog.show();
                EditText phoneView=(EditText)applyAlertDialog.findViewById(R.id.phone);
                if(phoneView!=null)
                {
                    phoneView.setText(GetFiscalApplication.getInstance().getCompanyPhone());
                }

                EditText emailView=(EditText)applyAlertDialog.findViewById(R.id.email);
                if(emailView!=null)
                {
                    emailView.setText(GetFiscalApplication.getInstance().getCompanyEmail());
                }
                applyAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.app_blue));
                applyAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.app_blue));

            }
        };
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }


    @Override
    public Object getItem(int i) {
        Transaction object=mDataList.get(i);
        return object;
    }

    @Override
    public long getItemId(int i) {
        Transaction object=mDataList.get(i);
        return object.getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        ViewHolder holder;
        if(view==null)
        {
            view = mInflater.inflate(R.layout.layout_transaction_item, parent, false);
            holder = new ViewHolder();
            holder.band = view.findViewById(R.id.band);
            holder.partyName = (TextView)view.findViewById(R.id.partyNameText);
            holder.invoiceNumber=(TextView)view.findViewById(R.id.invoicenumber);
            holder.amount = (TextView)view.findViewById(R.id.amount);
            holder.decimal=(TextView)view.findViewById(R.id.decimal);
            holder.duedate = (TextView)view.findViewById(R.id.duedate);
            holder.getFiscal = view.findViewById(R.id.getfiscal);
            holder.getFiscal.setOnClickListener(mListner);

            view.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)view.getTag();
        }
        holder.getFiscal.setTag(getItem(i));
        Transaction item= (Transaction)getItem(i);
        holder.partyName.setText(item.getPartyName());

        if(TextUtils.isEmpty(item.getRefNo()))
        {
            holder.invoiceNumber.setText("<empty>");
        }
        else
        {
            holder.invoiceNumber.setText(item.getRefNo());
        }


        String formatedAmount = formatter.format(item.getAmount());
        int index=formatedAmount.indexOf(".");
        if(index==-1) {
            holder.amount.setText(formatedAmount);
            holder.decimal.setText(".00");
        }
        else
        {
            String amt=formatedAmount.substring(0, index);
            holder.amount.setText(amt);

            String decimal=formatedAmount.substring(index);
            holder.decimal.setText(decimal);
        }

        //if(item.getType()== Constants.TRANSACTION_TYPE_MONEY_IN)
        {
            int colorId = -1;
            switch (item.getBand()) {
                case Constants.BAND_OVERDUE:
                    colorId = R.color.band_overdue;
                    break;
                case Constants.BAND_WITHIN_WEEK:
                    colorId = R.color.band_within_week;
                    break;
                case Constants.BAND_WITHIN_MONTH:
                    colorId = R.color.band_within_month;
                    break;
                case Constants.BAND_LATER:
                    colorId = R.color.band_later;
                    break;
            }
            if (colorId != -1) {
                holder.band.setBackgroundColor(mContext.getResources().getColor(colorId));
                holder.duedate.setText(getFormatedDate(item.getDueDate()));
            }
            else
            {
                holder.band.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
                holder.duedate.setText("< Unknow >");
            }
        }
        return view;
    }


    private class ViewHolder {
        public View band;
        public TextView partyName;
        public TextView invoiceNumber;
        public TextView amount;
        public TextView decimal;
        public TextView duedate;
        public View getFiscal;

    }


    public static final String[] suffixes =
            //    0     1     2     3     4     5     6     7     8     9
            { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    //    10    11    12    13    14    15    16    17    18    19
                    "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                    //    20    21    22    23    24    25    26    27    28    29
                    "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    //    30    31
                    "th", "st" };

    public static String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public static String getFormatedDate(Date date)
    {
        Calendar transactionDueDate = Calendar.getInstance();
        transactionDueDate.setTime(date);

        int day = transactionDueDate.get(Calendar.DAY_OF_MONTH);
        String dayStr = day + suffixes[day];

        int month = transactionDueDate.get(Calendar.MONTH);
        String monthStr = monthNames[month];

        int year = transactionDueDate.get(Calendar.YEAR);

        return dayStr + " " + monthStr +" " + year;

    }

    private void submitLoanRequest(final String type,final String email, final String phone) {

        if(DataUtils.isConnected(mContext)==false)
        {
            showNoNetworkDialog();
            return;
        }

        final String TAG = "json_obj_req";

        String url = GetFiscalApplication.BASE_URL + "/api/companies/" + GetFiscalApplication.getInstance().getCompanyId() + "/loanApps";


        final ProgressDialog pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Submiting loan request...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());

//                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
//                        alertDialogBuilder.setView(mInflater.inflate(R.layout.dialog_custom_layout_confirm, null));
//                        AlertDialog alertDialog = alertDialogBuilder.create();
//                        alertDialog.setCanceledOnTouchOutside(true);
//                        alertDialog.setCancelable(true);
//                        alertDialog.show();

                        pDialog.dismiss();

                        Toast toast = new Toast(mContext.getApplicationContext());
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(mInflater.inflate(R.layout.dialog_custom_layout_confirm, null));
                        toast.show();

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

                try {
                    JSONObject object = new JSONObject();
                    object.put("company",GetFiscalApplication.getInstance().getCompanyId());
                    object.put("email",email);
                    object.put(type,invoiceRef);
                    object.put("phone",phone);
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
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