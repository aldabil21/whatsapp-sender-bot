package com.wabot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class Help {
    @FXML
    private TextArea message;

    public void initialize() {
        System.out.println("Help Initialize " + message.getText());
    }
}
