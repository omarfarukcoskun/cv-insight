package com.cvinsight.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * PATTERN: Singleton
 *
 * Ensures exactly one SQLite connection exists for the entire application.
 * All DAOs obtain their connection through DatabaseManager.getInstance().getConnection().
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cvinsight.db";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            initSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id           TEXT PRIMARY KEY,
                    username     TEXT NOT NULL UNIQUE,
                    email        TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    created_at   TEXT NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cvs (
                    id          TEXT PRIMARY KEY,
                    user_id     TEXT NOT NULL,
                    owner_name  TEXT,
                    raw_text    TEXT,
                    source_file TEXT,
                    uploaded_at TEXT NOT NULL,
                    status      TEXT NOT NULL DEFAULT 'PENDING',
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS cv_sections (
                    id           TEXT PRIMARY KEY,
                    cv_id        TEXT NOT NULL,
                    section_type TEXT NOT NULL,
                    title        TEXT,
                    content      TEXT,
                    FOREIGN KEY (cv_id) REFERENCES cvs(id)
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS scores (
                    id          TEXT PRIMARY KEY,
                    cv_id       TEXT NOT NULL UNIQUE,
                    overall     INTEGER NOT NULL,
                    analyzed_at TEXT NOT NULL,
                    FOREIGN KEY (cv_id) REFERENCES cvs(id)
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS feedbacks (
                    id          TEXT PRIMARY KEY,
                    cv_id       TEXT NOT NULL UNIQUE,
                    strengths   TEXT,
                    weaknesses  TEXT,
                    suggestions TEXT,
                    FOREIGN KEY (cv_id) REFERENCES cvs(id)
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS example_cvs (
                    id         TEXT PRIMARY KEY,
                    company    TEXT,
                    role       TEXT,
                    category   TEXT,
                    raw_text   TEXT,
                    score      INTEGER,
                    created_at TEXT NOT NULL
                )
            """);

            // Migration: add columns introduced in Week 6
            for (String ddl : new String[]{
                "ALTER TABLE example_cvs ADD COLUMN person_name TEXT",
                "ALTER TABLE example_cvs ADD COLUMN pdf_filename TEXT"
            }) {
                try { stmt.executeUpdate(ddl); } catch (SQLException ignored) {}
            }
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
