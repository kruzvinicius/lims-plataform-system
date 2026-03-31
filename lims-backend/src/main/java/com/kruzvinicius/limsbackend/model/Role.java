package com.kruzvinicius.limsbackend.model;

/**
 * System roles mapped to laboratory functions.
 * Used for RBAC via Spring Security's @PreAuthorize.
 */
public enum Role {
    /** Full system access. Manages users, configuration, and all modules. */
    ADMIN,
    /** Lab manager: approves results, manages NCs, views reports, manages equipment. */
    MANAGER,
    /** Lab analyst: executes analyses, records results, transitions samples. */
    ANALYST,
    /** Lab technician: receives samples, registers custody events. */
    TECHNICIAN,
    /** External client: read-only access to own samples and released results. */
    CLIENT
}
