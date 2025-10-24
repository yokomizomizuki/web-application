package com.example;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // returnでテンプレートファイルをレスポンス
public class ExampleTemplates {

	// DB接続情報
	private static final String url = "jdbc:postgresql://localhost:5432/postgres";
	private static final String username = "postgres";
	private static final String password = "Yokomirpc";

	// 実行SQL
	String userExecution = "SELECT * FROM users ";
	String skillExecution = "SELECT users.name, skills.skill, skills.id FROM users JOIN skills ON users.id = skills.user_id ";

	@GetMapping("/users") // 呼び出されるメソッドを宣言
	public String getData(Model model) throws SQLException { // 引数にModelクラスを記述(Modelからデータを取得)

		List<Users> userList = new ArrayList<>();
		List<Skills> skillList = new ArrayList<>();

		// リソースを自動的にクローズ
		try (Connection connection = DriverManager.getConnection(url, username, password); // JDBC: DBアクセスするためのAPI
				PreparedStatement statement = connection.prepareStatement(userExecution)) {// preparedStatementメソッドで実行するSQLを設定,戻り値はクラス

			try (ResultSet resultSet = statement.executeQuery()) {// 戻り値はResultSetで取得しレコード格納
				while (resultSet.next()) {
					BigDecimal id = resultSet.getBigDecimal("id");
					String name = resultSet.getString("name");
					Users user = new Users(id, name);

					userList.add(user);
				}
			}

			try (PreparedStatement stmt = connection.prepareStatement(skillExecution)) {

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

		StringBuilder sqlu = new StringBuilder(userExecution);

		boolean userParameter = false;
		if (keyword != null && !keyword.isEmpty()) {
			if (value == 0) {
				sqlu.append("WHERE name = ? ");
				userParameter = true;
			} else if (value == 1) {
				sqlu.append("WHERE name LIKE ? ");
				userParameter = true;
			}
		}

		String sql = sqlu.toString();
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

		if (!"sName".equals(sortBy) && !"sSkill".equals(sortBy)) {
			sortBy = "sName";
		}
		if (!"sAsc".equals(sortOrder) && !"sDesc".equals(sortOrder)) {
			sortOrder = "sAsc";
		}

		StringBuilder sqls = new StringBuilder(skillExecution);

		boolean skillParameter = false;
		if (keyword != null && !keyword.isEmpty()) {
			if (value == 0) {
				sqls.append("WHERE skills.skill = ? ");
				skillParameter = true;
			} else if (value == 1) {
				sqls.append("WHERE skills.skill LIKE ? ");
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
		sqls.append("ORDER BY ").append(orderColumn).append(" ").append(orderDir);

		String sql = sqls.toString();
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
}