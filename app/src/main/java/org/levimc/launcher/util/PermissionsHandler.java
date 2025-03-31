package org.levimc.launcher.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import org.levimc.launcher.R;
import org.levimc.launcher.mods.ModManager;

public class PermissionsHandler {

    public static final int REQUEST_MANAGE_STORAGE = 1001;
    private final Activity activity;
    private final ModManager modManager;

    public PermissionsHandler(Activity activity, ModManager modManager) {
        this.activity = activity;
        this.modManager = modManager;
    }

    public boolean hasStoragePermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager();
    }

    public void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.storage_permission_title))
                    .setMessage(activity.getString(R.string.storage_permission_message))
                    .setPositiveButton(activity.getString(R.string.grant_permission), (d, w) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
                    })
                    .setNegativeButton(activity.getString(R.string.cancel), (d, w) -> {
                        activity.finish();
                    })
                    .create();

            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(activity, R.color.on_surface));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(activity, R.color.on_surface));
        }
    }

    public void handlePermissionResult(int requestCode) {
        if (requestCode == REQUEST_MANAGE_STORAGE && hasStoragePermission()) {
            modManager.refreshMods();
        } else {
            requestStoragePermission();
        }
    }
}