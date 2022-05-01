package com.wabot.model;

import com.wabot.util.Util;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class Sqlite {
    private static final String DB_NAME = "database.db";
    public static final String CONNECTION_STRING = "jdbc:sqlite:" + Paths.get(Util.getDbDir(), DB_NAME);
    private static final Sqlite instance = new Sqlite();
    private Connection connection;

    private Sqlite() {
        try {
            //Check if already has db
            Path dbPath = Paths.get(Util.getDbDir());
            boolean hasDB = false;
            if (!Files.isDirectory(dbPath)) {
                Files.createDirectory(dbPath);
            } else {
                File dbFile = new File(Paths.get(dbPath.toString(), DB_NAME).toString());
                hasDB = dbFile.exists();
            }

            connection = DriverManager.getConnection(CONNECTION_STRING);

            if (!hasDB) {
                this.createSchema();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("خطأ في الإتصال مع قاعدة البيانات، يرجى التواصل مع الأدمن");
            alert.showAndWait();
            Platform.exit();
        }
    }

    public static Sqlite getInstance() {
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void createSchema() {
        StringBuilder q = new StringBuilder("CREATE TABLE IF NOT EXISTS \"job\" (")
                .append("\"id\" INTEGER,")
                .append("\"date\" TEXT,")
                .append("PRIMARY KEY(\"id\") );");
        try (PreparedStatement statement = connection.prepareStatement(q.toString())) {
            boolean res = statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
