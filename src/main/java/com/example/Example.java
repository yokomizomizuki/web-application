package com.example;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
	public List<String> getUsers(@RequestParam("usersName") String nameValue) throws SQLException {
		List<String> userIds = new ArrayList<>();
		List<String> userNames = new ArrayList<>();
		// 実行SQL
		String sql = "SELECT * FROM users WHERE name LIKE ?";

		// リソースを自動的にクローズ
		try (Connection connection = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement statement = connection.prepareStatement(sql)) {// preparedStatementメソッドで実行するSQLを設定,戻り値はクラス

			String pattern = "%" + nameValue + "%";
			statement.setString(1, pattern);
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
	public List<String> getSkills(@RequestParam("skillsSkill") String skillValue) throws SQLException {
		List<String> names = new ArrayList<>();
		List<String> skills = new ArrayList<>();

		// 実行SQL
		String sql = "SELECT users.name, skills.skill FROM users JOIN skills ON users.id = skills.user_id WHERE skills.skill LIKE ?";

		// リソースを自動的にクローズ
		try (Connection connection = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement statement = connection.prepareStatement(sql)) {

			String pattern = "%" + skillValue + "%";
			statement.setString(1, pattern);
			try (ResultSet rs = statement.executeQuery()) {// 戻り値はResultSetで取得しレコード格納
				while (rs.next()) { // nextメソッドでレコードを一行ずつ呼び込む
					names.add(rs.getString("name")); // カラム名、nameの値を取得
					skills.add(rs.getString("skill")); // カラム名、skillの値を取得
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Collections.reverse(names);
			Collections.reverse(skills);

			List<String> result = new ArrayList<>();
			result.addAll(names);
			result.addAll(skills);
			return result;
		}
	}

	@DeleteMapping("/api/users/{id}")
	public void user_delete(@PathVariable("id") int id) throws FileNotFoundException {
		// 実行SQL
		String delete_sql = "DELETE FROM users WHERE id = ?";

		// try〜catch〜resources構文を使用
		try (Connection conn = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement stmt = conn.prepareStatement(delete_sql)) {
			conn.setAutoCommit(false);// autoCommit を無効にする
			stmt.setInt(1, id);
			int rows = stmt.executeUpdate(); // DBのデータを変更
			conn.commit(); // トランザクションを完了させSQL操作を保存

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@DeleteMapping("/api/users/{name}/skills/{skill}")
	public void skill_delete(@PathVariable("name") String name, @PathVariable("skill") String skill) throws FileNotFoundException {
		// 実行SQL
		String delete_sql = "DELETE FROM skills USING users WHERE skills.user_id = users.id AND users.name = ? AND skills.skill = ?";

		// try〜catch〜resources構文を使用
		try (Connection conn = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement stmt = conn.prepareStatement(delete_sql)) {
			conn.setAutoCommit(false);// autoCommit を無効にする

			stmt.setString(1, name);
			stmt.setString(2, skill);
			int rows = stmt.executeUpdate();
			conn.commit(); // トランザクションを完了させSQL操作を保存

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// フォーム送信処理
	@PostMapping("/usersubmit")
	public String usersForm(@RequestParam String userName) {
		// 実行SQL
		String userSql = "INSERT INTO users (name) VALUES (?)";

		try (Connection conn = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement stmt = conn.prepareStatement(userSql)) {
			conn.setAutoCommit(false);// autoCommit を無効にする
			stmt.setString(1, userName); // SELECTクエリに入力した値をセット
			stmt.executeUpdate();
			conn.commit(); // トランザクションを完了させSQL操作を保存
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "登録が完了しました。" + "<button onclick=\"history.back()\" style=\"margin: 3px;\">戻る</button>";
	}

	@PostMapping("/skillsubmit")
	public String skillsForm(@RequestParam Long userId, @RequestParam String skill) {
		// 実行SQL
		String getId = "SELECT id FROM users WHERE id = ?";
		String skillSql = "INSERT INTO skills (user_id, skill) VALUES (?, ?)";

		try (Connection conn = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement statement = conn.prepareStatement(getId);
				PreparedStatement stmt = conn.prepareStatement(skillSql)) {
			conn.setAutoCommit(false); // autoCommit を無効にする
			statement.setLong(1, userId); // SELECTクエリに入力した値をセット
			try (ResultSet rs = statement.executeQuery()) {
				if (!rs.next()) {
					return "ユーザーIDが存在しません" + "<button onclick=\"history.back()\" style=\"margin: 3px;\">戻る</button>";
				}
			}
			stmt.setLong(1, userId);
			stmt.setString(2, skill);
			stmt.executeUpdate();
			conn.commit(); // トランザクションを完了させSQL操作を保存
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "登録が完了しました。" + "<button onclick=\"history.back()\" style=\"margin: 3px;\">戻る</button>";
	}

}