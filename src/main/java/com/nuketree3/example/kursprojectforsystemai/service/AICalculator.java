package com.nuketree3.example.kursprojectforsystemai.service;

import com.nuketree3.example.kursprojectforsystemai.domain.ShipParameters;

import java.util.ArrayList;
import java.util.List;

public class AICalculator {

    private static final float KNOTS_TO_MPS = 0.514444f;

    private static final float WAVE_SPEED_COEFF = 1.25f;
    private static final float COURSE_CHANGE_MAX = 25.0f;

    static ShipParameters params;

    public AICalculator(ShipParameters params) {
        this.params = params;
    }

    public float calculateRollPeriod() {
        float B = params.getShipWidthTextField();
        float h = params.getMetacentricHeightTextField();
        float C = 0.8f;
        return (float) (C * B / Math.sqrt(h));
    }

    public float calculatePitchPeriod() {
        float T = params.getDraftOfTheShipTextField();
        return (float) (2.5 * Math.sqrt(T));
    }

    public float calculateWaveSpeed() {
        float lambda = params.getWaveLengthTextField();
        return WAVE_SPEED_COEFF * (float) Math.sqrt(lambda);
    }

    public float calculateApparentWavePeriod() {
        float lambda = params.getWaveLengthTextField();
        float V = params.getShipSpeedTextField() * KNOTS_TO_MPS;
        float phiRad = (float) Math.toRadians(params.getHeadingAngleTextField());
        float C_w = calculateWaveSpeed();

        float denominator = C_w - V * (float) Math.cos(phiRad);
        if (Math.abs(denominator) < 0.001f) {
            denominator = 0.001f;
        }

        return lambda / denominator;
    }

    public float calculateRollFrequencyRatio() {
        float naturalPeriod = calculateRollPeriod();
        float apparentPeriod = calculateApparentWavePeriod();
        return naturalPeriod / apparentPeriod;
    }

    public float calculatePitchFrequencyRatio() {
        float naturalPeriod = calculatePitchPeriod();
        float apparentPeriod = calculateApparentWavePeriod();
        return naturalPeriod / apparentPeriod;
    }

    public boolean isMainRollResonance() {
        float ratio = calculateRollFrequencyRatio();
        return ratio >= 0.8f && ratio <= 1.2f;
    }

    public boolean isParametricRollResonance() {
        float ratio = calculateRollFrequencyRatio();
        return ratio >= 1.8f && ratio <= 2.0f;
    }

    public boolean isMainPitchResonance() {
        float ratio = calculatePitchFrequencyRatio();
        return ratio >= 0.8f && ratio <= 1.2f;
    }

    public float linearMembership(float value, float minVal, float maxVal) {
        if (value <= minVal) {
            return 0.0f;
        }
        if (value >= maxVal) {
            return 1.0f;
        }
        return (value - minVal) / (maxVal - minVal);
    }

    public float triangularMembership(float value, float left, float center, float right) {
        if (value <= left || value >= right) {
            return 0.0f;
        }
        if (value == center) {
            return 1.0f;
        }
        if (value < center) {
            return (value - left) / (center - left);
        }
        return (right - value) / (right - center);
    }

    public float calculateRollAmplitudeMembership() {
        float amplitude = params.getAmplitudeOfTheOnBoardPitchingTextField();
        float minAmplitude = 12.0f;
        float maxAmplitude = 20.0f;
        return linearMembership(amplitude, minAmplitude, maxAmplitude);
    }

    public float calculatePitchAmplitudeMembership() {
        float amplitude = params.getPitchingAmplitudeTextField();
        float minAmplitude = 2.5f;
        float maxAmplitude = 4.5f;
        return linearMembership(amplitude, minAmplitude, maxAmplitude);
    }

    public float calculateMainRollFrequencyMembership() {
        float ratio = calculateRollFrequencyRatio();
        return triangularMembership(ratio, 0.8f, 1.0f, 1.2f);
    }

    public float calculateParametricRollFrequencyMembership() {
        float ratio = calculateRollFrequencyRatio();
        return triangularMembership(ratio, 1.85f, 1.95f, 2.10f);
    }

    public float calculateMainPitchFrequencyMembership() {
        float ratio = calculatePitchFrequencyRatio();
        return triangularMembership(ratio, 0.8f, 1.0f, 1.2f);
    }

