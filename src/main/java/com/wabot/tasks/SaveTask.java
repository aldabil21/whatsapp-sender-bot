package com.wabot.tasks;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Notifier;
import com.wabot.model.ExcelRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SaveTask extends Task<ObservableList<ExcelRow>> {
    private final ChromeDriver driver;
    private final Notifier notifier;

    public SaveTask(ChromeDriver driver, Notifier notifier) {
        this.driver = driver;
        this.notifier = notifier;
    }

    @Override
    protected ObservableList<ExcelRow> call() {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]")));
        WebElement list = driver.findElement(By.id("pane-side"));

        long scrollHeight = getElementScrollHeight(list);
        long sidePaneHeight = getElementClientHeight(list);
        long currentHeight = 0;

        List<ExcelRow> rows = new ArrayList<>();

        OUTER:
        do {
            try {
                List<WebElement> names = list.findElements(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]"));
                for (WebElement name : names) {
                    if (isCancelled()) {
                        break OUTER;
                    }

                    String cleanString = name.getText().replaceAll("[+ ]", "");
                    try {
                        long number = Long.parseLong(cleanString);
                        ExcelRow row = new ExcelRow(rows.size() + 1, String.valueOf(number), "");
                        if (!rows.contains(row)) {
                            rows.add(row);
                            updateMessage("تم العثور على " + rows.size() + " رقم ");
                        }
                    } catch (Exception e) {
                        //System.out.println("A saved number, omit.");
                    }
                }
                currentHeight += sidePaneHeight * 0.8;
                scrollTopPaneTo(list, currentHeight);
                Thread.sleep(500);
            } catch (Exception e) {
                String msg = e.getMessage();
                if (e instanceof WebDriverException) {
                    throw new RuntimeException(e);
                } else {
                    System.out.println("Receivers: " + msg);
                }
            }
        } while (currentHeight < scrollHeight);


        return FXCollections.observableArrayList(rows);
    }

    private long getElementScrollHeight(WebElement el) {
        return (long) ((JavascriptExecutor) driver).executeScript("return arguments[0].scrollHeight", el);
    }

    private long getElementClientHeight(WebElement el) {
        return (long) ((JavascriptExecutor) driver).executeScript("return arguments[0].clientHeight", el);
    }

    private void scrollTopPaneTo(WebElement element, long to) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTo(0, arguments[1])", element, to);
    }


    @Override
    protected void succeeded() {
        this.notifier.closeNotif();
        this.driver.quit();
    }

    @Override
    protected void failed() {
        this.notifier.closeNotif();
        this.driver.quit();
        Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, getException().getMessage());
        alert.setHeaderText("حدث خطأ ما");
        alert.show();
    }

    @Override
    protected void cancelled() {
        this.notifier.closeNotif();
        this.driver.quit();
    }
}
