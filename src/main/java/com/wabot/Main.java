package com.wabot;

import com.wabot.util.Sqlite;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("واتساب بوت");
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        Sqlite.getInstance().close();
    }
}