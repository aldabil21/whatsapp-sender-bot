package com.wabot.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    private static final String appDir = System.getProperty("java.app.dir");
    private static final boolean isProd = appDir != null;
    private static final String OS = (System.getProperty("os.name")).toUpperCase();


    public static String getAppDir() {
        if (isProd) {
            List<String> dirList = new ArrayList<>(Arrays.asList(appDir.split("/")));
            Path productionPath = Paths.get("", dirList.toArray(new String[0]));
            // TODO: Linux only?
            return System.getProperty("file.separator") + productionPath;
        }
        return "res";
    }

    public static String getDbDir() {
        if (isProd) {
            if (OS.contains("WIN")) {
                // TODO: Test on Windows
                return System.getenv("AppData");
            } else {
                return Paths.get(System.getProperty("user.home"), ".wabot").toString();
            }
        }
        return "res";
    }

    public static String getChromeDriverPath() {
        String appDir = getAppDir();
        if (isProd) {
            // TODO: Linux only?
            return Paths.get(appDir, "lib", "app", "chromedriver").toString();
        }
        return Paths.get(appDir, "chromedriver").toString();
    }
}
