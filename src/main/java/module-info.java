module com.nuketree3.example.kursprojectforsystemai {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires jdk.compiler;


    opens com.nuketree3.example.kursprojectforsystemai to javafx.fxml;
    exports com.nuketree3.example.kursprojectforsystemai;
}