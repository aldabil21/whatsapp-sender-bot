package com.wabot.util;

import java.nio.file.Paths;

public class Util {
    private static final String appDir = System.getProperty("java.app.dir");
    private static final boolean isProd = appDir != null;
    private static final String OS;
    private static final String folderName = "Whatsapp Bot";

    static {
        String os = System.getProperty("os.name").toLowerCase();
        OS = os.split(" ")[0].toLowerCase();
    }

    public static String getAppDir() {
        if (isProd) {
            return appDir;
        }
        return "";
    }

    public static String getDbDir() {
        if (isProd) {
            if (OS.equals("windows")) {
                String local = System.getenv("LocalAppData");
                return Paths.get(local, folderName).toString();
            } else {
                String home = System.getProperty("user.home");
                String cleanFolder = folderName.toLowerCase().replace(" ", "_");
                return Paths.get(home, "." + cleanFolder).toString();
            }
        }
        return "";
    }
}
