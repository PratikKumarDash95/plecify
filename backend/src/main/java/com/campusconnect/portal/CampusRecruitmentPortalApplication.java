package com.campusconnect.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Campus Recruitment Portal backend.
 *
 * <p>Enables JPA auditing (createdAt/updatedAt/createdBy), async execution for the
 * notification + email engines, and scheduling for token/notification housekeeping.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@EnableScheduling
public class CampusRecruitmentPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusRecruitmentPortalApplication.class, args);
    }
}
