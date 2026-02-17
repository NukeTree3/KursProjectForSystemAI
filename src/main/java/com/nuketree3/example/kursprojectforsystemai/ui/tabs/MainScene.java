package com.nuketree3.example.kursprojectforsystemai.ui.tabs;

import com.nuketree3.example.kursprojectforsystemai.domain.ShipParameters;
import com.nuketree3.example.kursprojectforsystemai.service.Calculator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.Setter;

public class MainTab{
    private TextField shipLengthTextField;
    private TextField shipWidthTextField;
    private TextField draftOfTheShipTextField;
    private TextField metacentricHeightTextField;
    private TextField shipSpeedTextField;
    private TextField headingAngleTextField;
    private TextField waveLengthTextField;
    private TextField amplitudeOfTheOnBoardPitchingTextField;
    private TextField pitchingAmplitudeTextField;
    private TextArea resultsTextArea;

    @Setter
    private Calculator calculator;
    private ShipParameters shipParameters;

    private Canvas polarChartCanvas;
    private GraphicsContext gc;
    private Text speedInfoText;
    private Text angleInfoText;


    public BorderPane createContent() {
        BorderPane mainContainer = new BorderPane();

        VBox inputArea = createInputArea();
        mainContainer.setLeft(inputArea);

        VBox chartsArea = createChartsArea();
        mainContainer.setCenter(chartsArea);

        VBox resultsArea = createResultsArea();
        mainContainer.setRight(resultsArea);

        BorderPane.setMargin(inputArea, new Insets(10));
        BorderPane.setMargin(chartsArea, new Insets(10));
        BorderPane.setMargin(resultsArea, new Insets(10));

        return mainContainer;
    }

    private VBox createInputArea() {
        VBox inputArea = new VBox(10);
        inputArea.setPrefWidth(300);
        inputArea.setAlignment(Pos.TOP_LEFT);
        inputArea.setPadding(new Insets(10));

        Text title = new Text("Параметры корабля");
        title.setFont(Font.font("Arial", 16));
        title.setStyle("-fx-font-weight: bold;");

        GridPane inputGrid = new GridPane();
        inputGrid.setVgap(8);
        inputGrid.setHgap(10);

        shipLengthTextField = createInputField(inputGrid, "Длина корабля (м):", "100", 0);
        shipWidthTextField = createInputField(inputGrid, "Ширина корабля (м):", "20", 1);
        draftOfTheShipTextField = createInputField(inputGrid, "Осадка корабля (м):", "8", 2);
        metacentricHeightTextField = createInputField(inputGrid, "Метацентрическая высота (м):", "1.5", 3);
        shipSpeedTextField = createInputField(inputGrid, "Скорость корабля (узлы):", "18", 4);
        headingAngleTextField = createInputField(inputGrid, "Курсовой угол (°):", "110", 5);
        waveLengthTextField = createInputField(inputGrid, "Длина волны (м):", "150", 6);
        amplitudeOfTheOnBoardPitchingTextField = createInputField(inputGrid, "Амплитуда бортовой качки (°):", "15", 7);
        pitchingAmplitudeTextField = createInputField(inputGrid, "Амплитуда килевой качки (°):", "5", 8);

        Button calculateButton = new Button("Произвести расчеты");
        calculateButton.setMaxWidth(Double.MAX_VALUE);
        calculateButton.setOnAction(e -> calculateParameters());

        Separator separator = new Separator();

        inputArea.getChildren().addAll(title, inputGrid, calculateButton, separator);

        return inputArea;
    }

    private TextField createInputField(GridPane grid, String labelText, String defaultValue, int row) {
        Label label = new Label(labelText);
        label.setPrefWidth(220);

        TextField textField = new TextField(defaultValue);
        textField.setPrefWidth(150);

        grid.add(label, 0, row);
        grid.add(textField, 1, row);

        return textField;
    }

    private VBox createChartsArea() {
        VBox mainChartsArea = new VBox(15);
        mainChartsArea.setPadding(new Insets(5));

        VBox mainChartArea = createMainChartArea();

        VBox functionChartsArea = createFunctionChartsArea();

        mainChartsArea.getChildren().addAll(mainChartArea, new Separator(), functionChartsArea);

        return mainChartsArea;
    }

