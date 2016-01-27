package callback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTemplate {

	// template method private final Object execute(StatementCallback action)
	// throws SQLException
	private final Object execute(StatementCallback action) throws SQLException {

		Connection con = null;// HsqldbUtil.getConnection();
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			ResultSet rs = null;// stmt.executeQuery(sql);
			// Object result = doInStatement(rs);//abstract method
			Object result = action.doInStatement(stmt);// abstract method
			return result;
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw ex;
		} finally {

			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (!con.isClosed()) {
					try {
						con.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Object query(StatementCallback stmt) throws SQLException {
		return execute(stmt);
	}

	// implements in subclass
	// protected abstract Object doInStatement(ResultSet rs);
}