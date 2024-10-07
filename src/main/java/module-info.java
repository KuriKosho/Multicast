module org.example.netreceivefx {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.netreceivefx to javafx.fxml;
    exports org.example.netreceivefx;
}