package com.kruzvinicius.limsbackend.model.enums;

/**
 * Type of custody event registered in the chain of custody.
 */
public enum CustodyEventType {

    /** Sample physically received from client or courier. */
    RECEIVED,

    /** Sample transferred between lab sections or personnel. */
    TRANSFERRED,

    /** Sample returned to client or external party. */
    RETURNED,

    /** Sample disposed after analysis. */
    DISPOSED
}
