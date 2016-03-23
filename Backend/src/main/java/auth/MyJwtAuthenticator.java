package auth;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.credentials.TokenCredentials;
import org.pac4j.jwt.JwtConstants;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.jwt.profile.JwtProfile;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

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
	
	 private static void createJwtProfile(final TokenCredentials credentials, final SignedJWT signedJWT) throws ParseException {
	        System.out.println("In my jwt method");
		 final JWTClaimsSet claimSet = signedJWT.getJWTClaimsSet();
	        String subject = claimSet.getSubject();

	        if (!subject.contains(UserProfile.SEPARATOR)) {
	            subject = JwtProfile.class.getName() + UserProfile.SEPARATOR + subject;
	        }

	        final Map<String, Object> attributes = new HashMap<>(claimSet.getClaims());
	        attributes.remove(JwtConstants.SUBJECT);
	        final List<String> roles = (List<String>) attributes.get(JwtGenerator.INTERNAL_ROLES);
	        attributes.remove(JwtGenerator.INTERNAL_ROLES);
	        final List<String> permissions = (List<String>) attributes.get(JwtGenerator.INTERNAL_PERMISSIONS);
	        attributes.remove(JwtGenerator.INTERNAL_PERMISSIONS);
	        final UserProfile profile = new UserProfile();
	        profile.setId(subject);
	        profile.addAttributes(attributes);
	        if (roles != null) {
	            profile.addRoles(roles);
	        }
	        if (permissions != null) {
	            profile.addPermissions(permissions);
	        }
	        credentials.setUserProfile(profile);
	    }

}
