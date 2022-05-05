package com.wabot.components;

import com.wabot.controller.Home;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;

public class UndecoratedAlert extends Alert {

    public UndecoratedAlert(AlertType alertType) {
        this(alertType, null);
    }

    public UndecoratedAlert(AlertType alertType, String msg, ButtonType... buttonTypes) {
        super(alertType, msg, buttonTypes);
        initStyle(StageStyle.UNDECORATED);
        getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        Label m = new Label(msg);
        m.setWrapText(true);
        m.setStyle("-fx-font-size: 16; -fx-padding: 5 10");
        getDialogPane().setContent(m);
        initOwner(Home.homeOverlayPane.getScene().getWindow());


        // Header
//        if (title != null) {
//            Label t = new Label(title);
//            t.setStyle("-fx-font-weight: 700; -fx-font-size: 20");
//            getDialogPane().setHeader(t);
//        }
    }

}
