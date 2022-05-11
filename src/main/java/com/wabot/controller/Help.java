package com.wabot.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Help {
    @FXML
    private Button websiteBtn;
    @FXML
    private Button twitterBtn;


    public void initialize() {
        websiteBtn.setOnAction(event -> {
            goToWebPage("https://aldabil.me");
        });
        twitterBtn.setOnAction(event -> {
            goToWebPage("https://twitter.com/aldabil21");
        });
    }

    private void goToWebPage(String url) {
        Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException e) {
                    System.out.println(e.getMessage());
                    throw new RuntimeException(e);
                }
            }).start();
        });
    }
}
