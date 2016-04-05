package auth;

import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;

public class MyJwtAuthenticator extends JwtAuthenticator {

	public MyJwtAuthenticator() {
		// TODO Auto-generated constructor stub
	}

	public MyJwtAuthenticator(String signingSecret) {
		super(signingSecret);
		// TODO Auto-generated constructor stub
	}

	public MyJwtAuthenticator(String signingSecret, String encryptionSecret) {
		super(signingSecret, encryptionSecret);
		// TODO Auto-generated constructor stub
	}

}
