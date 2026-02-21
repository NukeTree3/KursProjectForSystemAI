package com.nuketree3.example.kursprojectforsystemai.service;

import com.nuketree3.example.kursprojectforsystemai.domain.ShipParameters;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import java.util.List;

public class Calculator {

    private AICalculator aiCalculator;
    @Getter
    private AICalculator.FullAnalysisResult results;

    public Calculator() {
    }

    public void calculate(ShipParameters params) {
        this.aiCalculator = new AICalculator(params);
        this.results = aiCalculator.calculateFullAnalysis();
    }

    public List<AICalculator.ChartData> getThetaChartData() {
        return aiCalculator.getThetaChartData();
    }

    public List<AICalculator.ChartData> getPsiChartData() {
        return aiCalculator.getPsiChartData();
    }

    public List<AICalculator.ChartData> getMainResonanceChartData() {
        return aiCalculator.getMainResonanceChartData();
    }

    public List<AICalculator.ChartData> getParametricResonanceChartData() {
        return aiCalculator.getParametricResonanceChartData();
    }

    public List<AICalculator.ChartData> getMainPitchResonanceChartData() {
        return aiCalculator.getMainPitchResonanceChartData();
    }

    public PolarDiagramData getPolarDiagramData() {
        PolarDiagramData data = new PolarDiagramData();

        data.speed = aiCalculator.params.getShipSpeedTextField();
        data.courseAngle = aiCalculator.params.getHeadingAngleTextField();
        data.rollPeriod = results.rollPeriod;
        data.apparentWavePeriod = results.apparentWavePeriod;

        double maxSpeed = 25.0;
        double diagramRadius = 10.0;
        double normalizedSpeed = Math.min(maxSpeed, data.speed) * diagramRadius / maxSpeed;
        double courseRad = Math.toRadians(180 - data.courseAngle); // Как в эталоне
        data.x = -normalizedSpeed * Math.cos(courseRad);
        data.y = normalizedSpeed * Math.sin(courseRad);

        return data;
    }

    public static class PolarDiagramData {
        public float speed;
        public float courseAngle;
        public float rollPeriod;
        public float apparentWavePeriod;
        public double x;
        public double y;
    }


}