
package com.example.versionplanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
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
	VersionReleaseService service;

	@PostConstruct
	@Scheduled(fixedDelay = 60000)
	public void init() {
		add(new Version("175"));
		add(new Version("177"));
		add(new Version("178"));
		add(new Version("179"));
	}

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
	public void deleteVersion(final @RequestBody @Valid Vote vote) throws Exception {
		service.releaseVersion(vote.getVersion());
	}

	@PostMapping("/releaseAll")
	public void deleteAll() throws Exception {
		service.releaseAll();
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
