package com.getfiscal.mobile.utils.textwatcher;

import android.text.InputType;
import android.text.method.DigitsKeyListener;

public class NumberKeyListener extends DigitsKeyListener {

	public NumberKeyListener(boolean decimal) {
		super(false, decimal);
	}

	public NumberKeyListener(boolean sign, boolean decimal) {
		super(sign, decimal);
	}

	public int getInputType() {
		return InputType.TYPE_CLASS_NUMBER;
	}

}
