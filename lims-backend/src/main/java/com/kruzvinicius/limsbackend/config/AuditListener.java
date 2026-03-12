package com.kruzvinicius.limsbackend.config;

import com.kruzvinicius.limsbackend.model.Revision;
import org.hibernate.envers.RevisionListener;

public class AuditListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        Revision rev = (Revision) revisionEntity;
        rev.setModifiedBy("Vinicius Cruz");
    }
}
