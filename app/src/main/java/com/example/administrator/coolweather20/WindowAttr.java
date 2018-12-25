package com.example.administrator.coolweather20;

import android.app.Activity;

/**
 * Created by Administrator on 2018/2/18.
 */

public class WindowAttr {
    public static int getStateBarHeight(Activity a) {
        int result = 0;
        int resourceId = a.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = a.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
