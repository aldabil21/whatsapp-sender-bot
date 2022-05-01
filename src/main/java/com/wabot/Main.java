package com.wabot;

import com.wabot.model.Sqlite;
import com.wabot.util.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        // Set chrome driver path
        System.setProperty("webdriver.chrome.driver", Util.getChromeDriverPath());
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        Sqlite.getInstance().close();
    }
}