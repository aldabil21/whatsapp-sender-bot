module com.wabot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    requires org.apache.poi.poi;
    requires org.kordamp.ikonli.javafx;

    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.support;
    requires io.github.bonigarcia.webdrivermanager;

    exports com.wabot;
    exports com.wabot.controller;
    exports com.wabot.components;

    opens com.wabot to javafx.fxml;
    opens com.wabot.controller to javafx.fxml;
    opens com.wabot.components to javafx.fxml;
    opens com.wabot.model to javafx.base;
}