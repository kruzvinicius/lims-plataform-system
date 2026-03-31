package com.kruzvinicius.limsbackend.model.enums;

/** Outcome of a calibration check. */
public enum CalibrationResult {
    /** Equipment is within specification and approved for use. */
    PASS,
    /** Equipment is out of specification; must be taken out of service. */
    FAIL
}
