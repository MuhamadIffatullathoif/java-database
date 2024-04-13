package org.iffat.jdbc_challenge;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

record OrderDetail(int orderDetailId, String itemDescription, int qty) {
	public OrderDetail(String itemDescription, int qty) {
		this(-1, itemDescription, qty);
	}
}

record Order(int orderId, String dateString, List<OrderDetail> details) {

	public Order(String dateString) {
		this(-1, dateString, new ArrayList<>());
	}

	public void addDetail(String itemDescription, int qty) {
		OrderDetail item = new OrderDetail(itemDescription, qty);
		details.add(item);
	}
}

public class Challenge2 {

	public static void main(String[] args) {

		var datasource = new MysqlDataSource();
		datasource.setServerName("localhost");
		datasource.setPort(3306);
		datasource.setUser(System.getenv("MYSQL_USER"));
		datasource.setPassword(System.getenv("MYSQL_PASS"));
		List<Order> orders = readData();
		try (Connection connection = datasource.getConnection()) {

//			String alterString = "ALTER TABLE storefront.order_details ADD COLUMN quantity INT";
//			Statement statement = connection.createStatement();
//			statement.execute(alterString);
			addOrders(connection, orders);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	private static List<Order> readData() {

		List<Order> vals = new ArrayList<>();
		try (Scanner scanner = new Scanner(Path.of("Orders.csv"))) {

			scanner.useDelimiter("[,\\n]");
			var list = scanner.tokens().map(String::trim).toList();

			for (int i = 0; i < list.size(); i++) {
				String value = list.get(i);
				if (value.equals("order")) {
					var date = list.get(++i);
					vals.add(new Order(date));
				} else if (value.equals("item")) {
					var qty = Integer.parseInt(list.get(++i));
					var description = list.get(++i);
					Order order = vals.get(vals.size() - 1);
					order.addDetail(description, qty);
				}
			}
			vals.forEach(System.out::println);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return vals;
	}

	private static void addOrder(Connection connection, PreparedStatement psOrder,
								 PreparedStatement psDetail, Order order) throws SQLException {
		try {
			connection.setAutoCommit(false);
			int orderId = -1;
			psOrder.setString(1, order.dateString());
			if (psOrder.executeUpdate() == 1) {
				var rs = psOrder.getGeneratedKeys();
				if (rs.next()) {
					orderId = rs.getInt(1);
					System.out.println("orderId = " + orderId);

					if (orderId > -1) {
						psDetail.setInt(1, orderId);
						for (OrderDetail orderDetail : order.details()) {
							psDetail.setString(2, orderDetail.itemDescription());
							psDetail.setInt(3, orderDetail.qty());
							psDetail.addBatch();
						}
						int[] data = psDetail.executeBatch();
						int rowsInserted = Arrays.stream(data).sum();
						if (rowsInserted != order.details().size()) {
							throw new SQLException("Inserts don't match");
						}
					}
				}
			}
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private static void addOrders(Connection connection, List<Order> orders) {

		String insertOrder = "INSERT INTO storefront.order (order_date) VALUES (?)";
		String insertDetail = "INSERT INTO storefront.order_details " +
				"(order_id, item_description, quantity) VALUES(?, ?, ?)";

		try (PreparedStatement psOrder = connection.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
			 PreparedStatement psDetail = connection.prepareStatement(insertDetail, Statement.RETURN_GENERATED_KEYS)) {
			orders.forEach(order -> {
				try {
					addOrder(connection, psOrder, psDetail, order);
				} catch (SQLException e) {
					System.err.printf("%d (%s) %s%n", e.getErrorCode(), e.getSQLState(), e.getMessage());
					System.err.println("Problem: " + psOrder);
					System.err.println("Order: " + order);
				}
			});
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
