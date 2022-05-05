package com.wabot;

import com.wabot.controller.Home;
import com.wabot.util.Sqlite;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Home.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("واتساب بوت");

        // Set draggable ono top bar
        Home homeController = fxmlLoader.getController();
        Node topNode = homeController.setDraggableNode();
        topNode.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        topNode.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Start
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        Sqlite.getInstance().close();
        // In case user force close app while a task is running
        // interrupt all threads
        System.exit(0);
    }
}