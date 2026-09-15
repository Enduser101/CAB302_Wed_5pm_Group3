module com.ecotwin {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;

    opens com.ecotwin to javafx.fxml;
    opens com.ecotwin.controller to javafx.fxml;
    opens com.ecotwin.util;
    opens com.ecotwin.service;

    exports com.ecotwin;
    exports com.ecotwin.controller;
    exports com.ecotwin.model;
    exports com.ecotwin.service;
}