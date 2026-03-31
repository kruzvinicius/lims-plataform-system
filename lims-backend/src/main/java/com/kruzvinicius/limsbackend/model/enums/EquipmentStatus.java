package com.kruzvinicius.limsbackend.model.enums;

/** Operational status of a piece of laboratory equipment. */
public enum EquipmentStatus {
    /** Equipment is operational and available for use. */
    ACTIVE,
    /** Equipment is temporarily out of service. */
    INACTIVE,
    /** Equipment is being serviced or repaired. */
    UNDER_MAINTENANCE,
    /** Equipment has been permanently retired. */
    DECOMMISSIONED
}
