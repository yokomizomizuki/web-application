package com.example;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController // リクエストを受けとるクラスを宣言
public class Example {

	// DB接続情報
	String url = "jdbc:postgresql://localhost:5432/postgres";
	String username = "postgres";
	String password = "Yokomirpc";

	@GetMapping("/api/users") // 呼び出されるメソッドを宣言
	public List<String> getUsers(@RequestParam("userMatch") Integer value,
			@RequestParam(name = "userKeyword", required = false) String keyword) throws SQLException {
		List<String> userIds = new ArrayList<>();
		List<String> userNames = new ArrayList<>();

		// 実行SQL
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT *").append("FROM users ");

		boolean useParameter = false;
		if (value == 0) {
			sb.append("WHERE name = ? ");
			useParameter = true;
		} else if (value == 1) {
			sb.append("WHERE name LIKE ? ");
			useParameter = true;
		}

		String sql = sb.toString();
		// リソースを自動的にクローズ
		try (Connection connection = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement statement = connection.prepareStatement(sql)) {// preparedStatementメソッドで実行するSQLを設定,戻り値はクラス

			if (useParameter) {
				if (value == 0) {
					statement.setString(1, keyword); // 完全一致
				} else if (value == 1) {
					statement.setString(1, "%" + keyword + "%"); // 部分一致
				}
			}

			try (ResultSet resultSet = statement.executeQuery()) {// 戻り値はResultSetで取得しレコード格納
				while (resultSet.next()) { // nextメソッドでレコードを一行ずつ呼び込む
					userIds.add(resultSet.getString("id")); // カラム名、idの値を取得
					userNames.add(resultSet.getString("name")); // カラム名、nameの値を取得
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			List<String> result = new ArrayList<>();
			result.addAll(userIds);
			result.addAll(userNames);
			return result;
		}
	}

	@GetMapping("/api/skills") // 呼び出されるメソッドを宣言
	public List<String> getSkills(@RequestParam("skillMatch") Integer value,
			@RequestParam(name = "skillKeyword", required = false) String keyword,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "sortOrder", required = false) String sortOrder) throws SQLException {
		List<String> names = new ArrayList<>();
		List<String> skills = new ArrayList<>();

		if (!"sName".equals(sortBy) && !"sSkill".equals(sortBy)) {
			sortBy = "sName";
		}
		if (!"sAsc".equals(sortOrder) && !"sDesc".equals(sortOrder)) {
			sortOrder = "sAsc";
		}

		// 実行SQL
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT users.name, skills.skill ").append("FROM users ")
				.append("JOIN skills ON users.id = skills.user_id ");

		boolean useParameter = false;
		if (value == 0) {
			sb.append("WHERE skills.skill = ? ");
			useParameter = true;
		} else if (value == 1) {
			sb.append("WHERE skills.skill LIKE ? ");
			useParameter = true;
		}

		// ORDER BY 部分を追加
		String orderColumn;
		if ("sName".equals(sortBy)) {
			orderColumn = "users.name";
		} else { // sSkill
			orderColumn = "skills.skill";
		}

		String orderDir = ("sAsc".equals(sortOrder) ? "ASC" : "DESC");
		sb.append("ORDER BY ").append(orderColumn).append(" ").append(orderDir);

		String sql = sb.toString();

		// リソースを自動的にクローズ
		try (Connection connection = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement statement = connection.prepareStatement(sql)) {

			if (useParameter) {
				if (value == 0) {
					statement.setString(1, keyword); // 完全一致
				} else if (value == 1) {
					statement.setString(1, "%" + keyword + "%"); // 部分一致
				}
			}

			try (ResultSet rs = statement.executeQuery()) {// 戻り値はResultSetで取得しレコード格納
				while (rs.next()) { // nextメソッドでレコードを一行ずつ呼び込む
					names.add(rs.getString("name")); // カラム名、nameの値を取得
					skills.add(rs.getString("skill")); // カラム名、skillの値を取得
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			List<String> result = new ArrayList<>();
			result.addAll(names);
			result.addAll(skills);
			return result;
		}
	}

	@DeleteMapping({ "/api/users/{id}", "/api/users/{name}/skills/{skill}" })
	public String delete(@PathVariable(value = "id", required = false) Integer id,
			@PathVariable(value = "name", required = false) String name,
			@PathVariable(value = "skill", required = false) String skill) throws FileNotFoundException {

		// try〜catch〜resources構文を使用
		try (Connection conn = DriverManager.getConnection(url, username, password)) { // JDBC: DBアクセスするためのAPI
			conn.setAutoCommit(false);// autoCommit を無効にする

			//ユーザーテーブル
			if (id != null && (name == null && skill == null)) {
				String countsql = "SELECT COUNT(*) FROM skills WHERE user_id = ?";
				try (PreparedStatement stmt = conn.prepareStatement(countsql)) {
					stmt.setInt(1, id);
					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							int count = rs.getInt(1);
							if (count > 0) {
								// ロールバックで変更を無効
								conn.rollback();
								return "そのユーザーはスキルテーブルに存在します";
							}
						}
					}
				}

				String userDelete_sql = "DELETE FROM users WHERE id = ?";
				try (PreparedStatement stmt = conn.prepareStatement(userDelete_sql)) {
					stmt.setInt(1, id);
					stmt.executeUpdate(); //実行
				}
				conn.commit(); // トランザクションを完了させSQL操作を保存
			}
			
			//スキルテーブル
			if (name != null && skill != null && (id == null)) {
				String skillDelete_sql = "DELETE FROM skills USING users WHERE skills.user_id = users.id AND users.name = ? AND skills.skill = ?"; // 実行SQL
				try (PreparedStatement stmt = conn.prepareStatement(skillDelete_sql)) {
					stmt.setString(1, name);
					stmt.setString(2, skill);
					stmt.executeUpdate(); //実行
				}
				conn.commit(); // トランザクションを完了させSQL操作を保存
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "削除";
	}

	// フォーム送信処理
	@PostMapping({ "/usersubmit", "/skillsubmit" })
	public String Form(@RequestParam(required = false) String userName, @RequestParam(required = false) Long userId,
			@RequestParam(required = false) String skill) {
		try (Connection conn = DriverManager.getConnection(url, username, password)) { // JDBC: DBアクセスするためのAPI
			conn.setAutoCommit(false);// autoCommit を無効にする

			//ユーザーテーブル
			if (userName != null) {
				String userSql = "INSERT INTO users (name) VALUES (?)"; // 実行SQL
				try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
					stmt.setString(1, userName); // SELECTクエリに入力した値をセット
					stmt.executeUpdate(); //実行
				}
				conn.commit(); // トランザクションを完了させSQL操作を保存
			}

			//スキルテーブル
			if (userId != null && skill != null) {
				String getId = "SELECT 1 FROM users WHERE id = ?"; // 実行SQL
				try (PreparedStatement stmt = conn.prepareStatement(getId)) {
					stmt.setLong(1, userId); // SELECTクエリに入力した値をセット
					try (ResultSet rs = stmt.executeQuery()) {
						if (!rs.next()) {
							conn.rollback();
							return "ユーザーIDが存在しません";
						}
					}
				}
				String skillSql = "INSERT INTO skills (user_id, skill) VALUES (?, ?)"; // 実行SQL
				try (PreparedStatement stmt = conn.prepareStatement(skillSql)) {
					stmt.setLong(1, userId);
					stmt.setString(2, skill);
					stmt.executeUpdate(); //実行
				}
				conn.commit(); // トランザクションを完了させSQL操作を保存
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "登録";

	}
}