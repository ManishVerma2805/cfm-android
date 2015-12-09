package com.getfiscal.mobile.model;

/**
 * Created by mverma1 on 6/16/15.
 */
public class Constants {

    public static final String DATE_FORMAT="yyyy-MM-dd";
    public static final int NO_ACTIVE_USER=-1;
    public static final int NO_ACTIVE_COMPANY=-1;
    public static final String DATA="data";
    public static final int TRANSACTION_STATUS_PENDING=1;
    public static final int TRANSACTION_STATUS_COMPLETE=2;





    public static final String TRANSACTION_TYPE="TRANSACTION_TYPE";
    public static final int TRANSACTION_TYPE_MONEY_IN=1;
    public static final int TRANSACTION_TYPE_MONEY_OUT=2;
    public static final int COMPLETED_TRANSACTION=3;
    public static final int TRANSACTION_TYPE_OPENING_BALANCE=4;

    public static final int PARTY_SOURCE_CONTACTS=1;
    public static final int PARTY_SOURCE_FROM_APP=2;


    public static final int  BAND_OVERDUE=1;
    public static final int  BAND_WITHIN_WEEK=2;
    public static final int  BAND_WITHIN_MONTH=3;
    public static final int  BAND_LATER=4;
    public static final int  BAND_UNKNOWN=5;


    public static final String STATUS_MONEY_IN="STATUS_MONEY_IN";
    public static final String STATUS_MONEY_OUT="STATUS_MONEY_OUT";
    public static final String STATUS_CASH_IN_HAND="STATUS_CASH_IN_HAND";
    public static final String STATUS_CASH_FLOW="STATUS_CASH_FLOW";


    public static final String MODE="MODE";
    public static final int MODE_CREATE=1;
    public static final int MODE_EDIT=3;
    public static final int MODE_ONLY_VIEW=4;
    public static final int MODE_LIST=5;
    public static final int MODE_PICK_LIST=6;


    public static final String UPDATED_TRANSACTION="UPDATED_TRANSACTION";
    public static final String UPDATED_PARTY="UPDATED_PARTY";

    public static final int CURRENCY_MAX_INPUT_LENGTH = 10;

    public static final String _ID="_id";
    public static final String TRANSACTION_AMOUNT = "transaction_amt";
    public static final String TRANSACTION_BALANCE_AMOUNT = "transaction_balance_amt";
    public static final String TRANSACTION_STATUS = "transaction_status";
    public static final String TRANSACTION_DUE_DATE = "transaction_due_date";
    public static final String CUSTOMER_NAME= "transaction_customer_name";
    public static final String TRANSACTION_REF_NO = "transcation_ref_no";
    public static final String CUSTOMER_PHONE="transaction_customer_phone";
    public static final String COMPANY_NAME="company_name";
}
