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
import models.User;
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
import auth.AuthRequest;
import auth.AuthenticationHelpers;
import auth.MySparkWebContext;
import auth.XHRRequiresAuthenticationFilter;

import data.ArrayConverter;
import data.HerokuDataSource;

public class Main {

    
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
        final Config config = new AuthFactory(AuthenticationHelpers.JWT_SALT).build();

        before((request, response) -> {
        	response.header("Access-Control-Allow-Origin", "*");
        });  
        
        before("/testauth", new XHRRequiresAuthenticationFilter(config, "HeaderClient"));
        
        /**
         * Sign up a user
         */
        post("/signup", (req, res) -> {
        	final MySparkWebContext context = new MySparkWebContext(req, res, config.getSessionStore());  		
        	Gson gson = new Gson();
        	AuthRequest loginParams = gson.fromJson(context.getRequestBody(), AuthRequest.class);
        	//Ensure user has not already signed up
        	if(model.getUserByEmail(loginParams.email) != null)
        	{
        		res.status(409);
    			res.type("application/json");
            	return "This email address is already registered";
        	}
        	//hash password
        	String hashedPassword = AuthenticationHelpers.hashPassword(loginParams.password.toCharArray());
        	// store user info into database
        	model.insertUser(loginParams.name, loginParams.email, hashedPassword, loginParams.gender, loginParams.countries);
        	// pass to authenticator to get web token
        	String token = AuthenticationHelpers.getUserToken(AuthenticationHelpers.createUserProfile(loginParams));
        	// return token
        	res.status(200);
			res.type("application/json");
        	return token; 
        });
        
        /**
         * Login a user
         */
        //accept basic auth info in HTTPS header, return token
        post("/login/", (req, res) -> {  
        	//1. Check username and password
        	final MySparkWebContext context = new MySparkWebContext(req, res, config.getSessionStore()); 
        	Gson gson = new Gson();
        	AuthRequest loginParams = gson.fromJson(context.getRequestBody(), AuthRequest.class);
        	//Ensure user has not already signed up
        	User user = model.getUserByEmail(loginParams.email);
        	if(user == null)
        	{
        		res.status(403);
    			res.type("application/json");
            	return "No such user";
        	}
        	if(!AuthenticationHelpers.checkPassword(loginParams.password, user.getHashedPassword()))
        	{
        		res.status(401);
    			res.type("application/json");
            	return "Invalid password";
        	}
        	//2. Generate user profile
        	String token = AuthenticationHelpers.getUserToken(AuthenticationHelpers.createUserProfile(loginParams));
        	//3. Return token
			res.status(200);
			res.type("application/json");
        	return token;  		
        });
        
        // NOTE: this is currently just stubbed so i can interact via the javascript app
        get("/user", (req, res) -> {
        	User user = new User();
        	user.setCountries(new String[] { "canada", "mexico"});
        	user.setNotifications(true);
        	user.setGender("M");
        	user.setName("John Gadbois");
        	
    		Gson gson = new Gson();
			String json = gson.toJson(user); 
			
			res.status(200);
			res.type("application/json");
        	return json;        	
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
