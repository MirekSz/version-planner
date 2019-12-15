
package com.example.versionplanner;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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

	@PostMapping
	public void add(final @RequestBody @Valid Vote vote) {
		Vote findByLoginAndVersion = repo.findByLoginAndVersion(vote.getLogin(), vote.getVersion());
		if (findByLoginAndVersion == null)
			repo.save(vote);
	}

	@PostMapping("/delete")
	public void delete(final @RequestBody @Valid Vote vote) {
		Vote findByLoginAndVersion = repo.findByLoginAndVersion(vote.getLogin(), vote.getVersion());
		repo.save(findByLoginAndVersion);
	}

	@GetMapping("/list")
	public List<Vote> list() {
		return repo.findAll();
	}

}
