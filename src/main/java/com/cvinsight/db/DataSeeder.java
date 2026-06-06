package com.cvinsight.db;

import com.cvinsight.db.dao.ExampleCVDao;
import com.cvinsight.model.CV;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Seeds the example_cvs table on first launch (or after a schema migration).
 * Clears old data if the new person_name / pdf_filename columns are missing.
 */
public class DataSeeder {

    private DataSeeder() {}

    public static void seedIfEmpty() {
        ExampleCVDao dao = new ExampleCVDao();
        try {
            List<CV> existing = dao.findAll();
            // Re-seed if empty OR if old data (no pdf_filename → sourceFile is null)
            boolean needsReseed = existing.isEmpty()
                || existing.stream().allMatch(cv -> cv.getSourceFile() == null);
            if (!needsReseed) return;
            if (!existing.isEmpty()) dao.deleteAll();
            insertSamples(dao);
        } catch (SQLException e) {
            System.err.println("DataSeeder: could not seed example CVs — " + e.getMessage());
        }
    }

    private static void insertSamples(ExampleCVDao dao) throws SQLException {

        dao.insert(UUID.randomUUID().toString(),
            "Google", "Senior Staff Software Engineer", "Engineering",
            "Alexandra Chen",
            """
            Alexandra CHEN
            San Francisco, CA  •  alex.chen@gmail.com  •  LinkedIn  •  GitHub

            ABOUT ME
            Senior Software Engineer with 9 years of experience at Google, specializing in distributed
            systems and machine learning infrastructure. Led cross-functional teams of 12+ engineers to
            deliver large-scale products used by over 500 million users globally. Passionate about
            building reliable, high-performance backend systems and mentoring the next generation of
            engineers.

            EDUCATION
            Stanford University — M.Sc. Computer Science, Distributed Systems  (2013–2015)
            University of California, Berkeley — B.Sc. Electrical Engineering & CS  (2009–2013)

            EXPERIENCE
            Senior Staff Software Engineer — Google DeepMind  |  Jan 2020 – Present
            • Architected the ML training infrastructure serving Google's large language model research,
              reducing model iteration time by 40%.
            • Led a team of 12 engineers across 3 time zones to redesign the distributed job scheduler,
              improving GPU utilization from 61% to 89%.
            • Designed and deployed a fault-tolerant data pipeline processing 4 petabytes of training
              data daily with 99.99% uptime.
            • Mentored 8 junior and mid-level engineers; 3 promoted to Staff Engineer.

            Software Engineer III — Google Search  |  Aug 2015 – Jan 2020
            • Built real-time indexing system handling 80,000 document updates per second with
              sub-50ms latency.
            • Reduced search ranking model serving cost by 30% via quantization and batching.
            • Contributed to open-source projects including TensorFlow Serving and Apache Beam.

            PROJECTS
            DistributedKV — Open Source Key-Value Store
            • Built a Raft-consensus based distributed key-value store in Go achieving 200k ops/sec.
            • 500+ GitHub stars; adopted by 3 early-stage startups as their primary storage layer.

            SKILLS
            Go, Python, C++, Java, Distributed Systems, ML Infrastructure, Kubernetes, Raft, gRPC,
            TensorFlow, JAX, Apache Spark, BigQuery, System Design, Technical Leadership, Mentorship
            """,
            95, "CV_Alexandra_Chen_Google_SWE.pdf");

        dao.insert(UUID.randomUUID().toString(),
            "Apple", "Principal Product Manager", "Product",
            "James Okafor",
            """
            James OKAFOR
            London, UK  •  james.okafor@gmail.com  •  LinkedIn

            ABOUT ME
            Principal Product Manager at Apple with 11 years of experience driving product strategy
            for consumer hardware and software. Launched 4 major product lines generating a combined
            $3.2B in revenue. Expert in translating complex user research into compelling product
            roadmaps and working closely with engineering, design, and marketing teams.

            EDUCATION
            London Business School — MBA, Strategy & Innovation  (2012–2014)
            University of Edinburgh — B.Eng. Electronics & Software Engineering  (2007–2011)

            EXPERIENCE
            Principal Product Manager — Apple (iPhone & iOS)  |  Mar 2019 – Present
            • Owned end-to-end product strategy for iPhone Camera software, driving a 22-point NPS
              increase across 3 consecutive generations.
            • Defined and shipped 6 major iOS features with combined install base of 1.1 billion
              active devices.
            • Established a mixed-methods user research framework across Apple's European product org.
            • Managed a portfolio roadmap across 3 concurrent product lines with teams of 60+.

            Senior Product Manager — Spotify  |  Jun 2014 – Mar 2019
            • Led monetization product strategy for Spotify's Premium tier, contributing to 35%
              subscriber growth YoY.
            • Launched Spotify's Discover Weekly — now with 40M weekly active users.
            • Built an A/B testing framework used across 200+ experiments per quarter.

            PROJECTS
            Accessible UX Initiative — Apple Internal
            • Championed VoiceOver and Dynamic Type improvements, reducing accessibility friction 38%.

            SKILLS
            Product Strategy, Roadmapping, OKRs, A/B Testing, User Research, SQL, Python, Figma,
            JIRA, Amplitude, Consumer Hardware, Mobile Software, Stakeholder Management
            """,
            93, "CV_James_Okafor_Apple_PM.pdf");

        dao.insert(UUID.randomUUID().toString(),
            "Netflix", "Lead Data Scientist", "Data Science",
            "Priya Ramaswamy",
            """
            Priya RAMASWAMY
            Amsterdam, Netherlands  •  priya.ramaswamy@gmail.com  •  LinkedIn  •  GitHub

            ABOUT ME
            Lead Data Scientist at Netflix with 8 years of experience building recommendation systems
            and experimentation platforms. Delivered models that measurably increased viewer engagement
            and reduced churn, impacting billions of streaming hours annually. Speaker at NeurIPS and
            RecSys; published 6 peer-reviewed papers on large-scale collaborative filtering.

            EDUCATION
            ETH Zurich — Ph.D. Machine Learning, Recommender Systems  (2013–2017)
            IIT Bombay — B.Tech. Computer Science & Engineering, Gold Medalist  (2009–2013)

            EXPERIENCE
            Lead Data Scientist — Netflix Personalization  |  Feb 2020 – Present
            • Designed the next-generation two-tower retrieval model serving 260M subscribers,
              increasing top-10 precision by 18%.
            • Built Netflix's causal inference framework for recommendation evaluation.
            • Led a team of 7 data scientists and 4 ML engineers.
            • Reduced model training cost by 45% through mixed-precision training techniques.

            Senior Data Scientist — Booking.com  |  Sep 2017 – Feb 2020
            • Developed a price sensitivity model predicting booking conversion with 91% AUC,
              deployed across 43 markets.
            • Owned the hotel ranking algorithm serving 1.5M daily searches; delivered €28M
              incremental revenue in year one.

            PUBLICATIONS
            'Causal Debiasing in Industrial Recommender Systems' — NeurIPS 2023
            'Scalable Two-Tower Models for Streaming Content Discovery' — RecSys 2022

            SKILLS
            Python, Scala, SQL, R, PyTorch, TensorFlow, Spark MLlib, Ray, Recommender Systems,
            Causal Inference, A/B Testing, NLP, Kubernetes, Airflow, MLflow, Databricks
            """,
            94, "CV_Priya_Ramaswamy_Netflix_DS.pdf");

        dao.insert(UUID.randomUUID().toString(),
            "Figma", "Senior UX Designer", "Design",
            "Lucas Berger",
            """
            Lucas BERGER
            Berlin, Germany  •  lucas.berger@gmail.com  •  LinkedIn  •  Dribbble

            ABOUT ME
            Senior UX Designer at Figma with 7 years of experience designing developer tools and
            design systems used by 8 million professionals worldwide. Combines deep expertise in
            interaction design, accessibility, and user research. 120,000+ Figma Community downloads.

            EDUCATION
            Royal College of Art, London — M.A. Interaction Design  (2015–2017)
            Hochschule für Gestaltung Offenbach — B.A. Visual Communication  (2011–2015)

            EXPERIENCE
            Senior UX Designer — Figma  |  Nov 2019 – Present
            • Redesigned Figma's component properties panel — adopted by 4M+ users, rated the
              most-requested feature ship of 2023 in user surveys.
            • Led UX for Figma's Dev Mode product, bridging the design-to-code gap for 500k users.
            • Defined and maintained Figma's internal design system (Grail) across 15 product squads.
            • Conducted 200+ user research sessions per year.

            UX Designer — Zalando  |  Jul 2017 – Nov 2019
            • Redesigned the checkout funnel for Zalando's mobile app (30M MAU), cutting cart
              abandonment by 14%.
            • Built a unified design token system across Android, iOS, and web.
            • Received Zalando 'Design Excellence Award' 2018.

            PROJECTS
            Figma Community — LayerBase Design System Kit
            • Published production-ready design system with 800+ components, 120,000+ downloads.

            SKILLS
            Figma, Sketch, Principle, Framer, Lottie, User Research, Usability Testing,
            Accessibility (WCAG 2.2), Design Systems, HTML, CSS, React, Workshop Facilitation
            """,
            91, "CV_Lucas_Berger_Figma_UX.pdf");

        dao.insert(UUID.randomUUID().toString(),
            "MIT", "Associate Professor, CS", "Academia",
            "Dr. Samuel Adeyemi",
            """
            Dr. Samuel ADEYEMI
            Cambridge, MA  •  sadeyemi@mit.edu  •  MIT CSAIL  •  Google Scholar

            ABOUT ME
            Associate Professor of Computer Science at MIT with a research focus on systems security
            and privacy-preserving computation. Director of the MIT Secure Systems Lab. Published 42
            peer-reviewed papers cited 6,800+ times. NSF CAREER Award recipient. Courses taken by
            3,000+ MIT and Harvard students.

            EDUCATION
            Carnegie Mellon University — Ph.D. Computer Science, Systems Security  (2009–2014)
            University of Lagos — B.Sc. Computer Engineering, First Class Honours  (2004–2008)

            EXPERIENCE
            Associate Professor — MIT EECS & CSAIL  |  Jul 2020 – Present
            • Lead the MIT Secure Systems Lab (12 PhD students, 3 postdocs); secured $4.7M in
              research grants from NSF, DARPA, and Google Research.
            • Teach 6.858 (Computer Systems Security) and 6.875 (Cryptography) — avg. 6.8/7.0.
            • Program Chair, IEEE S&P 2024; Area Chair, USENIX Security 2023 and 2024.

            Assistant Professor — University of Michigan  |  Sep 2014 – Jun 2020
            • Established the PrivSys research group; mentored 7 PhD graduates.
            • Recipient of NSF CAREER Award 2016 ($575,000).
            • Best Paper Award — USENIX Security 2018.

            SELECTED PUBLICATIONS
            'Confidential ML Training on Untrusted Hardware' — IEEE S&P 2024 (Best Paper Nominee)
            'MemGuard: Defeating Side-Channel Attacks' — CCS 2022
            'FairProof: Verifiable Fairness for ML Models Under ZK' — NeurIPS 2021

            SKILLS
            Systems Security, Cryptography, Privacy-Preserving ML, Trusted Execution Environments,
            C, C++, Rust, Python, x86/ARM Assembly, Research Mentorship, Grant Writing
            """,
            92, "CV_Samuel_Adeyemi_MIT_Professor.pdf");
    }
}
