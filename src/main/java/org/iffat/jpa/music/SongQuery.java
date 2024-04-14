package org.iffat.jpa.music;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.*;

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

			System.out.printf("%-30s %-65s %s%n", "Artist", "Album", "Song Title");
			System.out.printf("%1$-30s %1$-65s %1$s%n", dashedString);

			var bMatches = getMatchedSongsBuilder(entityManager, "%" + word + "%");
			bMatches.forEach(mat -> {
				System.out.printf("%-30s %-65s %s%n",
						(String) mat[0], (String) mat[1], (String) mat[2]);
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

	private static List<Object[]> getMatchedSongsBuilder(EntityManager entityManager, String matchedValue) {

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);

		Root<Artist> root = query.from(Artist.class);
		Join<Artist, Album> albumJoin = root.join("albums", JoinType.INNER);
		Join<Album, Song> songJoin = albumJoin.join("playList", JoinType.INNER);

		query.multiselect(root.get("artistName"), albumJoin.get("albumName"), songJoin.get("songTitle"))
				.where(criteriaBuilder.like(songJoin.get("songTitle"), matchedValue))
				.orderBy(criteriaBuilder.asc(root.get("artistName")));

		return entityManager.createQuery(query).getResultList();
	}
}
