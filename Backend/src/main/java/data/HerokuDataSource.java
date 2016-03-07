package data;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

public class HerokuDataSource extends PGSimpleDataSource implements DataSource {
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
	    String dbUrl = System.getenv("JDBC_DATABASE_URL");
	    return DriverManager.getConnection(dbUrl);
	}

}
