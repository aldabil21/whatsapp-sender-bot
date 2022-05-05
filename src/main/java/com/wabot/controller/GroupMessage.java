package com.wabot.controller;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Sender;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;

public class GroupMessage {
    @FXML
    private RadioButton savedList;
    @FXML
    private RadioButton labeledList;
    @FXML
    private ToggleGroup receivers;
    @FXML
    private TextField labelName;
    @FXML
    private TextArea message;

    public void initialize() {
        message.setStyle("-fx-font-family: OpenSansEmoji");

        labelName.visibleProperty().bind(receivers.getToggles().get(2).selectedProperty());

        // Shouldn't this be the default behaviour?
        message.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.isShiftDown()) {
                NodeOrientation orientation = message.getNodeOrientation();
                if (orientation == NodeOrientation.RIGHT_TO_LEFT) {
                    message.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                } else {
                    message.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                }
            }
        });
    }

    @FXML
    public void onSend() {
        RadioButton selected = (RadioButton) receivers.getSelectedToggle();
        if (validate(selected)) {
            // Set selected job
            Sender.Types type = Sender.Types.CHAT_LIST;
            if (selected == labeledList) {
                type = Sender.Types.LABELED_LIST;
                Sender sender = new Sender(type, labelName.getText());
                sender.send(message.getText());
                return;
            } else if (selected == savedList) {
                type = Sender.Types.SAVED_LIST;
            }

            Sender sender = new Sender(type);
            sender.send(message.getText());
        }
    }

    private boolean validate(RadioButton selected) {
        boolean isValid = true;
        String msg = "";

        if (message.getText().trim().isEmpty()) {
            msg = "يرجى كتابة رسالة";
            isValid = false;
        }
        if (isValid && selected == labeledList && labelName.getText().trim().isEmpty()) {
            msg = "يرجى كتابة إسم التصنيف";
            isValid = false;
        }

        if (!isValid) {
            ButtonType btn = new ButtonType("إغلاق", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, msg, btn);
            alert.setHeaderText("بيانات غير مكتملة");
            alert.show();
        }
        return isValid;
    }
}
