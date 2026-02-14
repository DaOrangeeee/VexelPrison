package com.vexelcore.prison.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationService {
    public void migrate(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS schema_version (id INT PRIMARY KEY, version INT NOT NULL)");
            int current = 0;
            try (ResultSet rs = st.executeQuery("SELECT version FROM schema_version WHERE id=1")) {
                if (rs.next()) current = rs.getInt(1);
            }
            if (current < 1) {
                st.execute("INSERT INTO schema_version (id, version) VALUES (1,1) ON CONFLICT(id) DO UPDATE SET version=1");
            }
        }
    }
}
