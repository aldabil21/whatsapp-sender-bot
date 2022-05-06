package com.wabot.jobs;

import com.wabot.components.UndecoratedAlert;
import com.wabot.tasks.QrCodeScan;
import com.wabot.tasks.SendTask;
import io.github.bonigarcia.wdm.WebDriverManager;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;

public class Sender {
    private static final Notifier notifier = new Notifier();
    private final ChromeOptions chromeOptions = new ChromeOptions();
    private ChromeDriver driver;

    public Sender() {
        chromeOptions.addArguments("no-sandbox", "disable-dev-shm-usage", "disable-extensions", "app=https://web.whatsapp.com", "start-maximized");
    }

    public void send(Types type, String message, File media, String labelName) {
        if (notifier.agreeToScanNotif()) {
            if (initDriver()) {
                notifier.waitingScanNotif();
                Task<Boolean> task = new QrCodeScan(driver, notifier);
                new Thread(task).start();
                task.setOnSucceeded(workerStateEvent -> {
                    notifier.closeNotif();
                    SendTask sendTask = new SendTask(driver, notifier, type, message, media, labelName);
                    notifier.onProcessNotif("جاري التحضير للإرسال", sendTask);
                    new Thread(sendTask).start();
                });
            }
        }
    }

    private boolean initDriver() {
        try {
            WebDriverManager.chromedriver().setup();
            // TODO: This is blocking...
            driver = new ChromeDriver(chromeOptions);
            return true;
        } catch (Exception e) {
            ButtonType btn = new ButtonType("إغلاق", ButtonBar.ButtonData.CANCEL_CLOSE);
            String msg = "خطأ في تحميل إعدادات المتصفح، يرجى التواصل مع الأدمن من خلال خيار المساعدة";
            Alert alert = new UndecoratedAlert(Alert.AlertType.ERROR, msg, btn);
            alert.setHeaderText("خطأ في تحميل المتصفح");
            alert.show();
            return false;
        }
    }

    public enum Types {CHAT_LIST, SAVED_LIST, LABELED_LIST}
}
