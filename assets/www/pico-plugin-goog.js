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
  }
};
