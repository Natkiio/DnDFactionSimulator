module org.example.dndfactionsimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.dndfactionsimulator to javafx.fxml;
    opens org.example.dndfactionsimulator.model to javafx.base;
    opens org.example.dndfactionsimulator.ui to javafx.fxml;

    exports org.example.dndfactionsimulator;
    exports org.example.dndfactionsimulator.model;
    exports org.example.dndfactionsimulator.ui;
}