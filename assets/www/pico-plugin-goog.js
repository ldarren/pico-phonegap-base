var cordova = window.cordova || window.Cordova;
window.GOOG = {
  iabInit: function(apiKey, cb) {
      return cordova.exec(
          function(){
              cb();
          },
          function(err){
              cb(err);
          },
          'picoGoogleIAB', [apiKey]);
  }
};
