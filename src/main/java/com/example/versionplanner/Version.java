
package com.example.versionplanner;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class Version {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	@Column(nullable = true)
	private VersionState state;
	@Column(nullable = true)
	private LocalDateTime start;

	public Version() {

	}

	public Version(final String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public VersionState getState() {
		return state;
	}

	public void setState(final VersionState state) {
		this.state = state;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(final LocalDateTime start) {
		this.start = start;
	}

}
