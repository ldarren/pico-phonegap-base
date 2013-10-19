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
    gms: {
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
        }
    }
};
