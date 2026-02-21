package com.nuketree3.example.kursprojectforsystemai.ui.tabs;

import com.nuketree3.example.kursprojectforsystemai.domain.ShipParameters;
import com.nuketree3.example.kursprojectforsystemai.service.AICalculator;
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
import javafx.scene.*;
import java.util.List;
import javafx.scene.shape.Circle;
import lombok.Setter;

public class MainScene {
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

    private Canvas polarChartCanvas;
    private GraphicsContext gc;
    private Text speedInfoText;
    private Text angleInfoText;

    private LineChart<Number, Number> chartTheta;
    private LineChart<Number, Number> chartPsi;
    private LineChart<Number, Number> chartMainRes;
    private LineChart<Number, Number> chartParamRes;
    private LineChart<Number, Number> chartMainPitch;

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

        shipLengthTextField = createInputField(inputGrid, "Длина корабля (м):", "40", 0);
        shipWidthTextField = createInputField(inputGrid, "Ширина корабля (м):", "8", 1);
        draftOfTheShipTextField = createInputField(inputGrid, "Осадка корабля (м):", "3.4", 2);
        metacentricHeightTextField = createInputField(inputGrid, "Метацентрическая высота (м):", "0.5", 3);
        shipSpeedTextField = createInputField(inputGrid, "Скорость корабля (узлы):", "11", 4);
        headingAngleTextField = createInputField(inputGrid, "Курсовой угол (°):", "130", 5);
        waveLengthTextField = createInputField(inputGrid, "Длина волны (м):", "40", 6);
        amplitudeOfTheOnBoardPitchingTextField = createInputField(inputGrid, "Амплитуда бортовой качки (°):", "15", 7);
        pitchingAmplitudeTextField = createInputField(inputGrid, "Амплитуда килевой качки (°):", "3", 8);

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
        drawEmptyPolarDiagram();

        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(5, 0, 0, 0));

        StackPane legendDot = new StackPane();
        Circle circle = new Circle(6, Color.RED);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(1);
        legendDot.getChildren().add(circle);

        speedInfoText = new Text("V = 15.0 уз");
        speedInfoText.setFont(Font.font("Arial", 12));

        angleInfoText = new Text("φ = 45.0°");
        angleInfoText.setFont(Font.font("Arial", 12));

        infoBox.getChildren().addAll(legendDot, speedInfoText, angleInfoText);

        chartArea.getChildren().addAll(title, polarChartCanvas, infoBox);
        return chartArea;
    }

    private void drawEmptyPolarDiagram() {
        gc.clearRect(0, 0, 900, 550);

        double centerX = 450;
        double centerY = 460;
        double radius = 380;

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        for (int i = 1; i <= 5; i++) {
            double r = radius * (i / 5.0);
            gc.strokeArc(centerX - r, centerY - r, 2 * r, 2 * r, 0, 180, ArcType.OPEN);

            double speed = i * 5;
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 10));
            gc.fillText(speed + " уз", centerX, centerY - r - 5);
        }

        double[] angles = {0, 30, 60, 90, 120, 150, 180};
        gc.setFont(Font.font("Arial", 11));
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

    }

    private void drawShipPosition(double x, double y) {
        double centerX = 450;
        double centerY = 460;

        double maxRadius = 380;
        double canvasX = centerX + x * (maxRadius / 10.0);
        double canvasY = centerY - y * (maxRadius / 10.0);

        gc.setFill(Color.RED);
        gc.fillOval(canvasX - 7, canvasY - 7, 14, 14);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.8);
        gc.strokeLine(canvasX - 5, canvasY - 5, canvasX + 5, canvasY + 5);
        gc.strokeLine(canvasX + 5, canvasY - 5, canvasX - 5, canvasY + 5);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(canvasX - 7, canvasY - 7, 14, 14);
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

        chartTheta = createEmptyChart("θ (градусы)", "μ", "Амплитуда бортовой качки θ");
        chartPsi = createEmptyChart("ψ (градусы)", "μ", "Амплитуда килевой качки ψ");
        chartMainRes = createEmptyChart("σ/ωθ", "μ", "Основной резонанс σ/ωθ");
        chartParamRes = createEmptyChart("σ/ωθ", "μ", "Параметрический резонанс σ/ωθ");
        chartMainPitch = createEmptyChart("σ/ωψ", "μ", "Основной резонанс σ/ωψ");

        VBox chart1Container = createChartContainer(chartTheta);
        VBox chart2Container = createChartContainer(chartPsi);
        VBox chart3Container = createChartContainer(chartMainRes);
        VBox chart4Container = createChartContainer(chartParamRes);
        VBox chart5Container = createChartContainer(chartMainPitch);

        chartsArea.getChildren().addAll(chart1Container, chart2Container, chart3Container,
                chart4Container, chart5Container);

        mainContainer.getChildren().addAll(title, chartsArea);
        return mainContainer;
    }

    private LineChart<Number, Number> createEmptyChart(String xLabel, String yLabel, String titleText) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis(0, 1.1, 0.2);
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        xAxis.setTickLabelFont(Font.font("Arial", 9));
        yAxis.setTickLabelFont(Font.font("Arial", 9));

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(titleText);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setPrefSize(280, 250);

        chart.setCache(true);
        chart.setCacheHint(CacheHint.SPEED);

        return chart;
    }

    private VBox createChartContainer(LineChart<Number, Number> chart) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(chart);
        return container;
    }

    private VBox createResultsArea() {
        VBox resultsArea = new VBox(10);
        resultsArea.setPrefWidth(350);
        resultsArea.setAlignment(Pos.TOP_LEFT);
        resultsArea.setPadding(new Insets(10));

        Text title = new Text("Результаты анализа");
        title.setFont(Font.font("Arial", 16));
        title.setStyle("-fx-font-weight: bold;");

        resultsTextArea = new TextArea();
        resultsTextArea.setPrefHeight(500);
        resultsTextArea.setEditable(false);
        resultsTextArea.setWrapText(true);
        resultsTextArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        resultsTextArea.setText("Для получения результатов:\n1. Введите параметры корабля\n2. Нажмите 'Произвести расчеты'\n\nРезультаты анализа появятся здесь.");

        VBox buttonsBox = new VBox(8);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);

        Button analyzeButton = new Button("Провести анализ");
        analyzeButton.setOnAction(e -> performAnalysis());

        Button bayesButton = new Button("Расчет по Байесу");
        bayesButton.setOnAction(e -> showBayesCalculation());

        Button shortliffButton = new Button("Расчет по Шортлиффу");
        shortliffButton.setOnAction(e -> showShortliffCalculation());

        buttonsBox.getChildren().addAll(
                analyzeButton,
                bayesButton,
                shortliffButton
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

            if (calculator == null) {
                calculator = new Calculator();
            }

            calculator.calculate(params);
            updateCharts();

            performAnalysis();

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Некорректные данные", "Пожалуйста, введите корректные числовые значения.");
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка расчета", e.getMessage());
        }
    }

    private void updateCharts() {
        if (calculator == null) return;

        try {
            updatePolarDiagram();

            updateMembershipCharts();

        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка обновления графиков", e.getMessage());
        }
    }

    private void updatePolarDiagram() {
        if (calculator == null) return;

        Calculator.PolarDiagramData data = calculator.getPolarDiagramData();

        drawEmptyPolarDiagram();

        if (data != null) {
            drawShipPosition(data.x, data.y);

            speedInfoText.setText(String.format("V = %.1f уз", data.speed));
            angleInfoText.setText(String.format("φ = %.1f°", data.courseAngle));
        }
    }

    private void updateMembershipCharts() {
        if (calculator == null) return;

        updateChart(chartTheta, calculator.getThetaChartData(), Color.BLUE);
        updateChart(chartPsi, calculator.getPsiChartData(), Color.GREEN);
        updateChart(chartMainRes, calculator.getMainResonanceChartData(), Color.RED);
        updateChart(chartParamRes, calculator.getParametricResonanceChartData(), Color.ORANGE);
        updateChart(chartMainPitch, calculator.getMainPitchResonanceChartData(), Color.PURPLE);
    }

    private void updateChart(LineChart<Number, Number> chart, List<AICalculator.ChartData> data, Color color) {
        if (data == null || data.isEmpty()) {
            return;
        }

        chart.getData().clear();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        int step = Math.max(1, data.size() / 50);
        for (int i = 0; i < data.size(); i += step) {
            AICalculator.ChartData point = data.get(i);
            series.getData().add(new XYChart.Data<>(point.x, point.y));
        }

        chart.getData().add(series);

        if (!series.getData().isEmpty()) {
            Node line = series.getNode().lookup(".chart-series-line");
            if (line != null) {
                String colorHex = String.format("#%02X%02X%02X",
                        (int)(color.getRed() * 255),
                        (int)(color.getGreen() * 255),
                        (int)(color.getBlue() * 255));
                line.setStyle("-fx-stroke: " + colorHex + "; -fx-stroke-width: 2px;");
            }
        }
    }

    private void performAnalysis() {
        if (calculator == null || calculator.getResults() == null) {
            resultsTextArea.setText("Сначала выполните расчет параметров!\n\nНажмите 'Произвести расчеты' после ввода параметров.");
            return;
        }

        AICalculator.FullAnalysisResult results = calculator.getResults();
        resultsTextArea.setText(results.generateReport());
    }

    private void showBayesCalculation() {
        if (calculator == null || calculator.getResults() == null) {
            resultsTextArea.setText("Сначала выполните расчет параметров!\n\nНажмите 'Произвести расчеты' после ввода параметров.");
            return;
        }

        AICalculator.FullAnalysisResult results = calculator.getResults();

        String bayesText = "=== РАСЧЕТ ПО ФОРМУЛЕ БАЙЕСА ===\n\n" +
                "Формула: P(H) = P(H|E)*P(E) + P(H|¬E)*P(¬E)\n\n" +
                "Для первого варианта:\n" +
                "P(E) = 0.76 (вероятность сильной качки)\n" +
                "P(H|E) = 0.90 (вероятность резонанса при сильной качки)\n" +
                "P(H|¬E) = 0.01 (вероятность резонанса без сильной качки)\n" +
                "P(¬E) = 1 - P(E) = 0.24\n\n" +
                String.format("P(H) = %.2f*%.2f + %.2f*%.2f\n",
                        0.90f, 0.76f, 0.01f, 0.24f) +
                String.format("P(H) = %.4f\n\n", results.bayesianProbability) +
                "Интерпретация:\n" +
                "P(H) > 0.7 - высокая вероятность резонанса\n" +
                "0.4 < P(H) < 0.7 - средняя вероятность\n" +
                "P(H) < 0.4 - низкая вероятность";

        resultsTextArea.setText(bayesText);
    }

    private void showShortliffCalculation() {
        if (calculator == null || calculator.getResults() == null) {
            resultsTextArea.setText("Сначала выполните расчет параметров!\n\nНажмите 'Произвести расчеты' после ввода параметров.");
            return;
        }

        AICalculator.FullAnalysisResult results = calculator.getResults();

        String shortliffText = "=== РАСЧЕТ ПО ФОРМУЛЕ ШОРТЛИФФА ===\n\n" +
                "Формула: МД(H|E1,E2) = МД(H|E1) + МД(H|E2)*(1 - МД(H|E1))\n\n" +
                "Для первого варианта:\n" +
                "МД(H|E1) = 0.90 (мера доверия по амплитуде)\n" +
                "МД(H|E2) = 0.97 (мера доверия по частоте)\n\n" +
                String.format("МД(H) = %.2f + %.2f*(1 - %.2f)\n",
                        0.90f, 0.97f, 0.90f) +
                String.format("МД(H) = %.4f\n\n", results.shortliffConfidence) +
                "Интерпретация:\n" +
                "0.8-1.0 - высокая уверенность в резонансе\n" +
                "0.5-0.8 - средняя уверенность\n" +
                "0.0-0.5 - низкая уверенность";

        resultsTextArea.setText(shortliffText);
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}