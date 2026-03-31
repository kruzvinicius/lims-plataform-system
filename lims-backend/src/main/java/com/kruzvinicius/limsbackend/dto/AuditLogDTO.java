package com.kruzvinicius.limsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a single entry in the audit trail.
 * Includes entity context, who made the change, when, and on which field.
 */
@Data
@AllArgsConstructor
public class AuditLogDTO {
    /** Envers revision number. */
    private Long revisionId;

    /** Username of the user who triggered this change. */
    private String modifiedBy;

    /** When the change was recorded. */
    private String timestamp;

    /** Type of operation: MODIFIED, CREATED, DELETED. */
    private String action;

    /** Status or current state captured at this revision. */
    private String status;

    /** The kind of entity audited (e.g., "Sample", "TestResult"). */
    private String entityType;

    /** ID of the entity audited. */
    private Long entityId;

    /** Snapshot of a key value BEFORE the change (for diff display). */
    private String previousValue;

    /** Snapshot of the key value AFTER the change (for diff display). */
    private String newValue;

    /** Convenience constructor for backwards compatibility (without diff fields). */
    public AuditLogDTO(Long revisionId, String modifiedBy, String timestamp, String action, String status) {
        this.revisionId = revisionId;
        this.modifiedBy = modifiedBy;
        this.timestamp = timestamp;
        this.action = action;
        this.status = status;
    }
}
