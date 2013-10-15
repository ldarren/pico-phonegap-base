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

import com.baroq.pico.google.iab.IabHelper;
import com.baroq.pico.google.iab.IabResult;
import com.baroq.pico.google.iab.Inventory;

import java.util.ArrayList;
import java.util.List;

public class InAppBilling extends CordovaPlugin{
    private static final String TAG = "PICO-PLUGIN-GOOG";
    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgPp3pUWZTL/06V6Z4Ry/R5CRZ5lKFtB6afM5gfWK16Sisk7vEaidEXHzSx1fGgBl5TCV88fx3S7w7dAUCHU2nfDMwC/6YyQK7SkjI35P1wndWgRTefeCbkYy5UiwyGkb6S0Qtsa/igZtFRHlmAAjHj9oPHlWZ1zRHRr6TOzK5p8Vf0nOBewXMmsG467Fda6EYgJLpWzvS1SQRxw76wbpbWC5PDFNN/W9nhfkm0/C0xyXIyZMqeL2Ms2gepmAZAAhv+PHXaMGKs26uZDN5dyoYL0PsoSRXWetOO09Xt098hUJZScgN6nuRMxwWB2n1ujBAmPJp11MlnAi9rQYl5jSCQIDAQAB";
    
    private static final String ACTION_OPEN = "iabOpen";
    private static final String ACTION_CLOSE = "iabClose";
    private static final String ACTION_INV = "iabInventory";
    private static final String ACTION_BUY = "iabBuy";
    private static final String ACTION_TRANSACT = "iabTransact";
    private static final String ACTION_CONSUME = "iabConsume";

    // The helper object
    IabHelper mHelper;

    // Plugin action handler
    @Override
    public boolean execute(String action, JSONArray data,  CallbackContext callbackContext) {
        boolean result = true;
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);

        if (ACTION_OPEN.equals(action)) {
            open(cordova.getActivity(), PUBLIC_KEY, callbackContext);
            callbackContext.sendPluginResult(pluginResult);
        } else if (ACTION_CLOSE.equals(action)){
            close();
            callbackContext.success();
        } else if (ACTION_INV.equals(action)){
            List<String> moreSkus = new ArrayList<String>();

            try{
            for (int i=0, l=data.length(); i<l; i++) {
                moreSkus.add( data.getString(i) );
            }
            }catch(JSONException ex){
                callbackContext.error(ex.getMessage());
                return result;
            }

            inventory(moreSkus, callbackContext);
            callbackContext.sendPluginResult(pluginResult);
        } else if (ACTION_BUY.equals(action)){
        } else if (ACTION_TRANSACT.equals(action)){
        } else if (ACTION_CONSUME.equals(action)){
        }

        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }

    private void open(Activity activity, String key, final CallbackContext callbackContext){
        close();
        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(activity, key);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    callbackContext.error("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) {
                    callbackContext.error("The billing helper has been disposed");
                    return;
                }

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful.");
                callbackContext.success("Init successful");
            }
        });
    }

    private void close(){
        if (null != mHelper){
            mHelper.dispose();
            mHelper = null;
        }
    }

    private void inventory(List<String> moreSkus, final CallbackContext callbackContext){
        Log.d(TAG, "Querying inventory.");
        mHelper.queryInventoryAsync(true, moreSkus, new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                Log.d(TAG, "Query inventory finished.");
                
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) {
                    callbackContext.error("The billing helper has been disposed");
                    return;
                }
                
                // Is it a failure?
                if (result.isFailure()) {
                    callbackContext.error("Failed to query inventory: " + result);
                    return;
                }

                Log.d(TAG, "detail list size: "+inventory.jsonSkuDetailsList.size());
                JSONObject json = new JSONObject();
                JSONArray ownedSkus = new JSONArray();
                JSONArray purchaseDataList = new JSONArray();
                JSONArray signatureList = new JSONArray();
                JSONArray skuDetailsList = new JSONArray();
                int i, l;
                ArrayList<String> list1, list2, list3;
                try{
                    list1 = inventory.jsonOwnedSkus;
                    list2 = inventory.jsonPurchaseDataList;
                    list3 = inventory.jsonSignatureList;
                    for(i=0, l=list1.size(); i<l; i++){
                        ownedSkus.put(new JSONObject(list1.get(i)));
                        purchaseDataList.put(new JSONObject(list2.get(i)));
                        signatureList.put(new JSONObject(list3.get(i)));
                    }
                    list1 = inventory.jsonSkuDetailsList;
                    for(i=0, l=list1.size(); i<l; i++){
                        skuDetailsList.put(new JSONObject(list1.get(i)));
                    }
                    json.put("ownedSkus", ownedSkus);
                    json.put("purchaseDataList", purchaseDataList);
                    json.put("signatureList", signatureList);
                    json.put("skuDetailsList", skuDetailsList);
                }catch(JSONException ex){
                    callbackContext.error("Failed to contruct inventory json: " + ex);
                    return;
                }
                
                Log.d(TAG, "Query inventory was successful.");
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
    }

    private void consume(CallbackContext callbackContext){
        //mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
    }
};
