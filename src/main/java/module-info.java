module javacafe.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    opens controller to javafx.fxml;

    exports controller;
    exports model;
    exports model.enums;
}