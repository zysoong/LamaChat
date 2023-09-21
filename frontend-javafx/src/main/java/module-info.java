module com.example.frontendjavafx {

    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.json;
    requires spring.websocket;
    requires spring.messaging;
    requires lombok;

    opens com.example.frontendjavafx;
    exports com.example.frontendjavafx;

    opens com.example.frontendjavafx.security;
    exports com.example.frontendjavafx.security;

    opens com.example.frontendjavafx.controller;
    exports com.example.frontendjavafx.controller;

    opens com.example.frontendjavafx.model;
    exports com.example.frontendjavafx.model;

    opens com.example.frontendjavafx.service;
    exports com.example.frontendjavafx.service;


}