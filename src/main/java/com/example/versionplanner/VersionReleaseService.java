
package com.example.versionplanner;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Arrays;
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

	private static final int TIMEOUT = 60 * 1000 * 60 * 5;

	static Logger logger = LoggerFactory.getLogger(VersionReleaseService.class);
	@Autowired
	VoteRepo voteRepo;
	@Autowired
	VersionRepo versionRepo;
	@Autowired
	TxService txService;

	public void releaseVersion(final String name) throws Exception {
		Version version = versionRepo.findByName(name);
		if (version.getState() == VersionState.RUNNING) {
			return;
		}
		txService.run(() -> {
			version.setState(VersionState.RUNNING);
			version.setStart(LocalDateTime.now());
			versionRepo.save(version);
		});

		new Thread(() -> {
			try {
				executeCommand(name, Arrays.asList("date"));
				executeCommand(name, Arrays.asList("whoami"));
				int waitFor = executeCommand(name, Arrays.asList("./nedsy.sh", "-v", name));
				txService.run(() -> {
					Version findByName1 = versionRepo.findByName(name);
					if (waitFor == 0) {
						voteRepo.deleteAll(voteRepo.findAll(Example.of(new Vote(name))));
						findByName1.setState(VersionState.SUCCESS);
					} else {
						findByName1.setState(VersionState.ERROR);
					}
					versionRepo.save(findByName1);
				});
			} catch (Exception e) {
				e.printStackTrace();
				txService.run(() -> {
					Version findByName2 = versionRepo.findByName(name);
					findByName2.setState(VersionState.ERROR);
					versionRepo.save(findByName2);
				});
			}
		}).start();;
	}

	public static int executeCommand(final String name, final List<String> commands) throws Exception {
		ProcessBuilder builder = new ProcessBuilder(commands);
		// builder.inheritIO();

		builder.directory(new File("/home/neds/VERTO/"));
		// builder.directory(new File("F:\\Verto2017\\workspace\\server-verto-product"));
		builder.environment().put("PATH", System.getenv("PATH") + ";" + "/opt/maven/bin;/usr/bin;");

		Process exec = builder.start();

		log(name, exec.getErrorStream());
		log(name, exec.getInputStream());
		timeout(exec);
		return exec.waitFor();
	}

	public void releaseAll() throws Exception {
		// versionRepo.deleteUnused();
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

	public static void timeout(final Process exec) {
		long currentTimeMillis = System.currentTimeMillis();
		final Thread ioThread = new Thread() {

			@Override
			public void run() {
				while (exec.isAlive()) {
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
					}
					if (System.currentTimeMillis() > currentTimeMillis + TIMEOUT) {
						exec.destroyForcibly();
					}
				}
			}
		};
		ioThread.start();
	}
}
