package com.kruzvinicius.limsbackend.model.enums;

/**
 * Approval status for an individual analytical result.
 */
public enum TestResultStatus {

    /** Result submitted by analyst, awaiting supervisor review. */
    PENDING,

    /** Result approved by supervisor. */
    APPROVED,

    /** Result rejected by supervisor (requires correction or reanalysis). */
    REJECTED
}
