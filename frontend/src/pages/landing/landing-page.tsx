import { Link } from "react-router-dom";
import { Icon } from "@/components/ui/icon";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/use-auth";
import { paths, roleHome } from "@/app/routes";

/**
 * Public marketing landing page (adapted from the Stitch "placementpro_full_landing_page" design).
 * Rendered at "/". Anonymous visitors see Sign In / Get Started CTAs; an already-authenticated
 * user gets a "Go to dashboard" shortcut into their role home instead.
 */

const quickStats = [
  { icon: "work", value: "145+", label: "Active Jobs" },
  { icon: "apartment", value: "32+", label: "Universities" },
  { icon: "business", value: "520+", label: "Companies" },
  { icon: "group", value: "14,200+", label: "Students" },
  { icon: "verified", value: "98%", label: "Placement Success" },
  { icon: "support_agent", value: "24/7", label: "Support" },
];

const steps = [
  { icon: "description", title: "1. Company Posts Job", copy: "Companies create job postings with eligibility criteria" },
  { icon: "fact_check", title: "2. Placement Cell Approves", copy: "Placement cell reviews and approves the drive" },
  { icon: "notifications_active", title: "3. Eligible Students Notified", copy: "Only eligible students receive notifications" },
  { icon: "how_to_reg", title: "4. Students Apply", copy: "Students apply for jobs they are eligible for" },
  { icon: "groups", title: "5. Interview Process", copy: "Shortlisting and interviews are conducted" },
  { icon: "workspace_premium", title: "6. Offer Letter", copy: "Selected candidates receive offers" },
];

const roleCards = [
  {
    icon: "person",
    title: "For Students",
    accent: "text-primary bg-primary-fixed",
    linkAccent: "text-primary hover:text-on-primary-fixed-variant",
    to: paths.registerStudent,
    cta: "Explore as Student",
    points: [
      "Create your profile and showcase skills",
      "Discover opportunities that match you",
      "Apply in one click and track status",
      "Get notified about eligible drives",
    ],
  },
  {
    icon: "business",
    title: "For Companies",
    accent: "text-tertiary bg-tertiary-fixed",
    linkAccent: "text-tertiary hover:text-on-tertiary-fixed-variant",
    to: paths.registerCompany,
    cta: "Explore as Company",
    points: [
      "Post jobs and define eligibility criteria",
      "Review and shortlist candidates",
      "Manage interviews and offers",
      "Track recruitment progress",
    ],
  },
  {
    icon: "apartment",
    title: "For Placement Cells",
    accent: "text-on-secondary-container bg-secondary-container",
    linkAccent: "text-on-secondary-container hover:text-on-surface",
    to: paths.login,
    cta: "Sign in as Placement Cell",
    points: [
      "Approve and manage placement drives",
      "Ensure only eligible students apply",
      "Monitor placements and analytics",
      "Generate reports with ease",
    ],
  },
];

const bannerStats = [
  { value: "25+", label: "Universities" },
  { value: "15,000+", label: "Students" },
  { value: "700+", label: "Companies" },
  { value: "3,200+", label: "Placement Drives" },
];

const features = [
  { icon: "admin_panel_settings", title: "Role Based Access" },
  { icon: "verified", title: "Smart Eligibility" },
  { icon: "assignment", title: "Application Tracking" },
  { icon: "mail", title: "Email Notifications" },
  { icon: "event", title: "Drive Management" },
  { icon: "bar_chart", title: "Analytics & Reports" },
  { icon: "bolt", title: "AI Assistant" },
  { icon: "lock", title: "Secure & Reliable" },
];

const testimonials = [
  {
    quote: "This platform made the placement process so smooth and transparent. Got amazing opportunities!",
    name: "Ananya Verma",
    role: "B.Tech CSE, 2025",
  },
  {
    quote: "As a placement officer, this system saves us so much time and helps us focus on students.",
    name: "Rohit Mehta",
    role: "Placement Officer",
  },
  {
    quote: "We get quality candidates who fit our requirements. The analytics are a game changer!",
    name: "Sneha Kapoor",
    role: "HR Manager, TechSolutions",
  },
];

const faqs = [
  "How do I register on the platform?",
  "Is this platform free to use?",
  "How does eligibility filtering work?",
  "Can I track my application status?",
  "Can companies connect with students directly?",
  "Who can approve job postings?",
];

