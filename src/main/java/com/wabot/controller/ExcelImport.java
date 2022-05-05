package com.wabot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ExcelImport {
    @FXML
    private TextArea message;

    public void initialize() {
        System.out.println("ExcelImport Initialize " + message.getText());
    }
}
