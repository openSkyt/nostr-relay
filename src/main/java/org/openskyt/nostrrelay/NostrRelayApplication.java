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
		eventRepository.save(new Event("id1", "pubkey1", 111111, 1, new String[][]{{"p","pubkey3"}}, "content1", "sig1"));
		eventRepository.save(new Event("id2", "pubkey1", 111111, 1, new String[][]{}, "content2", "sig2"));
		eventRepository.save(new Event("id3", "pubkey2", 222222, 1, new String[][]{}, "content3", "sig3"));
		eventRepository.save(new Event("xxxxx", "xxxxxx", 222222, 1, new String[][]{}, "xxxxxxxxxxx", "xxxx"));
		eventRepository.save(new Event("xxxxy", "xxxxxx", 222222, 1, new String[][]{}, "xxxxxxxxxxx", "xxxx"));
		eventRepository.save(new Event("xxxxy", "xxxxxx", 222222, 1, new String[][]{}, "xxxxxxxxxxx", "xxxx"));
		eventRepository.save(new Event("xxxxz", "xxxxxx", 222222, -1, new String[][]{}, "xxxxxxxxxxx", "xxxx"));
		eventRepository.save(new Event("xxxxz", "xxxxxx", 222222, -1, new String[][]{}, "xxxxxxxxxxx", "xxxx"));
	}
}