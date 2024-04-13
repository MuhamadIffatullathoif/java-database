package org.iffat.query_music;

import java.sql.*;
import java.util.Arrays;

public class MusicDML {

	public static void main(String[] args) {

		// automatically
//		try {
//			Class.forName("com.mysql.jdbc.Driver");
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		}

		try (Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/music?continueBatchOnError=false",
				System.getenv("MYSQL_USER"),
				System.getenv("MYSQL_PASS")
		); Statement statement = connection.createStatement();) {
//			String artist = "Neil Young";
//			String query = "SELECT * FROM artists WHERE artist_name='%s'".formatted(artist);
//			boolean result = statement.execute(query);
//			System.out.println("Result = " + result);
//			var rs = statement.getResultSet();
//			boolean found = (rs != null && rs.next());
//			System.out.println("Artist was " + (found ? "found" : "not found"));
			String tableName = "music.artists";
			String columnName = "artist_name";
			String columnValue = "Bob Dylan";
			if (!executeSelect(statement, tableName, columnName, columnValue)) {
//				System.out.println("Maybe we should add this record");
//				insertRecord(statement, tableName, new String[]{columnName}, new String[]{columnValue});
				insertArtistAlbum(statement, columnValue, columnValue);
			} else {
				// deleteRecord(statement, tableName, columnName, columnValue);
				// updateRecord(statement, tableName, columnName, columnValue, columnName, columnValue.toUpperCase());
				try {
					deleteArtistAlbum(connection, statement, columnValue, columnValue);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				executeSelect(statement, "music.albumview", "album_name", columnValue);
				executeSelect(statement, "music.albums", "album_name", columnValue);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean printRecords(ResultSet resultSet) throws SQLException {

		boolean foundData = false;
		var meta = resultSet.getMetaData();

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
			foundData = true;
		}
		return foundData;
	}

	private static boolean executeSelect(Statement statement, String table,
										 String columnName, String columnValue) throws SQLException {
		String query = "SELECT * FROM %s WHERE %s='%s'".formatted(table, columnName, columnValue);
		var rs = statement.executeQuery(query);
		if (rs != null) {
			return printRecords(rs);
		}
		return false;
	}

	private static boolean insertRecord(Statement statement, String table,
										String[] columnNames, String[] columnValues) throws SQLException {

		String colNames = String.join(",", columnNames);
		String colValues = String.join(",", columnValues);
		String query = "INSERT INTO %s (%s) VALUES ('%s')".formatted(table, colNames, colValues);
		System.out.println(query);
		boolean insertResult = statement.execute(query);
		int recordsInserted = statement.getUpdateCount();
		if (recordsInserted > 0) {
			executeSelect(statement, table, columnNames[0], columnValues[0]);
		}
		return recordsInserted > 0;
	}

	private static boolean deleteRecord(Statement statement, String table,
										String columnName, String columnValue) throws SQLException {

		String query = "DELETE FROM %s WHERE %s='%s'".formatted(table, columnName, columnValue);
		System.out.println(query);
		statement.execute(query);
		int recordDeleted = statement.getUpdateCount();
		if (recordDeleted > 0) {
			executeSelect(statement, table, columnName, columnValue);
		}
		return recordDeleted > 0;

	}

	private static boolean updateRecord(Statement statement, String table,
										String matchedColumn, String matchedValue,
										String updatedColumn, String updatedValue) throws SQLException {

		String query = "UPDATE %s SET %s = '%s' WHERE %s='%s'"
				.formatted(table, updatedColumn, updatedValue, matchedColumn, matchedValue);
		System.out.println(query);
		statement.execute(query);
		int recordUpdated = statement.getUpdateCount();
		if (recordUpdated > 0) {
			executeSelect(statement, table, updatedColumn, updatedValue);
		}
		return recordUpdated > 0;

	}

	private static void insertArtistAlbum(Statement statement, String artistName, String albumName) throws SQLException {

		String artistInsert = "INSERT INTO music.artists (artist_name) VALUES (%s)"
				.formatted(statement.enquoteLiteral(artistName));
		System.out.println(artistInsert);
		statement.execute(artistInsert, Statement.RETURN_GENERATED_KEYS);

		ResultSet resultSet = statement.getGeneratedKeys();
		int artistId = (resultSet != null && resultSet.next()) ? resultSet.getInt(1) : -1;
		String albumInsert = ("INSERT INTO music.albums (album_name,artist_id)" +
				" VALUES(%s, %d)")
				.formatted(statement.enquoteLiteral(albumName), artistId);
		System.out.println(albumInsert);
		statement.execute(albumInsert, Statement.RETURN_GENERATED_KEYS);
		resultSet = statement.getGeneratedKeys();
		int albumId = (resultSet != null && resultSet.next()) ? resultSet.getInt(1) : -1;

		String[] songs = new String[]{
				"You're No Good",
				"Talking New York",
				"In My Time of Dyin'",
				"Man of Constant Sorrow",
				"Fixin to Die",
				"Pretty Peggy-O",
				"Highway 51 Blues"
		};

		String songInsert = "INSERT INTO music.songs " +
				"(track_number, song_title, album_id) VALUES (%d, %s, %d)";

		for (int i = 0; i < songs.length; i++) {
			String songQuery = songInsert.formatted(i + 1, statement.enquoteLiteral(songs[i]), albumId);
			System.out.println(songQuery);

			statement.execute(songQuery);
		}

		executeSelect(statement, "music.albumview", "album_name", "Bob Dylan");
	}

	private static void deleteArtistAlbum(Connection connection, Statement statement,
										  String artistName, String albumName) throws SQLException {
		try {
			System.out.println("AUTOCOMMIT = " + connection.getAutoCommit());
			connection.setAutoCommit(false);
			String deleteSongs = """
					DELETE FROM music.songs WHERE album_id =
					(SELECT ALBUM_ID from music.albums WHERE album_name = '%s')"""
					.formatted(albumName);
			String deleteAlbums = "DELETE FROM music.albums WHERE album_name='%s'"
					.formatted(albumName);
			String deleteArtist = "DELETE FROM music.artists WHERE artist_name='%s'"
					.formatted(artistName);
			statement.addBatch(deleteSongs);
			statement.addBatch(deleteAlbums);
			statement.addBatch(deleteArtist);
			int[] results = statement.executeBatch();
			System.out.println(Arrays.toString(results));
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			connection.rollback();
		}
		connection.setAutoCommit(true);
	}
}
