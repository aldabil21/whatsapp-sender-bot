module com.wabot {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    requires org.kordamp.ikonli.javafx;

    requires org.seleniumhq.selenium.chrome_driver;
    requires io.github.bonigarcia.webdrivermanager;


    opens com.wabot to javafx.fxml;
    exports com.wabot;
}