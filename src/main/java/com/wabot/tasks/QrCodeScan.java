package com.wabot.tasks;

import com.wabot.components.UndecoratedAlert;
import com.wabot.controller.Home;
import com.wabot.jobs.Notifier;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

public class QrCodeScan extends Task<ChromeDriver> {
    private final Notifier notifier;
    private ChromeDriver driver;
    private static final ChromeOptions chromeOptions = new ChromeOptions();

    static {
        chromeOptions.addArguments("no-sandbox", "disable-dev-shm-usage", "disable-extensions",
                "app=https://web.whatsapp.com", "start-maximized");
    }

    public QrCodeScan(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    protected ChromeDriver call() {
        StringBuilder sb = new StringBuilder("فتح المتصفح لأول مرة قد يستغرق");
        sb.append(System.lineSeparator()).append("2-3 دقائق يرجى الإنتظار");
        updateMessage(sb.toString());
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(chromeOptions);
        updateMessage("");

        // 180 seconds... too much? Donno downloading time...
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(180));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pane-side")));

        return driver;
    }

    @Override
    protected void succeeded() {
        driver.manage().window().maximize();
    }

    @Override
    protected void failed() {
        if (driver != null) {
            driver.quit();
        }
        notifier.closeNotif();
        Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING,
                "إذا لم تقم بإغلاق المتصفح بنفسك، تواصل مع الأدمن لحل المشكلة");
        alert.setHeaderText("المتصفح مفقود");
        alert.initOwner(Home.homeOverlayPane.getScene().getWindow());
        alert.show();
    }
}
