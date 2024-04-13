package org.iffat.query_music;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Main {
	public static void main(String[] args) {

		Properties properties = new Properties();
		try {
			properties.load(Files.newInputStream(Path.of("music.properties"), StandardOpenOption.READ));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String albumName = "Tapestry";
		String query = "SELECT * FROM music.albumview WHERE album_name='%s'"
				.formatted(albumName);

		var dataSource = new MysqlDataSource();
		dataSource.setServerName(properties.getProperty("serverName"));
		dataSource.setPort(Integer.parseInt(properties.getProperty("port")));
		dataSource.setDatabaseName(properties.getProperty("databaseName"));

		try (var connection = dataSource.getConnection(properties.getProperty("user"), System.getenv("MYSQL_PASS"));
			 Statement statement = connection.createStatement();) {
			ResultSet resultSet = statement.executeQuery(query);

			var meta = resultSet.getMetaData();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				System.out.printf("%d %s %s%n",
						i,
						meta.getColumnName(i),
						meta.getColumnTypeName(i));
			}
			System.out.println("=".repeat(90));

			for (int i = 1; i <= meta.getColumnCount(); i++) {
				System.out.printf("%-15s", meta.getColumnName(i).toUpperCase());
			}
			System.out.println();

			while (resultSet.next()) {
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					System.out.printf("%-15s", resultSet.getString(i));
				}
				System.out.println();
//				System.out.printf("%d %s %s %n",
//						resultSet.getInt("track_number"),
//						resultSet.getString("artist_name"),
//						resultSet.getString("song_title"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
