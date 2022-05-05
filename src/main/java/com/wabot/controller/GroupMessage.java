package com.wabot.controller;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Sender;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;

import java.io.IOException;

public class GroupMessage {
    @FXML
    private RadioButton chatList;
    @FXML
    private RadioButton savedList;
    @FXML
    private RadioButton groupList;
    @FXML
    private ToggleGroup receivers;
    @FXML
    private TextField groupListName;
    @FXML
    private TextArea message;

    public void initialize() {
        message.setStyle("-fx-font-family: OpenSansEmoji");

        groupListName.visibleProperty().bind(receivers.getToggles().get(2).selectedProperty());

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
            if (selected == groupList) {
                type = Sender.Types.GROUP_LIST;
            } else if (selected == savedList) {
                type = Sender.Types.SAVED_LIST;
            }

            try {
                Sender sender = new Sender(type);
                sender.send(message.getText());
            } catch (IOException e) {
                // TODO: show alert
                throw new RuntimeException(e);
            }
        }
    }

    private boolean validate(RadioButton selected) {
        boolean isValid = true;
        String msg = "";

        if (message.getText().trim().isEmpty()) {
            msg = "يرجى كتابة رسالة";
            isValid = false;
        }
        if (isValid && selected == groupList && groupListName.getText().trim().isEmpty()) {
            msg = "يرجى كتابة إسم المجموعة";
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
