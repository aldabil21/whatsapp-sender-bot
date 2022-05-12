package com.wabot.controller;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Sender;
import com.wabot.jobs.Notifier;
import com.wabot.model.ExcelRow;
import com.wabot.tasks.SaveTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportNumbers {
    private final FileChooser fileChooser = new FileChooser();
    @FXML
    private TableView<ExcelRow> table;
    @FXML
    private ProgressBar progress;
    @FXML
    private Button saveButton;

    public void initialize() {
        saveButton.setOnAction(event -> onDownloadExcel());

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(".xlsx", "*.xlsx"));
    }

    @FXML
    private void onScanNumbers() {
        Sender sender = new Sender();

        Task<ChromeDriver> task = sender.initScanTask();

        if (task != null) {

            task.setOnSucceeded(event -> {
                Notifier notifier = sender.getNotifier();
                notifier.closeNotif();
                SaveTask saveTask = new SaveTask(task.getValue(), notifier);
                notifier.onProcessNotif("جاري التحضير للإرسال", saveTask);
                new Thread(saveTask).start();
                table.itemsProperty().bind(saveTask.valueProperty());
                progress.visibleProperty().bind(saveTask.runningProperty());
                saveButton.disableProperty().bind(saveTask.runningProperty());
            });
        }
    }

    private void onDownloadExcel() {

        if (table.getItems() == null || table.getItems().size() <= 0) {
            ButtonType btn = new ButtonType("إغلاق", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, "لا توجد أي أرقام في الجدول", btn);
            alert.setHeaderText("ﻻ توجد أرقام");
            alert.show();
            return;
        }

        fileChooser.setTitle("حفظ أرقام الواتساب");
        fileChooser.setInitialFileName("whatsapp-numbers.xlsx");
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new HSSFWorkbook();
                    FileOutputStream os = new FileOutputStream(file.getAbsolutePath())) {
                Sheet sheet = workbook.createSheet("numbers");
                for (int i = 0; i < table.getItems().size(); i++) {
                    Row row = sheet.createRow(i);
                    Cell numberCell = row.createCell(0);
                    numberCell.setCellValue(Long.parseLong(table.getItems().get(i).getNumber()));
                }
                workbook.write(os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