    public MembershipValues fuzzify() {
        MembershipValues values = new MembershipValues();

        values.rollAmplitudeDegree = calculateRollAmplitudeMembership();
        values.pitchAmplitudeDegree = calculatePitchAmplitudeMembership();
        values.mainRollFreqDegree = calculateMainRollFrequencyMembership();
        values.parametricRollFreqDegree = calculateParametricRollFrequencyMembership();
        values.mainPitchFreqDegree = calculateMainPitchFrequencyMembership();

        return values;
    }

    public float calculateCourseCorrection(float alpha) {
        if (alpha <= 0) {
            return 0.0f;
        }

        float numerator = 0.0f;
        float denominator = 0.0f;
        float step = 1.0f;

        for (float angle = 0; angle <= COURSE_CHANGE_MAX; angle += step) {
            float mu = angle / COURSE_CHANGE_MAX;
            float muFinal = Math.min(mu, alpha);
            numerator += angle * muFinal;
            denominator += muFinal;
        }

        return denominator > 0 ? numerator / denominator : 0.0f;
    }

    public FuzzyInferenceResult fuzzyInference() {
        MembershipValues degrees = fuzzify();
        FuzzyInferenceResult result = new FuzzyInferenceResult();

        result.mainRollResonanceDegree = Math.min(
                degrees.rollAmplitudeDegree,
                degrees.mainRollFreqDegree
        );

        result.parametricRollResonanceDegree = Math.min(
                degrees.rollAmplitudeDegree,
                degrees.parametricRollFreqDegree
        );

        result.mainPitchResonanceDegree = Math.min(
                degrees.pitchAmplitudeDegree,
                degrees.mainPitchFreqDegree
        );

        float maxDegree = Math.max(
                result.mainRollResonanceDegree,
                Math.max(result.parametricRollResonanceDegree, result.mainPitchResonanceDegree)
        );

        result.recommendedCourseChange = calculateCourseCorrection(maxDegree);

        return result;
    }

    public float calculateBayesianProbability(float P_E, float P_H_given_E, float P_H_given_notE) {
        float P_notE = 1 - P_E;
        return P_H_given_E * P_E + P_H_given_notE * P_notE;
    }

    public float calculateShortliffTwoEvidences(float MD1, float MD2) {
        return MD1 + MD2 * (1 - MD1);
    }

    public List<ChartData> getThetaChartData() {
        List<ChartData> data = new ArrayList<>();
        float minAmplitude = 0.0f;
        float maxAmplitude = 30.0f;
        float step = 0.5f;

        for (float x = minAmplitude; x <= maxAmplitude; x += step) {
            float y = linearMembership(x, 12.0f, 20.0f);
            data.add(new ChartData(x, y));
        }
        return data;
    }

    public List<ChartData> getPsiChartData() {
        List<ChartData> data = new ArrayList<>();
        float minAmplitude = 0.0f;
        float maxAmplitude = 6.0f;
        float step = 0.1f;

        for (float x = minAmplitude; x <= maxAmplitude; x += step) {
            float y = linearMembership(x, 2.5f, 4.5f);
            data.add(new ChartData(x, y));
        }
        return data;
    }

    public List<ChartData> getMainResonanceChartData() {
        List<ChartData> data = new ArrayList<>();
        float minRatio = 0.5f;
        float maxRatio = 1.5f;
        float step = 0.01f;

        for (float x = minRatio; x <= maxRatio; x += step) {
            float y = triangularMembership(x, 0.8f, 1.0f, 1.2f);
            data.add(new ChartData(x, y));
        }
        return data;
    }

    public List<ChartData> getParametricResonanceChartData() {
        List<ChartData> data = new ArrayList<>();
        float minRatio = 1.5f;
        float maxRatio = 2.5f;
        float step = 0.01f;

        for (float x = minRatio; x <= maxRatio; x += step) {
            float y = triangularMembership(x, 1.8f, 1.9f, 2.0f);
            data.add(new ChartData(x, y));
        }
        return data;
    }

    public List<ChartData> getMainPitchResonanceChartData() {
        return getMainResonanceChartData();
    }

