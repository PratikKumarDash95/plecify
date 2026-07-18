package com.campusconnect.portal.mapper;

import com.campusconnect.portal.dto.student.ApplicationResponse;
import com.campusconnect.portal.entity.Application;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.Job;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Application} entities to their API projection. Assumes the job and company
 * associations are initialised (repositories fetch them via join fetch), so no lazy loads
 * happen during mapping.
 */
@Component
public class ApplicationMapper {

    public ApplicationResponse toResponse(Application application) {
        Job job = application.getJob();
        Company company = job.getCompany();
        return new ApplicationResponse(
                application.getId(),
                job.getId(),
                job.getTitle(),
                company.getId(),
                company.getName(),
                application.getStatus(),
                application.getResumeUrl(),
                application.getCoverLetter(),
                application.getInterviewAt(),
                application.getInterviewDetails(),
                application.getStatusNote(),
                application.getLastStatusChangeAt(),
                application.getCreatedAt());
    }
}
