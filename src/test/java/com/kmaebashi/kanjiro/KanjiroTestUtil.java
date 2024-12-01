package com.kmaebashi.kanjiro;
import java.sql.DriverManager;
import java.sql.Connection;

public class KanjiroTestUtil {
    private KanjiroTestUtil() {}

    public static Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/kanjirotestdb?currentSchema=kanjiro",
                "kanjirouser", "XXXXXXX");
        return conn;
    }
}
