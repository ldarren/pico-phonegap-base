var cordova = window.cordova || window.Cordova;
window.GOOG = {
    iabOpen: function(apiKey, cb) {
      cordova.exec(
          function(){ cb(); },
          function(err){ cb(err); },
          'InAppBilling', 'iabOpen', [apiKey]);
    },
    iabClose: function(cb) {
      cordova.exec(
          function(){ if (cb) cb(); },
          function(err){ if (cb) cb(err); },
          'InAppBilling', 'iabClose', []);
    },
    iabInventory: function(skus, cb){
      cordova.exec(
          function(inventory){ if (cb) cb(null, inventory); },
          function(err){ if (cb) cb(err); },
          'InAppBilling', 'iabInventory', skus || []);
    },
    iabBuy: function(sku, payload, cb){
      cordova.exec(
          function(inventory){ if (cb) cb(null, inventory); },
          function(err){ if (cb) cb(err); },
          'InAppBilling', 'iabBuy', [sku, payload]);
    },
    iabSubscribe: function(sku, payload, cb){
      cordova.exec(
          function(inventory){ if (cb) cb(null, inventory); },
          function(err){ if (cb) cb(err); },
          'InAppBilling', 'iabSubscribe', [sku, payload]);
    },
    iabConsumes: function(purchaseList, cb){
        var consume = function(inList, outList, index, cb){
            if (inList.length >= index) return cb(null, outList);
            cordova.exec(
                function(out){ outList.push(out); consume(inList, outList, ++index, cb); },
                function(err){ if (cb) cb(err, outList); },
                'InAppBilling', 'iabConsume', [inList[index]]);
        };

        if (!purchaseList || !purchaseList.length){
            return cb(purchaseList);
        }else{
            consume(purchaseList, [], 0, cb);
        }
    }
};
