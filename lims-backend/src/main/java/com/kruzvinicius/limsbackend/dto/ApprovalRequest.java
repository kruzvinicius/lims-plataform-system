package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Generic request DTO for approve/reject actions on samples or results.
 * The 'reason' field is required only for rejection — validated at service level.
 */
public record ApprovalRequest(
        @NotBlank(message = "Reviewer username is required")
        String reviewerUsername,

        /** Reason for rejection. Required when action = REJECT; optional for approval. */
        String reason
) {}
