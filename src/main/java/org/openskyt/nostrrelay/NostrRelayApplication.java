package org.openskyt.nostrrelay;

import lombok.AllArgsConstructor;
import org.openskyt.nostrrelay.model.Event;
import org.openskyt.nostrrelay.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AllArgsConstructor
public class NostrRelayApplication implements CommandLineRunner {

	private EventRepository eventRepository;

	public static void main(String[] args) {
		SpringApplication.run(NostrRelayApplication.class, args);
	}

	@Override
	public void run(String... args) {
		eventRepository.save(new Event("id1", "pubkey1", 111111, 3, new String[][]{{"p","pubkey3"}}, "content1", "sig1"));
		eventRepository.save(new Event("id3", "pubkey2", 222222, 1, new String[][]{{"p","pubkey2"}}, "content3", "sig3"));
	}
}