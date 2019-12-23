
package com.example.versionplanner;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/vote", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class VoteRestController {

	@Autowired
	VoteRepo repo;

	@Autowired
	VersionRepo versionRepo;

	@PostMapping
	public void add(final @RequestBody @Valid Vote vote) {
		Vote findByLoginAndVersion = repo.findByLoginAndVersion(vote.getLogin(), vote.getVersion());
		if (findByLoginAndVersion == null) {
			vote.setDate(LocalDateTime.now());
			repo.save(vote);
		}
	}

	@PostMapping("/delete")
	public void delete(final @RequestBody @Valid Vote vote) {
		Vote findByLoginAndVersion = repo.findByLoginAndVersion(vote.getLogin(), vote.getVersion());
		repo.delete(findByLoginAndVersion);
	}

	@PostMapping("/releaseVersion")
	public void deleteVersion(final @RequestBody @Valid Vote vote) {
		versionRepo.deleteUnused();
		repo.deleteAll(repo.findAll(Example.of(vote)));
	}

	@PostMapping("/releaseAll")
	public void deleteAll() {
		versionRepo.deleteUnused();
		repo.deleteAll();
	}

	@GetMapping("/list")
	public List<Vote> list() {
		return repo.findAll();
	}

}
