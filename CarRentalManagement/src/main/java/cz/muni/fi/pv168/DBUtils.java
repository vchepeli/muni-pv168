package cz.muni.fi.pv168;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class DBUtils {

    public static void closeQuietly(Connection connection, Statement... statements) {

        for (Statement statement : statements) {
            if (null != statement) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error when closing statement");
                }
            }
        }
        if (null != connection) {
            try {
                connection.close();

            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error when closing connection");
            }
        }
    }

    public static void tryCreateTables(DataSource dataSource) throws SQLException {

        try {
            createTables(dataSource);
            logger.warning("Tables created");
        } catch (SQLException ex) {
            if ("XOY32".equals(ex.getSQLState())) {
                // This code represents "Table view already exists"
                // This code is Derby specific!
            } else {
                throw ex;
            }
        }
    }

    public static Long getID(ResultSet keys) throws SQLException {
        if (keys.next()) {
            Long result = keys.getLong(1);
            if (keys.next()) {
                throw new IllegalArgumentException("Given ResultSet contains more rows");
            }
            return result;
        } else {
            throw new IllegalArgumentException("Given ResultSet contains no rows");
        }
    }

    private static String[] readataSourceqlStatements(URL url) {
        try {
            char buffer[] = new char[256];
            StringBuilder result = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8");
            while (true) {
                int count = reader.read(buffer);
                if (count < 0) {
                    break;
                }
                result.append(buffer, 0, count);
            }
            return result.toString().split(";");
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read " + url, ex);
        }
    }

    public static void createTables(DataSource dataSource) throws SQLException {
        executeSqlScript(dataSource, "createTables.sql");
    }

    public static void dropTables(DataSource dataSource) throws SQLException {
        executeSqlScript(dataSource, "dropTables.sql");
    }

    private static void executeSqlScript(DataSource dataSource, String scriptName) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            URL url = DBUtils.class.getResource(scriptName);
            for (String sqlStatement : readataSourceqlStatements(url)) {
                if (!sqlStatement.trim().isEmpty()) {
                    connection.prepareStatement(sqlStatement).executeUpdate();
                }
            }
        } catch (SQLException ex) {
            // Write some good code
        } finally {
            closeQuietly(connection);
        }
    }

    public static boolean equalsDates(Date date1, Date date2) {
        if ((null == date1) && (null == date2)) {
            return true;
        }
        if ((null == date1 && null != date2) || (null != date1 && null == date2)) {
            return false;
        }
        if (date1.toString().equals(date2.toString())) {
            return true;
        }
        return false;
    }
    private static final Logger logger = Logger.getLogger(DBUtils.class.getName());
}
