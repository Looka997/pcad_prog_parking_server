package server.model;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractDao<T> implements Dao<T> {
	
	protected Connection connection;
	private String tableSQL;
	private String tableName;

	protected AbstractDao(String tableSQL, String tableName){
		this(tableSQL, tableName, ConnectionFactory.getConnection());
	}
	
	protected AbstractDao(String tableSQL, String tableName, Connection connection){
		this.connection = connection;
		this.tableSQL = tableSQL;
		this.tableName = tableName;
	}
	
	protected boolean checkTable(String tableName) {
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			ResultSet rs = dbmd.getTables(null, null, tableName, null);
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}  
		return false;
	}
	
	protected boolean createTable(String tableSQL){
        Statement stmt;
		try {
			stmt = connection.createStatement();
	        stmt.executeUpdate(tableSQL);
	        return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return false;
	}
	
	public boolean createTable() {
		return createTable(tableSQL);
	}
	
	public void load() {
    	if (!checkTable(tableName))
    		createTable(tableSQL);
	}
}
