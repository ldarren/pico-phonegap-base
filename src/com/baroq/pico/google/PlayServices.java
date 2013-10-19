// ref: http://developer.android.com/google/play/billing/billing_integrate.html
package com.baroq.pico.google;

import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.baroq.pico.google.gms.GmsHelper;

import java.util.ArrayList;
import java.util.List;

public class PlayServices extends CordovaPlugin implements GmsHelper.GmsHelperListener {
    private static final String TAG = "PICO-GOOG-GMS";
    private static final boolean DEBUG_ENABLED = true;    

    private static final String ACTION_SETUP = "setup";
    private static final String ACTION_SIGNIN = "signin";
    private static final String ACTION_SIGNOUT = "signout";

    GmsHelper mHelper;
    CallbackContext connectionCB;

    // Plugin action handler
    @Override
    public boolean execute(String action, JSONArray data,  CallbackContext callbackContext) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);

        try{
            if (ACTION_SETUP.equals(action)) {
                int l = data.length();
                if (0 == l) callbackContext.error("Expecting at least 1 parameter for action: "+action);
                int serviceIds = data.getInt(0);
                String[] extraScopes = new String[l-1];
                for(int i=1; i<l; i++){
                    extraScopes[i-1] = data.getString(i);
                }
                setup(serviceIds, extraScopes, callbackContext);
                callbackContext.sendPluginResult(pluginResult);
            }else if (ACTION_SIGNIN.equals(action)){
                signin();
                callbackContext.success();
            }else if (ACTION_SIGNOUT.equals(action)){
                signout();
                callbackContext.success();
            }else{
                callbackContext.error("Unknown action: " + action);
                return false;
            }
        }catch(JSONException ex){
            callbackContext.error(ex.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        signout();
    }

    @Override
    public void onSignInSucceeded(){
        JSONObject json = new JSONObject();
        try{
            json.put("signin", true);
        }catch(JSONException ex){
            Log.e(TAG, "failed to construct signin succeeded json");
            return;
        }
        Log.d(TAG, "signin succeeded");

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onSignInFailed(String reason){
        JSONObject json = new JSONObject();
        try{
            json.put("signin", false);
            json.put("reason", reason);
        }catch(JSONException ex){
            Log.e(TAG, "failed to construct signin failed json");
            return;
        }
        Log.d(TAG, "signin failed");

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + responseCode + "," + data);

        super.onActivityResult(requestCode, responseCode, data);
        mHelper.onActivityResult(requestCode, responseCode, data);
        
        Log.d(TAG, "onActivityResult handled by "+TAG);
    }

    private void setup(int serviceId, String[] extraScopes, final CallbackContext callbackContext){
        mHelper = new GmsHelper(cordova.getActivity());
        connectionCB = callbackContext;

        mHelper.enableDebugLog(DEBUG_ENABLED, TAG);

        mHelper.setup(this, serviceId, extraScopes);
    }

    private void signin(){
        mHelper.beginUserInitiatedSignIn();
    }

    private void signout(){
        mHelper.signOut();
    }
};