    private VBox createMainChartArea() {
        VBox chartArea = new VBox(5);
        chartArea.setAlignment(Pos.TOP_CENTER);

        Text title = new Text("Диаграмма качки");
        title.setFont(Font.font("Arial", 18));
        title.setStyle("-fx-font-weight: bold;");

        polarChartCanvas = new Canvas(900, 550);
        gc = polarChartCanvas.getGraphicsContext2D();
        drawHalfPolarDiagram(gc, 450, 460, 380);

        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(5, 0, 0, 0));

        StackPane legendDot = new StackPane();
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(6, Color.RED);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1);
        legendDot.getChildren().add(circle);

        Text legendText = new Text("текущее положение судна");
        legendText.setFont(Font.font("Arial", 12));

        speedInfoText = new Text("V = 18.0 уз");
        speedInfoText.setFont(Font.font("Arial", 12));

        angleInfoText = new Text("φ = 110.0°");
        angleInfoText.setFont(Font.font("Arial", 12));

        infoBox.getChildren().addAll(legendDot, legendText, speedInfoText, angleInfoText);

        chartArea.getChildren().addAll(title, polarChartCanvas, infoBox);
        return chartArea;
    }

    private void drawHalfPolarDiagram(GraphicsContext gc, double centerX, double centerY, double radius) {
        gc.clearRect(0, 0, 900, 550);

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        for (int i = 1; i <= 5; i++) {
            double r = radius * (i / 5.0);
            gc.strokeArc(centerX - r, centerY - r, 2 * r, 2 * r, 0, 180, ArcType.OPEN);

            double speed = i * 5;
            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font("Arial", 10));
            gc.fillText(speed + " уз", centerX, centerY - r - 5);
        }

        double[] angles = {0, 30, 60, 90, 120, 150, 180};
        gc.setFont(javafx.scene.text.Font.font("Arial", 11));
        for (double angle : angles) {
            double rad = Math.toRadians(angle);
            double x = centerX + radius * Math.cos(rad);
            double y = centerY - radius * Math.sin(rad);

            gc.strokeLine(centerX, centerY, x, y);

            double labelRadius = radius + 25;
            double labelX = centerX + labelRadius * Math.cos(rad);
            double labelY = centerY - labelRadius * Math.sin(rad);

            double offsetX = 0;
            double offsetY = 0;
            if (angle == 0) {
                offsetX = -15;
                offsetY = 10;
            } else if (angle == 180) {
                offsetX = -20;
                offsetY = 10;
            } else if (angle <= 90) {
                offsetX = -12;
                offsetY = 5;
            } else {
                offsetX = -15;
                offsetY = 5;
            }

            gc.fillText(angle + "°", labelX + offsetX, labelY + offsetY);
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.2);
        gc.strokeLine(centerX - radius, centerY, centerX + radius, centerY);

        drawShipPosition(gc, centerX, centerY, radius, 18.0, 110.0);
    }

    private void drawShipPosition(GraphicsContext gc, double centerX, double centerY,
                                  double maxRadius, double speed, double angle) {
        double rad = Math.toRadians(angle);
        double r = maxRadius * (speed / 25.0);
        double pointX = centerX + r * Math.cos(rad);
        double pointY = centerY - r * Math.sin(rad);

        gc.setFill(Color.RED);
        gc.fillOval(pointX - 7, pointY - 7, 14, 14);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.8);
        gc.strokeLine(pointX - 5, pointY - 5, pointX + 5, pointY + 5);
        gc.strokeLine(pointX + 5, pointY - 5, pointX - 5, pointY + 5);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(pointX - 7, pointY - 7, 14, 14);
    }

    private VBox createFunctionChartsArea() {
        VBox mainContainer = new VBox(10);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(5, 0, 10, 0));

        Label title = new Label("Функции принадлежности");
        title.setFont(Font.font("Arial", 14));
        title.setStyle("-fx-font-weight: bold;");
        title.setPadding(new Insets(0, 0, 5, 0));

        HBox chartsArea = new HBox(15);
        chartsArea.setAlignment(Pos.CENTER);

        VBox chart1Container = createChartContainer(
                createFunctionChart("θ (градусы)", "μ", "Амплитуда бортовой качки θ")
        );

        VBox chart2Container = createChartContainer(
                createFunctionChart("ψ (градусы)", "μ", "Амплитуда килевой качки ψ")
        );

        VBox chart3Container = createChartContainer(
                createFunctionChart("σ/ωθ", "μ", "Основной резонанс σ/ωθ")
        );

        VBox chart4Container = createChartContainer(
                createFunctionChart("σ/ωθ", "μ", "Параметрический резонанс σ/ωθ")
        );

        chartsArea.getChildren().addAll(chart1Container, chart2Container, chart3Container, chart4Container);

        mainContainer.getChildren().addAll(title, chartsArea);
        return mainContainer;
    }

    private VBox createChartContainer(LineChart<Number, Number> chart) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        chart.setPrefSize(280, 250);
        container.getChildren().add(chart);
        return container;
    }

    private LineChart<Number, Number> createFunctionChart(String xLabel, String yLabel, String titleText) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(titleText);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i <= 10; i++) {
            double x = i;
            double y = Math.exp(-Math.pow(x - 5, 2) / 8);
            series.getData().add(new XYChart.Data<>(x, y));
        }

        chart.getData().add(series);

        return chart;
    }

    private VBox createResultsArea() {
        VBox resultsArea = new VBox(10);
        resultsArea.setPrefWidth(300);
        resultsArea.setAlignment(Pos.TOP_LEFT);
        resultsArea.setPadding(new Insets(10));

        Text title = new Text("Результаты анализа");
        title.setFont(Font.font("Arial", 16));
        title.setStyle("-fx-font-weight: bold;");

        resultsTextArea = new TextArea();
        resultsTextArea.setPrefHeight(300);
        resultsTextArea.setEditable(false);
        resultsTextArea.setWrapText(true);
        resultsTextArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        resultsTextArea.setText("Результаты расчетов будут отображены здесь...");

        VBox buttonsBox = new VBox(8);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);

        Button saveResultsButton = new Button("Провести анализ");
        Button exportChartButton =  new Button("Расчет по Байесу");
        Button generateReportButton =  new Button("Расчет по Шортлиффу");

        buttonsBox.getChildren().addAll(
                saveResultsButton,
                exportChartButton,
                generateReportButton
        );

        resultsArea.getChildren().addAll(title, resultsTextArea, buttonsBox);
        return resultsArea;
    }

    private void calculateParameters() {
        try {
            ShipParameters params = new ShipParameters(
                    Float.parseFloat(shipLengthTextField.getText()),
                    Float.parseFloat(shipWidthTextField.getText()),
                    Float.parseFloat(draftOfTheShipTextField.getText()),
                    Float.parseFloat(metacentricHeightTextField.getText()),
                    Float.parseFloat(shipSpeedTextField.getText()),
                    Float.parseFloat(headingAngleTextField.getText()),
                    Float.parseFloat(waveLengthTextField.getText()),
                    Float.parseFloat(amplitudeOfTheOnBoardPitchingTextField.getText()),
                    Float.parseFloat(pitchingAmplitudeTextField.getText())
            );

            if (calculator != null) {
                calculator.calculate(params);
                updateCharts();
            }

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Некорректные данные", "Пожалуйста, введите корректные числовые значения.");
        }
    }

    private void updateCharts() {
        double speed = Float.parseFloat(shipSpeedTextField.getText());
        double angle = Float.parseFloat(headingAngleTextField.getText());

        drawHalfPolarDiagram(gc, 350, 175, 160);
        drawShipPosition(gc, 350, 175, 160, speed, angle);

        speedInfoText.setText(String.format("V = %.1f уз", speed));
        angleInfoText.setText(String.format("φ = %.1f°", angle));
    }


    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}