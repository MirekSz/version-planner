
package com.example.versionplanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class VersionRestController {

	@Autowired
	VersionRepo repo;
	@Autowired
	PropRepo propRepo;
	@Autowired
	VersionReleaseService service;

	@PostMapping
	public void add(final @RequestBody @Valid Version version) {
		Version findByLoginAndVersion = repo.findByName(version.getName());
		if (findByLoginAndVersion == null) {
			repo.save(version);
		}
	}

	@PostMapping("/delete")
	public void delete(final @RequestBody @Valid Version version) {
		Version findByLoginAndVersion = repo.findByName(version.getName());
		if (findByLoginAndVersion != null) {
			repo.delete(findByLoginAndVersion);
		}
	}

	@PostMapping("/updateReleaser")
	public void updateReleaser(final @RequestBody @Valid Prop prop) {
		Prop findByName = propRepo.findByName(PropRepo.RELEASER);
		if (findByName == null) {
			findByName = new Prop();
			findByName.setName(PropRepo.RELEASER);
		}
		findByName.setValue(prop.getValue());
		propRepo.save(findByName);
	}

	@GetMapping("/getReleaser")
	public Prop getReleaser() {
		Prop findByName = propRepo.findByName(PropRepo.RELEASER);
		return findByName;
	}

	@GetMapping("/list")
	public List<Version> list() {
		return repo.findAll();
	}

	@GetMapping("/error")
	public String log(@RequestParam("version") final String version) throws Exception {
		String path = new File("").getAbsolutePath() + File.separator + "logs" + File.separator + "vvp-" + version + ".log";
		return Tail.tailFile(new File(path).toPath(), 400).stream().reduce("", String::concat);
	}

	@PostMapping("/releaseVersion")
	public void deleteVersion(final @RequestBody @Valid Vote vote, @RequestParam("user") final String user,
			@RequestParam("fast") final Boolean fast) throws Exception {
		service.releaseVersion(vote.getVersion(), user, fast);
	}

	@PostMapping("/releaseAll")
	public void deleteAll(@RequestParam("user") final String user) throws Exception {
		service.releaseAll(user);
	}

	private static final class RingBuffer {

		private final int limit;
		private final String[] data;
		private int counter = 0;

		public RingBuffer(final int limit) {
			this.limit = limit;
			this.data = new String[limit];
		}

		public void collect(final String line) {
			data[counter++ % limit] = line;
		}

		public List<String> contents() {
			return IntStream.range(counter < limit ? 0 : counter - limit, counter).mapToObj(index -> data[index % limit])
					.collect(Collectors.toList());
		}

	}

	public static class Tail {

		public static final List<String> tailFile(final Path source, final int limit) throws IOException {

			try (Stream<String> stream = Files.lines(source)) {
				RingBuffer buffer = new RingBuffer(limit);
				stream.forEach(line -> buffer.collect(line + "\n"));

				return buffer.contents();
			}

		}

	}
}
