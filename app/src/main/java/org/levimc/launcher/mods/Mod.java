package org.levimc.launcher.mods;

public class Mod {
    private final String fileName;
    private boolean enabled;

    public Mod(String fileName, boolean enabled) {
        this.fileName = fileName;
        this.enabled = enabled;
    }

    public String getFileName() {
        return fileName;
    }
    public String getDisplayName() {
        return fileName.replace(".so", "");
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}