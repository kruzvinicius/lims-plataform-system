package com.kruzvinicius.limsbackend.model.enums;

/** Lifecycle status of a service order. */
public enum ServiceOrderStatus {
    /** OS created but not yet assigned to an analyst. */
    CREATED,
    /** OS assigned to an analyst, pending start. */
    ASSIGNED,
    /** Analysis work is in progress. */
    IN_PROGRESS,
    /** All analyses completed. */
    COMPLETED,
    /** OS cancelled before completion. */
    CANCELLED
}
