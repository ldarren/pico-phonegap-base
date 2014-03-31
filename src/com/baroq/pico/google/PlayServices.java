// ref:
// http://developer.android.com/google/play/billing/billing_integrate.html
// http://developer.android.com/reference/com/google/android/gms/games/GamesClient.html
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
import android.net.Uri;

import com.google.android.gms.appstate.AppState;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.appstate.AppStateBuffer;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GameBuffer;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.PlayerBuffer;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.leaderboard.LeaderboardBuffer;
import com.google.android.gms.games.leaderboard.Leaderboard;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;
import com.google.android.gms.games.leaderboard.SubmitScoreResult.Result;

import com.google.android.gms.appstate.OnStateLoadedListener;
import com.google.android.gms.appstate.OnStateListLoadedListener;
import com.google.android.gms.appstate.OnStateDeletedListener;
import com.google.android.gms.games.achievement.OnAchievementUpdatedListener;
import com.google.android.gms.games.achievement.OnAchievementsLoadedListener;
import com.google.android.gms.games.OnGamesLoadedListener;
import com.google.android.gms.games.leaderboard.OnLeaderboardMetadataLoadedListener;
import com.google.android.gms.games.leaderboard.OnLeaderboardScoresLoadedListener;
import com.google.android.gms.games.OnPlayersLoadedListener;
import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;

import com.baroq.pico.google.gms.GmsHelper;

import java.util.ArrayList;
import java.util.List;

