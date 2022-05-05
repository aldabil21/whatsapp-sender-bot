package com.wabot.controller;

import com.wabot.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Home {
    public static AnchorPane homeOverlayPane = null;
    @FXML
    private AnchorPane overlayPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button minimize;
    @FXML
    private Button groupMessage;
    @FXML
    private Button help;
    @FXML
    private Button exportNumbers;
    @FXML
    private Button uploadExcel;
    private Button activeMenu = groupMessage;


    public void initialize() {
        // Used for job overlay infos
        homeOverlayPane = overlayPane;

        // Menu listeners
        groupMessage.setOnAction(event -> handleClick(groupMessage));
        uploadExcel.setOnAction(event -> handleClick(uploadExcel));
        exportNumbers.setOnAction(event -> handleClick(exportNumbers));
        help.setOnAction(event -> handleClick(help));

        // Set initial view
        handleClick(groupMessage);

        // minimization
        minimize.setOnAction(event -> {
            Stage stage = (Stage) minimize.getScene().getWindow();
            stage.setIconified(true);
        });
    }

    public Node setDraggableNode() {
        return borderPane.getTop();
    }

    @FXML
    private void handleClose() {
        Platform.exit();
    }

    private void handleClick(Button menu) {
        // Avoid double resource reloading
        if (activeMenu == menu) {
            return;
        }

        String resource = "Help";
        if (menu == groupMessage) {
            resource = "GroupMessage";
        } else if (menu == uploadExcel) {
            resource = "ExcelImport";
        } else if (menu == exportNumbers) {
            resource = "ExportNumbers";
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(resource + ".fxml"));
            borderPane.setCenter(loader.load());
            activeMenu = menu;
            handleStyle(activeMenu);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleStyle(Button active) {
        String className = "active";
        // remove from all
        groupMessage.getStyleClass().remove(className);
        uploadExcel.getStyleClass().remove(className);
        exportNumbers.getStyleClass().remove(className);
        help.getStyleClass().remove(className);

        // add to active
        active.getStyleClass().add(className);
    }

}

