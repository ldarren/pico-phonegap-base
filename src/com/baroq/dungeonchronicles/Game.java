package com.baroq.dungeonchronicles;

import org.apache.cordova.*;
import android.os.Bundle;
import android.view.WindowManager;

public class Game extends DroidGap
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        super.loadUrl("file:///android_asset/www/index.html");
    }
}
