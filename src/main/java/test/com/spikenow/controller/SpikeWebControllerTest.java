package test.com.spikenow.controller;

import junit.framework.TestCase;

import org.springframework.beans.factory.annotation.Autowired;

import com.spikenow.service.SpikeService;

public class SpikeWebControllerTest extends TestCase{
	
	@Autowired
	private SpikeService spikeService;
	
	public void testGetActivityTypes() {
		assertNotNull(spikeService.fetchActivityTypes());
	}
}