    public FullAnalysisResult calculateFullAnalysis() {
        FullAnalysisResult result = new FullAnalysisResult();

        result.rollPeriod = calculateRollPeriod();
        result.pitchPeriod = calculatePitchPeriod();
        result.apparentWavePeriod = calculateApparentWavePeriod();
        result.waveSpeed = calculateWaveSpeed();

        result.rollFrequencyRatio = calculateRollFrequencyRatio();
        result.pitchFrequencyRatio = calculatePitchFrequencyRatio();

        result.mainRollResonance = isMainRollResonance();
        result.parametricRollResonance = isParametricRollResonance();
        result.mainPitchResonance = isMainPitchResonance();

        FuzzyInferenceResult fuzzyResult = fuzzyInference();
        result.mainRollActivation = fuzzyResult.mainRollResonanceDegree;
        result.parametricRollActivation = fuzzyResult.parametricRollResonanceDegree;
        result.mainPitchActivation = fuzzyResult.mainPitchResonanceDegree;
        result.recommendedCourseChange = fuzzyResult.recommendedCourseChange;

        result.membershipTheta = calculateRollAmplitudeMembership();
        result.membershipPsi = calculatePitchAmplitudeMembership();
        result.membershipMainRoll = calculateMainRollFrequencyMembership();
        result.membershipParamRoll = calculateParametricRollFrequencyMembership();
        result.membershipMainPitch = calculateMainPitchFrequencyMembership();

        result.bayesianProbability = calculateBayesianProbability(0.76f, 0.9f, 0.01f);
        result.shortliffConfidence = calculateShortliffTwoEvidences(0.9f, 0.97f);

        return result;
    }

    public static class ChartData {
        public final float x;
        public final float y;

