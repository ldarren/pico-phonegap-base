var cordova = window.cordova || window.Cordova;
window.gapi= {
    iab: {
        open: function(apiKey, cb) {
          cordova.exec(
              function(){ cb(); },
              function(err){ cb(err); },
              'InAppBilling', 'iabOpen', [apiKey]);
        },
        close: function(cb) {
          cordova.exec(
              function(){ if (cb) cb(); },
              function(err){ if (cb) cb(err); },
              'InAppBilling', 'iabClose', []);
        },
        inventory: function(skus, cb){
          cordova.exec(
              function(inventory){ if (cb) cb(null, inventory); },
              function(err){ if (cb) cb(err); },
              'InAppBilling', 'iabInventory', skus || []);
        },
        buy: function(sku, payload, cb){
          cordova.exec(
              function(inventory){ if (cb) cb(null, inventory); },
              function(err){ if (cb) cb(err); },
              'InAppBilling', 'iabBuy', [sku, payload]);
        },
        subscribe: function(sku, payload, cb){
          cordova.exec(
              function(inventory){ if (cb) cb(null, inventory); },
              function(err){ if (cb) cb(err); },
              'InAppBilling', 'iabSubscribe', [sku, payload]);
        },
        consumes: function(purchaseList, cb){
            var _c = function(inList, outList, index, cb){
                if (inList.length >= index) return cb(null, outList);
                cordova.exec(
                    function(out){ outList.push(out); _c(inList, outList, ++index, cb); },
                    function(err){ if (cb) cb(err, outList); },
                    'InAppBilling', 'iabConsume', [inList[index]]);
            };

            if (!purchaseList || !purchaseList.length){
                return cb(purchaseList);
            }else{
                _c(purchaseList, [], 0, cb);
            }
        }
    }
};
