package com.wabot.controller;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Sender;
import com.wabot.model.ExcelRow;
import com.wabot.services.LoadExcel;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;

public class ExcelImport {
    private final FileChooser fileChooser = new FileChooser();
    private final LoadExcel loadExcelService = new LoadExcel();
    @FXML
    private TableView<ExcelRow> table;
    @FXML
    private ProgressBar progress;
    @FXML
    private Button sendButton;

    public void initialize() {
        table.itemsProperty().bind(loadExcelService.valueProperty());
        progress.visibleProperty().bind(loadExcelService.runningProperty());
        progress.progressProperty().bind(loadExcelService.progressProperty());
        sendButton.disableProperty().bind(loadExcelService.runningProperty());
        sendButton.setOnAction(event -> onSend());
    }

    @FXML
    public void onSelectExcel() {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(".xlsx", "*.xlsx")
        );
        File file = fileChooser.showOpenDialog(table.getScene().getWindow());

        if (file != null) {
            if (loadExcelService.getState() == Worker.State.SUCCEEDED) {
                loadExcelService.reset();
            }
            loadExcelService.setFile(file);
            loadExcelService.start();
        }
    }

    public void onSend() {
        if (table.itemsProperty().get() == null) {
            ButtonType btn = new ButtonType("إغلاق", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, "ﻻ يوجد بيانات في الجدول", btn);
            alert.setHeaderText("بيانات غير مكتملة");
            alert.show();
            return;
        }

        Sender sender = new Sender();
        sender.send(table.itemsProperty().get());
    }
}
