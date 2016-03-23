package auth;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.http.profile.creator.ProfileCreator;

public class MyProfileCreator implements ProfileCreator<UsernamePasswordCredentials, UserProfile> {

	 @Override
	    public UserProfile create(UsernamePasswordCredentials credentials) {
		 	System.out.println(credentials.getUsername());
	        String username = credentials.getUsername();
	        final UserProfile profile = new UserProfile();
	        profile.setId(username);
	        profile.addAttribute(CommonProfile.USERNAME, username);
	        return profile;
	    }

}