        public ChartData(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class MembershipValues {
        public float rollAmplitudeDegree;
        public float pitchAmplitudeDegree;
        public float mainRollFreqDegree;
        public float parametricRollFreqDegree;
        public float mainPitchFreqDegree;
    }

    public static class FuzzyInferenceResult {
        public float mainRollResonanceDegree;
        public float parametricRollResonanceDegree;
        public float mainPitchResonanceDegree;
        public float recommendedCourseChange;
    }

    public static class FullAnalysisResult {
        public float rollPeriod;
        public float pitchPeriod;
        public float apparentWavePeriod;
        public float waveSpeed;

        public float rollFrequencyRatio;
        public float pitchFrequencyRatio;

        public boolean mainRollResonance;
        public boolean parametricRollResonance;
        public boolean mainPitchResonance;

        public float mainRollActivation;
        public float parametricRollActivation;
        public float mainPitchActivation;
        public float recommendedCourseChange;

        public float membershipTheta;
        public float membershipPsi;
        public float membershipMainRoll;
        public float membershipParamRoll;
        public float membershipMainPitch;

        public float bayesianProbability;
        public float shortliffConfidence;

        public String generateReport() {
            StringBuilder sb = new StringBuilder();

            sb.append("=== АНАЛИЗ РЕЗОНАНСНЫХ РЕЖИМОВ ===\n");
            sb.append(String.format("Собственный период бортовой качки: %.2f с\n", rollPeriod));
            sb.append(String.format("Собственный период килевой качки: %.2f с\n", pitchPeriod));
            sb.append(String.format("Кажущийся период волны: %.2f с\n", apparentWavePeriod));
            sb.append(String.format("Скорость волны: %.2f м/с\n", waveSpeed));
            sb.append("==================================================\n");
            sb.append("Соотношения частот:\n");
            sb.append(String.format("σ/ωθ = %.3f (диапазон резонанса: 0.8-1.2)\n", rollFrequencyRatio));
            sb.append(String.format("σ/ωψ = %.3f (диапазон резонанса: 0.8-1.2)\n", pitchFrequencyRatio));

            if (mainRollResonance) sb.append("!!! ОБНАРУЖЕН ОСНОВНОЙ БОРТОВОЙ РЕЗОНАНС !!!\n");
            if (parametricRollResonance) sb.append("!!! ОБНАРУЖЕН ПАРАМЕТРИЧЕСКИЙ РЕЗОНАНС !!!\n");
            if (mainPitchResonance) sb.append("!!! ОБНАРУЖЕН ОСНОВНОЙ КИЛЕВОЙ РЕЗОНАНС !!!\n");

            sb.append("\n=== РЕЗУЛЬТАТЫ ПРАВИЛ ===\n");

            sb.append("Правило: Основной бортовой резонанс\n");
            sb.append(String.format("Статус: %s\n", mainRollActivation > 0.1f ? "АКТИВИРОВАНО" : "НЕ АКТИВИРОВАНО"));
            sb.append(String.format("Степень активации: %.3f\n", mainRollActivation));
            sb.append(String.format("Уверенность: %.3f\n", mainRollActivation * 0.95f));
            sb.append("----------------------------------------\n");

            sb.append("Правило: Параметрический резонанс\n");
            sb.append(String.format("Статус: %s\n", parametricRollActivation > 0.1f ? "АКТИВИРОВАНО" : "НЕ АКТИВИРОВАНО"));
            sb.append(String.format("Степень активации: %.3f\n", parametricRollActivation));
            sb.append(String.format("Уверенность: %.3f\n", parametricRollActivation * 0.95f));
            sb.append("----------------------------------------\n");

            sb.append("Правило: Основной килевой резонанс\n");
            sb.append(String.format("Статус: %s\n", mainPitchActivation > 0.1f ? "АКТИВИРОВАНО" : "НЕ АКТИВИРОВАНО"));
            sb.append(String.format("Степень активации: %.3f\n", mainPitchActivation));
            sb.append(String.format("Уверенность: %.3f\n", mainPitchActivation * 0.95f));
            sb.append("----------------------------------------\n");

            sb.append("\n=== НЕЧЕТКИЙ ЛОГИЧЕСКИЙ ВЫВОД ===\n\n");
            sb.append("Функции принадлежности:\n");
            sb.append(String.format("μ(θ=%.1f°) = %.3f\n",
                    params.getAmplitudeOfTheOnBoardPitchingTextField(), membershipTheta));
            sb.append(String.format("μ(ψ=%.1f°) = %.3f\n",
                    params.getPitchingAmplitudeTextField(), membershipPsi));
            sb.append(String.format("μ(σ/ωθ=%.3f) = %.3f (осн)\n",
                    rollFrequencyRatio, membershipMainRoll));
            sb.append(String.format("μ(σ/ωθ=%.3f) = %.3f (пар)\n",
                    rollFrequencyRatio, membershipParamRoll));

            sb.append("\nСтепени истинности правил (α):\n");
            if (mainRollActivation > 0) {
                sb.append(String.format("Правило 1 (Основной бортовой): α = min(%.3f, %.3f) = %.3f\n",
                        membershipTheta, membershipMainRoll, mainRollActivation));
            }
            if (parametricRollActivation > 0) {
                sb.append(String.format("Правило 2 (Параметрический): α = min(%.3f, %.3f) = %.3f\n",
                        membershipTheta, membershipParamRoll, parametricRollActivation));
            }
            if (mainPitchActivation > 0) {
                sb.append(String.format("Правило 3 (Основной килевой): α = min(%.3f, %.3f) = %.3f\n",
                        membershipPsi, membershipMainPitch, mainPitchActivation));
            }

            sb.append("\nДефаззификация:\n");
            float maxAlpha = Math.max(mainRollActivation, Math.max(parametricRollActivation, mainPitchActivation));
            sb.append(String.format("Максимальная α = %.3f\n", maxAlpha));
            sb.append(String.format("Метод центра тяжести → Δφ = %.1f°\n", recommendedCourseChange));
            sb.append(String.format("\nРЕКОМЕНДАЦИЯ: Изменить курс на %.0f°\n", recommendedCourseChange));

            sb.append("\n=== РАСЧЕТ ПО ФОРМУЛЕ БАЙЕСА ===\n");
            sb.append("P(H) = P(H|E)*P(E) + P(H|¬E)*P(¬E)\n");
            sb.append("P(H) = 0.9*0.76 + 0.01*0.32\n");
            sb.append(String.format("P(H) = %.4f\n", bayesianProbability));

            sb.append("\n=== РАСЧЕТ ПО ФОРМУЛЕ ШОРТЛИФФА ===\n");
            sb.append("МД(H) = МД(H|E1) + МД(H|E2)*(1 - МД(H|E1))\n");
            sb.append("МД(H) = 0.9 + 0.97*(1 - 0.9)\n");
            sb.append(String.format("МД(H) = %.4f\n", shortliffConfidence));

            return sb.toString();
        }
    }
}