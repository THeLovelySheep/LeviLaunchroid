package org.levimc.launcher.mods;

import android.content.Context;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModManager {
    private static final String CONFIG_NAME = "mods_config.json";
    private static ModManager instance;
    private final File modsDir;
    private final File configFile;
    private final Gson gson = new Gson();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Map<String, Boolean> configMap = new HashMap<>();
    private OnModsUpdateListener listener;

    public interface OnModsUpdateListener {
        void onModsUpdated(List<Mod> mods);
    }

    private ModManager(Context context) {
        modsDir = new File(
                Environment.getExternalStorageDirectory(),
                "games/org.levimc/mods"
        );
        configFile = new File(modsDir, CONFIG_NAME);
        initialize();
        startWatching();
    }

    private void initialize() {
        if (!modsDir.exists() && !modsDir.mkdirs()) {
            Log.e("ModManager", "Failed to create mods directory");
        }
        loadConfig();
    }

    public static synchronized ModManager getInstance(Context context) {
        if (instance == null) {
            instance = new ModManager(context);
        }
        return instance;
    }

    public void refreshMods() {
        new Thread(() -> {
            List<Mod> mods = new ArrayList<>();
            File[] files = modsDir.listFiles(file ->
                    file.isFile() && file.getName().endsWith(".so"));

            if (files != null) {
                syncConfig(files);
                for (File file : files) {
                    String name = file.getName();
                    boolean enabled = Boolean.TRUE.equals(configMap.getOrDefault(name, true));
                    mods.add(new Mod(name, enabled));
                }
            }
            notifyUpdate(mods);
        }).start();
    }

    private void syncConfig(File[] currentFiles) {
        Set<String> existingFiles = Arrays.stream(currentFiles)
                .map(File::getName)
                .collect(Collectors.toSet());

        configMap.keySet().removeIf(key ->
                !existingFiles.contains(key) && !key.equals(CONFIG_NAME));

        for (String fileName : existingFiles) {
            if (!configMap.containsKey(fileName)) {
                configMap.put(fileName, true);
                Log.d("ModManager", "发现新mod: " + fileName);
            }
        }
    }

    public void setModEnabled(String fileName, boolean enabled) {
        if (!fileName.endsWith(".so")) {
            fileName += ".so";
        }

        configMap.put(fileName, enabled);
        saveConfig();

        handler.postDelayed(this::refreshMods, 100);
    }

    private void loadConfig() {
        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<HashMap<String, Boolean>>(){}.getType();
            configMap = gson.fromJson(reader, type);
        } catch (Exception e) {
            configMap = new HashMap<>();
        }
    }

    private void saveConfig() {
        try {
            if (!modsDir.exists() && !modsDir.mkdirs()) {
                Log.e("ModManager", "无法创建mods目录");
                return;
            }

            File tempFile = new File(configFile.getAbsolutePath() + ".tmp");
            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(configMap, writer);
                writer.flush();

                if (tempFile.renameTo(configFile)) {
                    Log.d("ModManager", "配置保存成功");
                } else {
                    Log.e("ModManager", "文件重命名失败");
                }
            }
        } catch (Exception e) {
            Log.e("ModManager", "保存配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void startWatching() {
        FileObserver observer = new FileObserver(modsDir.getAbsolutePath(),
                FileObserver.CLOSE_WRITE |
                        FileObserver.MOVED_TO) {

            @Override
            public void onEvent(int event, String path) {
                if (path != null && (path.endsWith(".so") || path.equals(CONFIG_NAME))) {
                    handler.postDelayed(() -> {
                        refreshMods();
                        Log.d("FileObserver", "检测到文件变化: " + path);
                    }, 300);
                }
            }
        };
        observer.startWatching();
    }

    private void notifyUpdate(List<Mod> mods) {
        if (listener != null) {
            handler.post(() -> listener.onModsUpdated(mods));
        }
    }

    public void setOnModsUpdateListener(OnModsUpdateListener listener) {
        this.listener = listener;
    }
}