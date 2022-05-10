package com.wabot.services;

import com.wabot.model.ExcelRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LoadExcel extends Service<ObservableList<ExcelRow>> {
    private File file;

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    protected Task<ObservableList<ExcelRow>> createTask() {
        return new Task<>() {
            @Override
            protected ObservableList<ExcelRow> call() {
                List<ExcelRow> rows = new ArrayList<>();
                try (InputStream is = Files.newInputStream(Paths.get(file.toURI()))) {
                    Workbook workbook = WorkbookFactory.create(is);
                    Sheet sheet = workbook.getSheetAt(0);

                    int i = 1;
                    for (Row row : sheet) {
                        try {
                            String phone = getCellValue(row.getCell(0));
                            String message = getCellValue(row.getCell(1));
                            if (!phone.isEmpty() && !message.isEmpty()) {
                                ExcelRow tableRow = new ExcelRow(i, phone.replaceAll(" ", ""), message);
                                if (!rows.contains(tableRow)) {
                                    rows.add(tableRow);
                                    updateValue(FXCollections.observableArrayList(rows));
                                    i++;
                                }
                                updateProgress(row.getRowNum(), sheet.getLastRowNum());
                            }
                        } catch (Exception e) {
                            //
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return FXCollections.observableArrayList(rows);
            }
        };
    }


    private String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> String.valueOf(BigDecimal.valueOf(cell.getNumericCellValue()).longValue());
            case STRING -> cell.getStringCellValue();
            default -> "";
        };
    }
}
