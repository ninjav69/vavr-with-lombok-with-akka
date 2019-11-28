package servers.brokerbase.bbimport;

import com.opencsv.CSVReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class TestBrokerbaseImport {

    private static Connection con;

    @Before
    public void setup() throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.OracleDriver");
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@meboralobdev101.metmom.mmih.biz:1521:test3",
                "testuser3", "password3");

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from v$version");
        while (rs.next()) {
            System.out.println(rs.getString(1) + "  " + rs.getInt(2));
        }
    }

    @After
    public void teardown() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException e) {
            }
        }
    }

    @Test
    public void doBrokerbaseImport() {
        cleanup(con);
        init();
        seedBranch();
        importAdviser();
        importBranch();
        importBrokerbase();
        importBroker();

        process();

        exportAdviser();
        exportBranch();
        exportBrokerhouse();
        exportBroker();
    }

    private static void exportBroker() {
    }

    private static void exportBrokerhouse() {
    }

    private static void exportBranch() {
    }

    private static void exportAdviser() {
    }

    private static void process() {
    }

    private static void importBroker() {
    }

    private static void importBrokerbase() {
    }

    private static void importBranch() {
    }

    private static void importAdviser() {
    }

    private static void seedBranch() {
    }

    private static void init() {
    }

    private static void cleanup(Connection con) {
        System.out.println("Cleaning up...");

        try {
            final Statement stmt = con.createStatement();
            stmt.executeQuery("DROP TABLE B_AD");
            stmt.executeQuery("DROP TABLE B_BRANCH");
            stmt.executeQuery("DROP TABLE B_BH");
            stmt.executeQuery("DROP TABLE B_BROKER");
        } catch (SQLException e) {
            throw new BBImportException();
        }
    }

    public static class BBImportException extends RuntimeException {
    }
}
