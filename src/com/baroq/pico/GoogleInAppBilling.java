// ref: http://developer.android.com/google/play/billing/billing_integrate.html
package com.baroq.pico;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

public class GoogleInAppBilling extends Plugin {
    IabHelper mHelper;
    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    public void init(){
        mHelper = new IabHelper(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgPp3pUWZTL/06V6Z4Ry/R5CRZ5lKFtB6afM5gfWK16Sisk7vEaidEXHzSx1fGgBl5TCV88fx3S7w7dAUCHU2nfDMwC/6YyQK7SkjI35P1wndWgRTefeCbkYy5UiwyGkb6S0Qtsa/igZtFRHlmAAjHj9oPHlWZ1zRHRr6TOzK5p8Vf0nOBewXMmsG467Fda6EYgJLpWzvS1SQRxw76wbpbWC5PDFNN/W9nhfkm0/C0xyXIyZMqeL2Ms2gepmAZAAhv+PHXaMGKs26uZDN5dyoYL0PsoSRXWetOO09Xt098hUJZScgN6nuRMxwWB2n1ujBAmPJp11MlnAi9rQYl5jSCQIDAQAB");
        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
    }

    public void deinit(){
        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }   
    }
};
