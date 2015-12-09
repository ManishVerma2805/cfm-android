package com.getfiscal.mobile;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.getfiscal.mobile.utils.LruBitmapCache;

/**
 * Created by mverma1 on 10/16/15.
 */
public class GetFiscalApplication extends Application {

    public static final String TAG = "GetFiscalApplication";
    //public static final String BASE_URL="http://ec2-54-72-9-34.eu-west-1.compute.amazonaws.com";
    //public static final String BASE_URL="http://ec2-52-31-132-254.eu-west-1.compute.amazonaws.com";
    //public static final String BASE_URL="http://ec2-52-19-95-154.eu-west-1.compute.amazonaws.com";

    public static final String BASE_URL="http://ec2-52-31-132-254.eu-west-1.compute.amazonaws.com";


    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private static GetFiscalApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized GetFiscalApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    String mToken;

    public void setAuthToken(String mToken) {
        this.mToken = mToken;
    }

    public String getToken() {
        return mToken;
    }


    String companyId;
    String companyName;
    String companyPhone;
    String companyEmail;


    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }
}
