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
              'InAppBilling', 'inevntory', skus || []);
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
    games: {
        CLIENT_NONE: 0x00,
        CLIENT_GAMES: 0x01,
        CLIENT_PLUS: 0x02,
        CLIENT_APPSTATE: 0x04,
        CLIENT_ALL: 0x07,
        setup: function(clientCode, extraScopes, listener){
            var params = extraScopes || [];
            params.unshift(clientCode);
            cordova.exec(
                function(result){ listener(null, result); },
                function(err){ listener(err); },
                'PlayServices', 'setup', params);
        },
        signin: function(){
            cordova.exec(
                function(){},
                function(){},
                'PlayServices', 'signin', []);
        },
        signout: function(){
            cordova.exec(
                function(){},
                function(){},
                'PlayServices', 'signout', []);
        },
        incrementAchievement: function(id, steps, cb){
            if (cb){
                cordova.exec(
                    function(){ cb(); },
                    function(err){ cb(err); },
                    'PlayServices', 'incrementAchievementImmediate', [id, steps]);
            }else{
                cordova.exec(
                    function(){},
                    function(){},
                    'PlayServices', 'incrementAchievement', [id, steps]);
            }
        },
        loadAchievements: function(forceReload, cb){
            cordova.exec(
                function(list){ cb(null, list); },
                function(err){ cb(err); },
                'PlayServices', 'loadAchievements:', [forceReload]);
        },
        revealAchievement: function(id, cb){
            if (cb){
                cordova.exec(
                    function(){ cb(); },
                    function(err){ cb(err); },
                    'PlayServices', 'revealAchievementImmediate', [id]);
            }else{
                cordova.exec(
                    function(){},
                    function(err){},
                    'PlayServices', 'revealAchievement', [id]);
            }
        },
        unlockAchievement: function(id, cb){
            if (cb){
                cordova.exec(
                    function(){ cb(); },
                    function(err){ cb(err); },
                    'PlayServices', 'unlockAchievementImmediate', [id]);
            }else{
                cordova.exec(
                    function(){},
                    function(err){},
                    'PlayServices', 'unlockAchievement', [id]);
            }
        },
        loadLeaderboardMetadata: function(leaderboardId, forceReload, cb){
            cordova.exec(
                function(){ cb(); },
                function(err){ cb(err); },
                'PlayServices', 'loadLeaderboardMetadata', [leaderboardId, forceReload]);
        },
        loadMoreScores: function(pos, max, dir, cb){
            cordova.exec(
                function(){ cb(); },
                function(err){ cb(err); },
                'PlayServices', 'loadMoreScores', [pos, max, dir]);
        },
        loadPlayerCenteredScores: function(leaderboardId, span, leaderboardCollection, maxResults, forceReload, cb){
            cordova.exec(
                function(){ cb(); },
                function(err){ cb(err); },
                'PlayServices', 'loadPlayerCenteredScores', [leaderboardId, span, leaderboardCollection, maxResults, forceReload]);
        },
        loadTopScores: function(leaderboardId, span, leaderboardCollection, maxResults, forceReload, cb){
            cordova.exec(
                function(){ cb(); },
                function(err){ cb(err); },
                'PlayServices', 'loadTopScores', [leaderboardId, span, leaderboardCollection, maxResults, forceReload]);
        },
        submitScore: function(id, score, cb){
            if (cb){
                cordova.exec(
                    function(){ cb(); },
                    function(err){ cb(err); },
                    'PlayServices', 'submitScoreImmediate', [id, score]);
            }else{
                cordova.exec(
                    function(){},
                    function(err){},
                    'PlayServices', 'unlockAchievement', [id]);
            }
        }
    },
    appState:{
        getMaxNumKeys: function(cb){
            cordova.exec(
                function(size){ cb(null, size); },
                function(err){ cb(err); },
                'PlayServices', 'getMaxNumKeys', []);
        },
        getMaxStateSize: function(cb){
            cordova.exec(
                function(size){ cb(null, size); },
                function(err){ cb(err); },
                'PlayServices', 'getMaxStateSize', []);
        },
        listStates: function(cb){
            cordova.exec(
                function(states){ cb(null, states); },
                function(err){ cb(err); },
                'PlayServices', 'listStates', []);
        },
        loadState: function(stateKey, cb){
            cordova.exec(
                function(state){ cb(null, state); },
                function(err){ cb(err); },
                'PlayServices', 'loadState', [stateKey]);
        },
        loadState: function(stateKey, cb){
            cordova.exec(
                function(state){ cb(null, state); },
                function(err){ cb(err); },
                'PlayServices', 'loadState', [stateKey]);
        },
        resolveState: function(stateKey, resolvedData, cb){
            cordova.exec(
                function(state){ cb(null, state); },
                function(err){ cb(err); },
                'PlayServices', 'resolveState', [stateKey, resolvedData]);
        },
        updateState: function(stateKey, data, cb){
            cordova.exec(
                function(state){ cb(null, state); },
                function(err){ cb(err); },
                'PlayServices', 'updateState', [stateKey, data]);
        }
    }
};
