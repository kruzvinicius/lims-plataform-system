package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import com.kruzvinicius.limsbackend.config.AuditListener;

import java.util.Date;

@Entity
@Table(name = "revisions")
@RevisionEntity(AuditListener.class)
@Data

public class Revision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    private Date timestamp;

    private String modifiedBy;
}