function initials(name: string) {
  return name
    .split(" ")
    .map((n) => n[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

export function LandingPage() {
  const { isAuthenticated, role } = useAuth();
  const dashboardTo = role ? roleHome[role] : paths.login;

  return (
    <div className="min-h-screen bg-background text-on-surface">
      {/* ------------------------------------------------------------------ Header */}
      <header className="sticky top-0 z-50 border-b border-outline-variant/40 bg-white/90 backdrop-blur">
        <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
          <Link to={paths.landing} className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-container text-xl font-bold text-white">
              P
            </div>
            <span className="text-xl font-bold tracking-tight">PlacementPro</span>
          </Link>

          <nav className="hidden items-center gap-8 md:flex">
            {["Features", "How It Works", "For Students", "For Companies"].map((item) => (
              <a
                key={item}
                href={`#${item.toLowerCase().replace(/\s+/g, "-")}`}
                className="text-sm font-medium text-on-surface-variant transition-colors hover:text-primary"
              >
                {item}
              </a>
            ))}
          </nav>

          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <Link to={dashboardTo}>
                <Button size="sm">Go to dashboard</Button>
              </Link>
            ) : (
              <>
                <Link
                  to={paths.login}
                  className="hidden rounded-md px-3 py-2 text-sm font-medium text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface sm:block"
                >
                  Sign In
                </Link>
                <Link to={paths.registerStudent}>
                  <Button size="sm">Get Started</Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main>
        {/* ---------------------------------------------------------------- Hero */}
        <section className="relative overflow-hidden bg-white pt-16 pb-24 lg:pt-24 lg:pb-32">
          <div className="pointer-events-none absolute -right-24 -top-24 h-96 w-96 rounded-full bg-primary-fixed opacity-40 blur-3xl" />
          <div className="relative z-10 mx-auto grid max-w-7xl items-center gap-12 px-4 sm:px-6 lg:grid-cols-2 lg:gap-8 lg:px-8">
            <div className="max-w-2xl">
              <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-primary-fixed bg-primary-fixed/50 px-3 py-1.5 text-sm font-medium text-on-primary-fixed-variant">
                <Icon name="school" className="text-lg" />
                Bridging Talent. Building Futures.
              </div>
              <h1 className="mb-6 text-4xl font-extrabold leading-tight tracking-tight text-on-surface sm:text-5xl lg:text-6xl">
                Smart Placement <br />
                Starts <span className="text-primary">Here.</span>
              </h1>
              <p className="mb-8 max-w-lg text-lg leading-relaxed text-on-surface-variant">
                A unified platform for Colleges, Companies and Students to connect, collaborate and
                create successful careers.
              </p>

              <div className="mb-10 flex flex-wrap gap-3">
                <Link to={paths.registerStudent}>
                  <Button size="lg" className="gap-2">
                    I'm a Student
                    <Icon name="person" className="text-xl" />
                  </Button>
                </Link>
                <Link to={paths.login}>
                  <Button size="lg" variant="outline" className="gap-2">
                    I'm a College
                    <Icon name="apartment" className="text-xl" />
                  </Button>
                </Link>
                <Link to={paths.registerCompany}>
                  <Button size="lg" variant="outline" className="gap-2">
                    I'm a Recruiter
                    <Icon name="business" className="text-xl" />
                  </Button>
                </Link>
              </div>

              <div className="flex items-center gap-4">
                <div className="flex -space-x-3">
                  {["AV", "RM", "SK", "PN"].map((i) => (
                    <div
                      key={i}
                      className="flex h-10 w-10 items-center justify-center rounded-full border-2 border-white bg-surface-container text-xs font-bold text-on-surface-variant"
                    >
                      {i}
                    </div>
                  ))}
                  <div className="z-10 flex h-10 w-10 items-center justify-center rounded-full border-2 border-white bg-primary-fixed text-xs font-bold text-on-primary-fixed-variant">
                    2K+
                  </div>
                </div>
                <p className="text-sm font-medium text-on-surface-variant">
                  Trusted by 2000+ students, colleges &amp; recruiters
                </p>
              </div>
            </div>

            <div className="relative lg:ml-auto">
              <img
                src="/image.png"
                alt="PlacementPro dashboard preview showing job listings and application analytics"
                className="mx-auto h-auto w-full max-w-xl rounded-2xl lg:translate-x-6"
                loading="eager"
              />
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------------------- Quick stats */}
        <section className="border-b border-outline-variant/40 bg-white py-12">
          <div className="mx-auto grid max-w-7xl grid-cols-2 gap-8 px-4 sm:px-6 md:grid-cols-3 lg:grid-cols-6 lg:px-8">
            {quickStats.map((s) => (
              <div key={s.label} className="flex items-center justify-center gap-4 sm:justify-start">
                <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-primary-fixed text-primary">
                  <Icon name={s.icon} className="text-2xl" />
                </div>
                <div>
                  <div className="text-2xl font-bold text-on-surface">{s.value}</div>
                  <div className="text-sm text-on-surface-variant">{s.label}</div>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* ---------------------------------------------------------------- How it works */}
        <section id="how-it-works" className="bg-background py-20">
          <div className="mx-auto max-w-7xl px-4 text-center sm:px-6 lg:px-8">
            <h2 className="mb-2 text-3xl font-bold text-on-surface">How It Works</h2>
            <div className="mx-auto mb-16 h-1 w-16 rounded-full bg-primary" />
            <div className="grid grid-cols-1 gap-8 md:grid-cols-3 lg:grid-cols-6">
              {steps.map((step, i) => (
                <div key={step.title} className="flex flex-col items-center">
                  <div
                    className={`mb-4 flex h-20 w-20 items-center justify-center rounded-full border-4 border-background shadow-sm ${
                      i === 1 ? "bg-primary-container text-white" : "bg-white text-primary"
                    }`}
                  >
                    <Icon name={step.icon} className="text-3xl" />
                  </div>
                  <h3 className="mb-2 text-sm font-bold text-on-surface">{step.title}</h3>
                  <p className="mx-auto max-w-[150px] text-xs text-on-surface-variant">{step.copy}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------------------- Built for every role */}
        <section id="for-students" className="bg-white py-20">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="mb-16 text-center">
              <h2 className="mb-2 text-3xl font-bold text-on-surface">
                Built for Every Role in Campus Recruitment
              </h2>
              <div className="mx-auto h-1 w-16 rounded-full bg-primary" />
            </div>
            <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
              {roleCards.map((card) => (
                <div
                  key={card.title}
                  className="rounded-xl border border-outline-variant/40 bg-white p-8 shadow-sm transition-shadow hover:shadow-md"
                >
                  <div className="mb-6 flex items-center gap-4">
                    <div className={`flex h-12 w-12 items-center justify-center rounded-lg ${card.accent}`}>
                      <Icon name={card.icon} className="text-2xl" />
                    </div>
                    <h3 className="text-xl font-bold text-on-surface">{card.title}</h3>
                  </div>
                  <ul className="mb-8 space-y-4">
                    {card.points.map((p) => (
                      <li key={p} className="flex items-start gap-3">
                        <Icon name="check" className="mt-0.5 shrink-0 text-lg text-primary" />
                        <span className="text-sm text-on-surface-variant">{p}</span>
                      </li>
                    ))}
                  </ul>
                  <Link
                    to={card.to}
                    className={`inline-flex items-center gap-1 text-sm font-medium ${card.linkAccent}`}
                  >
                    {card.cta}
                    <Icon name="arrow_forward" className="text-lg" />
                  </Link>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------------------- Stats banner */}
        <section className="bg-primary py-12">
          <div className="mx-auto grid max-w-7xl grid-cols-2 gap-8 px-4 text-center text-white sm:px-6 md:grid-cols-4 lg:px-8">
            {bannerStats.map((s) => (
              <div key={s.label}>
                <div className="mb-2 text-4xl font-bold">{s.value}</div>
                <div className="text-sm uppercase tracking-wide text-primary-fixed">{s.label}</div>
              </div>
            ))}
          </div>
        </section>

        {/* ---------------------------------------------------------------- Everything you need */}
        <section id="features" className="bg-background py-20">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="mb-16 text-center">
              <h2 className="mb-2 text-3xl font-bold text-on-surface">
                Everything You Need in One Platform
              </h2>
              <div className="mx-auto h-1 w-16 rounded-full bg-primary" />
            </div>
            <div className="grid grid-cols-2 gap-6 md:grid-cols-4">
              {features.map((f) => (
                <div
                  key={f.title}
                  className="rounded-xl border border-outline-variant/30 bg-white p-6 text-center transition-shadow hover:shadow-md"
                >
                  <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-primary-fixed text-primary">
                    <Icon name={f.icon} className="text-2xl" />
                  </div>
                  <h3 className="text-sm font-semibold text-on-surface">{f.title}</h3>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------------------- Testimonials */}
        <section className="bg-white py-20">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="mb-16 text-center">
              <h2 className="mb-2 text-3xl font-bold text-on-surface">What Our Users Say</h2>
              <div className="mx-auto h-1 w-16 rounded-full bg-primary" />
            </div>
            <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
              {testimonials.map((t) => (
                <div key={t.name} className="relative rounded-xl bg-background p-8">
                  <Icon name="format_quote" className="absolute right-6 top-4 text-5xl text-surface-container-highest" filled />
                  <p className="relative z-10 mb-6 text-sm italic text-on-surface-variant">"{t.quote}"</p>
                  <div className="flex items-center gap-4">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary-fixed text-sm font-bold text-primary">
                      {initials(t.name)}
                    </div>
                    <div>
                      <div className="text-sm font-semibold text-on-surface">{t.name}</div>
                      <div className="text-xs text-on-surface-variant">{t.role}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------------------- FAQ */}
        <section className="bg-background py-20">
          <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
            <div className="mb-12 text-center">
              <h2 className="mb-2 text-3xl font-bold text-on-surface">Frequently Asked Questions</h2>
              <div className="mx-auto h-1 w-16 rounded-full bg-primary" />
            </div>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              {faqs.map((q) => (
                <div
                  key={q}
                  className="flex cursor-pointer items-center justify-between rounded-lg border border-outline-variant/40 bg-white p-4 transition-colors hover:bg-surface-container-low"
                >
                  <span className="text-sm font-medium text-on-surface">{q}</span>
                  <Icon name="expand_more" className="text-xl text-on-surface-variant" />
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------------------- CTA banner */}
        <section className="py-16">
          <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="flex flex-col items-center justify-between gap-8 rounded-2xl bg-primary p-10 text-white shadow-xl md:flex-row md:p-14">
              <div className="max-w-2xl text-center md:text-left">
                <h2 className="mb-4 text-3xl font-bold">Ready to Transform Your Placement Process?</h2>
                <p className="text-lg text-primary-fixed">
                  Join thousands of students, companies and placement cells already using our platform.
                </p>
              </div>
              <div className="flex shrink-0 gap-4">
                <Link to={paths.registerStudent}>
                  <Button size="lg" variant="secondary">
                    Get Started Now
                  </Button>
                </Link>
                <Link to={paths.login}>
                  <Button
                    size="lg"
                    className="border border-white/30 bg-transparent text-white hover:bg-white/10"
                  >
                    Sign In
                  </Button>
                </Link>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* ------------------------------------------------------------------ Footer */}
      <footer className="border-t border-outline-variant/40 bg-white pb-8 pt-16">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="mb-12 grid grid-cols-2 gap-8 md:grid-cols-4 lg:grid-cols-6">
            <div className="col-span-2">
              <Link to={paths.landing} className="mb-4 flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-container text-xl font-bold text-white">
                  P
                </div>
                <span className="text-xl font-bold tracking-tight">PlacementPro</span>
              </Link>
              <p className="mb-6 max-w-sm text-sm text-on-surface-variant">
                Empowering campuses with intelligent recruitment solutions.
              </p>
            </div>
            {[
              { title: "Platform", links: ["Features", "How it Works", "Pricing", "Resources"] },
              { title: "For Students", links: ["Browse Jobs", "Track Applications", "Career Advice", "Help Center"] },
              { title: "For Companies", links: ["Post a Job", "Browse Candidates", "Pricing", "Resources"] },
            ].map((col) => (
              <div key={col.title}>
                <h3 className="mb-4 text-sm font-bold text-on-surface">{col.title}</h3>
                <ul className="space-y-3">
                  {col.links.map((l) => (
                    <li key={l}>
                      <span className="cursor-pointer text-sm text-on-surface-variant hover:text-primary">
                        {l}
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
          <div className="border-t border-outline-variant/40 pt-8 text-center text-sm text-on-surface-variant">
            © {new Date().getFullYear()} PlacementPro. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}
