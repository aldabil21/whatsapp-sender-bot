package com.wabot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ExportNumbers {
    @FXML
    private TextArea message;

    public void initialize() {
        System.out.println("ExportNumbers Initialize " + message.getText());
    }
}
