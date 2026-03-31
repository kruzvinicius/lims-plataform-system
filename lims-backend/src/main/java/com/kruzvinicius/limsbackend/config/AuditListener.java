package com.kruzvinicius.limsbackend.config;

import com.kruzvinicius.limsbackend.model.Revision;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Captures the currently authenticated user for every Hibernate Envers revision.
 * This ensures every audit record knows who performed the action.
 */
public class AuditListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        Revision rev = (Revision) revisionEntity;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            rev.setModifiedBy(authentication.getName());
        } else {
            rev.setModifiedBy("system");
        }
    }
}
