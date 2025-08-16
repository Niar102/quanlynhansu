module com.example.quanlynhansu {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;

    opens com.main to javafx.fxml;
    exports com.main;
    exports com.view;
    opens com.view to javafx.fxml;
    exports com.controller;
    opens com.controller to javafx.fxml;
    opens com.model to javafx.base;
    exports com.model;
}