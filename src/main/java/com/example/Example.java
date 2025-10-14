package com.example;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<List<Map<String, Object>>> getUsers(@RequestParam("userMatch") Integer value,
			@RequestParam(name = "userKeyword", required = false) String keyword) throws SQLException {

		List<Map<String, Object>> usersList = new ArrayList<Map<String, Object>>();

		// 実行SQL
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM users ");

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
					Map<String, Object> users = new HashMap<String, Object>();
					users.put("id", resultSet.getInt("id")); // カラム名、idの値を取得
					users.put("name", resultSet.getString("name"));// カラム名、nameの値を取得
					// MapをListに格納
					usersList.add(new HashMap<>(users));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return ResponseEntity.ok(usersList);
		}
	}

	@GetMapping("/api/skills") // 呼び出されるメソッドを宣言
	public ResponseEntity<List<Map<String, Object>>> getSkills(@RequestParam("skillMatch") Integer value,
			@RequestParam(name = "skillKeyword", required = false) String keyword,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "sortOrder", required = false) String sortOrder) throws SQLException {

		List<Map<String, Object>> skillsList = new ArrayList<Map<String, Object>>();

		if (!"sName".equals(sortBy) && !"sSkill".equals(sortBy)) {
			sortBy = "sName";
		}
		if (!"sAsc".equals(sortOrder) && !"sDesc".equals(sortOrder)) {
			sortOrder = "sAsc";
		}

		// 実行SQL
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT users.name, skills.skill ,skills.id FROM users JOIN skills ON users.id = skills.user_id ");

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
					Map<String, Object> skills = new HashMap<String, Object>();
					skills.put("name", rs.getString("name")); // カラム名、nameの値を取得
					skills.put("skill", rs.getString("skill")); // カラム名、skillの値を取得
					skills.put("id", rs.getInt("id"));
					skillsList.add(new HashMap<>(skills));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return ResponseEntity.ok(skillsList);
		}
	}

	@DeleteMapping("/api/users/{id}")
	public ResponseEntity<Object> user_delete(@PathVariable(name = "id", required = false) Integer id)
			throws FileNotFoundException {

		// try〜catch〜resources構文を使用
		try (Connection conn = DriverManager.getConnection(url, username, password)) { // JDBC: DBアクセスするためのAPI
			conn.setAutoCommit(false);// autoCommit を無効にする

			// ユーザーテーブル
			String countsql = "SELECT COUNT(*) FROM skills WHERE user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(countsql)) {
				stmt.setInt(1, id);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						int count = rs.getInt(1);
						if (count > 0) {
							// ロールバックで変更を無効
							conn.rollback();
							return ResponseEntity.status(HttpStatus.CONFLICT).body(409);
						}
					}
				}
			}

			String userDelete_sql = "DELETE FROM users WHERE id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(userDelete_sql)) {
				stmt.setInt(1, id);
				stmt.executeUpdate(); // 実行
				conn.commit(); // トランザクションを完了させSQL操作を保存
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.OK).body(200);
	}

	@DeleteMapping("/api/skills/{id}")
	public ResponseEntity<Object> skill_delete(@PathVariable(name = "id", required = false) Integer id)
			throws FileNotFoundException {

		try (Connection conn = DriverManager.getConnection(url, username, password)) { // JDBC: DBアクセスするためのAPI
			conn.setAutoCommit(false);// autoCommit を無効にする

			// スキルテーブル
			String skillDelete_sql = "DELETE FROM skills USING users WHERE skills.user_id = users.id AND skills.id = ?"; // 実行SQL
			try (PreparedStatement stmt = conn.prepareStatement(skillDelete_sql)) {
				stmt.setInt(1, id);
				stmt.executeUpdate(); // 実行
				conn.commit(); // トランザクションを完了させSQL操作を保存
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body(200);
	}

	// フォーム送信処理
	@PostMapping("/usersubmit")
	public ResponseEntity<Object> usersForm(@RequestParam(required = false) String userName) {
		try (Connection conn = DriverManager.getConnection(url, username, password)) { // JDBC: DBアクセスするためのAPI
			conn.setAutoCommit(false);// autoCommit を無効にする

			// ユーザーテーブル
			String userSql = "INSERT INTO users (name) VALUES (?)"; // 実行SQL
			try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
				stmt.setString(1, userName); // SELECTクエリに入力した値をセット
				stmt.executeUpdate(); // 実行
				conn.commit(); // トランザクションを完了させSQL操作を保存
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body(200);
	}

	@PostMapping("/skillsubmit")
	public ResponseEntity<Object> skillsForm(@RequestParam(name = "userId", required = false) Integer userId,
			@RequestParam(name = "skill", required = false) String skill) {
		try (Connection conn = DriverManager.getConnection(url, username, password)) { // JDBC: DBアクセスするためのAPI
			conn.setAutoCommit(false);// autoCommit を無効にする

			// スキルテーブル
			String getId = "SELECT 1 FROM users WHERE id = ?"; // 実行SQL
			try (PreparedStatement stmt = conn.prepareStatement(getId)) {
				stmt.setInt(1, userId); // SELECTクエリに入力した値をセット
				try (ResultSet rs = stmt.executeQuery()) {
					if (!rs.next()) {
						conn.rollback();
						return ResponseEntity.status(HttpStatus.NOT_FOUND).body(404);
					}
				}
			}

			String skillSql = "INSERT INTO skills (user_id, skill) VALUES (?, ?)"; // 実行SQL
			try (PreparedStatement stmt = conn.prepareStatement(skillSql)) {
				stmt.setInt(1, userId);
				stmt.setString(2, skill);
				stmt.executeUpdate(); // 実行
				conn.commit(); // トランザクションを完了させSQL操作を保存
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body(200);
	}
}