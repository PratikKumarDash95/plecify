package com.campusconnect.portal.service.email;

/**
 * Outbound transactional email. Implementations must be non-blocking from the caller's
 * perspective (dispatched asynchronously) and must never propagate a delivery failure back
 * into the triggering business transaction — a bounced email must not roll back a signup.
 */
public interface EmailService {

    /**
     * Sends an email-verification link to a freshly registered user.
     *
     * @param toEmail     recipient address
     * @param recipientName display name for the greeting
     * @param verifyUrl   fully-formed verification URL (token embedded)
     */
    void sendEmailVerification(String toEmail, String recipientName, String verifyUrl);

    /**
     * Sends a password-reset link.
     *
     * @param toEmail     recipient address
     * @param recipientName display name for the greeting
     * @param resetUrl    fully-formed password-reset URL (token embedded)
     */
    void sendPasswordReset(String toEmail, String recipientName, String resetUrl);

    /**
     * Sends a one-time login code (2FA) that the user must enter to complete sign-in.
     *
     * @param toEmail     recipient address
     * @param recipientName display name for the greeting
     * @param code        the numeric one-time code
     * @param expiryMinutes how long the code stays valid, in minutes (for the copy)
     */
    void sendLoginOtp(String toEmail, String recipientName, String code, long expiryMinutes);

    /**
     * Notifies a company that their job posting was approved and is now live to students.
     *
     * @param toEmail       company contact address
     * @param recipientName display name for the greeting
     * @param jobTitle      the approved job's title
     * @param eligibleCount number of students the engine matched
     * @param jobUrl        deep link to the job in the company portal
     */
    void sendJobApproved(String toEmail, String recipientName, String jobTitle,
                         int eligibleCount, String jobUrl);

    /**
     * Notifies a company that their job posting was rejected by the placement cell.
     *
     * @param toEmail       company contact address
     * @param recipientName display name for the greeting
     * @param jobTitle      the rejected job's title
     * @param reason        reason supplied by the placement cell
     * @param jobUrl        deep link to the job in the company portal
     */
    void sendJobRejected(String toEmail, String recipientName, String jobTitle,
                         String reason, String jobUrl);

    /**
     * Notifies a student that a newly approved job matches their profile.
     *
     * @param toEmail       student address
     * @param recipientName display name for the greeting
     * @param jobTitle      the newly eligible job's title
     * @param companyName   hiring company
     * @param jobUrl        deep link to the job in the student portal
     */
    void sendNewEligibleJob(String toEmail, String recipientName, String jobTitle,
                           String companyName, String jobUrl);

    /**
     * Notifies a company that a student has applied to one of their jobs.
     *
     * @param toEmail       company contact address
     * @param recipientName display name for the greeting
     * @param studentName   the applicant
     * @param jobTitle      the job applied to
     * @param applicationUrl deep link to the application in the company portal
     */
    void sendApplicationReceived(String toEmail, String recipientName, String studentName,
                                String jobTitle, String applicationUrl);
}
