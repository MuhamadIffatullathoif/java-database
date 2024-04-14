package org.iffat.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Tuple;
import org.iffat.jpa.music.Artist;

import java.util.List;
import java.util.stream.Stream;

public class MainQuery {

	public static void main(String[] args) {

		List<Artist> artists = null;
		try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.iffat.jpa");
			 EntityManager entityManager = emf.createEntityManager();) {

			var transaction = entityManager.getTransaction();
			transaction.begin();
			artists = getArtistsJPQL(entityManager, "%Stev%");
			artists.forEach(System.out::println);

			var names = getArtistsNames(entityManager, "%Stev%");
			// names.forEach(System.out::println);
			names
					.map(a -> new Artist(
							a.get("id", Integer.class),
							(String) a.get("names")
					))
					.forEach(System.out::println);
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<Artist> getArtistsJPQL(EntityManager entityManager, String matchedValue) {

		// String jpql = "SELECT a FROM Artist a WHERE a.artistName LIKE :partialName";
		// String jpql = "SELECT a FROM Artist a WHERE a.artistName LIKE ?1";
		String jpql = "SELECT a FROM Artist a JOIN Album as album " +
				"WHERE album.albumName LIKE ?1 OR album.albumName LIKE ?2";
		var query = entityManager.createQuery(jpql, Artist.class);
		query.setParameter(1, matchedValue);
		query.setParameter(2, "%Best of%");
		return query.getResultList();
	}

	private static Stream<Tuple> getArtistsNames(EntityManager entityManager, String matchedValue) {

		String jpql = "SELECT a.artistId id, a.artistName as names FROM Artist a WHERE a.artistName LIKE ?1";
		var query = entityManager.createQuery(jpql, Tuple.class);
		query.setParameter(1, matchedValue);
		return query.getResultStream();
	}
}
