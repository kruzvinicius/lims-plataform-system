package com.kruzvinicius.limsbackend.model.enums;

/** Lifecycle status of a non-conformance record. */
public enum NonConformanceStatus {
    /** NC detected and registered, not yet assigned. */
    OPEN,
    /** NC assigned and being actively investigated. */
    UNDER_INVESTIGATION,
    /** Root cause identified and corrective action defined. */
    RESOLVED,
    /** NC formally closed after preventive action is confirmed. */
    CLOSED
}
