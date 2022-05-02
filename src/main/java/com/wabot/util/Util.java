package com.wabot.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    private static final String appDir = System.getProperty("java.app.dir");
    private static final boolean isProd = appDir != null;
    private static final String OS;
    private static final String folderName = "Whatsapp Bot";
    static {
        String os = System.getProperty("os.name").toLowerCase();
        OS = os.split(" ")[0];
    }

    public static String getAppDir() {
        if (isProd) {
            if (OS.equals("windows")) {
                return appDir;
            } else {
                List<String> dirList = new ArrayList<>(Arrays.asList(appDir.split("/")));
                Path productionPath = Paths.get("", dirList.toArray(new String[0]));
                return System.getProperty("file.separator") + productionPath;
            }
        }
        return "res";
    }

    public static String getDbDir() {
        if (isProd) {
            if (OS.equals("windows")) {
                String local = System.getenv("LocalAppData");
                return Paths.get(local, folderName).toString();
            } else {
                String home = System.getProperty("user.home");
                return Paths.get(home, "."+folderName).toString();
            }
        }
        return "res";
    }

    public static String getChromeDriverPath() {
        String appDir = getAppDir();
        String chromeDriver = ("chromedriver");
        if(OS.equals("windows")){
            chromeDriver  += (".exe");
        }

        if (isProd) {
            if (OS.equals("windows")) {
                return Paths.get(appDir, chromeDriver).toString();
            } else {
                return Paths.get(appDir, "lib", "app", chromeDriver).toString();
            }
        }

        return Paths.get(appDir, OS, chromeDriver).toString();
    }
}
