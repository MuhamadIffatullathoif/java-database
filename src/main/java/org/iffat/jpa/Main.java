package org.iffat.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import org.iffat.jpa.music.Artist;

public class Main {

	public static void main(String[] args) {

		try (var sessionFactory = Persistence.createEntityManagerFactory("org.iffat.jpa");
			 EntityManager entityManager = sessionFactory.createEntityManager()) {

			var transaction = entityManager.getTransaction();
			transaction.begin();
			// entityManager.persist(new Artist("Muddy Water"));
			// Artist artist = entityManager.find(Artist.class, 207);
			Artist artist = new Artist(207, "Muddy Waters");
			entityManager.merge(artist); // must use merge for constructor to persists db
			System.out.println(artist);
			// entityManager.remove(artist);
			// artist.setArtistName("Muddy Waters"); // auto persists because transaction.commit()
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
