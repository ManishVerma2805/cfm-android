package com.getfiscal.mobile.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.getfiscal.mobile.adapter.TransactionAdapter;
import com.getfiscal.mobile.model.Constants;
import com.getfiscal.mobile.utils.textwatcher.CurrencyTextWatcher;
import com.getfiscal.mobile.utils.textwatcher.NumberKeyListener;

import java.util.Date;

/**
 * Created by mverma1 on 10/17/15.
 */
public class DataUtils {

    public static void getIntDataForBundle(int id, String key, Bundle bundle, Activity activity) {
        EditText editText = (EditText) activity.findViewById(id);
        int value = 0;
        if (TextUtils.isEmpty(editText.getText()) == false) {
            value = Integer.parseInt(editText.getText().toString());
        }
        bundle.putInt(key, value);
    }

    public static void getFloatDataForBundle(int id, String key, Bundle bundle, Activity activity) {
        EditText editText = (EditText) activity.findViewById(id);
        float value = 0.0f;
        if (TextUtils.isEmpty(editText.getText())==false) {
            value = Float.parseFloat(editText.getText().toString());
        }
        bundle.putFloat(key, value);
    }

    public static void getDataForBundle(int id, String key, Bundle bundle, Activity activity) {
        EditText editText = (EditText) activity.findViewById(id);
        String value = "";
        if (editText.getText() != null) {
            value = editText.getText().toString();
        }
        bundle.putString(key, value);
    }


    public static void setFieldsFromBundle(int id, String key, Bundle bundle, String defaultData, Activity activity) {
        EditText editText = (EditText) activity.findViewById(id);
        String value = bundle.getString(key);
        if (TextUtils.isEmpty(value)) {
            editText.setText(defaultData);
        } else {
            editText.setText(value);
        }
    }

    public static void setIntFieldsFromBundle(int id, String key, Bundle bundle, int defaultData, Activity activity) {
        EditText editText = (EditText) activity.findViewById(id);
        int value = defaultData;
        if (bundle.containsKey(key)) {
            value = bundle.getInt(key);
        }
        editText.setText(value + "");
    }

    public static void setFloatFieldsFromBundle(int id, String key, Bundle bundle, float defaultData, Activity activity) {
        EditText editText = (EditText) activity.findViewById(id);
        float value = defaultData;
        if (bundle.containsKey(key)) {
            value = bundle.getFloat(key);
        }
        editText.setText(value + "");

    }

    public static void setDoubleFieldsFromBundle(int id, String key, Bundle bundle, float defaultData, Activity activity) {
        EditText editText = (EditText) activity.findViewById(id);
        double value = defaultData;
        if (bundle.containsKey(key)) {
            value = bundle.getDouble(key);
        }
        editText.setText(value + "");

    }

    public static void setDateFieldsFromBundle(int id, String key, Bundle bundle,  Activity activity) {
        TextView editText = (TextView) activity.findViewById(id);
        Date value=null;
        if (bundle.containsKey(key)) {
            value = (Date)bundle.getSerializable(key);
        }
        if(value!=null)
        {
            editText.setText(TransactionAdapter.getFormatedDate(value));
        }
        else{
            editText.setText( "");
        }
    }

    public static void setInputFilterForCurrency(Activity activity, int id,
                                                 boolean decimal) {
        View v = activity.findViewById(id);
        if (v instanceof EditText) {
            EditText inputEditText = (EditText) v;
            if (inputEditText != null) {
                inputEditText.addTextChangedListener(new CurrencyTextWatcher(
                        inputEditText));
                inputEditText.setKeyListener(new NumberKeyListener(false,
                        decimal));
                int length = Constants.CURRENCY_MAX_INPUT_LENGTH;
                addLengthFilter(inputEditText, length);
            }
        }
    }

    private static void addLengthFilter(EditText inputEditText, int length) {
        if (length != -1) {
            addInputFilter(new InputFilter.LengthFilter(length), inputEditText);
        }
    }

    private static void addInputFilter(InputFilter filter, EditText view) {
        InputFilter viewFilters[] = view.getFilters();
        InputFilter newFilter[];
        int newFilterIndex = 0;
        if (viewFilters != null) {
            newFilter = new InputFilter[viewFilters.length + 1];
            System.arraycopy(viewFilters, 0, newFilter, 0, viewFilters.length);
            newFilterIndex = viewFilters.length;
        } else {
            newFilter = new InputFilter[1];
        }
        newFilter[newFilterIndex] = filter;
        view.setFilters(newFilter);
    }

    public static boolean isConnected(Context context) {
        if(context != null) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo[] networkInfos = cm.getAllNetworkInfo();

                for (NetworkInfo info : networkInfos) {

                    if (info.isConnected()) {
                        return info.isConnected();
                    }
                }

            }
        }

        return false;
    }
}
