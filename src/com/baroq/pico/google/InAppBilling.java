// ref: http://developer.android.com/google/play/billing/billing_integrate.html
package com.baroq.pico.google;

import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.content.ServiceConnection;
import android.content.Context;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

public class InAppBilling extends CordovaPlugin{
    private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgPp3pUWZTL/06V6Z4Ry/R5CRZ5lKFtB6afM5gfWK16Sisk7vEaidEXHzSx1fGgBl5TCV88fx3S7w7dAUCHU2nfDMwC/6YyQK7SkjI35P1wndWgRTefeCbkYy5UiwyGkb6S0Qtsa/igZtFRHlmAAjHj9oPHlWZ1zRHRr6TOzK5p8Vf0nOBewXMmsG467Fda6EYgJLpWzvS1SQRxw76wbpbWC5PDFNN/W9nhfkm0/C0xyXIyZMqeL2Ms2gepmAZAAhv+PHXaMGKs26uZDN5dyoYL0PsoSRXWetOO09Xt098hUJZScgN6nuRMxwWB2n1ujBAmPJp11MlnAi9rQYl5jSCQIDAQAB";
    
    private static final String ACTION_INIT = "iabInit";
    private static final String ACTION_INV = "iabInventory";
    private static final String ACTION_GOODS = "iabGoods";
    private static final String ACTION_BUY = "iabBuy";
    private static final String ACTION_TRANSACT = "iabTransact";
    private static final String ACTION_CONSUME = "iabConsume";

    private IInAppBillingService mService;

    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    // Plugin action handler
    @Override
    public boolean execute(String action, JSONArray data,  CallbackContext callbackContext) {
        // Save the callback id
        // this.callbackId = callbackId;
        boolean result = true;
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);

        if (ACTION_INIT.equals(action)) {
            init(callbackContext);
            callbackContext.sendPluginResult(pluginResult);
        } else if (ACTION_INV.equals(action)){
        } else if (ACTION_GOODS.equals(action)){
        } else if (ACTION_BUY.equals(action)){
        } else if (ACTION_TRANSACT.equals(action)){
        } else if (ACTION_CONSUME.equals(action)){
        }

        return result;
    }

    private void init(CallbackContext callbackContext){
        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
        PluginResult result;
        if (mServiceConn != null){
            result = new PluginResult(PluginResult.Status.OK, "In app billing initialized");
        }else{
            result = new PluginResult(PluginResult.Status.ERROR, "In app billing not supported");
        }
        result.setKeepCallback(false);
        callbackContext.sendPluginResult(result);
    }

    public void deinit(){
        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }   
    }

    public void getSKUDetails(){
        ArrayList skuList = new ArrayList();
        skuList.add("premiumUpgrade");
        skuList.add("gas");
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList(“ITEM_ID_LIST”, skuList);

        Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);

        int response = skuDetails.getInt("RESPONSE_CODE");
        if (response == 0) {
            ArrayList responseList = skuDetails.getStringArrayList("DETAILS_LIST");

            for (String thisResponse : responseList) {
                JSONObject object = new JSONObject(thisResponse);
                String sku = object.getString("productId");
                String price = object.getString("price");
                if (sku.equals("premiumUpgrade")) mPremiumUpgradePrice = price;
                else if (sku.equals("gas")) mGasPrice = price;
            }
        }
    }

    public void itemPurchase(String sku){
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), sku, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
        switch(buyIntentBundle.getInt("RESPONSE_CODE", 9)){
            case 0: // BILLING_RESPONSE_RESULT_OK
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                break;
            case 1: // BILLING_RESPONSE_RESULT_USER_CANCELED 
                break;
            case 3: // BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE 
                break;
            case 4: // BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE 
                break;
            case 5: // BILLING_RESPONSE_RESULT_DEVELOPER_ERROR 
                break;
            case 6: // BILLING_RESPONSE_RESULT_ERROR 
                break;
            case 7: // BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED 
                break;
            case 8: // BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED 
                break;
            case 9: // BILLING_RESPONSE_RESULT_OK
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
        if (requestCode == 1001) {           
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    alert("You have bought the " + sku + ". Excellent choice, 
                    adventurer!");
                }
                catch (JSONException e) {
                    alert("Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }
        }
    }

    public void getPurchase(){
        Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
        int response = ownedItems.getInt("RESPONSE_CODE");
        if (response == 0) {
            ArrayList ownedSkus = 
            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            ArrayList purchaseDataList = 
            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
            ArrayList signatureList = 
            ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
            String continuationToken = 
            ownedItems.getString("INAPP_CONTINUATION_TOKEN");

            for (int i = 0; i < purchaseDataList.size(); ++i) {
                String purchaseData = purchaseDataList.get(i);
                String signature = signatureList.get(i);
                String sku = ownedSkus.get(i);

                // do something with this purchase information
                // e.g. display the updated list of products owned by user
            } 

            // if continuationToken != null, call getPurchases again 
            // and pass in the token to retrieve more items
        }
    }

    public void consumeItem(){
        int response = mService.consumePurchase(3, getPackageName(), token);
    }
};
