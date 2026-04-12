package com.cvinsight.db;

import com.cvinsight.db.dao.ExampleCVDao;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Seeds the example_cvs table with sample CVs on first launch.
 * Does nothing if the table already has data.
 */
public class DataSeeder {

    private DataSeeder() {}

    public static void seedIfEmpty() {
        ExampleCVDao dao = new ExampleCVDao();
        try {
            if (!dao.findAll().isEmpty()) return; // already seeded
            insertSamples(dao);
        } catch (SQLException e) {
            System.err.println("DataSeeder: could not seed example CVs — " + e.getMessage());
        }
    }

    private static void insertSamples(ExampleCVDao dao) throws SQLException {

        dao.insert(UUID.randomUUID().toString(), "Google", "Software Engineer", "Tech", """
                John Smith
                Software Engineer

                SUMMARY
                Experienced software engineer with 5 years building scalable backend systems at
                high-growth startups. Passionate about distributed systems and developer tooling.

                EXPERIENCE
                Senior Software Engineer — Stripe (2021–present)
                • Designed and shipped a fraud-detection pipeline processing 10M events/day
                • Reduced P99 API latency by 40% through query optimisation and caching
                • Mentored 3 junior engineers; led bi-weekly architecture reviews

                Software Engineer — Dropbox (2019–2021)
                • Built real-time file-sync conflict resolution in Go
                • Contributed to open-source FUSE driver used by 200k+ users

                EDUCATION
                B.S. Computer Science — MIT, 2019  (GPA 3.9/4.0)

                SKILLS
                Go, Java, Python, PostgreSQL, Redis, Kafka, Kubernetes, AWS
                """, 91);

        dao.insert(UUID.randomUUID().toString(), "Meta", "Product Manager", "PM", """
                Sarah Johnson
                Product Manager

                SUMMARY
                Data-driven PM with 6 years shipping consumer products at scale.
                Strong background in growth and monetisation experiments.

                EXPERIENCE
                Senior Product Manager — Airbnb (2020–present)
                • Owned host-onboarding funnel; lifted completion rate by 18% (A/B tested)
                • Defined roadmap for payments localisation across 12 new markets
                • Collaborated with design, engineering, data science, and legal teams

                Product Manager — LinkedIn (2018–2020)
                • Launched Job Alerts feature to 50M users; drove 23% increase in applies
                • Managed backlog of 80+ items; ran weekly sprint reviews

                EDUCATION
                MBA — Wharton School, 2018
                B.A. Economics — UC Berkeley, 2015

                SKILLS
                Product strategy, roadmapping, SQL, Amplitude, Figma, Jira, OKRs
                """, 87);

        dao.insert(UUID.randomUUID().toString(), "Figma", "UX Designer", "Design", """
                Emily Chen
                UX / Product Designer

                SUMMARY
                Designer with 4 years crafting intuitive experiences for B2B SaaS products.
                Fluent in end-to-end design: research → wireframes → high-fidelity → handoff.

                EXPERIENCE
                Senior UX Designer — Notion (2022–present)
                • Redesigned the sidebar navigation; reduced task-completion time by 31%
                • Ran 20+ moderated usability sessions per quarter
                • Built and maintained the Notion design system (600+ components)

                UX Designer — Atlassian (2020–2022)
                • Shipped onboarding flows for Jira Cloud used by 3M new users/year
                • Partnered with PM and engineering from discovery through launch

                EDUCATION
                B.F.A. Interaction Design — RISD, 2020

                SKILLS
                Figma, Protopie, Maze, HTML/CSS, design systems, accessibility (WCAG 2.1)
                """, 83);

        dao.insert(UUID.randomUUID().toString(), "Goldman Sachs", "Financial Analyst", "Finance", """
                Michael Torres
                Financial Analyst

                SUMMARY
                CFA Level II candidate with 3 years in investment banking (M&A advisory).
                Strong modelling skills; comfortable presenting to C-suite stakeholders.

                EXPERIENCE
                Analyst — JPMorgan M&A (2022–present)
                • Built LBO and DCF models for 9 closed transactions ($200M–$2B deal size)
                • Prepared CIM, management presentations, and board-level materials
                • Coordinated due diligence across legal, accounting, and strategy teams

                Summer Analyst — Lazard (2021)
                • Supported live sell-side mandate in TMT sector
                • Created comparable-company and precedent-transaction analyses

                EDUCATION
                B.S. Finance — NYU Stern, 2022  (GPA 3.8/4.0)

                SKILLS
                Excel (VBA), PowerPoint, Bloomberg, Capital IQ, SQL, financial modelling
                """, 79);

        dao.insert(UUID.randomUUID().toString(), "NHS", "Operations Manager", "Other", """
                Rachel Adams
                Operations Manager

                SUMMARY
                Operations leader with 7 years improving processes in healthcare and logistics.
                Track record of cutting costs while maintaining service quality.

                EXPERIENCE
                Operations Manager — NHS Trust (2020–present)
                • Reduced patient-discharge bottlenecks by 25% through lean process redesign
                • Managed £4M annual budget and a team of 18 coordinators
                • Implemented new scheduling system adopted across 3 hospitals

                Supply Chain Analyst — DHL (2017–2020)
                • Optimised last-mile delivery routes; saved £600k/year in fuel costs
                • Coordinated with 40+ depot managers across the UK

                EDUCATION
                M.Sc. Operations Management — University of Manchester, 2017
                B.Sc. Business Administration — University of Leeds, 2015

                SKILLS
                Lean / Six Sigma (Green Belt), SAP, Excel, Power BI, stakeholder management
                """, 74);
    }
}
