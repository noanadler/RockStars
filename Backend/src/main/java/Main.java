import static spark.Spark.*;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Country;
import models.Model;
import models.Sql2oModel;
import spark.Request;
import spark.Response;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jwt.profile.JwtGenerator;
import org.sql2o.Sql2o;
import org.sql2o.quirks.PostgresQuirks;
import org.sql2o.quirks.Quirks;

import com.google.gson.Gson;

import auth.AuthFactory;
import auth.MySparkWebContext;
import auth.MyUserProfile;
import auth.XHRRequiresAuthenticationFilter;

import data.ArrayConverter;
import data.HerokuDataSource;

public class Main {
	
	private final static String JWT_SALT = "12341234123412341234123412341234";
	
	private static UserProfile getUserProfile(final Request request, final Response response, final SessionStore sessionStore) {
		final MySparkWebContext context = new MySparkWebContext(request, response, sessionStore);
		
    	Gson gson = new Gson();
    	AuthRequest loginParams = gson.fromJson(context.getRequestBody(), AuthRequest.class);
    	byte[] hashedPassword = AuthFactory.hashPassword(loginParams.password.toCharArray(), JWT_SALT.getBytes());
    	boolean existsUser = false;
    	
    	/* need code here to look up user in db */
    	String retrievedUsername = "xyz";
    	String retrievedPassword = "xyz";
    	byte[] hashedActual = AuthFactory.hashPassword(retrievedPassword.toCharArray(), JWT_SALT.getBytes());
    	
    	boolean blnResult = Arrays.equals(hashedPassword,hashedActual);
        System.out.println("Does supplied password match hashed db value ? : " + blnResult);
    	
    	if(existsUser)
    	{
    		final ProfileManager manager = new ProfileManager(context);
    		return manager.get(true);
    	}else if((loginParams.email).equals(loginParams.password))
    	{
    		UserProfile myProfile = new UserProfile();	
    		myProfile.setId(loginParams.email);
	        myProfile.addAttribute("email", loginParams.email);
    		return myProfile;
    	}else
    	{
    		return null;
    	}
	}
    
    public static void main(String[] args) {
	    try {
	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
	        field.setAccessible(true);
	        field.set(null, java.lang.Boolean.FALSE);
	    } catch (Exception ex) {
	    }
        port(getHerokuAssignedPort());
        Quirks arraySupport = ArrayConverter.arrayConvertingQuirks(new PostgresQuirks(), true, false);
    	Sql2o sql2o = new Sql2o(new HerokuDataSource(), arraySupport);
        Model model = new Sql2oModel(sql2o);
               
        // JWT user auth setup
        final Config config = new AuthFactory(JWT_SALT).build();

        before((request, response) -> {
        	response.header("Access-Control-Allow-Origin", "*");
        });  
        
        before("/testauth", new XHRRequiresAuthenticationFilter(config, "HeaderClient"));
        
        /**
         * Sign up a user
         */
        post("/signup", (req, res) -> {
        	// store user info into database
        	
        	// pass to authenticator to get web token
        	
        	// return token
        	return "";
        });
        
        /**
         * Login a user
         */
        //accept basic auth info in HTTPS header, return token
        post("/login/", (req, res) -> {      
        	System.out.println("Session: " + config.getSessionStore());
        	final UserProfile profile = getUserProfile(req, res, config.getSessionStore());
        	System.out.println("Profile: " + profile);
    		JwtGenerator generator = new JwtGenerator(JWT_SALT);
    		String token = "";
    		if (profile != null) {
    			token = generator.generate(profile);
        		final Map map = new HashMap();
        		map.put("token", token);
        		
        		Gson gson = new Gson();
    			String json = gson.toJson(map); 
    			
    			res.status(200);
    			res.type("application/json");
            	return json;
    		}else
    		{
    			res.status(401);
    			res.type("application/json");
            	return "Login error";
    		}
    		
        });
        
        //this protected page is only visible with a valid JWT token
        get("/testauth", (req, res) -> {          
        	List<Country> countries = model.getCountries();
			Gson gson = new Gson();
			String json = gson.toJson(countries); 
			
			res.status(200);
			res.type("application/json");
        	return json;
    		
        });
        
        get("/country/:name", (req, res) -> {
        	Country country = model.getCountry(req.params("name"));
			Gson gson = new Gson();
			String json = gson.toJson(country); 
			
			res.status(200);
			res.type("application/json");
        	return json;
        }); 
        
        get("/countries", (req, res) -> {
        	List<Country> countries = model.getCountries();
			Gson gson = new Gson();
			String json = gson.toJson(countries); 
			
			res.status(200);
			res.type("application/json");
        	return json;
        }); 
        
        // SETUP CORS
        options("/*", (request, response) -> {
    		String accessControlRequestHeaders = request
    				.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
            	response.header("Access-Control-Allow-Headers",
            			accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request
            		.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
            	response.header("Access-Control-Allow-Methods",
        			accessControlRequestMethod);
            }

            return "OK";
        });      
    }
    
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}

class AuthRequest {
	public String email;
	public String password;
	AuthRequest() {}
}
