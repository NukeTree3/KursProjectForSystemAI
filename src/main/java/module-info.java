module com.nuketree3.example.kursprojectforsystemai {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.nuketree3.example.kursprojectforsystemai to javafx.fxml;
    exports com.nuketree3.example.kursprojectforsystemai;
}