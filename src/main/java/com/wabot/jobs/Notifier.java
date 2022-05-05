package com.wabot.jobs;

import com.wabot.Main;
import com.wabot.controller.Home;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;
import java.util.Optional;

public class Notifier {
    private final AnchorPane homeOverlayPane = Home.homeOverlayPane;
    private final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

    public Notifier() {
        // Bind backdrop
        homeOverlayPane.visibleProperty().bind(alert.showingProperty());

        // alert settings
        alert.initOwner(homeOverlayPane.getScene().getWindow());
        alert.setGraphic(null);
        alert.initStyle(StageStyle.UNDECORATED);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        dialogPane.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("css/styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("notifier");
    }

    private void updateHeader(String title) {
        HBox hBox = structureHeader(title);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setHeader(hBox);
    }

    private void updateContent(String subtitle, Node... node) {
        VBox vBox = structureContent(subtitle);
        if (node != null) {
            vBox.getChildren().addAll(node);
        }
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(null);
        dialogPane.setContent(vBox);
    }

    public boolean agreeToScanNotif() {
        updateHeader("مسح QR Code");
        String content = "سيتم فتح نافذة متصفح خاصة لإتمام العملية، يرجى مسح الـQR Code حتى يتمكن البرنامج بدأ عملية الإرسال";
        String imgSrc = Objects.requireNonNull(Main.class.getResource("images/qrcode.png")).toExternalForm();
        ImageView image = new ImageView(imgSrc);
        image.setFitWidth(220);
        image.setFitHeight(200);
        updateContent(content, image);

        ButtonType cancel = new ButtonType("إلغاء", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType confirm = new ButtonType("تأكيد", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(confirm, cancel);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.lookupButton(cancel).getStyleClass().add("cancel");
        dialogPane.lookupButton(confirm).getStyleClass().add("confirm");

        Optional<ButtonType> result = alert.showAndWait();

        return result.get() == confirm;
    }

    public void waitingScanNotif() {
        String imgSrc = Objects.requireNonNull(Main.class.getResource("images/qrcode.png")).toExternalForm();
        ImageView image = new ImageView(imgSrc);
        image.setFitWidth(170);
        image.setFitHeight(150);
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefWidth(100);
        progress.prefHeight(100);
        progress.setPadding(new Insets(0, 0, 10, 0));

        updateHeader("بإنتظار مسح الـQR");
        updateContent(null, progress, image);
        alert.setAlertType(Alert.AlertType.NONE);
        alert.getButtonTypes().setAll();
        alert.show();
    }

    public void onProcessNotif(String msg, Task<Boolean> task) {
        VBox vBox = new VBox();
        Label message = new Label(msg);
        message.setWrapText(true);
        message.setStyle("-fx-font-size: 16; -fx-padding: 5 10");
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(message);
        message.textProperty().bind(task.messageProperty());

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefWidth(100);
        progress.prefHeight(100);
        progress.setPadding(new Insets(0, 0, 10, 0));

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(30, 0, 0, 0));

        Button minimize = new Button();
        minimize.setText("تصغير  النافذة");
        minimize.getStyleClass().add("secondary");
        minimize.setOnAction(event -> {
            Stage stage = (Stage) homeOverlayPane.getScene().getWindow();
            stage.setIconified(true);
        });

        Button cancel = new Button();
        cancel.setText("إلغاء المهمة");
        cancel.getStyleClass().add("cancel");
        cancel.setOnAction(event -> task.cancel());

        hBox.getChildren().addAll(minimize, cancel);
        updateHeader("جاري التنفيذ");
        updateContent(null, progress, vBox, hBox);
        alert.setAlertType(Alert.AlertType.NONE);
        alert.show();
    }

    public void onProcessUpdater() {
//        DialogPane dialogPane = alert.getDialogPane();
//        VBox box = (VBox) dialogPane.contentProperty().get();
//        VBox labelBox = (VBox) box.getChildren().get(1);
//        Label label = labelBox.getChildren().get()
    }

    public void closeNotif() {
        if (alert.isShowing()) {
            alert.close();
        }
    }

    private VBox structureContent(String content) {
        VBox vBox = new VBox();
        Label s = new Label(content);
        s.setWrapText(true);
        s.setStyle("-fx-font-size: 16; -fx-padding: 5 10");
        vBox.setAlignment(Pos.CENTER);
        if (content != null) {
            vBox.getChildren().add(s);
        }
        return vBox;
    }

    private HBox structureHeader(String content) {
        HBox hBox = new HBox();
        Label t = new Label(content);
        t.setStyle("-fx-font-size: 32; -fx-padding: 10");
        hBox.setAlignment(Pos.CENTER);
        if (content != null) {
            hBox.getChildren().add(t);
        }
        return hBox;
    }
}
