package com.hiyura.bots.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/** SQLite sederhana. Satu koneksi, semua akses dari main thread pada Tahap 1. */
public final class Database implements AutoCloseable {

    private final Connection conn;

    public Database(File file, Logger log) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    extra_slots INTEGER NOT NULL DEFAULT 0
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS bots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner TEXT NOT NULL,
                    type TEXT NOT NULL,
                    name TEXT NOT NULL,
                    level INTEGER NOT NULL DEFAULT 1,
                    world TEXT NOT NULL,
                    x REAL NOT NULL, y REAL NOT NULL, z REAL NOT NULL,
                    yaw REAL NOT NULL DEFAULT 0,
                    state TEXT NOT NULL DEFAULT 'IDLE',
                    created_at INTEGER NOT NULL
                )""");
            st.execute("CREATE INDEX IF NOT EXISTS idx_bots_owner ON bots(owner)");
        }
        log.info("Database siap: " + file.getName());
    }

    public Connection connection() { return conn; }

    @Override
    public void close() {
        try { conn.close(); } catch (SQLException ignored) { }
    }
}
