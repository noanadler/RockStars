package auth;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.pac4j.core.config.Config;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jwt.profile.JwtGenerator;

import com.google.gson.Gson;

import spark.Request;
import spark.Response;

public class AuthenticationHelpers {
	public final static String JWT_SALT = "12341234123412341234123412341234";
	
	public AuthenticationHelpers() {
		// TODO Auto-generated constructor stub
	}
	
	public static String hashPassword( final char[] password) {		 
	       try {
	           SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
	           PBEKeySpec spec = new PBEKeySpec( password, JWT_SALT.getBytes(), 1000, 16 );
	           SecretKey key = skf.generateSecret( spec );
	           byte[] res = key.getEncoded( );
	           return new String(res, "UTF-8");
	 
	       } catch( NoSuchAlgorithmException | InvalidKeySpecException | UnsupportedEncodingException e ) {
	           throw new RuntimeException( e );
	       }
	   }
	
	public static boolean checkPassword(String password, String hashedPassword)
	{
	 	String hashedPassword2 = hashPassword(password.toCharArray());	
	 	boolean blnResult = hashedPassword.equals(hashedPassword2);
		return blnResult;
	}
	
	public static UserProfile createUserProfile(AuthRequest request) {
    		UserProfile myProfile = new UserProfile();	
    		myProfile.setId(request.email);
    		myProfile.addAttribute("name", request.name);
	        myProfile.addAttribute("email", request.email); 		
        	System.out.println("Profile: " + myProfile);
    		return myProfile;
	}
	
	public static String getUserToken(UserProfile p)
	{
		JwtGenerator generator = new JwtGenerator(AuthenticationHelpers.JWT_SALT);
		String token = generator.generate(p);
    	Gson gson = new Gson();
		final Map map = new HashMap();
		map.put("token", token);   		
		String json = gson.toJson(map);
		return json;
	}

}
