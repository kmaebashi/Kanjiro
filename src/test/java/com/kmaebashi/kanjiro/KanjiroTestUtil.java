package com.kmaebashi.kanjiro;
import com.kmaebashi.dbutil.NamedParameterPreparedStatement;
import com.kmaebashi.nctfw.DbAccessContext;

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

    public static int deleteAll(DbAccessContext context, String tableName) throws Exception {
        String sql = "DELETE FROM " + tableName;
        NamedParameterPreparedStatement npps
                = NamedParameterPreparedStatement.newInstance(context.getConnection(), sql);
        return npps.getPreparedStatement().executeUpdate();
    }
}
