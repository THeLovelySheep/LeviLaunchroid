package org.levimc.launcher.util;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

public class UIHelper {

    public static void setTransparentNavigationBar(Activity activity) {
        Window window = activity.getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}