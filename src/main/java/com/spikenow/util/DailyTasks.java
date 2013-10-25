package com.spikenow.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.spikenow.service.SpikeService;

public class DailyTasks {
	
	@Autowired
	private SpikeService spikeService;
	
	private Logger logger = Logger.getLogger("com.spikenow.util");
	
	public void runDailyTasks() {
		logger.info("Running daily quartz tasks");
		try {
			spikeService.sendDailyDigestEmails();
		} catch (Exception e) {
			logger.error("Failed sending Daily Digest", e);
		}
	}

}
