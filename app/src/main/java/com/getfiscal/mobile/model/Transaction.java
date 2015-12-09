package com.getfiscal.mobile.model;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by mverma1 on 6/21/15.
 */
public class Transaction implements Comparator<Transaction> {


    private String _id;
    private long mId;
    private String mServerId;
    private String mPartyName;
    private String mPartyPhoneNumber;
    private double mAmount;
    private double mBalanceAmount;

    private int mPartyId;
    private int mPartySource;

    private int mType;
    private int mStatus = Constants.TRANSACTION_STATUS_PENDING;


    private String mRefNo;

    private Date mDueDate;
    private String mStrDueDate;

    private int weight;
    private int band;


    public void setStrId(String _id) {
        this._id = _id;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }


    public String getServerId() {
        return mServerId;
    }

    public void setServerId(String mServerId) {
        this.mServerId = mServerId;
    }

    public String getPartyName() {
        return mPartyName;
    }

    public void setPartyName(String mPartyName) {
        this.mPartyName = mPartyName;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setPartyPhoneNumber(String mPartyPhoneNumber) {
        this.mPartyPhoneNumber = mPartyPhoneNumber;
    }

    public String getPartyPhoneNumber() {
        return mPartyPhoneNumber;
    }

    public void setAmount(double mAmount) {
        this.mAmount = mAmount;
    }

    public double getBalanceAmount() {
        return mBalanceAmount;
    }

    public void setBalanceAmount(double mBalanceAmount) {
        this.mBalanceAmount = mBalanceAmount;
    }

    public int getPartyId() {
        return mPartyId;
    }

    public void setPartyId(int mPartyId) {
        this.mPartyId = mPartyId;
    }

    public int getPartySource() {
        return mPartySource;
    }

    public void setPartySource(int mPartySource) {
        this.mPartySource = mPartySource;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int mStatus) {
        this.mStatus = mStatus;
    }

    public Date getDueDate() {
        return mDueDate;
    }

    public void setDueDate(Date mDueDate) {
        this.mDueDate = mDueDate;
    }

    public String getStrDueDate() {
        return mStrDueDate;
    }

    public void setStrDueDate(String mStrDueDate) {
        this.mStrDueDate = mStrDueDate;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getBand() {
        return band;
    }

    public void setBand(int band) {
        this.band = band;
    }


    public String getRefNo() {
        return mRefNo;
    }

    public void setRefNo(String mRefNo) {
        this.mRefNo = mRefNo;
    }


    @Override
    public int compare(Transaction o1, Transaction o2) {
        if (o1.weight > o2.weight) {
            return 1;
        } else if (o1.weight < o2.weight) {
            return -1;
        }
        return 0;
    }

    public Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.putLong(Constants._ID, mId);
        bundle.putDouble(Constants.TRANSACTION_AMOUNT, mAmount);
        bundle.putDouble(Constants.TRANSACTION_BALANCE_AMOUNT, mBalanceAmount);
        bundle.putString(Constants.TRANSACTION_REF_NO, mRefNo);

        bundle.putSerializable(Constants.TRANSACTION_DUE_DATE, mDueDate);
        bundle.putString(Constants.CUSTOMER_NAME, mPartyName);
        bundle.putString(Constants.CUSTOMER_PHONE, mPartyPhoneNumber);
        bundle.putInt(Constants.TRANSACTION_TYPE, mType);
        bundle.putInt(Constants.TRANSACTION_STATUS, mStatus);
        return bundle;
    }

    public void parseFromJsonObject(JSONObject object) {
        if (object != null) {
            try {

                if (object.has("_id")) {
                    String id = object.getString("_id");
                    setStrId(id);
                    setId(0);
                }

                if (object.has("totalAmt")) {
                    setAmount(object.getDouble("totalAmt"));
                }

                if (object.has("refNumber")) {
                    setRefNo(object.getString("refNumber"));
                }

                if (object.has("balance")) {
                    setBalanceAmount(object.getDouble("balance"));
                }

                if (object.has("dueDate")) {
                    setStrDueDate(object.getString("dueDate"));
                }

                if (!TextUtils.isEmpty(getStrDueDate())) {
                    //transaction.mDate =new Date(transaction.mStrDate);
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
                    Calendar today = Calendar.getInstance();

                    Calendar transactionDueDate = Calendar.getInstance();
                    try {
                        transactionDueDate.setTime(sdf.parse(getStrDueDate()));
                        setDueDate(transactionDueDate.getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long diff = transactionDueDate.getTimeInMillis() - today.getTimeInMillis();
                    int weight = (int) (diff / 86400000);
                    int band = Constants.BAND_LATER;
                    if (weight < 0) {
                        band = Constants.BAND_OVERDUE;
                    } else if (weight < 7) {
                        band = Constants.BAND_WITHIN_WEEK;
                    } else if (weight < 30) {
                        band = Constants.BAND_WITHIN_MONTH;
                    }
                    setWeight(weight);
                    setBand(band);
                } else {
                    setWeight(Integer.MAX_VALUE);
                    setBand(Constants.BAND_UNKNOWN);
                }

                JSONObject customer = object.getJSONObject("customer");

                if (customer != null) {
                    if (customer.has("name")) {
                        setPartyName(customer.getString("name"));
                    }
                    if (customer.has("phone")) {
                        setPartyPhoneNumber(customer.getString("phone"));
                    }
                }

            } catch (JSONException e) {
                Log.e("Transaction", e.toString());

            }
        }
    }


    public void parseAsPayableFromJsonObject(JSONObject object) {
        if (object != null) {
            try {
                if (object.has("_id")) {
                    String id = object.getString("_id");
                    setStrId(id);
                    setId(0);
                }


                if (object.has("totalAmt")) {
                    setAmount(object.getDouble("totalAmt"));
                }

                if (object.has("refNumber")) {
                    setRefNo(object.getString("refNumber"));
                }

                if (object.has("balance")) {
                    setBalanceAmount(object.getInt("balance"));
                }

                if (object.has("dueDate")) {
                    setStrDueDate(object.getString("dueDate"));
                }

                if (!TextUtils.isEmpty(getStrDueDate())) {
                    //transaction.mDate =new Date(transaction.mStrDate);
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
                    Calendar today = Calendar.getInstance();

                    Calendar transactionDueDate = Calendar.getInstance();
                    try {
                        transactionDueDate.setTime(sdf.parse(getStrDueDate()));
                        setDueDate(transactionDueDate.getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long diff = transactionDueDate.getTimeInMillis() - today.getTimeInMillis();
                    int weight = (int) (diff / 86400000);
                    int band = Constants.BAND_LATER;
                    if (weight < 0) {
                        band = Constants.BAND_OVERDUE;
                    } else if (weight < 7) {
                        band = Constants.BAND_WITHIN_WEEK;
                    } else if (weight < 30) {
                        band = Constants.BAND_WITHIN_MONTH;
                    }
                    setWeight(weight);
                    setBand(band);
                } else {
                    setWeight(Integer.MAX_VALUE);
                    setBand(Constants.BAND_UNKNOWN);
                }

                if (object.has("refNumber")) {
                    JSONObject vendor = object.getJSONObject("vendor");
                    if (vendor != null) {
                        if (vendor.has("name")) {
                            setPartyName(vendor.getString("name"));
                        }
                        if (vendor.has("phone")) {
                            setPartyPhoneNumber(vendor.getString("phone"));
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("Transaction", e.toString());
            }
        }
    }
}
