
package com.example.versionplanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VersionReleaserTimer {

	@Autowired
	VersionReleaseService service;

	@Scheduled(cron = "0 0 18 * * *")
	public void run() throws Exception {
		service.releaseAll("system");
	}

}
