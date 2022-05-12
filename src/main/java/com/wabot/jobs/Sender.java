package com.wabot.jobs;

import com.wabot.components.UndecoratedAlert;
import com.wabot.model.ExcelRow;
import com.wabot.tasks.QrCodeScan;
import com.wabot.tasks.SendTask;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.List;

public class Sender {
    private final Notifier notifier = new Notifier();

    public Notifier getNotifier() {
        return notifier;
    }

    /**
     * Send to chat list, saved numbers, or labels
     *
     * @param type      enum @Sender.Types
     * @param message   String message
     * @param media     File of image/video
     * @param labelName option for Whatsapp business to send to specific label
     */
    public void send(Types type, String message, File media, String labelName) {
        Task<ChromeDriver> task = initScanTask();
        if (task != null) {
            task.setOnSucceeded(workerStateEvent -> {
                notifier.closeNotif();
                SendTask sendTask = new SendTask(task.getValue(), notifier, type, message,
                        media, labelName);
                notifier.onProcessNotif("جاري التحضير للإرسال", sendTask);
                new Thread(sendTask).start();
            });
        }
    }

    /**
     * Send to specific numbers loaded via Sxcel
     *
     * @param list List of @ExcelRow
     */
    public void send(List<ExcelRow> list) {
        Task<ChromeDriver> task = initScanTask();
        if (task != null) {
            task.setOnSucceeded(workerStateEvent -> {
                notifier.closeNotif();
                SendTask sendTask = new SendTask(task.getValue(), notifier, list);
                notifier.onProcessNotif("جاري التحضير للإرسال", sendTask);
                new Thread(sendTask).start();
            });

        }

    }

    /**
     * Scan unsaved numbers in whatsapp and save them in Excel file
     */
    public Task<ChromeDriver> initScanTask() {
        if (notifier.agreeToScanNotif()) {
            Task<ChromeDriver> task = new QrCodeScan(notifier);
            notifier.waitingScanNotif(task);
            new Thread(task).start();
            return task;
        }
        return null;
    }

    public enum Types {
        CHAT_LIST, SAVED_LIST, LABELED_LIST, EXCEL_LIST
    }
}
