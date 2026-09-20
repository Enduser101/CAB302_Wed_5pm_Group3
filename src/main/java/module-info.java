module com.ecotwin {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;

    opens com.ecotwin to javafx.fxml;
    opens com.ecotwin.controller to javafx.fxml;

    // Unqualified opens so JUnit's reflective test runner (and Mockito's mock-maker) can
    // construct/inspect these classes when IntelliJ runs tests on the module path directly
    // instead of Maven Surefire's plain classpath (which never needed this).
    opens com.ecotwin.service;
    opens com.ecotwin.util;

    exports com.ecotwin;
    exports com.ecotwin.controller;
    exports com.ecotwin.model;
    exports com.ecotwin.service;
}