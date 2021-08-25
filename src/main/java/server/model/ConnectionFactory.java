package server.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.h2.Driver;


public class ConnectionFactory {

	public static final String URL = "jdbc:h2:./db/mytestdb";
	public static final String USER = "testuser";
	public static final String PSW = "testpsw";

    public static Connection getConnection()
    {
      try {
          DriverManager.registerDriver(new Driver());
          return DriverManager.getConnection(URL, USER, PSW);
      } catch (SQLException ex) {
          throw new RuntimeException("Error connecting to the database", ex);
      }
    }
    
    public static Connection getConnection(String URL)
    {
      try {
          DriverManager.registerDriver(new Driver());
          return DriverManager.getConnection(URL, USER, PSW);
      } catch (SQLException ex) {
          throw new RuntimeException("Error connecting to the database", ex);
      }
    }

}