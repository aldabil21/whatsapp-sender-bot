package com.wabot.tasks;

import com.wabot.components.UndecoratedAlert;
import com.wabot.controller.Home;
import com.wabot.jobs.Notifier;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class QrCodeScan extends Task<Boolean> {
    private final ChromeDriver driver;
    private final Notifier notifier;

    public QrCodeScan(ChromeDriver driver, Notifier notifier) {
        this.driver = driver;
        this.notifier = notifier;
    }

    @Override
    protected Boolean call() {
        WebElement sidePane = null;
        while (sidePane == null) {
            try {
                // Check every 5 seconds if page is opened
                Thread.sleep(5000);
                // If side pane exists then its opened
                sidePane = driver.findElement(By.id("pane-side"));
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg.contains("chrome not reachable")) {
                    throw new RuntimeException(e);
                } else {
                    System.out.println("Not scanned yet");
                }
            }
        }
        return true;
    }

    @Override
    protected void succeeded() {
        driver.manage().window().maximize();
    }

    @Override
    protected void failed() {
        notifier.closeNotif();
        Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, "إذا لم تقم بإغلاق المتصفح بنفسك، تواصل مع الأدمن لحل المشكلة");
        alert.setHeaderText("المتصفح مفقود");
        alert.initOwner(Home.homeOverlayPane.getScene().getWindow());
        alert.show();
    }
}
