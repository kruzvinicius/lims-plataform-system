package com.kruzvinicius.limsbackend.model.enums;

/** Classification of the type of non-conformance detected. */
public enum NonConformanceType {
    /** Analytical result is outside the acceptance limit. */
    RESULT_OUT_OF_RANGE,
    /** Deviation from the established SOP or workflow. */
    PROCESS_DEVIATION,
    /** Internal quality control failure (blank, standard, duplicate). */
    QC_FAILURE,
    /** Equipment malfunction or out-of-calibration condition. */
    EQUIPMENT_FAILURE,
    /** Any other non-conformance not covered above. */
    OTHER
}
