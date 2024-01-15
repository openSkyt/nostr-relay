package org.openskyt.nostrrelay;

import lombok.AllArgsConstructor;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.model.EventRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AllArgsConstructor
public class NostrRelayApplication implements CommandLineRunner {

	private EventRepo repo;

	public static void main(String[] args) {
		SpringApplication.run(NostrRelayApplication.class, args);
	}

	@Override
	public void run(String... args) {

		repo.save(new Event("id1", "pubkey1", System.currentTimeMillis() / 1000, 1, new String[][]{}, "content1", "sig1"));
		repo.save(new Event("id2", "pubkey2", System.currentTimeMillis() / 1000, 1, new String[][]{}, "content2", "sig2"));
		repo.save(new Event("id3", "pubkey3", System.currentTimeMillis() / 1000, 1, new String[][]{}, "content3", "sig3"));
		repo.save(new Event("id4", "pubkey1", System.currentTimeMillis() / 1000, 1, new String[][]{}, "content4", "sig4"));
		repo.save(new Event("id5", "pubkey3", System.currentTimeMillis() / 1000, 1, new String[][]{}, "content5", "sig5"));
		repo.save(new Event("id6", "pubkey1", System.currentTimeMillis() / 1000, 3, new String[][]{}, "content6", "sig6"));
		repo.save(new Event("id7", "pubkey1", System.currentTimeMillis() / 1000, 3, new String[][]{}, "content7", "sig7"));
		repo.save(new Event("id8", "pubkey2", System.currentTimeMillis() / 1000, 3, new String[][]{}, "content8", "sig8"));
		repo.save(new Event("id9", "pubkey2", System.currentTimeMillis() / 1000, 3, new String[][]{}, "content9", "sig9"));
	}
}
