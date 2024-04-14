package org.iffat.jpa.music;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class SongQuery {

	public static void main(String[] args) {

		try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.iffat.jpa");
			 EntityManager entityManager = emf.createEntityManager()) {

			String dashedString = "-".repeat(19);
			String word = "Storm";
			var matches = getMatchedSongs(entityManager, "%" + word + "%");
			System.out.printf("%-30s %-65s %s%n", "Artist", "Album", "Song Title");
			System.out.printf("%1$-30s %1$-65s %1$s%n", dashedString);

			matches.forEach(artist -> {
				String artistName = artist.getArtistName();
				artist.getAlbums().forEach(album -> {
					String albumName = album.getAlbumName();
					album.getPlayList().forEach(song -> {
						String songTitle = song.getSongTitle();
						if (songTitle.contains(word)) {
							System.out.printf("%-30s %-65s %s%n", artistName, albumName, songTitle);
						}
					});
				});
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<Artist> getMatchedSongs(EntityManager entityManager, String matchedValue) {

		String jpql = "SELECT a FROM Artist a JOIN albums album JOIN playList p" +
				" WHERE p.songTitle LIKE ?1";
		var query = entityManager.createQuery(jpql, Artist.class);
		query.setParameter(1, matchedValue);
		return query.getResultList();
	}
}
