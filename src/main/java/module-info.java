module java.cafe.pos {
    requires javafx.controls;
    requires javafx.fxml;

    opens controller to javafx.fxml;
    exports controller;
}