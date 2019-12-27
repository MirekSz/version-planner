
package com.example.versionplanner;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxService {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void run(final Runnable run) {
		run.run();
	}
}
