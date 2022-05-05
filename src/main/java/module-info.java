module com.wabot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    requires org.kordamp.ikonli.javafx;

    requires org.seleniumhq.selenium.chrome_driver;
    requires io.github.bonigarcia.webdrivermanager;


    opens com.wabot to javafx.fxml;
    opens com.wabot.controller to javafx.fxml;
    exports com.wabot;
    exports com.wabot.controller;
    exports com.wabot.components;
    opens com.wabot.components to javafx.fxml;
}