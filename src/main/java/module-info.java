module com.ecotwin {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.ecotwin to javafx.fxml;
    opens com.ecotwin.controller to javafx.fxml;

    exports com.ecotwin;
    exports com.ecotwin.controller;
}