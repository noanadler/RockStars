package services;

import java.util.UUID;
import org.junit.Test;

public class EmailGeneratorTest {
	
	@Test
	public void testSendVerificationEmail() {
		EmailGenerator gen = new EmailGenerator();
		UUID id = UUID.randomUUID();
		gen.sendVerificationEmail("eddie.fl@gmail.com", "Eddie", id);
	}
	
	@Test
	public void testSendGoodbyeEmail() {
		EmailGenerator gen = new EmailGenerator();
		gen.sendGoodbyeEmail("eddie.fl@gmail.com", "Eddie");
	}

}
