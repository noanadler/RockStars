package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

@SuppressWarnings("serial")
public class HerokuDataSource extends PGSimpleDataSource implements DataSource {
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
	    String dbUrl = System.getenv("JDBC_DATABASE_URL");

		if(System.getenv("JDBC_DATABASE_URL") == null) {
		    dbUrl = "jdbc:postgresql://ec2-54-83-201-196.compute-1.amazonaws.com:5432/d85bpoit51rit4?user=zbveiihtcvxqzi&password=lsWrOZHYZZT7TnBylzxXM3wxIz&sslmode=require";
		}
		
		return DriverManager.getConnection(dbUrl);
	}

}
