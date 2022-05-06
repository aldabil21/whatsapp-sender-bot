package com.wabot.controller;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Sender;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

public class GroupMessage {
    private final FileChooser fileChooser = new FileChooser();
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
    @FXML
    private ImageView imagePreview;
    private File selectedMedia;

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

        // Setup FileChooser
        fileChooser.setTitle("إختر صورة أو فيديو");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All", "*.png", "*.jpg", "*.gif", "*.mp4", "*.avi", "*.mkv"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi", "*.mkv")
        );
    }

    @FXML
    public void onMediaAdd(ActionEvent event) {
        selectedMedia = null;
        imagePreview.setImage(null);
        Button $this = (Button) event.getSource();
        $this.setText("أضف صورة أو فيديو");
        File file = fileChooser.showOpenDialog(message.getScene().getWindow());
        if (file != null) {
            selectedMedia = file;
            Image img = new Image(String.valueOf(file.toURI()));
            imagePreview.setImage(img);
            // Edit button text
            $this.setText(file.getName());
        }

    }

    @FXML
    public void onSend() {
        RadioButton selectedOption = (RadioButton) receivers.getSelectedToggle();
        if (validate(selectedOption)) {
            // Set selectedOption job
            Sender.Types type = Sender.Types.CHAT_LIST;
            if (selectedOption == labeledList) {
                type = Sender.Types.LABELED_LIST;
            } else if (selectedOption == savedList) {
                type = Sender.Types.SAVED_LIST;
            }

            Sender sender = new Sender();
            sender.send(type, message.getText(), selectedMedia, labelName.getText());
        }
    }

    private boolean validate(RadioButton selectedOption) {
        boolean isValid = true;
        String msg = "";

        if (message.getText().trim().isEmpty() && selectedMedia == null) {
            msg = "يرجى كتابة رسالة أو إرفاق صورة/فيديو قصير";
            isValid = false;
        }
        if (isValid && selectedOption == labeledList && labelName.getText().trim().isEmpty()) {
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
