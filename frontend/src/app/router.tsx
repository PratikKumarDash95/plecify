import { createBrowserRouter, Navigate } from "react-router-dom";
import { LandingPage } from "@/pages/landing/landing-page";
import { LoginPage } from "@/pages/auth/login-page";
import { RegisterStudentPage } from "@/pages/auth/register-student-page";
import { RegisterCompanyPage } from "@/pages/auth/register-company-page";
import { ForgotPasswordPage } from "@/pages/auth/forgot-password-page";
import { ResetPasswordPage } from "@/pages/auth/reset-password-page";
import { VerifyEmailPage } from "@/pages/auth/verify-email-page";
import { StudentDashboardPage } from "@/pages/student/student-dashboard-page";
import { EligibleJobsPage } from "@/pages/student/eligible-jobs-page";
import { ApplicationsPage } from "@/pages/student/applications-page";
import { CompanyDashboardPage } from "@/pages/company/company-dashboard-page";
import { CompanyJobsPage } from "@/pages/company/company-jobs-page";
import { CompanyJobNewPage } from "@/pages/company/company-job-new-page";
import { CompanyJobEditPage } from "@/pages/company/company-job-edit-page";
import { CompanyJobDetailPage } from "@/pages/company/company-job-detail-page";
import { PlacementDashboardPage } from "@/pages/placement/placement-dashboard-page";
import { PlacementPendingPage } from "@/pages/placement/placement-pending-page";
import { PlacementJobDetailPage } from "@/pages/placement/placement-job-detail-page";
import { ProtectedRoute } from "@/features/auth/protected-route";
import { DashboardLayout } from "@/components/layout/dashboard-layout";
import { paths } from "./routes";

/**
 * Application router.
 *
 * Public auth routes live at the top level. Every authenticated route sits inside a
 * <ProtectedRoute allow={[...]}> section keyed to a single role: unauthenticated users are sent
 * to /login, and a signed-in user who hits a path outside their role is redirected to their own
 * role's home (see ProtectedRoute). This is defence-in-depth on the client — the backend still
 * authorizes every request — but it stops a STUDENT from ever rendering a COMPANY or
 * PLACEMENT_CELL screen, and vice versa.
 */
export const router = createBrowserRouter([
  { path: paths.landing, element: <LandingPage /> },
  { path: paths.login, element: <LoginPage /> },
  { path: paths.registerStudent, element: <RegisterStudentPage /> },
  { path: paths.registerCompany, element: <RegisterCompanyPage /> },
  { path: paths.forgotPassword, element: <ForgotPasswordPage /> },
  { path: paths.resetPassword, element: <ResetPasswordPage /> },
  { path: paths.verifyEmail, element: <VerifyEmailPage /> },

  // ---- Student area -------------------------------------------------------
  {
    element: <ProtectedRoute allow={["STUDENT"]} />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { path: paths.studentDashboard, element: <StudentDashboardPage /> },
          { path: paths.studentJobs, element: <EligibleJobsPage /> },
          { path: paths.studentApplications, element: <ApplicationsPage /> },
        ],
      },
    ],
  },

  // ---- Company area -------------------------------------------------------
  {
    element: <ProtectedRoute allow={["COMPANY"]} />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { path: paths.companyDashboard, element: <CompanyDashboardPage /> },
          { path: paths.companyJobs, element: <CompanyJobsPage /> },
          { path: paths.companyJobNew, element: <CompanyJobNewPage /> },
          { path: paths.companyJobDetail(), element: <CompanyJobDetailPage /> },
          { path: paths.companyJobEdit(), element: <CompanyJobEditPage /> },
        ],
      },
    ],
  },

  // ---- Placement cell area (ADMIN shares this view; see roleHome) ---------
  {
    element: <ProtectedRoute allow={["PLACEMENT_CELL", "ADMIN"]} />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { path: paths.placementDashboard, element: <PlacementDashboardPage /> },
          { path: paths.placementPending, element: <PlacementPendingPage /> },
          { path: paths.placementJobDetail(), element: <PlacementJobDetailPage /> },
        ],
      },
    ],
  },

  { path: "*", element: <Navigate to={paths.login} replace /> },
]);
