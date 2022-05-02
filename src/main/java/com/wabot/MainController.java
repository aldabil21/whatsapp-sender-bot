package com.wabot;

import com.wabot.model.Job;
import com.wabot.services.GetJobs;
import io.github.bonigarcia.wdm.WebDriverManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;


public class MainController {
    @FXML
    private Button groupMsgBtn;
    @FXML
    private Button excelMsgBtn;
    @FXML
    private Button exportNumBtn;
    @FXML
    private Button jobsBtn;

    @FXML
    private GridPane groupPane;
    @FXML
    private GridPane excelPane;
    @FXML
    private GridPane exportPane;
    @FXML
    private GridPane jobsPane;
    @FXML
    private Label dbTest;


    @FXML
    public void testJob() {

        // test
        GetJobs service = new GetJobs();
        service.start();

        service.setOnSucceeded(workerStateEvent -> {
            StringBuilder res = new StringBuilder();
            for (Job job : service.getValue()) {
                res.append(job.getId()).append("-").append(job.getDate());
            }
            dbTest.setText(dbTest.getText() + ": " + res);
        });

        Platform.runLater(() -> {
            new Thread(() -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("no-sandbox", "disable-dev-shm-usage", "disable-extensions");
                WebDriver driver = new ChromeDriver(options);
                driver.get("https://google.com");
                List<WebElement> element = driver.findElements(By.linkText("Gmail"));
                element.get(0).click();
                driver.navigate().back();
                List<WebElement> input = driver.findElements(By.tagName("input"));
                input.get(0).sendKeys("Is this working?");
            }).start();

        });
    }

    public void initialize() {
        menuListeners();
    }

    private void menuListeners() {
        groupMsgBtn.setOnAction(actionEvent -> switchMenuPane(groupPane));
        excelMsgBtn.setOnAction(actionEvent -> switchMenuPane(excelPane));
        exportNumBtn.setOnAction(actionEvent -> switchMenuPane(exportPane));
        jobsBtn.setOnAction(actionEvent -> switchMenuPane(jobsPane));
    }

    private void switchMenuPane(Pane pane) {
        groupPane.setVisible(false);
        excelPane.setVisible(false);
        exportPane.setVisible(false);
        jobsPane.setVisible(false);
        pane.setVisible(true);
    }

}

