package com.kruzvinicius.limsbackend.model.enums;

/**
 * Represents the full lifecycle status of a laboratory sample.
 *
 * Flow:
 * PENDING_RECEIPT → RECEIVED → IN_ANALYSIS → PENDING_APPROVAL
 *   → APPROVED → RELEASED
 *   → REJECTED → REANALYSIS_REQUESTED → IN_REANALYSIS → PENDING_APPROVAL (cycle)
 */
public enum SampleStatus {

    /** Sample registered but not yet physically received in the lab. */
    PENDING_RECEIPT,

    /** Sample physically received and logged in the lab. */
    RECEIVED,

    /** Sample is currently being analysed. */
    IN_ANALYSIS,

    /** Analysis complete, awaiting supervisor approval. */
    PENDING_APPROVAL,

    /** Results approved by supervisor, ready to be released. */
    APPROVED,

    /** Official results released to the customer. */
    RELEASED,

    /** Sample or results rejected (contamination, QC failure, etc.). */
    REJECTED,

    /** Reanalysis formally requested after rejection. */
    REANALYSIS_REQUESTED,

    /** Sample is being reanalysed. */
    IN_REANALYSIS
}