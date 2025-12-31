module com.example.demo2rusho {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires java.desktop;
    requires org.slf4j;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires com.google.gson;
    requires jbcrypt;
    requires org.jfree.jfreechart;
    requires java.prefs;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;

    opens com.example.demo2rusho to javafx.fxml;

    exports com.example.demo2rusho;

    opens com.agriminds to javafx.fxml;
    opens com.agriminds.controller to javafx.fxml;

    exports com.agriminds;
    exports com.agriminds.model;
    exports com.agriminds.view;
    exports com.agriminds.controller;
    exports com.agriminds.service;
    exports com.agriminds.repository;
    exports com.agriminds.util;
}
