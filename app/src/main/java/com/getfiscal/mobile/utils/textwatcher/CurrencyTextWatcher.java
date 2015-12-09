/*
 * Copyright 2010 Intuit, Inc. All rights reserved.
 */
package com.getfiscal.mobile.utils.textwatcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


public class CurrencyTextWatcher implements TextWatcher {

    private EditText currencyEditText;

    public CurrencyTextWatcher(EditText e) {

        currencyEditText = e;
    }

    @Override
    public void afterTextChanged(Editable editMe) {
        try {
            int indexOfDot = editMe.toString().indexOf('.');
            if (indexOfDot > -1) {
                if (editMe.subSequence(indexOfDot + 1, editMe.length())
                        .length() >= 3) {
                    // check for 2 digits after the decimal point
                    editMe.delete(indexOfDot + 3, editMe.length());
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }
}
