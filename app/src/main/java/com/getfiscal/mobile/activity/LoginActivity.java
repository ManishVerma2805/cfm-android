package com.getfiscal.mobile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.getfiscal.mobile.GetFiscalApplication;
import com.getfiscal.mobile.R;
import com.getfiscal.mobile.utils.DataUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


/**
 * Created by mverma1 on 10/15/15.
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    public static final String TAG = "LoginActivity";
    public static final String MyPREFERENCES = "MyPrefs" ;

    private static final int REGISTER = 1;
    private static final int SIGNIN = 2;

    String token;


    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sigin_screen_layout);


        View v= findViewById(R.id.new_user_signup_here);
        v.setOnClickListener(this);

        v= findViewById(R.id.existing_user_login_here);
        v.setOnClickListener(this);


        v= findViewById(R.id.buttonSignIn);
        v.setOnClickListener(this);

        v= findViewById(R.id.buttonSignUp);
        v.setOnClickListener(this);


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        String loginName=sharedpreferences.getString("loginName",null);
        if(loginName!=null)
        {
            EditText loginEmailText = (EditText)findViewById(R.id.user_email);
            loginEmailText.setText(loginName);
            View signupView=findViewById(R.id.signup_layout);
            signupView.setVisibility(View.GONE);

            View signinView=findViewById(R.id.signin_layout);
            signinView.setVisibility(View.VISIBLE);

            View password=findViewById(R.id.user_password);
            password.requestFocus();
        }
        else {
            View signupView=findViewById(R.id.signup_layout);
            signupView.setVisibility(View.VISIBLE);

            View signinView=findViewById(R.id.signin_layout);
            signinView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.new_user_signup_here:
            {
                View signupView=findViewById(R.id.signup_layout);
                signupView.setVisibility(View.VISIBLE);

                View signinView=findViewById(R.id.signin_layout);
                signinView.setVisibility(View.GONE);
                break;
            }
            case R.id.existing_user_login_here:
            {
                View signupView=findViewById(R.id.signup_layout);
                signupView.setVisibility(View.GONE);

                View signinView=findViewById(R.id.signin_layout);
                signinView.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.buttonSignIn:
            {

                JSONObject loginJson = new JSONObject();
                EditText login = (EditText) findViewById(R.id.user_email);
                EditText password = (EditText) findViewById(R.id.user_password);
                try {
                    loginJson.put("email", login.getText());
                    loginJson.put("password", password.getText());

                    signin(loginJson);
                } catch (JSONException e) {

                }
                break;
            }
            case R.id.buttonSignUp:
            {

                JSONObject loginJson = new JSONObject();
                    EditText newuser_email = (EditText) findViewById(R.id.newuser_email);
                    EditText newuser_password = (EditText) findViewById(R.id.newuser_password);
                    EditText newuser_phone = (EditText) findViewById(R.id.newuser_phone);
                    EditText companyNameView = (EditText) findViewById(R.id.companyName);
                    try {
                        String name=companyNameView.getText().toString();
                        String adminEmail=newuser_email.getText().toString();
                        String password= newuser_password.getText().toString();
                        String phone=newuser_phone.getText().toString();

                        loginJson.put("name", name);
                        loginJson.put("adminEmail", adminEmail);
                        loginJson.put("password", password);
                        loginJson.put("phone", phone);

                        signup(loginJson);
                    }
                    catch (JSONException e)
                    {

                    }
                break;
            }
        }

    }

    //    SharedPreferences sharedpreferences;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//
//        super.onCreate(savedInstanceState);
//
//
//        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
//
//        setContentView(R.layout.login_activity);
//
//        mButtonAction = (Button) findViewById(R.id.buttonAction);
//        mButtonAction.setOnClickListener(this);
//        mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
//        mOtherAction = (TextView) findViewById(R.id.otherAction);
//        mOtherAction.setOnClickListener(this);
//
//
//        String loginName=sharedpreferences.getString("loginName",null);
//        if(loginName!=null)
//        {
//            EditText loginEmailText = (EditText)findViewById(R.id.loginEmailText);
//            loginEmailText.setText(loginName);
//            mOtherAction.setText(R.string.signupHere);
//            mButtonAction.setText(R.string.signin);
//            mViewFlipper.showNext();
//            type = SIGNIN;
//        }
//        else {
//
//            type = REGISTER;
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.otherAction: {
//                if (type == REGISTER) {
//                    mViewFlipper.setInAnimation(LoginActivity.this, R.anim.in_from_right);
//                    mViewFlipper.setOutAnimation(LoginActivity.this, R.anim.out_to_left);
//                    mOtherAction.setText(R.string.signupHere);
//                    mButtonAction.setText(R.string.signin);
//                    mViewFlipper.showNext();
//                    type = SIGNIN;
//                } else if (type == SIGNIN) {
//                    mViewFlipper.setInAnimation(LoginActivity.this, R.anim.in_from_left);
//                    mViewFlipper.setOutAnimation(LoginActivity.this, R.anim.out_to_right);
//                    mOtherAction.setText(R.string.alreadyOnGetFiscal);
//                    mButtonAction.setText(R.string.register);
//                    mViewFlipper.showPrevious();
//                    type = REGISTER;
//                }
//                break;
//            }
//            case R.id.buttonAction: {
//                if (type == SIGNIN) {
//                    JSONObject loginJson = new JSONObject();
//                    EditText login = (EditText) findViewById(R.id.loginEmailText);
//                    EditText password = (EditText) findViewById(R.id.loginPasswordText);
//                    try {
//                        loginJson.put("email", login.getText());
//                        loginJson.put("password", password.getText());
//
//                        signin(loginJson);
//                    }
//                    catch (JSONException e)
//                    {
//
//                    }
//
//                }
//                else if(type == REGISTER)
//                {
//                    JSONObject loginJson = new JSONObject();
//                    EditText email = (EditText) findViewById(R.id.companyEmailText);
//                    EditText password = (EditText) findViewById(R.id.companyPasswordText);
//                    EditText phone = (EditText) findViewById(R.id.companyPhoneText);
//                    EditText companyname = (EditText) findViewById(R.id.companyNameText);
//                    try {
//                        loginJson.put("name", companyname.getText());
//                        loginJson.put("adminEmail", email.getText());
//                        loginJson.put("password", password.getText());
//                        loginJson.put("phone", phone.getText());
//
//                        signup(loginJson);
//                    }
//                    catch (JSONException e)
//                    {
//
//                    }
//                }
//            }
//        }
//
//    }
//
    private void signin(final JSONObject loginJson) {

        if(DataUtils.isConnected(this)==false)
        {
            showNoNetworkDialog();
            return;
        }

        String tag_json_obj = "json_obj_req";

        String url = GetFiscalApplication.BASE_URL+"/auth/local";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Signing...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try
                        {
                            token=(String)response.get("token");
                            GetFiscalApplication.getInstance().setAuthToken(token);
                            Intent intent= new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("loginName", loginJson.getString("email"));

                            GetFiscalApplication.getInstance().setCompanyEmail(loginJson.getString("email"));
                            editor.commit();
                            pDialog.cancel();
                            finish();

                        }
                        catch (JSONException e)
                        {
                            token=null;
                            pDialog.cancel();
                            showLoginFailed();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        token=null;
                        pDialog.cancel();

                        if(error instanceof NoConnectionError)
                        {
                            showServerDownError();
                        }else
                        {
                            showLoginFailed();
                        }
                    }
                }) {

            @Override
            public byte[] getBody() {
                //return "{\"email\":\"manish.verma2805@gmail.com\", \"password\": \"man123\"}".getBytes();
                return loginJson.toString().getBytes();


            }

            @Override
            public String getBodyContentType() {

                return "application/json;charset=UTF-8";

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return super.getHeaders();

            }

        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        GetFiscalApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }


    private void showLoginFailed()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Login failed! Invalid credentials!");

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private void signup(final JSONObject loginJson) {


        if(DataUtils.isConnected(this)==false)
        {
            showNoNetworkDialog();
            return;
        }
        String tag_json_obj = "json_obj_req";

        String url = GetFiscalApplication.BASE_URL+"/api/companies";
        //String url = "http://ec2-54-72-9-34.eu-west-1.compute.amazonaws.com/api/companies";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Registering...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try
                        {
                            token=(String)response.get("token");
                            GetFiscalApplication.getInstance().setAuthToken(token);
                            Intent intent= new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("loginName", loginJson.getString("adminEmail"));
                            editor.commit();
                            pDialog.hide();

                            GetFiscalApplication.getInstance().setCompanyEmail(loginJson.getString("adminEmail"));
                            finish();
                        }
                        catch (JSONException e)
                        {
                            token=null;
                            pDialog.hide();
                            showLoginFailed();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        token=null;
                        pDialog.hide();
                        //if(error.networkResponse.statusCode==422)

                        if(error instanceof NoConnectionError)
                        {
                            showServerDownError();
                        }else
                        {
                            showRegisterFailed();
                        }
                    }
                }) {

            @Override
            public byte[] getBody() {
                //return "{\"email\":\"manish.verma2805@gmail.com\", \"password\": \"man123\"}".getBytes();
                return loginJson.toString().getBytes();


            }

            @Override
            public String getBodyContentType() {

                return "application/json;charset=UTF-8";

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return super.getHeaders();

            }

        };


        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        GetFiscalApplication.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void showRegisterFailed()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Registration failed! Try with diffrent e-mail address!");

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showServerDownError()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Oops! Due to some maintenance activity, our server are down please try after some time.");

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
