
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
	@Column(nullable = true)
	private Integer counter = 0;
	@Column(nullable = true)
	private Boolean lastError;
	@Column(nullable = true)
	private String releaser;

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
		if (VersionState.ERROR == state) {
			this.lastError = true;
		} else if (VersionState.SUCCESS == state) {
			this.lastError = false;
			this.releaser = null;
		}
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(final LocalDateTime start) {
		this.start = start;
		if (this.counter == null) {
			this.counter = 0;
		}
		this.counter++;
	}

	public Integer getCounter() {
		return counter;
	}

	public void setCounter(final Integer counter) {
		this.counter = counter;
	}

	public Boolean getLastError() {
		return lastError;
	}

	public void setReleaser(final String releaser) {
		this.releaser = releaser;
	}

	public String getReleaser() {
		return this.releaser;
	}
}
