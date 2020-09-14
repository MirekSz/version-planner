
package com.example.versionplanner;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;

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

	public void releaseVersion(final String name, final String user, final Boolean fast) throws Exception {
		Version version = versionRepo.findByName(name);
		Long ver = Long.valueOf(name);
		if (version.getState() == VersionState.RUNNING) {
			return;
		}
		Boolean lastError = version.getLastError();
		txService.run(() -> {
			version.setState(VersionState.RUNNING);
			version.setStart(LocalDateTime.now());
			version.setReleaser(user);
			versionRepo.save(version);
		});

		new Thread(() -> {
			try {
				String increaseNumber = "";
				if (Boolean.TRUE.equals(lastError) && Boolean.FALSE.equals(fast)) {
					increaseNumber = "fb";
				}
				String command = "./nedsy" + increaseNumber + ".sh";
				if (ver > 180) {
					command = command.replace("neds", "neds11");
				}
				Thread.sleep(new Random().nextInt(600000));
				executeCommand(name, Arrays.asList("date"));
				executeCommand(name, Arrays.asList("whoami"));
				long currentTimeMillis = System.currentTimeMillis();
				AtomicInteger waitFor = new AtomicInteger();
				waitFor.set(executeCommand(name, Arrays.asList(command, "-v", name)));
				long end = System.currentTimeMillis() - currentTimeMillis;
				if (end < 30000) {
					waitFor.set(executeCommand(name, Arrays.asList(command, "-v", name)));
				}
				txService.run(() -> {
					Version findByName1 = versionRepo.findByName(name);
					if (waitFor.get() == 0) {
						voteRepo.deleteAll(voteRepo.findAll(Example.of(new Vote(name))));
						findByName1.setState(VersionState.SUCCESS);
					} else {
						findByName1.setState(VersionState.ERROR);
					}
					versionRepo.save(findByName1);
				});
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				txService.run(() -> {
					Version findByName2 = versionRepo.findByName(name);
					findByName2.setState(VersionState.ERROR);
					versionRepo.save(findByName2);
				});
			}
		}).start();;
	}

	public static int executeCommand(final String name, final List<String> commands) throws Exception {
		logger.info(Joiner.on(",").join(commands));
		ProcessBuilder builder = new ProcessBuilder(commands);
		// builder.inheritIO();

		builder.directory(new File("/home/neds/VERTO/"));
		// builder.directory(new File("F:\\Verto2017\\workspace\\server-verto-product"));
		builder.environment().put("PATH", System.getenv("PATH") + ";" + "/opt/maven/bin;/bin;/sbin;/usr/bin;/usr/local/bin;");
		builder.environment().putAll(System.getenv());
		Process exec = builder.start();

		log(name, exec.getErrorStream());
		log(name, exec.getInputStream());
		timeout(exec);
		return exec.waitFor();
	}

	public void releaseAll(final String user) throws Exception {
		List<String> collect = voteRepo.findAll().stream().map(Vote::getVersion).distinct().collect(toList());
		for (String name : collect) {
			releaseVersion(name, user, false);
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
					logger.error(e.getMessage(), e);
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

	public static void main(final String[] args) {
	}
}
