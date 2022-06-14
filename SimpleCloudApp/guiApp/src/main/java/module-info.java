module com.example.guiapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.guiapp to javafx.fxml;
    exports com.example.guiapp;
}