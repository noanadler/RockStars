package auth;

import java.util.Map;

import org.pac4j.core.profile.UserProfile;

public class MyUserProfile extends UserProfile {

	public MyUserProfile(Object id, Map<String, Object> attributes) {
		// TODO Auto-generated constructor stub
		build(id, attributes);
	}

}
