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

import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.OnStateLoadedListener;
import com.baroq.pico.google.gms.GmsHelper;

import java.util.ArrayList;
import java.util.List;

public class PlayServices extends CordovaPlugin implements GmsHelper.GmsHelperListener {
    private static final String TAG = "PICO-GOOG-GMS";
    private static final boolean DEBUG_ENABLED = true;    

    private static final String ACTION_SETUP = "setup";
    private static final String ACTION_SIGNIN = "signin";
    private static final String ACTION_SIGNOUT = "signout";
    private static final String ACTION_AS_MAX_KEYS = "getMaxNumKeys";
    private static final String ACTION_AS_MAX_SIZE = "getMaxStateSize";
    private static final String ACTION_AS_STATE_DEL = "deleteState";
    private static final String ACTION_AS_STATE_LIST = "listStates";
    private static final String ACTION_AS_STATE_LOAD = "loadState";
    private static final String ACTION_AS_STATE_RESOLVE = "resolveState";
    private static final String ACTION_AS_STATE_UPDATE = "updateState";
    private static final String ACTION_AS_STATE_UPDATE_CB = "updateStateImmediate";

    GmsHelper mHelper;
    CallbackContext connectionCB;

    // Plugin action handler
    @Override
    public boolean execute(String action, JSONArray data,  CallbackContext callbackContext) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);

        try{
            if (!ACTION_SETUP.equals(action) && !ACTION_SIGNIN.equals(action) && null == mHelper){
                callbackContext.error("Please setup and signin to use PlayServices plugin");
                return false;
            }
            if (ACTION_SETUP.equals(action)) {
                int l = data.length();
                if (0 == l) {
                    callbackContext.error("Expecting at least 1 parameter for action: "+action);
                    return false;
                }
                int serviceIds = data.getInt(0);
                String[] extraScopes = new String[l-1];
                for(int i=1; i<l; i++){
                    extraScopes[i-1] = data.getString(i);
                }
                setup(serviceIds, extraScopes, callbackContext);
                callbackContext.sendPluginResult(pluginResult);
            }else if (ACTION_SIGNIN.equals(action)){
                mHelper.beginUserInitiatedSignIn();
                callbackContext.success();
            }else if (ACTION_SIGNOUT.equals(action)){
                signout();
                callbackContext.success();
            }else if (ACTION_AS_MAX_KEYS.equals(action)){
                pluginResult = new PluginResult(PluginResult.Status.OK, mHelper.getAppStateClient().getMaxNumKeys());
                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }else if (ACTION_AS_MAX_SIZE.equals(action)){
                pluginResult = new PluginResult(PluginResult.Status.OK, mHelper.getAppStateClient().getMaxStateSize());
                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }else if (ACTION_AS_STATE_DEL.equals(action)){
            }else if (ACTION_AS_STATE_LIST.equals(action)){
            }else if (ACTION_AS_STATE_LOAD.equals(action)){
            }else if (ACTION_AS_STATE_RESOLVE.equals(action)){
                getAppStateClient().resolveState(this, OUR_STATE_KEY, resolvedVersion, resolvedGame.toBytes());
            }else if (ACTION_AS_STATE_UPDATE.equals(action)){
                int key = data.getInt(0);
                String dataStr = data.getString(1);
                mHelper.getAppStateClient().updateState(key, data.getBytes());
                callbackContext.success();
            }else if (ACTION_AS_STATE_UPDATE_CB.equals(action)){
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

    @Override           
    public void onStateLoaded(int statusCode, int stateKey, byte[] localData) {
        switch (statusCode) {
            case AppStateClient.STATUS_OK:
                // Data was successfully loaded from the cloud: merge with local data.
                break;        
            case AppStateClient.STATUS_STATE_KEY_NOT_FOUND:
                // key not found means there is no saved data. To us, this is the same as
                // having empty data, so we treat this as a success.
                mAlreadyLoadedState = true;
                hideAlertBar();
                break;
            case AppStateClient.STATUS_NETWORK_ERROR_NO_DATA:
                // can't reach cloud, and we have no local state. Warn user that
                // they may not see their existing progress, but any new progress won't be lost.
                showAlertBar(R.string.no_data_warning);
                break;
            case AppStateClient.STATUS_NETWORK_ERROR_STALE_DATA:
                // can't reach cloud, but we have locally cached data.
                showAlertBar(R.string.stale_data_warning);
                break;
            case AppStateClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                // need to reconnect AppStateClient
                reconnectClients(BaseGameActivity.CLIENT_APPSTATE);
                break;
            default:
                // error
                showAlertBar(R.string.load_error_warning);
                break;
        }
    }

    @Override
    public void onStateConflict(int stateKey, String resolvedVersion, byte[] localData, byte[] serverData) {
        // Need to resolve conflict between the two states.
        // We do that by taking the union of the two sets of cleared levels,
        // which means preserving the maximum star rating of each cleared
        // level:
        SaveGame localGame = new SaveGame(localData);
        SaveGame serverGame = new SaveGame(serverData);
        SaveGame resolvedGame = localGame.unionWith(serverGame);
    }

    private void setup(int serviceId, String[] extraScopes, final CallbackContext callbackContext){
        mHelper = new GmsHelper(cordova.getActivity());
        connectionCB = callbackContext;

        Log.d(TAG, "Setup onActivityCallback to this");
        cordova.setActivityResultCallback(this);

        mHelper.enableDebugLog(DEBUG_ENABLED, TAG);

        mHelper.setup(this, serviceId, extraScopes);
    }

    private void signout(){
        mHelper.signOut();
    }
};
