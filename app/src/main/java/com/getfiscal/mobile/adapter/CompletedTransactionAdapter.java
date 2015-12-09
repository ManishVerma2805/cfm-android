package com.getfiscal.mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.getfiscal.mobile.R;
import com.getfiscal.mobile.model.Constants;
import com.getfiscal.mobile.model.Transaction;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by mverma1 on 6/21/15.
 */
public class CompletedTransactionAdapter extends BaseAdapter {

    ArrayList<Transaction> mDataList;
    Context mContext;
    private LayoutInflater mInflater;

    public CompletedTransactionAdapter(Context context, ArrayList<Transaction> list)
    {
        mDataList=list;
        mContext=context;
        mInflater = LayoutInflater.from(context);
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
            holder.amount = (TextView)view.findViewById(R.id.amount);
            holder.duedate = (TextView)view.findViewById(R.id.duedate);
            view.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)view.getTag();
        }
        Transaction item= (Transaction)getItem(i);
        holder.partyName.setText(item.getPartyName());

        DecimalFormat formatter = new DecimalFormat("##,##,##,###.##");
        String formatedAmount = formatter.format(item.getAmount());


        int colorId = -1;
        if(item.getType()== Constants.TRANSACTION_TYPE_MONEY_IN) {
            colorId = R.color.band_within_month;
            holder.amount.setText("+"+formatedAmount);
        }
        else if(item.getType()==Constants.TRANSACTION_TYPE_MONEY_OUT){
            colorId = R.color.band_within_week;
            holder.amount.setText("-"+formatedAmount);
        }
        else
        {
            colorId = R.color.band_within_month;
            holder.amount.setText(formatedAmount);
        }
        if (colorId != -1) {
            holder.band.setBackgroundColor(mContext.getResources().getColor(colorId));
            holder.amount.setTextColor(mContext.getResources().getColor(colorId));

            //holder.phone.setText(item.getStrDueDate());
        }
        return view;
    }


    private class ViewHolder {
        public View band;
        public TextView partyName;
        public TextView amount;
        public TextView duedate;
    }
}
