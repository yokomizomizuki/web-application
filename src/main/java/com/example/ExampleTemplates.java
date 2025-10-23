package com.example;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // returnでテンプレートファイルをレスポンス
public class ExampleTemplates {

	// DB接続情報
	String url = "jdbc:postgresql://localhost:5432/postgres";
	String username = "postgres";
	String password = "Yokomirpc";

	@GetMapping("/users") // 呼び出されるメソッドを宣言
	public String getData(Model model) throws SQLException { // 引数にModelクラスを記述(Modelからデータを取得)

		List<Users> userList = new ArrayList<>();
		List<Skills> skillList = new ArrayList<>();

		// 実行SQL
		String sqlu = "SELECT * FROM users";
		String sqls = "SELECT users.name, skills.skill, skills.id FROM users JOIN skills ON users.id = skills.user_id";

		// リソースを自動的にクローズ
		try (Connection connection = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement statement = connection.prepareStatement(sqlu)) {// preparedStatementメソッドで実行するSQLを設定,戻り値はクラス

			try (ResultSet resultSet = statement.executeQuery()) {// 戻り値はResultSetで取得しレコード格納
				while (resultSet.next()) {
					BigDecimal id = resultSet.getBigDecimal("id");
					String name = resultSet.getString("name");
					Users user = new Users(id, name);

					userList.add(user);
				}
			}

			try (PreparedStatement stmt = connection.prepareStatement(sqls)) {

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						String name = rs.getString("name");
						String skill = rs.getString("skill");
						BigDecimal id = rs.getBigDecimal("id");
						Skills skilln = new Skills(name, skill, id);

						skillList.add(skilln);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			model.addAttribute("userList", userList); // Modelクラスに値をセット(テンプレートで使用する名前, 渡す値)
			model.addAttribute("skillList", skillList);
			return "users"; // /templates/配下の.htmlを除いたファイルの返却
		}
	}

	@GetMapping("/users/searchusers")
	public String search_users(@RequestParam(name = "userMatch", required = false) Integer value,
			@RequestParam(name = "userKeyword", required = false) String keyword, Model model) {

		List<Users> userList = new ArrayList<>();

		// 実行SQL
		StringBuilder sb = new StringBuilder("SELECT * FROM users ");

		boolean userParameter = false;
		if (keyword != null && !keyword.isEmpty()) {
			if (value == 0) {
				sb.append("WHERE name = ? ");
				userParameter = true;
			} else if (value == 1) {
				sb.append("WHERE name LIKE ? ");
				userParameter = true;
			}
		}

		String sql = sb.toString();
		try (Connection connection = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement statement = connection.prepareStatement(sql)) {// preparedStatementメソッドで実行するSQLを設定,戻り値はクラス

			if (userParameter) {
				if (value == 0) {
					statement.setString(1, keyword); // 完全一致
				} else if (value == 1) {
					statement.setString(1, "%" + keyword + "%"); // 部分一致
				}
			}
			
			try (ResultSet resultSet = statement.executeQuery()) {// 戻り値はResultSetで取得しレコード格納
				while (resultSet.next()) {
					BigDecimal id = resultSet.getBigDecimal("id");
					String name = resultSet.getString("name");
					Users user = new Users(id, name);

					userList.add(user);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		model.addAttribute("userMatch", value);
		model.addAttribute("userKeyword", keyword);
		model.addAttribute("userList", userList);
		return "users";
	}

	@GetMapping("/users/searchskill")
	public String search_skill(@RequestParam(name = "skillMatch", required = false) Integer value,
			@RequestParam(name = "skillKeyword", required = false) String keyword,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "sortOrder", required = false) String sortOrder, Model model) {

		List<Skills> skillList = new ArrayList<>();

		StringBuilder sb = new StringBuilder(
				"SELECT users.name, skills.skill, skills.id FROM users JOIN skills ON users.id = skills.user_id ");

		if (!"sName".equals(sortBy) && !"sSkill".equals(sortBy)) {
			sortBy = "sName";
		}
		if (!"sAsc".equals(sortOrder) && !"sDesc".equals(sortOrder)) {
			sortOrder = "sAsc";
		}

		boolean skillParameter = false;
		if (keyword != null && !keyword.isEmpty()) {
			if (value == 0) {
				sb.append("WHERE skills.skill = ? ");
				skillParameter = true;
			} else if (value == 1) {
				sb.append("WHERE skills.skill LIKE ? ");
				skillParameter = true;
			}
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
		try (Connection connection = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement stmt = connection.prepareStatement(sql)) {

			if (skillParameter) {
				if (value == 0) {
					stmt.setString(1, keyword); // 完全一致
				} else if (value == 1) {
					stmt.setString(1, "%" + keyword + "%"); // 部分一致
				}
			}

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String name = rs.getString("name");
					String skill = rs.getString("skill");
					BigDecimal id = rs.getBigDecimal("id");
					Skills skilln = new Skills(name, skill, id);

					skillList.add(skilln);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		model.addAttribute("skillMatch", value);
		model.addAttribute("skillKeyword", keyword);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("sortOrder", sortOrder);
		model.addAttribute("skillList", skillList);
		return "users";
	}

	@DeleteMapping("/api/tusers/{id}")
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

	@DeleteMapping("/api/tskills/{id}")
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
	@PostMapping("/tusersubmit")
	public ResponseEntity<Object> usersForm(@RequestParam(name = "userName", required = false) String userName) {
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

	@PostMapping("/tskillsubmit")
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