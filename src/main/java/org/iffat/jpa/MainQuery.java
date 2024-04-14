package org.iffat.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.iffat.jpa.music.Artist;

import java.util.List;

public class MainQuery {

	public static void main(String[] args) {

		List<Artist> artists = null;
		try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.iffat.jpa");
			 EntityManager entityManager = emf.createEntityManager();) {

			var transaction = entityManager.getTransaction();
			transaction.begin();
			artists = getArtistsJPQL(entityManager, "%Stev%");
			artists.forEach(System.out::println);
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<Artist> getArtistsJPQL(EntityManager entityManager, String matchedValue) {

		// String jpql = "SELECT a FROM Artist a WHERE a.artistName LIKE :partialName";
		String jpql = "SELECT a FROM Artist a WHERE a.artistName LIKE ?1";
		var query = entityManager.createQuery(jpql, Artist.class);
		query.setParameter(1, matchedValue);
		return query.getResultList();
	}
}
