pico-phonegap-base
==================

Base phonegap (2.9) application for pico framework

##Features
1. Facebook connect 3.5 phonegap plugin
2. Google In App Billing phonegap plugin
3. Google App State phonegap plugin
4. Google Leaderboard phonegap plugin
5. Google Achievement phonegap plugin

##Setup
This setup guide assume that you have an app in google play developer console and an app in facebook app developer console, if not please visit [Google Play Game Services](https://developers.google.com/games/services/console/enabling) and [Facebook Android App Guide](https://developers.facebook.com/docs/android/getting-started/)

1. git clone https://github.com/ldarren/pico-phonegap-base.git .
2. vi ant.properties
    * key.store=path/to/my.keystore
    * key.alias=mykeystore
3. vi res/values/ids.xml
    * change app_id, get it from [Google Play Developer Console](https://play.google.com) -> Game Services -> Select your game -> should show on title
4. vi src/com/baroq/pico/google/InAppBilling.java
    * change public key, get it from [Google Play Developer Console](https://play.google.com) -> All Applications -> Select your game -> Services & API
5. vi asset/www/index.html
    * change FB.init(APP_ID), get it from [Facebook Developer](https://developers.facebook.com)
