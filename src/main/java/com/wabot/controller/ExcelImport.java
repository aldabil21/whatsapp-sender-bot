package com.wabot.controller;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Sender;
import com.wabot.model.ExcelRow;
import com.wabot.services.LoadExcel;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(".xlsx", "*.xlsx"));
    }

    @FXML
    public void onSelectExcel() {
        fileChooser.setTitle("إختر ملف إكسل");
        File file = fileChooser.showOpenDialog(table.getScene().getWindow());
        if (file != null) {
            if (loadExcelService.getState() == Worker.State.SUCCEEDED) {
                loadExcelService.reset();
            }
            loadExcelService.setFile(file);
            loadExcelService.start();
        }
    }

    @FXML
    public void onDownloadExample() {
        fileChooser.setTitle("حفظ مثال إكسل");
        fileChooser.setInitialFileName("whatsapp-example.xlsx");
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook();
                    FileOutputStream os = new FileOutputStream(file.getAbsolutePath())) {
                Sheet sheet = workbook.createSheet("numbers");
                for (int i = 0; i < 2; i++) {
                    Row row = sheet.createRow(i);
                    Cell numberCell = row.createCell(0);
                    numberCell.setCellValue(966507487620L);
                    Cell msgCell = row.createCell(1);
                    msgCell.setCellValue("رسالة رقم " + (i + 1));
                }
                workbook.write(os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void onSend() {
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
