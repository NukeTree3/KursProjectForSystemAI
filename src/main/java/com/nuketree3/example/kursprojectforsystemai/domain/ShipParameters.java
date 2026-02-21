package com.nuketree3.example.kursprojectforsystemai.domain;

import javafx.scene.control.TextField;
import lombok.Data;

@Data
public class ShipParameters {
    private float shipLengthTextField;
    private float shipWidthTextField;
    private float draftOfTheShipTextField;
    private float metacentricHeightTextField;
    private float shipSpeedTextField;
    private float headingAngleTextField;
    private float waveLengthTextField;
    private float amplitudeOfTheOnBoardPitchingTextField;
    private float pitchingAmplitudeTextField;

    public ShipParameters(float shipLengthTextField, float shipWidthTextField,
                          float draftOfTheShipTextField, float metacentricHeightTextField,
                          float shipSpeedTextField, float headingAngleTextField,
                          float waveLengthTextField, float amplitudeOfTheOnBoardPitchingTextField,
                          float pitchingAmplitudeTextField) {
        this.shipLengthTextField = shipLengthTextField;
        this.shipWidthTextField = shipWidthTextField;
        this.draftOfTheShipTextField = draftOfTheShipTextField;
        this.metacentricHeightTextField = metacentricHeightTextField;
        this.shipSpeedTextField = shipSpeedTextField;
        this.headingAngleTextField = headingAngleTextField;
        this.waveLengthTextField = waveLengthTextField;
        this.amplitudeOfTheOnBoardPitchingTextField = amplitudeOfTheOnBoardPitchingTextField;
        this.pitchingAmplitudeTextField = pitchingAmplitudeTextField;

    }
}
