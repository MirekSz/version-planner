
package com.example.versionplanner;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class VersionReleaseService {

	static Logger logger = LoggerFactory.getLogger(VersionReleaseService.class);
	@Autowired
	VoteRepo voteRepo;
	@Autowired
	VersionRepo versionRepo;
	@Autowired
	TxService txService;

	public void releaseVersion(final String name) throws Exception {
		txService.run(() -> {
			Version version = versionRepo.findByName(name);
			version.setState(VersionState.RUNNING);
			version.setStart(LocalDateTime.now());
			versionRepo.save(version);
		});

		ProcessBuilder builder = new ProcessBuilder("mvn.bat", "compile");
		// builder.inheritIO();

		// builder.directory(new File("/home/neds/VERTO/"));
		builder.directory(new File("F:\\Verto2017\\workspace\\abc\\"));
		builder.environment().put("PATH", "F:\\Verto2017\\apache-maven-3.0.4\\bin\\");
		try {

			Process exec = builder.start();

			log(name, exec.getErrorStream());
			log(name, exec.getInputStream());
			int waitFor = exec.waitFor();
			txService.run(() -> {
				Version findByName = versionRepo.findByName(name);
				if (waitFor == 0) {
					voteRepo.deleteAll(voteRepo.findAll(Example.of(new Vote(name))));
					findByName.setState(VersionState.SUCCESS);
				} else {
					findByName.setState(VersionState.ERROR);
				}
				versionRepo.save(findByName);
			});
		} catch (Exception e) {
			txService.run(() -> {
				Version findByName = versionRepo.findByName(name);
				findByName.setState(VersionState.ERROR);
				versionRepo.save(findByName);
			});
		}

	}

	public void releaseAll() throws Exception {
		versionRepo.deleteUnused();
		List<String> collect = voteRepo.findAll().stream().map(Vote::getVersion).distinct().collect(toList());
		for (String name : collect) {
			releaseVersion(name);
		}
	}

	public static void log(final String version, final InputStream in) {
		final Thread ioThread = new Thread() {

			@Override
			public void run() {
				MDC.put("version", version);
				try {
					final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String line = null;
					while ((line = reader.readLine()) != null) {
						logger.error(line);
					}
					reader.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		};
		ioThread.start();
	}
}
