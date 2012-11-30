package org.togglz.core.repository.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.util.DbUtils;

class SchemaUpdater {

    private static final String V1_CREATE_TABLE = "CREATE TABLE %TABLE% (FEATURE_NAME CHAR(100) PRIMARY KEY, FEATURE_ENABLED INTEGER, FEATURE_USERS CHAR(2000))";

    private final Log log = LogFactory.getLog(SchemaUpdater.class);

    private final Connection connection;

    private final String tableName;

    protected SchemaUpdater(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    protected void migrate() throws SQLException {

        // schema version 1
        if (!doesTableExist()) {
            log.info("Creating initial version of Togglz database table...");
            migrateToVersion1();
        }

    }

    protected boolean doesTableExist() throws SQLException {

        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();

        ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[] { "TABLE" });
        try {
            return resultSet.next();
        } finally {
            DbUtils.closeQuietly(resultSet);
        }

    }

    protected void migrateToVersion1() throws SQLException {
        Statement statement = connection.createStatement();
        try {
            statement.executeUpdate(insertTableName(V1_CREATE_TABLE));
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private String insertTableName(String s) {
        return s.replace("%TABLE%", tableName);
    }

}
