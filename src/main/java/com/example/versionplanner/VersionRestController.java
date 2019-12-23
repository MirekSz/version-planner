
package com.example.versionplanner;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class VersionRestController {

	@Autowired
	VersionRepo repo;

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

	@GetMapping("/list")
	public List<Version> list() {
		return repo.findAll();
	}

}
