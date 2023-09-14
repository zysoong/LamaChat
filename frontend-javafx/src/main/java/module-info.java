module com.example.frontendjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.example.frontendjavafx to javafx.fxml;
    exports com.example.frontendjavafx;
}