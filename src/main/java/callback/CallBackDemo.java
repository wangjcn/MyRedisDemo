package callback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CallBackDemo {

	// 匿名类方式
	public Object query2(final String sql) throws Exception {
		JdbcTemplate jt = new JdbcTemplate();
		return jt.query(new StatementCallback() {
			public Object doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = stmt.executeQuery(sql);
				List<User> userList = new ArrayList<User>();
				User user = null;
				while (rs.next()) {
					user = new User();
					user.setId(rs.getInt("id"));
					user.setUserName(rs.getString("user_name"));
					user.setBirth(rs.getDate("birth"));
					user.setCreateDate(rs.getDate("create_date"));
					userList.add(user);
				}
				return userList;
			}
		});
	}
}