public class PlayServices   extends     CordovaPlugin
                            implements
                                        GmsHelper.GmsHelperListener,
                                        OnStateLoadedListener,
                                        OnStateListLoadedListener,
                                        OnStateDeletedListener,
                                        OnAchievementUpdatedListener,
                                        OnAchievementsLoadedListener,
                                        OnGamesLoadedListener,
                                        OnLeaderboardMetadataLoadedListener,
                                        OnLeaderboardScoresLoadedListener,
                                        OnPlayersLoadedListener,
                                        OnScoreSubmittedListener{
    private static final String TAG = "PICO-GOOG-GMS";
    private static final boolean DEBUG_ENABLED = true;    

    private static final String ACTION_SETUP = "setup";
    private static final String ACTION_SIGNIN = "signin";
    private static final String ACTION_SIGNOUT = "signout";

    private static final String ACTION_AS_MAX_KEYS = "getMaxNumKeys";
    private static final String ACTION_AS_MAX_SIZE = "getMaxStateSize";
    private static final String ACTION_AS_DEL = "deleteState";
    private static final String ACTION_AS_LIST = "listStates";
    private static final String ACTION_AS_LOAD = "loadState";
    private static final String ACTION_AS_RESOLVE = "resolveState";
    private static final String ACTION_AS_UPDATE = "updateState";
    private static final String ACTION_AS_UPDATE_NOW = "updateStateImmediate";
    
    private static final String ACTION_GAME_SHOW_ACHIEVEMENTS = "getAchievementsIntent";
    private static final String ACTION_GAME_SHOW_LEADERBOARDS = "getAllLeaderboardsIntent";
    private static final String ACTION_GAME_SHOW_LEADERBOARD = "getLeaderboardIntent";
    private static final String ACTION_GAME_INCR_ACHIEVEMENT = "incrementAchievement";
    private static final String ACTION_GAME_INCR_ACHIEVEMENT_NOW = "incrementAchievementImmediate";
    private static final String ACTION_GAME_LOAD_ACHIEVEMENTS = "loadAchievements";
    private static final String ACTION_GAME_LOAD_GAME = "loadGame";
    private static final String ACTION_GAME_LOAD_LEADERBOARD_METADATA = "loadLeaderboardMetadata";
    private static final String ACTION_GAME_LOAD_MORE_SCORES = "loadMoreScores";
    private static final String ACTION_GAME_LOAD_PLAYER = "loadPlayer";
    private static final String ACTION_GAME_LOAD_PLAYER_CENTERED_SCORES = "loadPlayerCenteredScores";
    private static final String ACTION_GAME_LOAD_TOP_SCORES = "loadTopScores";
    private static final String ACTION_GAME_REVEAL_ACHIEVEMENT = "revealAchievement";
    private static final String ACTION_GAME_REVEAL_ACHIEVEMENT_NOW = "revealAchievementImmediate";
    private static final String ACTION_GAME_SUBMIT_SCORE = "submitScore";
    private static final String ACTION_GAME_SUBMIT_SCORE_NOW = "submitScoreImmediate";
    private static final String ACTION_GAME_UNLOCK_ACHIEVEMENT = "unlockAchievement";
    private static final String ACTION_GAME_UNLOCK_ACHIEVEMENT_NOW = "unlockAchievementImmediate";

    private static final int GMS_SIGNIN = 1;
    private static final int STATE_LOADED = 2;
    private static final int STATE_LIST_LOADED = 3;
    private static final int STATE_CONFLICTED = 4;
    private static final int STATE_DELETED = 5;
    private static final int GAMES_LOADED = 6;
    private static final int PLAYER_LOADED = 7;
    private static final int GAME_ACHIEVEMENT_LOADED = 8;
    private static final int GAME_ACHIEVEMENT_UPDATED = 9;
    private static final int GAME_LEADERBOARD_METADATA_LOADED = 10;
    private static final int GAME_LEADERBOARD_SCORES_LOADED = 11;
    private static final int GAME_SCORES_SUBMITTED = 12;

    // request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;

    GmsHelper mHelper;
    CallbackContext connectionCB;
    int clientTypes = GmsHelper.CLIENT_NONE;
    LeaderboardScoreBuffer scoreBuffer = null;

    // Plugin action handler
    @Override
    public boolean execute(String action, JSONArray data,  CallbackContext callbackContext) {

        try{
            if (!ACTION_SETUP.equals(action) && !ACTION_SIGNIN.equals(action) && (null == mHelper || !mHelper.isConnected())){
                callbackContext.error("Please setup and signin to use PlayServices plugin");
                return false;
            }
            if (ACTION_SETUP.equals(action)) {
                int l = data.length();
                if (0 == l) {
                    callbackContext.error("Expecting at least 1 parameter for action: "+action);
                    return false;
                }
                clientTypes = data.getInt(0);
                String[] extraScopes = new String[l-1];
                for(int i=1; i<l; i++){
                    extraScopes[i-1] = data.getString(i);
                }
                setup(clientTypes, extraScopes, callbackContext);
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }else if (ACTION_SIGNIN.equals(action)){
                mHelper.beginUserInitiatedSignIn();
                callbackContext.success();
            }else if (ACTION_SIGNOUT.equals(action)){
                signout();
                callbackContext.success();
            }else if (ACTION_AS_MAX_KEYS.equals(action)){
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, mHelper.getAppStateClient().getMaxNumKeys());
                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }else if (ACTION_AS_MAX_SIZE.equals(action)){
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, mHelper.getAppStateClient().getMaxStateSize());
                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }else if (ACTION_AS_DEL.equals(action)){
                int key = data.getInt(0);
                mHelper.getAppStateClient().deleteState(this, key);
                callbackContext.success();
            }else if (ACTION_AS_LIST.equals(action)){
                mHelper.getAppStateClient().listStates(this);
                callbackContext.success();
            }else if (ACTION_AS_LOAD.equals(action)){
                int key = data.getInt(0);
                mHelper.getAppStateClient().loadState(this, key);
                callbackContext.success();
            }else if (ACTION_AS_RESOLVE.equals(action)){
                int key = data.getInt(0);
                String resolvedVersion = data.getString(1);
                String value = data.getString(2);
                mHelper.getAppStateClient().resolveState(this, key, resolvedVersion, value.getBytes());
                callbackContext.success();
            }else if (ACTION_AS_UPDATE.equals(action)){
                int key = data.getInt(0);
                String value= data.getString(1);
                mHelper.getAppStateClient().updateState(key, value.getBytes());
                callbackContext.success();
            }else if (ACTION_AS_UPDATE_NOW.equals(action)){
                int key = data.getInt(0);
                String value = data.getString(1);
                mHelper.getAppStateClient().updateStateImmediate(this, key, value.getBytes());
                callbackContext.success();
            }else if (ACTION_GAME_SHOW_ACHIEVEMENTS.equals(action)){
                cordova.startActivityForResult((CordovaPlugin)this, mHelper.getGamesClient().getAchievementsIntent(), RC_UNUSED);
                callbackContext.success();
            }else if (ACTION_GAME_SHOW_LEADERBOARDS.equals(action)){
                cordova.startActivityForResult((CordovaPlugin)this, mHelper.getGamesClient().getAllLeaderboardsIntent(), RC_UNUSED);
                callbackContext.success();
            }else if (ACTION_GAME_SHOW_LEADERBOARD.equals(action)){
                String id = data.getString(0);
                cordova.startActivityForResult((CordovaPlugin)this, mHelper.getGamesClient().getLeaderboardIntent(id), RC_UNUSED);
                callbackContext.success();
            }else if (ACTION_GAME_INCR_ACHIEVEMENT.equals(action)){
                String id = data.getString(0);
                int numSteps = data.getInt(1);
                mHelper.getGamesClient().incrementAchievement(id, numSteps);
                callbackContext.success();
            }else if (ACTION_GAME_INCR_ACHIEVEMENT_NOW.equals(action)){
                String id = data.getString(0);
                int numSteps = data.getInt(1);
                mHelper.getGamesClient().incrementAchievementImmediate(this, id, numSteps);
                callbackContext.success();
            }else if (ACTION_GAME_LOAD_ACHIEVEMENTS.equals(action)){
                boolean forceReload = data.getBoolean(0);
                mHelper.getGamesClient().loadAchievements(this, forceReload);
                callbackContext.success();
            }else if (ACTION_GAME_LOAD_GAME.equals(action)){
                mHelper.getGamesClient().loadGame(this);
                callbackContext.success();
            }else if (ACTION_GAME_LOAD_LEADERBOARD_METADATA.equals(action)){
                if (1 == data.length()){
                    mHelper.getGamesClient().loadLeaderboardMetadata(this, data.getBoolean(0));
                }else{
                    mHelper.getGamesClient().loadLeaderboardMetadata(this, data.getString(0), data.getBoolean(1));
                }
                callbackContext.success();
            }else if (ACTION_GAME_LOAD_MORE_SCORES.equals(action)){
                if (null == scoreBuffer){
                    callbackContext.error("Get a leaderboard fist before calling: "+action);
                    return false;
                }
                int maxResults = data.getInt(0);
                int pageDirection = data.getInt(0);
                mHelper.getGamesClient().loadMoreScores(this, scoreBuffer, maxResults, pageDirection);
                callbackContext.success();
            }else if (ACTION_GAME_LOAD_PLAYER.equals(action)){
                String playerId = data.getString(0);
                mHelper.getGamesClient().loadPlayer(this, playerId);
                callbackContext.success();
            }else if (ACTION_GAME_LOAD_PLAYER_CENTERED_SCORES.equals(action)){
                String leaderboardId = data.getString(0);
                int span = data.getInt(1);
                int leaderboardCollection = data.getInt(2);
                int maxResults = data.getInt(3);
                if (data.isNull(4)){
                    mHelper.getGamesClient().loadPlayerCenteredScores(this, leaderboardId, span, leaderboardCollection, maxResults);
                }else{
                    boolean forceReload = data.getBoolean(4);
                    mHelper.getGamesClient().loadPlayerCenteredScores(this, leaderboardId, span, leaderboardCollection, maxResults, forceReload);
                }
                callbackContext.success();
            }else if (ACTION_GAME_LOAD_TOP_SCORES.equals(action)){
                String leaderboardId = data.getString(0);
                int span = data.getInt(1);
                int leaderboardCollection = data.getInt(2);
                int maxResults = data.getInt(3);
                if (data.isNull(4)){
                    mHelper.getGamesClient().loadTopScores(this, leaderboardId, span, leaderboardCollection, maxResults);
                }else{
                    boolean forceReload = data.getBoolean(4);
                    mHelper.getGamesClient().loadTopScores(this, leaderboardId, span, leaderboardCollection, maxResults, forceReload);
                }
                callbackContext.success();
            }else if (ACTION_GAME_REVEAL_ACHIEVEMENT.equals(action)){
                String id = data.getString(0);
                mHelper.getGamesClient().revealAchievement(id);
                callbackContext.success();
            }else if (ACTION_GAME_REVEAL_ACHIEVEMENT_NOW.equals(action)){
                String id = data.getString(0);
                mHelper.getGamesClient().revealAchievementImmediate(this, id);
                callbackContext.success();
            }else if (ACTION_GAME_SUBMIT_SCORE.equals(action)){
                String leaderboardId = data.getString(0);
                int score = data.getInt(1);
                mHelper.getGamesClient().submitScore(leaderboardId, score);
                callbackContext.success();
            }else if (ACTION_GAME_SUBMIT_SCORE_NOW.equals(action)){
                String leaderboardId = data.getString(0);
                int score = data.getInt(1);
                mHelper.getGamesClient().submitScoreImmediate(this, leaderboardId, score);
                callbackContext.success();
            }else if (ACTION_GAME_UNLOCK_ACHIEVEMENT.equals(action)){
                String id = data.getString(0);
                mHelper.getGamesClient().unlockAchievement(id);
                callbackContext.success();
            }else if (ACTION_GAME_UNLOCK_ACHIEVEMENT_NOW.equals(action)){
                String id = data.getString(0);
                mHelper.getGamesClient().unlockAchievementImmediate(this, id);
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
            json.put("type", GMS_SIGNIN);
            json.put("signin", true);
        }catch(JSONException ex){
            Log.e(TAG, "signin succeeded exception: "+ex.getMessage());
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
            json.put("type", GMS_SIGNIN);
            json.put("signin", false);
            json.put("message", reason);
        }catch(JSONException ex){
            Log.e(TAG, "signin failed exception: "+ex.getMessage());
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
    }

    @Override           
    public void onStateLoaded(int statusCode, int stateKey, byte[] localData) {
        JSONObject json = new JSONObject();
        try{
            json.put("type", STATE_LOADED);
            json.put("status", statusCode);
            switch (statusCode) {
                case AppStateClient.STATUS_OK:
                    // Data was successfully loaded from the cloud: merge with local data.
                    json.put("stateKey", stateKey);
                    json.put("data", new String(localData));
                    break;        
                case AppStateClient.STATUS_STATE_KEY_NOT_FOUND:
                    // key not found means there is no saved data. To us, this is the same as
                    // having empty data, so we treat this as a success.
                    break;
                case AppStateClient.STATUS_STATE_KEY_LIMIT_EXCEEDED:
                    // if the application already has data present in the maximum number of state keys.
                    break;
                case AppStateClient.STATUS_NETWORK_ERROR_NO_DATA:
                    // can't reach cloud, and we have no local state. Warn user that
                    // they may not see their existing progress, but any new progress won't be lost.
                    break;
                case AppStateClient.STATUS_NETWORK_ERROR_STALE_DATA:
                    // can't reach cloud, but we have locally cached data.
                    break;
                case AppStateClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect AppStateClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case AppStateClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "STATE_LOADED ["+statusCode+"] ["+stateKey+"] exception: "+ex.getMessage());
            return;
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onStateConflict(int stateKey, String resolvedVersion, byte[] localData, byte[] serverData) {
        // Need to resolve conflict between the two states.
        // We do that by taking the union of the two sets of cleared levels,
        // which means preserving the maximum star rating of each cleared
        // level:
        JSONObject json = new JSONObject();
        try{
            json.put("type", STATE_CONFLICTED);
            json.put("stateKey", stateKey);
            json.put("version", resolvedVersion);
            json.put("localData", new JSONObject(new String(localData)));
            json.put("serverData", new JSONObject(new String(serverData)));
        }catch(JSONException ex){
            Log.e(TAG, "STATE_CONFLICTED ["+stateKey+"] ["+resolvedVersion+"] exception: "+ex.getMessage());
            return;
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onStateListLoaded(int statusCode, AppStateBuffer buffer) {
        JSONObject json = new JSONObject();
        try{
            json.put("type", STATE_LIST_LOADED);
            json.put("status", statusCode);
            switch (statusCode) {
                case AppStateClient.STATUS_OK:
                    JSONArray jsonStates = new JSONArray();
                    json.put("states", jsonStates);
                    AppState state;
                    JSONObject jsonState;
                    for(int i=0,l=buffer.getCount();i<l;i++){
                        state = buffer.get(i);
                        jsonState = new JSONObject();
                        if (state.hasConflict()){
                            jsonState.put("type", STATE_CONFLICTED);
                            jsonState.put("stateKey", state.getKey());
                            jsonState.put("version", state.getConflictVersion());
                            jsonState.put("localVersion", state.getLocalVersion());
                            jsonState.put("localData", new JSONObject(new String(state.getLocalData())));
                            jsonState.put("serverData", new JSONObject(new String(state.getConflictData())));
                        }else{
                            jsonState.put("type", STATE_LOADED);
                            jsonState.put("status", statusCode);
                            jsonState.put("stateKey", state.getKey());
                            jsonState.put("data", new JSONObject(new String(state.getLocalData())));
                        }
                        jsonStates.put(jsonState);
                    }
                    // Data was successfully loaded from the cloud: merge with local data.
                    break;        
                case AppStateClient.STATUS_NETWORK_ERROR_NO_DATA:
                    // can't reach cloud, and we have no local state. Warn user that
                    // they may not see their existing progress, but any new progress won't be lost.
                    break;
                case AppStateClient.STATUS_NETWORK_ERROR_STALE_DATA:
                    // can't reach cloud, but we have locally cached data.
                    break;
                case AppStateClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect AppStateClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case AppStateClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "STATE_LIST_LOADED ["+statusCode+"]["+buffer.getCount()+"] exception: "+ex.getMessage());
            return;
        }
        buffer.close();

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onStateDeleted(int statusCode, int stateKey){
        JSONObject json = new JSONObject();
        try{
            json.put("type", STATE_CONFLICTED);
            json.put("statusCode", statusCode);
            json.put("stateKey", stateKey);
            switch (statusCode) {
                case AppStateClient.STATUS_OK:
                    // if data was successfully deleted from the server.
                    break;        
                case AppStateClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service 
                    break;
                case AppStateClient.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                    // if the device was unable to communicate with the network. In this case, the operation is not retried automatically.
                    break;
                case AppStateClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect AppStateClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "STATE_DELETED ["+statusCode+"] ["+stateKey+"] exception: "+ex.getMessage());
            return;
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onGamesLoaded (int statusCode, GameBuffer buffer){
        JSONObject json = new JSONObject();
        try{
            json.put("type", GAMES_LOADED);
            json.put("statusCode", statusCode);
            switch (statusCode) {
                case GamesClient.STATUS_OK:
                    // if data was successfully loaded and is up-to-date.
                    JSONArray games = new JSONArray();
                    JSONObject game;
                    for(int i=0,l=buffer.getCount(); i<l; i++){
                        Game g = buffer.get(i);
                        game = new JSONObject();
                        game.put("achievementTotalCount", g.getAchievementTotalCount());
                        game.put("applicationId", g.getApplicationId());
                        game.put("description", g.getDescription());
                        game.put("developerName", g.getDeveloperName());
                        game.put("displayName", g.getDisplayName());
                        Uri uri = g.getFeaturedImageUri();
                        if (null != uri)
                            game.put("featuredImageUri", uri.getScheme()+':'+uri.getSchemeSpecificPart());
                        uri = g.getHiResImageUri();
                        if (null != uri)
                            game.put("hiResImageUri", uri.getScheme()+':'+uri.getSchemeSpecificPart());
                        uri = g.getIconImageUri();
                        if (null != uri)
                            game.put("iconImageUri", uri.getScheme()+':'+uri.getSchemeSpecificPart());
                        game.put("leaderboardCount", g.getLeaderboardCount());
                        game.put("primaryCategory", g.getPrimaryCategory());
                        game.put("secondaryCategory", g.getSecondaryCategory());
                        games.put(game);
                    }
                    json.put("list", games);
                    break;        
                case GamesClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service 
                    break;
                case GamesClient.STATUS_NETWORK_ERROR_STALE_DATA:
                    // if the device was unable to communicate with the network. In this case, the operation is not retried automatically.
                    break;
                case GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect GamesClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case GamesClient.STATUS_LICENSE_CHECK_FAILED:
                    // The game is not licensed to the user. Further calls will return the same code.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "GAMES_LOADED ["+statusCode+"] exception: "+ex.getMessage());
            return;
        }

        buffer.close();
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onPlayersLoaded (int statusCode, PlayerBuffer buffer){
        JSONObject json = new JSONObject();
        try{
            json.put("type", PLAYER_LOADED);
            json.put("statusCode", statusCode);
            switch (statusCode) {
                case GamesClient.STATUS_OK:
                    // if data was successfully loaded and is up-to-date.
                    JSONArray players = new JSONArray();
                    JSONObject player;
                    for(int i=0,l=buffer.getCount(); i<l; i++){
                        Player p = buffer.get(i);
                        player = new JSONObject();
                        player.put("displayName", p.getDisplayName());
                        if (p.hasHiResImage()){
                            Uri uri = p.getHiResImageUri();
                            player.put("hiResImageUri", uri.getScheme()+':'+uri.getSchemeSpecificPart());
                        }
                        if (p.hasIconImage()){
                            Uri uri = p.getIconImageUri();
                            player.put("iconImageUri", uri.getScheme()+':'+uri.getSchemeSpecificPart());
                        }
                        player.put("playerId", p.getPlayerId());
                        player.put("retrievedTimestamp", p.getRetrievedTimestamp());
                        players.put(player);
                    }
                    json.put("list", players);
                    break;        
                case GamesClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service 
                    break;
                case GamesClient.STATUS_NETWORK_ERROR_STALE_DATA:
                    // if the device was unable to communicate with the network. In this case, the operation is not retried automatically.
                    break;
                case GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect GamesClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case GamesClient.STATUS_LICENSE_CHECK_FAILED:
                    // The game is not licensed to the user. Further calls will return the same code.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "PLAYER_LOADED ["+statusCode+"] exception: "+ex.getMessage());
            return;
        }

        buffer.close();
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onAchievementsLoaded(int statusCode, AchievementBuffer buffer){
        JSONObject json = new JSONObject();
        try{
            json.put("type", GAME_ACHIEVEMENT_LOADED);
            json.put("statusCode", statusCode);
            switch (statusCode) {
                case GamesClient.STATUS_OK:
                    // if data was successfully loaded and is up-to-date
                    JSONArray achievements = new JSONArray();
                    JSONObject achievement;
                    Achievement a;
                    for(int i=0,l=buffer.getCount(); i<l; i++){
                        a = buffer.get(i);
                        achievement = new JSONObject();
                        achievement.put("achievementId", a.getAchievementId());
                        achievement.put("description", a.getDescription());
                        achievement.put("lastUpdatedTimestamp", a.getLastUpdatedTimestamp());
                        achievement.put("name", a.getName());
                        achievement.put("achievementId", a.getPlayer().getPlayerId());
                        achievement.put("state", a.getState());
                        achievement.put("type", a.getType());
                        if (Achievement. TYPE_INCREMENTAL == a.getType()){
                            achievement.put("currentSteps", a.getCurrentSteps());
                            achievement.put("totalSteps", a.getTotalSteps());
                            achievement.put("formattedCurrentSteps", a.getFormattedCurrentSteps());
                            achievement.put("formattedTotalSteps", a.getFormattedTotalSteps());
                        }
                        Uri uri = a.getRevealedImageUri();
                        if (null != uri)
                            achievement.put("revealedImageUri", uri.getScheme()+':'+uri.getSchemeSpecificPart());
                        uri = a.getUnlockedImageUri();
                        if (null != uri)
                            achievement.put("unlockedImageUri", uri.getScheme()+':'+uri.getSchemeSpecificPart());
                        achievements.put(achievement);
                    }
                    json.put("list", achievements);
                    break;        
                case GamesClient.STATUS_NETWORK_ERROR_NO_DATA:
                    // A network error occurred while attempting to retrieve fresh data, and no data was available locally.
                    break;
                case GamesClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service 
                    break;
                case GamesClient.STATUS_NETWORK_ERROR_STALE_DATA:
                    // if the device was unable to communicate with the network. In this case, the operation is not retried automatically.
                    break;
                case GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect GamesClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case GamesClient.STATUS_LICENSE_CHECK_FAILED:
                    // The game is not licensed to the user. Further calls will return the same code.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "GAME_ACHIEVEMENT_LOADED ["+statusCode+"] exception: "+ex.getMessage());
            return;
        }

        buffer.close();
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onAchievementUpdated(int statusCode, String achievementId){
        JSONObject json = new JSONObject();
        try{
            json.put("type", GAME_ACHIEVEMENT_UPDATED);
            json.put("statusCode", statusCode);
            switch (statusCode) {
                case GamesClient.STATUS_OK:
                    // if data was successfully loaded and is up-to-date 
                    json.put("achievementId", achievementId);
                    break;        
                case GamesClient.STATUS_NETWORK_ERROR_NO_DATA:
                    // A network error occurred while attempting to retrieve fresh data, and no data was available locally.
                    break;
                case GamesClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service 
                    break;
                case GamesClient.STATUS_NETWORK_ERROR_STALE_DATA:
                    // if the device was unable to communicate with the network. In this case, the operation is not retried automatically.
                    break;
                case GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect GamesClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case GamesClient.STATUS_LICENSE_CHECK_FAILED:
                    // The game is not licensed to the user. Further calls will return the same code.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "GAME_ACHIEVEMENT_UPDATED ["+statusCode+"] exception: "+ex.getMessage());
            return;
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onLeaderboardMetadataLoaded (int statusCode, LeaderboardBuffer leaderboard){
        JSONObject json = new JSONObject();
        try{
            json.put("type", GAME_LEADERBOARD_METADATA_LOADED);
            json.put("statusCode", statusCode);
            switch (statusCode) {
                case GamesClient.STATUS_OK:
                    // if data was successfully loaded and is up-to-date.
                    JSONArray list = new JSONArray();
                    JSONObject obj;
                    JSONArray vList;
                    JSONObject v;
                    Leaderboard lb;
                    ArrayList<LeaderboardVariant> variants;
                    LeaderboardVariant variant;
                    int i, l, j, k;
                    for(i=0,l=leaderboard.getCount();i<l;i++){
                        obj = new JSONObject();
                        lb = leaderboard.get(i);
                        obj.put("displayName", lb.getDisplayName());
                        Uri uri = lb.getIconImageUri();
                        if (null != uri)
                            obj.put("iconImageUri", uri.getScheme() + ':' + uri.getSchemeSpecificPart());
                        obj.put("leaderboardId", lb.getLeaderboardId());
                        obj.put("scoreOrder", lb.getScoreOrder());
                        variants = lb.getVariants();
                        vList = new JSONArray();
                        for(j=0,k=variants.size();j<k;j++){
                            v = new JSONObject();
                            variant = variants.get(i);
                            v.put("collection", variant.getCollection());
                            v.put("numScores", variant.getNumScores());
                            v.put("timeSpan", variant.getTimeSpan());
                            v.put("hasPlayerInfo", variant.hasPlayerInfo());
                            if(variant.hasPlayerInfo()){
                                v.put("displayPlayerRank", variant.getDisplayPlayerRank());
                                v.put("displayPlayerScore", variant.getDisplayPlayerScore());
                                v.put("playerRank", variant.getPlayerRank());
                                v.put("rawPlayerScore", variant.getRawPlayerScore());
                            }
                            vList.put(v);
                            obj.put("variants", vList);
                        }
                        list.put(obj);
                    }
                    json.put("leaderboard", list);
                    break;        
                case GamesClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service 
                    break;
                case GamesClient.STATUS_NETWORK_ERROR_STALE_DATA:
                    // if the device was unable to communicate with the network. in this case, the operation is not retried automatically.
                    break;
                case GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect GamesClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case GamesClient.STATUS_LICENSE_CHECK_FAILED:
                    // the game is not licensed to the user. further calls will return the same code.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "game_leaderboard_scores_loaded ["+statusCode+"] exception: "+ex.getMessage());
            return;
        }

        leaderboard.close();
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onLeaderboardScoresLoaded (int statusCode, LeaderboardBuffer leaderboard, LeaderboardScoreBuffer scores){
        JSONObject json = new JSONObject();
        try{
            json.put("type", GAME_LEADERBOARD_SCORES_LOADED);
            json.put("statusCode", statusCode);
            switch (statusCode) {
                case GamesClient.STATUS_OK:
                    // if data was successfully loaded and is up-to-date.
                    JSONArray list = new JSONArray();
                    JSONObject obj;
                    JSONArray vList;
                    JSONObject v;
                    Leaderboard lb;
                    ArrayList<LeaderboardVariant> variants;
                    LeaderboardVariant variant;
                    int i, l, j, k;
                    for(i=0,l=leaderboard.getCount();i<l;i++){
                        obj = new JSONObject();
                        lb = leaderboard.get(i);
                        obj.put("displayName", lb.getDisplayName());
                        Uri uri = lb.getIconImageUri();
                        if (null != uri)
                            obj.put("iconImageUri", uri.getScheme() + ':' + uri.getSchemeSpecificPart());
                        obj.put("leaderboardId", lb.getLeaderboardId());
                        obj.put("scoreOrder", lb.getScoreOrder());
                        variants = lb.getVariants();
                        vList = new JSONArray();
                        for(j=0,k=variants.size();j<k;j++){
                            v = new JSONObject();
                            variant = variants.get(i);
                            v.put("collection", variant.getCollection());
                            v.put("numScores", variant.getNumScores());
                            v.put("timeSpan", variant.getTimeSpan());
                            v.put("hasPlayerInfo", variant.hasPlayerInfo());
                            if(variant.hasPlayerInfo()){
                                v.put("displayPlayerRank", variant.getDisplayPlayerRank());
                                v.put("displayPlayerScore", variant.getDisplayPlayerScore());
                                v.put("playerRank", variant.getPlayerRank());
                                v.put("rawPlayerScore", variant.getRawPlayerScore());
                            }
                            vList.put(v);
                            obj.put("variants", vList);
                        }
                        list.put(obj);
                    }
                    json.put("leaderboard", list);
                    LeaderboardScore lbs;
                    for(i=0,l=scores.getCount();i<l;i++){
                        obj = new JSONObject();
                        lbs = scores.get(i);
                        obj.put("displayRank", lbs.getDisplayRank());
                        obj.put("displayScore", lbs.getDisplayScore());
                        obj.put("rank", lbs.getRank());
                        obj.put("rawScore", lbs.getRawScore());
                        obj.put("scoreHolderPlayerId", lbs.getScoreHolder().getPlayerId());
                        obj.put("scoreHolderDisplayName", lbs.getScoreHolderDisplayName());
                        Uri uri = lbs.getScoreHolderHiResImageUri();
                        if (null != uri)
                            obj.put("scoreHolderHiResImageUri", uri.getScheme() + ':' + uri.getSchemeSpecificPart());
                        uri = lbs.getScoreHolderIconImageUri();
                        if (null != uri)
                            obj.put("scoreHolderIconImageUri", uri.getScheme() + ':' + uri.getSchemeSpecificPart());
                        obj.put("timestampMillis", lbs.getTimestampMillis());
                        list.put(obj);
                    }
                    json.put("scores", list);
                    break;        
                case GamesClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service 
                    break;
                case GamesClient.STATUS_NETWORK_ERROR_STALE_DATA:
                    // if the device was unable to communicate with the network. in this case, the operation is not retried automatically.
                    break;
                case GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect GamesClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case GamesClient.STATUS_LICENSE_CHECK_FAILED:
                    // the game is not licensed to the user. further calls will return the same code.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "game_leaderboard_scores_loaded ["+statusCode+"] exception: "+ex.getMessage());
            return;
        }

        leaderboard.close();
        scores.close();
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    @Override
    public void onScoreSubmitted (int statusCode, SubmitScoreResult result){
        JSONObject json = new JSONObject();
        try{
            json.put("type", GAME_SCORES_SUBMITTED);
            json.put("statusCode", statusCode);
            switch (statusCode) {
                case GamesClient.STATUS_OK:
                    // if data was successfully loaded and is up-to-date.
                    json.put("leaderboardId", result.getLeaderboardId());
                    json.put("playerId", result.getPlayerId());
                    json.put("resultStatusCode", result.getStatusCode());
                    SubmitScoreResult.Result timeResult = result.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME);
                    JSONObject r = new JSONObject();
                    r.put("formattedScore", timeResult.formattedScore);
                    r.put("newBest", timeResult.newBest);
                    r.put("rawScore", timeResult.rawScore);
                    json.put("timeResult", r);
                    timeResult = result.getScoreResult(LeaderboardVariant.TIME_SPAN_WEEKLY);
                    r = new JSONObject();
                    r.put("formattedScore", timeResult.formattedScore);
                    r.put("newBest", timeResult.newBest);
                    r.put("rawScore", timeResult.rawScore);
                    json.put("weekly", r);
                    timeResult = result.getScoreResult(LeaderboardVariant.TIME_SPAN_DAILY);
                    r = new JSONObject();
                    r.put("formattedScore", timeResult.formattedScore);
                    r.put("newBest", timeResult.newBest);
                    r.put("rawScore", timeResult.rawScore);
                    json.put("daily", r);
                    break;        
                case GamesClient.STATUS_INTERNAL_ERROR:
                    // if an unexpected error occurred in the service 
                    break;
                case GamesClient.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                    // if the device is offline or was otherwise unable to post the score to the server. The score was stored locally and will be posted to the server the next time the device is online and is able to perform a sync (no further action is required from the client).
                    break;
                case GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                    // need to reconnect GamesClient
                    mHelper.reconnectClients(clientTypes);
                    break;
                case GamesClient.STATUS_LICENSE_CHECK_FAILED:
                    // The game is not licensed to the user. Further calls will return the same code.
                    break;
                default:
                    // error
                    break;
            }
        }catch(JSONException ex){
            Log.e(TAG, "GAME_SCORES_SUBMITTED ["+statusCode+"] exception: "+ex.getMessage());
            return;
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        connectionCB.sendPluginResult(pluginResult);
    }

    private void setup(int clientTypes, String[] extraScopes, final CallbackContext callbackContext){
        mHelper = new GmsHelper(cordova.getActivity());
        connectionCB = callbackContext;

        Log.d(TAG, "Setup onActivityCallback to this");
        cordova.setActivityResultCallback(this);

        mHelper.enableDebugLog(DEBUG_ENABLED, TAG);

        mHelper.setup(this, clientTypes, extraScopes);
    }

    private void signout(){
        mHelper.signOut();
    }
};
