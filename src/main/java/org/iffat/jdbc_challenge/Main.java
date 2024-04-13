package org.iffat.jdbc_challenge;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

	private static String USE_SCHEMA = "Use storefront";
	private static int MYSQL_DB_NOT_FOUND = 1049;

	public static void main(String[] args) {

		var datasource = new MysqlDataSource();
		datasource.setServerName("localhost");
		datasource.setPort(3306);
		datasource.setUser(System.getenv("MYSQL_USER"));
		datasource.setPassword(System.getenv("MYSQL_PASS"));

		try (Connection connection = datasource.getConnection()) {
			if (!checkSchema(connection)) {
				System.out.println("storefront schema does not exists");
				setUpSchema(connection);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	private static boolean checkSchema(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement();) {
			statement.execute(USE_SCHEMA);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("Error Code: " + e.getErrorCode());
			System.err.println("Message: " + e.getMessage());

			if (connection.getMetaData().getDatabaseProductName().equals("MySQL")
					&& e.getErrorCode() == MYSQL_DB_NOT_FOUND) {
				return false;
			} else throw e;
		}
		return true;
	}

	private static void setUpSchema(Connection connection) throws SQLException {

		String createSchema = "CREATE SCHEMA storefront";
		String createOrder = """
				CREATE TABLE storefront.order(
				order_id int NOT NULL AUTO_INCREMENT,
				order_date DATETIME NOT NULL,
				PRIMARY KEY (order_id)	
				)""";
		String createOrderDetails = """
				CREATE TABLE storefront.order_details(
				order_detail_id int NOT NULL AUTO_INCREMENT,
				item_description text,
				order_id int DEFAULT NULL,
				PRIMARY KEY (order_detail_id),
				KEY FK_ORDERID (order_id),
				CONSTRAINT FK_ORDERID FOREIGN KEY (order_id)
				REFERENCES storefront.order (order_id) ON DELETE CASCADE
				)""";

		try (Statement statement = connection.createStatement()) {

			System.out.println("Creating storefront Database");
			statement.execute(createSchema);
			if (checkSchema(connection)) {
				statement.execute(createOrder);
				System.out.println("Successfully Created Order");
				statement.execute(createOrderDetails);
				System.out.println("Successfully Created Order Details");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
