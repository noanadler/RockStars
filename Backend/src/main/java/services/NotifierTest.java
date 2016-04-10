package services;

import static org.junit.Assert.*;

import org.junit.Test;

public class NotifierTest {

	@Test
	public void testSendDailyUpdates() {
		Notifier notifier = new Notifier();
		notifier.sendDailyUpdates();
	}

}
