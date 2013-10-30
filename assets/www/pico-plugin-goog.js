var cordova = window.cordova || window.Cordova;
window.GOOG = {
    iab: {
        open: function(apiKey, cb) {
          cordova.exec(
              function(){ cb(); },
              function(err){ cb(err); },
              'InAppBilling', 'open', [apiKey]);
        },
        close: function(cb) {
          cordova.exec(
              function(){ if (cb) cb(); },
              function(err){ if (cb) cb(err); },
              'InAppBilling', 'close', []);
        },
        inventory: function(skus, cb){
          cordova.exec(
              function(inv){ if (cb) cb(null, inv); },
              function(err){ if (cb) cb(err); },
              'InAppBilling', 'inventory', skus || []);
        },
        buy: function(sku, payload, cb){
          cordova.exec(
              function(inv){ if (cb) cb(null, inv); },
              function(err){ if (cb) cb(err); },
              'InAppBilling', 'buy', [sku, payload]);
        },
        subscribe: function(sku, payload, cb){
          cordova.exec(
              function(inv){ if (cb) cb(null, inv); },
              function(err){ if (cb) cb(err); },
              'InAppBilling', 'subscribe', [sku, payload]);
        },
        consumes: function(purchaseList, cb){
            var _c = function(inList, outList, index, cb){
                if (inList.length >= index) return cb(null, outList);
                cordova.exec(
                    function(out){ outList.push(out); _c(inList, outList, ++index, cb); },
                    function(err){ if (cb) cb(err, outList); },
                    'InAppBilling', 'consume', [inList[index]]);
            };

            if (!purchaseList || !purchaseList.length){
                return cb(purchaseList);
            }else{
                _c(purchaseList, [], 0, cb);
            }
        }
    },
    gms: {
        CLIENT_NONE: 0x00,
        CLIENT_GAMES: 0x01,
        CLIENT_PLUS: 0x02,
        CLIENT_APPSTATE: 0x04,
        CLIENT_ALL: 0x07,

        GMS_SIGNIN: 1,
        STATE_LOADED: 2,
        STATE_LIST_LOADED: 3,
        STATE_CONFLICTED: 4,
        STATE_DELETED: 5,
        GAMES_LOADED: 6,
        PLAYER_LOADED: 7,
        GAME_ACHIEVEMENT_LOADED: 8,
        GAME_ACHIEVEMENT_UPDATED: 9,
        GAME_LEADERBOARD_METADATA_LOADED: 10,
        GAME_LEADERBOARD_SCORES_LOADED: 11,
        GAME_SCORES_SUBMITTED: 12,

        setup: function(clientCode, extraScopes, listener){
            var params = extraScopes || [];
            params.unshift(clientCode);
            cordova.exec(
                function(result){ listener(null, result); },
                function(err){ listener(err); },
                'PlayServices', 'setup', params);
        },
        signin: function(){
            cordova.exec(function(){},function(){},'PlayServices', 'signin', []);
        },
        signout: function(){
            cordova.exec(function(){},function(){},'PlayServices', 'signout', []);
        }
    },
    games: {
        // for achievement
        STATE_HIDDEN: 2,
        STATE_REVEALED: 1,
        STATE_UNLOCKED: 0,
        TYPE_INCREMENTAL: 1,
        TYPE_STANDARD: 0,
        // leaderboard
        COLLECTION_PUBLIC: 0,
        COLLECTION_SOCIAL: 1,
        NUM_SCORES_UNKNOWN: -1,
        NUM_TIME_SPANS: 3,
        PLAYER_RANK_UNKNOWN: -1,
        PLAYER_SCORE_UNKNOWN: -1,
        TIME_SPAN_ALL_TIME: 2,
        TIME_SPAN_WEEKLY: 1,
        TIME_SPAN_DAILY: 0,
        // errors
        STATUS_ACHIEVEMENT_NOT_INCREMENTAL: 3002,
        STATUS_ACHIEVEMENT_UNKNOWN: 3001,
        STATUS_ACHIEVEMENT_UNLOCKED: 3003,
        STATUS_ACHIEVEMENT_UNLOCK_FAILURE: 3000,
        STATUS_CLIENT_RECONNECT_REQUIRED: 2,
        STATUS_INTERNAL_ERROR: 1,
        STATUS_INVALID_REAL_TIME_ROOM_ID: 7002,
        STATUS_LICENSE_CHECK_FAILED: 7,
        STATUS_NETWORK_ERROR_NO_DATA: 4,
        STATUS_NETWORK_ERROR_OPERATION_DEFERRED: 5,
        STATUS_NETWORK_ERROR_OPERATION_FAILED: 6,
        STATUS_NETWORK_ERROR_STALE_DATA: 3,
        STATUS_OK: 0,
        getAchievementsIntent: function(){
            cordova.exec(function(){},function(){},'PlayServices', 'getAchievementsIntent', []);
        },
        getAllLeaderboardsIntent: function(){
            cordova.exec(function(){},function(){},'PlayServices', 'getAllLeaderboardsIntent', []);
        },
        getLeaderboardIntent: function(id){
            cordova.exec(function(){},function(){},'PlayServices', 'getLeaderboardIntent', [id]);
        },
        incrementAchievement: function(id, numSteps){
            cordova.exec(function(){},function(){},'PlayServices', 'incrementAchievement', [id, numSteps]);
        },
        incrementAchievementImmediate: function(id, numSteps){
            cordova.exec(function(){},function(){},'PlayServices', 'incrementAchievementImmediate', [id, numSteps]);
        },
        loadAchievements: function(forceReload){
            cordova.exec(function(){},function(){},'PlayServices', 'loadAchievements', [forceReload]);
        },
        loadGame: function(){
            cordova.exec(function(){},function(){},'PlayServices', 'loadGame', []);
        },
        loadMoreScores: function(maxResults, pageDirection){
            cordova.exec(function(){},function(){},'PlayServices', 'loadMoreScores', [maxResults, pageDirection]);
        },
        loadPlayer: function(playerId){
            cordova.exec(function(){},function(){},'PlayServices', 'loadPlayer', [playerId]);
        },
        loadLeaderboardMetadata: function(){
            cordova.exec(function(){},function(){},'PlayServices', 'loadLeaderboardMetadata', Array.prototype.slice.call(arguments));
        },
        loadPlayerCenteredScores: function(leaderboardId, span, leaderboardCollection, maxResults, forceReload){
            cordova.exec(function(){},function(err){},'PlayServices', 'loadPlayerCenteredScores', Array.prototype.slice.call(arguments));
        },
        loadTopScores: function(leaderboardId, span, leaderboardCollection, maxResults, forceReload){
            cordova.exec(function(){},function(err){},'PlayServices', 'loadTopScores', Array.prototype.slice.call(arguments));
        },
        revealAchievement: function(id){
            cordova.exec(function(){},function(err){},'PlayServices', 'revealAchievement', [id]);
        },
        revealAchievementImmediate: function(id){
            cordova.exec(function(){},function(err){},'PlayServices', 'revealAchievementImmediate', [id]);
        },
        submitScore: function(leaderboardId, score){
            cordova.exec(function(){},function(err){},'PlayServices', 'submitScore', [leaderboardId, score]);
        },
        submitScoreImmediate: function(leaderboardId, score){
            cordova.exec(function(){},function(err){},'PlayServices', 'submitScoreImmediate', [leaderboardId, score]);
        },
        unlockAchievement: function(id){
            cordova.exec(function(){},function(err){},'PlayServices', 'unlockAchievement', [id]);
        },
        unlockAchievementImmediate: function(id){
            cordova.exec(function(){},function(err){},'PlayServices', 'unlockAchievementImmediate', [id]);
        }
    },
    appState:{
        STATUS_CLIENT_RECONNECT_REQUIRED: 2,
        STATUS_DEVELOPER_ERROR: 7,
        STATUS_INTERNAL_ERROR: 1,
        STATUS_NETWORK_ERROR_NO_DATA: 4,
        STATUS_NETWORK_ERROR_OPERATION_DEFERRED: 5,
        STATUS_NETWORK_ERROR_OPERATION_FAILED: 6,
        STATUS_NETWORK_ERROR_STALE_DATA: 3,
        STATUS_OK: 0,
        STATUS_STATE_KEY_LIMIT_EXCEEDED: 2003,
        STATUS_STATE_KEY_NOT_FOUND: 2002,
        STATUS_WRITE_OUT_OF_DATE_VERSION: 2000,
        STATUS_WRITE_SIZE_EXCEEDED: 2001,

        deleteState: function(stateKey){
            cordova.exec(function(){},function(){},'PlayServices', 'deleteState', [stateKey]);
        },
        getMaxNumKeys: function(cb){
            cordova.exec(
                function(size){ cb(null, size); },
                function(err){ cb(err); },
                'PlayServices', 'getMaxNumKeys', []);
        },
        getMaxStateSize: function(cb){
            cordova.exec(function(size){ cb(null, size); },
                function(err){ cb(err); },
                'PlayServices', 'getMaxStateSize', []);
        },
        listStates: function(){
            cordova.exec(function(){},function(){},'PlayServices', 'listStates', []);
        },
        loadState: function(stateKey){
            cordova.exec(function(){},function(){},'PlayServices', 'loadState', [stateKey]);
        },
        resolveState: function(stateKey, resolvedVersion, resolvedData){
            cordova.exec(function(){},function(){},'PlayServices', 'resolveState', [stateKey, resolvedVersion, resolvedData]);
        },
        updateState: function(stateKey, data){
            cordova.exec(function(){},function(){},'PlayServices', 'updateState', [stateKey, data]);
        },
        updateStateImmediate: function(stateKey, data){
            cordova.exec(function(){},function(){},'PlayServices', 'updateStateImmediate', [stateKey, data]);
        }
    }
};
