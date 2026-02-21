package com.nuketree3.example.kursprojectforsystemai;

import com.nuketree3.example.kursprojectforsystemai.service.Calculator;
import com.nuketree3.example.kursprojectforsystemai.ui.tabs.MainScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {

        MainScene mainTab = new MainScene();
        BorderPane root = mainTab.createContent();

        Calculator calculator = new Calculator();
        mainTab.setCalculator(calculator);

        Scene scene = new Scene(root, 800, 800);
        stage.setTitle("Контроль резонансных режимов качки корабля на волнении");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}